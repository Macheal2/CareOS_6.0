/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.format.DateFormat;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.ListSlotView.ItemCoordinate;
import com.android.gallery3d.ui.ListSlotView.GroupData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ListAlbumDataLoader {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/ListAlbumDataAdapter";
    private static final int DATA_CACHE_SIZE = 1000;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    private static final int MIN_LOAD_COUNT = 32;
    private static final int MAX_LOAD_COUNT = 64;

    private final MediaItem[] mData;
    private ArrayList<ItemCoordinate> mItemCoordinate = new ArrayList<ItemCoordinate>();
    private ArrayList<GroupData> mGroupData = new ArrayList<GroupData>();
    private final String[] dateTime;
    
    private final long[] mItemVersion;
    private final long[] mSetVersion;
    

    public static interface DataListener {
        public void onContentChanged(int index);
        public void onSizeChanged(int size);
//        public void onItemCoordinateChanged(ArrayList<ItemCoordinate> item);
//        public void onGroupDataChanged(ArrayList<GroupData> item);
		public void onDataChanged(ArrayList<ItemCoordinate> itemCoordinate,
				ArrayList<GroupData> groupData);
    }

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private final MediaSet mSource;
    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;

    private final Handler mMainHandler;
    private int mSize = 0;

    private DataListener mDataListener;
    private MySourceListener mSourceListener = new MySourceListener();
    private LoadingListener mLoadingListener;

    private ReloadTask mReloadTask;
    // the data version on which last loading failed
    private long mFailedVersion = MediaObject.INVALID_DATA_VERSION;

    //private int orientation ;
    private int cols=2;
    //private  AbstractGalleryActivity mContext;
    public ListAlbumDataLoader(AbstractGalleryActivity context, MediaSet mediaSet) {
        mSource = mediaSet;

        mData = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        dateTime =new String[DATA_CACHE_SIZE];
//        mContext =context;
//        orientation = context.getResources().getConfiguration().orientation;
//        if(orientation==Configuration.ORIENTATION_PORTRAIT){
//        	cols=4;
//        }else{
//        	cols=6;
//        }
        //mItemCoordinate= new ItemCoordinate[DATA_CACHE_SIZE];
        //mGroupData= new GroupData[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);

        mMainHandler = new SynchronizedHandler(context.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
                    case MSG_LOAD_START:
                        if (mLoadingListener != null) mLoadingListener.onLoadingStarted();
                        return;
                    case MSG_LOAD_FINISH:
                        if (mLoadingListener != null) {
                            boolean loadingFailed =
                                    (mFailedVersion != MediaObject.INVALID_DATA_VERSION);
                            mLoadingListener.onLoadingFinished(loadingFailed);
                        }
                        return;
                }
            }
        };
    }

    public void resume() {
//    	if(orientation!=mContext.getResources().getConfiguration().orientation){
//    		orientation=mContext.getResources().getConfiguration().orientation;
//    		if(orientation==Configuration.ORIENTATION_PORTRAIT){
//            	cols=4;
//            }else{
//            	cols=6;
//            }
//            Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
//            Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);
//    	}
        mSource.addContentListener(mSourceListener);
        mReloadTask = new ReloadTask();
        mReloadTask.start();
    }

    public void pause() {
        mReloadTask.terminate();
        mReloadTask = null;
        mSource.removeContentListener(mSourceListener);
    }

    public ItemCoordinate getItemCoordinate(int index) {
        return mItemCoordinate.get(index);
    }
    
    public ArrayList<ItemCoordinate> getItemCoordinate() {
        return mItemCoordinate;
    }
    
    public GroupData getGroupData(int index) {
        return mGroupData.get(index);
    }
    
    public ArrayList<GroupData> getGroupData() {
        return mGroupData;
    }
    
    
    public MediaItem get(int index) {
        if (!isActive(index)) {
            /// M: [BUG.MODIFY] fix indexOutOfBoundsException @{
            // return mSource.getMediaItem(index, 1).get(0);
            ArrayList<MediaItem> list = mSource.getMediaItem(index, 1);
            if (list != null && list.size() != 0) {
                return list.get(0);
            } else {
                return null;
            }
            /// @}
        }
        return mData[index % mData.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int size() {
        return mSize;
    }

    // Returns the index of the MediaItem with the given path or
    // -1 if the path is not cached
    public int findItem(Path id) {
        for (int i = mContentStart; i < mContentEnd; i++) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            if (item != null && id == item.getPath()) {
                return i;
            }
        }
        return -1;
    }

    private void clearSlot(int slotIndex) {
        mData[slotIndex] = null;
        mItemVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;
        int end = mContentEnd;
        int start = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        long[] itemVersion = mItemVersion;
        long[] setVersion = mSetVersion;
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null) mReloadTask.notifyDirty();
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;

        Utils.assertTrue(start <= end
                && end - start <= mData.length && end <= mSize);

        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;

        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);
        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements ContentListener {
        @Override
        public void onContentDirty() {
            /// M: [PERF.MODIFY] add for delete many files performance improve @{
            /*if (mReloadTask != null) mReloadTask.notifyDirty();*/
            if (mIsSourceSensive && mReloadTask != null) {
                mReloadTask.notifyDirty();
            }
            /// @}
        }
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(
                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<MediaItem> items;
        
        public ArrayList<MediaItem> mItems;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        @Override
        public UpdateInfo call() throws Exception {
            if (mFailedVersion == mVersion) {
                // previous loading failed, return null to pause loading
                return null;
            }
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mSourceVersion;
            info.size = mSize;
            long setVersion[] = mSetVersion;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % DATA_CACHE_SIZE;
                if (setVersion[index] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                    return info;
                }
            }
            return mSourceVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
            if(mGroupData.size()!=0&&mItemCoordinate.size()!=0){
		    	mGroupData.clear();
		    	mItemCoordinate.clear();
		    }
            if (mSize != info.size) {
                mSize = info.size;
                if (mDataListener != null) mDataListener.onDataChanged(mItemCoordinate,mGroupData);
                if (mDataListener != null) mDataListener.onSizeChanged(mSize);
                if (mContentEnd > mSize) mContentEnd = mSize;
                if (mActiveEnd > mSize) mActiveEnd = mSize;
            }

            ArrayList<MediaItem> items = info.items;

            mFailedVersion = MediaObject.INVALID_DATA_VERSION;
            if ((items == null) || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);
            final Locale mLocale = Locale.getDefault();
	        String mDateStr = DateFormat.getBestDateTimePattern(mLocale, "yyyyMMMMd");
		    SimpleDateFormat mDateFormat = new SimpleDateFormat(mDateStr, mLocale);
		    int grop=0;
		    int sudIndex=0;
		    int DateStart=0;
		    int DateEnd=0;
		    ItemCoordinate mItem ;
		    ArrayList<MediaItem> mItems = info.mItems;
		    //GroupData mGData= new GroupData();
		    if((mItems == null)||mItems.isEmpty()){
		    	if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
		    }
		    //if(mGroupData.size()!=0&&mItemCoordinate.size()!=0){
		    //	mGroupData.clear();
		    //	mItemCoordinate.clear();
		    //}
		    for (int i = 0; i < mItems.size(); ++i) {
                int index = i % DATA_CACHE_SIZE;                		   
                MediaItem updateItem = mItems.get(i);
                    long time = updateItem.getDateInMs();
    		        dateTime[index] = mDateFormat.format(new Date(time));
    		        if(index == 0){
    		        	mItem = new ItemCoordinate(0,0);
    		        }else if(dateTime[index-1].equals(dateTime[index])){
    		        	sudIndex+=1;
    		        	mItem = new ItemCoordinate(grop,sudIndex);
    		        }else{
    		        	DateEnd=(sudIndex)/cols + DateStart;
    		        	mGroupData.add(new GroupData(sudIndex,dateTime[index-1],DateStart,DateEnd));
    		        	DateStart=DateEnd+1;
    		        	grop+=1;
    		        	sudIndex=0;
    		        	mItem = new ItemCoordinate(grop,sudIndex);
    		        }
    		        if((end-1)==index){
    		        	DateEnd=(sudIndex+cols)/cols + DateStart;
    		        	mGroupData.add(new GroupData(sudIndex,dateTime[end-1],DateStart,DateEnd));
    		        }
    		        //Log.d("dengjianzhang6", "DateEnd: " + DateEnd);
    		        mItemCoordinate.add(mItem);
		    }
            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;   		   
                MediaItem updateItem = items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                
//                    long time = updateItem.getDateInMs();
//    		        dateTime[index] = mDateFormat.format(new Date(time));
//    		        if(index == 0){
//    		        	mItem = new ItemCoordinate(0,0);
//    		        }else if(dateTime[index-1].equals(dateTime[index])){
//    		        	sudIndex+=1;
//    		        	mItem = new ItemCoordinate(grop,sudIndex);
//    		        }else{
//    		        	DateEnd=(sudIndex)/4 + DateStart;
//    		        	mGroupData.add(new GroupData(sudIndex,dateTime[index-1],DateStart,DateEnd));
//    		        	DateStart=DateEnd+1;
//    		        	grop+=1;
//    		        	sudIndex=0;
//    		        	mItem = new ItemCoordinate(grop,sudIndex);
//    		        }
//    		        if((end-1)==index){
//    		        	DateEnd=(sudIndex+4)/4 + DateStart;
//    		        	mGroupData.add(new GroupData(sudIndex,dateTime[end-1],DateStart,DateEnd));
//    		        }
//    		        //Log.d("dengjianzhang6", "DateEnd: " + DateEnd);
//    		        mItemCoordinate.add(mItem);
                  
                    if (mItemVersion[index] != itemVersion) {
                    	mData[index] = updateItem;
                        mItemVersion[index] = itemVersion;
                        if (mDataListener != null && i >= mActiveStart && i < mActiveEnd) {
                        	mDataListener.onContentChanged(i);
                        }
                }         
            }
            if (mDataListener != null) {
            	mDataListener.onDataChanged(mItemCoordinate,mGroupData);
//            	mDataListener.onItemCoordinateChanged(mItemCoordinate);
//            	mDataListener.onGroupDataChanged(mGroupData);
            }
            Log.d("dengjianzhang6", "loading failed: " + mGroupData.size());
            return null;
        }

    }

    /*
     * The thread model of ReloadTask
     *      *
     * [Reload Task]       [Main Thread]
     *       |                   |
     * getUpdateInfo() -->       |           (synchronous call)
     *     (wait) <----    getUpdateInfo()
     *       |                   |
     *   Load Data               |
     *       |                   |
     * updateContent() -->       |           (synchronous call)
     *     (wait)          updateContent()
     *       |                   |
     *       |                   |
     */
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading) return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        updateLoading(false);
                        if (mFailedVersion != MediaObject.INVALID_DATA_VERSION) {
                            Log.d(TAG, "reload pause");
                        }
                        Utils.waitWithoutInterrupt(this);
                        if (mActive && (mFailedVersion != MediaObject.INVALID_DATA_VERSION)) {
                            Log.d(TAG, "reload resume");
                        }
                        continue;
                    }
                    mDirty = false;
                }
                updateLoading(true);
                long version = mSource.reload();
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete) continue;
                if (info.version != version) {
                    info.size = mSource.getMediaItemCount();
                    info.version = version;
                }
                if (info.size > 0) {
                	info.mItems = mSource.getMediaItem(0, info.size);
                }
                if (info.reloadCount > 0) {
                    info.items = mSource.getMediaItem(info.reloadStart, info.reloadCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }
    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************
    public int getActiveEnd() {
        return mActiveEnd;
    }

    /// M: [PERF.ADD] add for delete many files performance improve @{
    private volatile boolean mIsSourceSensive = true;
    public void setSourceSensive(boolean isSourceSensive) {
        mIsSourceSensive = isSourceSensive;
    }
    public void fakeSourceChange() {
        mSourceListener.onContentDirty();
    }
    /// @}
}
