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

package com.android.gallery3d.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.GLPaint;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.ui.ListSlotView.GroupData;
import com.android.gallery3d.ui.ListSlotView.ItemCoordinate;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.app.GalleryActionBar;
import com.mediatek.gallery3d.layout.FancyHelper;
import com.mediatek.gallery3d.layout.FancyLayout;
import com.android.gallery3d.ui.ListLayout;
import com.mediatek.gallery3d.layout.FancyPaper;
import com.mediatek.galleryfeature.config.FeatureConfig;
import com.mediatek.galleryframework.base.MediaData.MediaType;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.text.format.DateFormat;
import android.graphics.Color;

import java.util.Date;
//dengjianzhang@20160112 add
public class ListSlotView extends GLView implements ListLayout.DataChangeListener {

    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/ListSlotView";


    private static final boolean mIsFancyLayoutSupported = FancyHelper.isFancyLayoutSupported();

    private static final int INDEX_NONE = -1;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    public interface Listener {
        public void onDown(int index);
        public void onUp(boolean followedByLongPress);
        public void onSingleTapUp(int index);
        public void onLongTap(int index);
        public void onScrollPositionChanged(int position, int total);
    }

    public static class SimpleListener implements Listener {
        @Override public void onDown(int index) {}
        @Override public void onUp(boolean followedByLongPress) {}
        @Override public void onSingleTapUp(int index) {}
        @Override public void onLongTap(int index) {}
        @Override public void onScrollPositionChanged(int position, int total) {}
    }

    public static interface SlotRenderer {
        public void prepareDrawing();
        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);
        public void onSlotSizeChanged(int width, int height);
        public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height);
    }

    private final GestureDetector mGestureDetector;
    private final ScrollerHelper mScroller;

    private final Paper mPaper = mIsFancyLayoutSupported ? new FancyPaper() : new Paper();

    private Listener mListener;
    private UserInteractionListener mUIListener;

    private boolean mMoreAnimation = false;
    private SlotAnimation mAnimation = null;
    /// M: [FEATURE.MODIFY] fancy layout @{
    //private final Layout mLayout = new Layout();
    private ListLayout mLayout;
    /// @}

    private int mStartIndex = INDEX_NONE;

    // whether the down action happened while the view is scrolling.
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_3D;
    private final Handler mHandler;

    private SlotRenderer mRenderer;

    private int[] mRequestRenderSlots = new int[16];

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;
    
    private String[] dateTime = new String[16];

    // to prevent allocating memory
    private final Rect mTempRect = new Rect();
    
    private ArrayList<ItemCoordinate> mItemCoordinate = new ArrayList<ItemCoordinate>();
    private ArrayList<GroupData> mGroupData = new ArrayList<GroupData>();
    private final ResourceTexture mTitleDateTable;
    private String mToday;
    private String today;
    private int mSize=0;
    private int mEnd=0;
    public ListSlotView(AbstractGalleryActivity activity, Spec spec) {
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLRoot());
        mLayout = new DefaultLayout();
        setSlotSpec(spec);
        mTitleDateTable = new ResourceTexture(activity, R.drawable.ic_title_date_table);
        
        mToday=activity.getString(R.string.today);
    	long time =System.currentTimeMillis();
    	final Locale mLocale = Locale.getDefault();
        String mDateStr = DateFormat.getBestDateTimePattern(mLocale, "yyyyMMMMd");
	    SimpleDateFormat mDateFormat = new SimpleDateFormat(mDateStr, mLocale);
	    today=mDateFormat.format(new Date(time));
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        mRenderer = slotDrawer;
        if (mRenderer != null) {
            mRenderer.onSlotSizeChanged(mLayout.getSlotWidth(), mLayout.getSlotHeight());
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
            mLayout.setSlotRenderer(mRenderer);
 
    }

    public void setCenterIndex(int index) {
        /// M: [FEATURE.MODIFY] fancy layout @{
        //int slotCount = mLayout.mSlotCount;
        int slotCount = mLayout.getSlotCount();
        /// @}
        if (index < 0 || index >= slotCount) {
            return;
        }
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int position =(rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }

    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int position =0;
        int slotCount = mLayout.getSlotCount();
//        int visibleBegin = mScrollY;
//        int visibleLength = getHeight();
//        int visibleEnd =visibleBegin + visibleLength;
//        int slotBegin =rect.top;
//        int slotEnd =rect.bottom;
//
//        int position = visibleBegin;
//        if (visibleLength < slotEnd - slotBegin) {
//            position = visibleBegin;
//        } else if (slotBegin < visibleBegin) {
//            position = slotBegin;
//        } else if (slotEnd > visibleEnd) {
//            position = slotEnd - visibleLength;
//        }
        if((rect.top + mLayout.getViewHeight())>((DefaultLayout)mLayout).getContentHeight()){
        	position=((DefaultLayout)mLayout).getContentHeight()-mLayout.getViewHeight();
        }else{
        	position =(rect.top + rect.bottom - mLayout.getViewHeight()) / 2;
        }
        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
        mBackUpSpec = spec;
    }

    @Override
    public void addComponent(GLView view) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize) return;

        // Make sure we are still at a resonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setSize(r - l, b - t);
        /// M: [FEATURE.ADD] fancy layout @{
        if (mIsFancyLayoutSupported && mLayout != null && (mActivity != null)) {
            // mLayout.setSize() may not be called when switch to Fancy
            // so mViewHeight will be 0, then visible Range will be calculated incorrectly
            // so backup this value
            if (FeatureConfig.IS_TABLET) {////modify by even c687
                int orientation = mActivity.getResources().getConfiguration().orientation;
                if ((b - t) > (r - l) && orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayout.setViewHeightWhenPortrait(b - t);
                }
            } else {
                if ((b - t) > (r - l)) {
                    mLayout.setViewHeightWhenPortrait(b - t);
                }
            }
        }
        /// @}
        makeSlotVisible(visibleIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        /// M: [FEATURE.MODIFY] fancy layout @{
        //if (mLayout.mSlotCount != 0) invalidate();
        if (mLayout.getSlotCount() != 0) invalidate();
        /// @}
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        /// M: [FEATURE.MODIFY] fancy layout @{
        //if (mLayout.mSlotCount != 0) invalidate();
        if (mLayout.getSlotCount() != 0) invalidate();
        /// @}
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && ( position == mScrollY)) return;
        mScrollY = position;
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        /// M: [FEATURE.MODIFY] fancy layout @{
        //mListener.onScrollPositionChanged(newPosition, limit);
        if (mListener != null) {
            mListener.onScrollPositionChanged(newPosition, limit);
        }
        /// @}
    }

    public Rect getSlotRect(int slotIndex) {
        return mLayout.getSlotRect(slotIndex, new Rect());
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        if (mUIListener != null) mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
                mPaper.onRelease();
                invalidate();
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }

    private static int[] expandIntArray(int array[], int capacity) {
        while (array.length < capacity) {
            array = new int[array.length * 2];
        }
        return array;
    }
    
    private static String[] expandIntArray(String array[], int capacity) {
        while (array.length < capacity) {
            array = new String[array.length * 2];
        }
        return array;
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);

        if (mRenderer == null) return;
        mRenderer.prepareDrawing();

        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        more |= mLayout.advanceAnimation(animTime);
        int oldX = mScrollX;
        updateScrollPosition(mScroller.getPosition(), false);

        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit) v = -v;

                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }

        more |= paperActive;

        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
        
        if(mGroupData.size()!=0 && mItemCoordinate.size()!=0&&mLayout.getSlotCount()!=0){
        	GLPaint paint= new GLPaint();
        	paint.setColor(Color.LTGRAY);
        	paint.setLineWidth(3);
        	canvas.drawLine(60, 0 , 63 , ((DefaultLayout)mLayout).getContentHeight(), paint);
        }
        
        canvas.translate(-mScrollX, -mScrollY);

        int requestCount = 0;
        /// M: [FEATURE.MODIFY] fancy layout @{
        /*
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.mVisibleEnd - mLayout.mVisibleStart);
        */
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.getVisibleEnd() - mLayout.getVisibleStart());


        /*for (int i = mLayout.mVisibleEnd - 1; i >= mLayout.mVisibleStart; --i) {*/
        for (int i = mLayout.getVisibleEnd() - 1; i >= mLayout.getVisibleStart(); --i) {
        /// @}
//        	long time = mRenderer.getDateTakenInMs(i);
//    	    String mDate = mDateFormat.format(new Date(time));
//    	    Log.d("dengjianzhang2", "i="+ i +" mDate " + mDate);
            int r = renderItem(canvas, i, 0, paperActive);
            if ((r & RENDER_MORE_FRAME) != 0) more = true;
            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
        }

        for (int pass = 1; requestCount != 0; ++pass) {
            int newCount = 0;
            for (int i = 0; i < requestCount; ++i) {
                int r = renderItem(canvas,
                        requestedSlot[i], pass, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[newCount++] = i;
            }
            requestCount = newCount;
        }

        canvas.translate(mScrollX, mScrollY);

        if (more) invalidate();

        final UserInteractionListener listener = mUIListener;
        if (mMoreAnimation && !more && listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUserInteractionEnd();
                }
            });
        }
        mMoreAnimation = more;
    }

    private int renderItem(
            GLCanvas canvas, int index, int pass, boolean paperActive) {
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        
        String titleDate=null;
        boolean isShowTitle= false;
        int titleHeight=((DefaultLayout)mLayout).getTitleHeight();
        if(mGroupData.size()!=0 && mItemCoordinate.size()!=0){
        	 titleDate = mGroupData.get(mItemCoordinate.get(index).mGroup).Date;
        	 if(today.equals(titleDate)){
        		 titleDate=mToday;
 		    }
        	 if(index-1<0){
        		 isShowTitle=true;
        	 }else{
        		 isShowTitle = mItemCoordinate.get(index).mGroup!=mItemCoordinate.get(index-1).mGroup;
        	 }
        }
        if(isShowTitle){
        	//GLPaint paint= new GLPaint();
        	//paint.setColor(Color.LTGRAY);
        	//paint.setLineWidth(2);
        	
        	if(index==0){
        		//canvas.drawLine(0, 40 , ((DefaultLayout)mLayout).mWidth , 42 , paint);
        		StringTexture mTitleText= StringTexture.newInstance(titleDate, 44, Color.BLACK, 0,true);//modify by even c687 temp StringTexture.newInstance(titleDate, 44, Color.BLACK, true);
                mTitleText.draw(canvas, rect.left-20, 15);
                mTitleDateTable.draw(canvas, 43 , 23 , 35 , 35);
                
        	}else{
        		//canvas.drawLine(0, rect.top - titleHeight+40 , ((DefaultLayout)mLayout).mWidth , rect.top - titleHeight+42, paint);
        		StringTexture mTitleText= StringTexture.newInstance(titleDate, 44, Color.BLACK ,0, true);//modify by even c687 temp 
                mTitleText.draw(canvas, rect.left-20,  rect.top - titleHeight+15);
                mTitleDateTable.draw(canvas, 43 , rect.top - titleHeight+23 , 35, 35);
        	}
        }
        
        if (paperActive) {
            canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollY), 0);            
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }
        
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        int result = mRenderer.renderSlot(
                canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    public static abstract class SlotAnimation extends Animation {
        protected float mProgress = 0;

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends SlotAnimation {
        private static final int RISING_DISTANCE = 128;

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        }
    }

    public static class ScatteringAnimation extends SlotAnimation {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }

    // This Spec class is used to specify the size of each slot in the SlotView.
    // There are two ways to do it:
    //
    // (1) Specify slotWidth and slotHeight: they specify the width and height
    //     of each slot. The number of rows and the gap between slots will be
    //     determined automatically.
    // (2) Specify rowsLand, rowsPort, and slotGap: they specify the number
    //     of rows in landscape/portrait mode and the gap between slots. The
    //     width and height of each slot is determined automatically.
    //
    // The initial value of -1 means they are not specified.
    public static class Spec {
        public int slotWidth = -1;
        public int slotHeight = -1;
        public int slotHeightAdditional = 0;

        public int rowsLand = -1;
        public int rowsPort = -1;
        public int slotGap = -1;

        /// M: [FEATURE.ADD] fancy layout @{
        public int colsLand = -1;
        public int colsPort = -1;
        /// @}
        public int paddingBottom = -1;
        public int titleHeight = -1;
    }

    /// M: [FEATURE.MODIFY] fancy layout @{
    //public class Layout {
    public class DefaultLayout extends ListLayout {
    /// @}

        private int mVisibleStart;
        private int mVisibleEnd;

        private int mSlotCount;
        private int mSlotWidth;
        private int mSlotHeight;
        private int mSlotGap;

        private Spec mSpec;

        private int mWidth;
        private int mHeight;
        
        private int mUnitCount;
        private int mContentLength;
        private int mScrollPosition;

        private int mTitleHeight;
        private int mPaddingBottom;
        private int isLand=-1;
        
        private IntegerAnimation mVerticalPadding = new IntegerAnimation();
        private IntegerAnimation mHorizontalPadding = new IntegerAnimation();
        
        private ArrayList<ItemCoordinate> mItemGroup = new ArrayList<ItemCoordinate>();
        private ArrayList<GroupData> mGroup = new ArrayList<GroupData>();
        
        public void setSlotSpec(Spec spec) {
            mSpec = spec;
        }

        public boolean setSlotCount(int slotCount) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            //if (slotCount == mSlotCount) return false;
            if (!mForceRefreshFlag && slotCount == mSlotCount) return false;
            /// @}
            if (mSlotCount != 0) {
                mHorizontalPadding.setEnabled(true);
                mVerticalPadding.setEnabled(true);
            }
            mSlotCount = slotCount;
            int hPadding = mHorizontalPadding.getTarget();
            int vPadding = mVerticalPadding.getTarget();
            initLayoutParameters();
            return vPadding != mVerticalPadding.getTarget()
                    || hPadding != mHorizontalPadding.getTarget();
        }

        /// M: [FEATURE.ADD] fancy layout @{
        private boolean mForceRefreshFlag = false;

        public int getSlotCount() {
            return mSlotCount;
        }

        public Spec getSlotSpec() {
            return mSpec;
        }

        public int getSlotGap() {
            return mSpec.slotGap;
        }

        public int getViewWidth() {
            return mWidth;
        }

        public int getViewHeight() {
            return mHeight;
        }

        public void setForceRefreshFlag(boolean needForceRefresh) {
            mForceRefreshFlag = needForceRefresh;
        }

        public void clearColumnArray(int index, boolean clearAll) {
            if (mSlotMapByColumn == null || mSlotArray == null) return;
            for (int i = 0; i < COL_NUM; i++) {
                ArrayList<SlotEntry> array = mSlotMapByColumn.get(i);
                if (array == null) return;
                if (clearAll) {
                    array.clear();
                } else {
                    int arraySize = array.size();
                    for (int j = arraySize - 1; j >= 0; j--) {
                        if (array.get(j).slotIndex >= index) {
                            array.remove(j);
                        }
                    }
                }
            }
        }
        /// @}

    	public void setItemGroup(ArrayList<ItemCoordinate> itemCoordinate) {
    		// TODO Auto-generated method stub
    		if(itemCoordinate!=null){
    			mItemGroup=itemCoordinate;
    		}
    	}
    	
    	public void setGroup(ArrayList<GroupData> groupData) {
    		// TODO Auto-generated method stub
    		if(groupData!=null){
    			mGroup=groupData;
    		}
    		initLayoutParameters();
    	}
    	
        public Rect getSlotRect(int index, Rect rect) {
        	 int col, row;
        	 if(mSlotCount==0){
        		 return rect;
        	 }
        	 if(index>=mSlotCount){
        		 index=mSlotCount-1;
        	 }
        	if(mItemGroup.size()!=0){
        		ItemCoordinate mItem=mItemGroup.get(index);
        		row = mItem.mSubIndex / mUnitCount;
        		col = mItem.mSubIndex - row * mUnitCount;
                int k=getSubContentLen(mItem.mGroup);
               
                int x = 120+ col* (mSlotWidth + mSlotGap);
                int y = k + row* (mSlotHeight + mSlotGap);
                rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        		
        	}
        	
            return rect;
        }
        
        public int getSubContentLen(int index){
          int i = mTitleHeight;    
  
          for(int k = 0; k < index; k++){          
                  int n = (mGroup.get(k).count + mUnitCount) / mUnitCount;
                  i +=  (n * mSlotHeight + (n - 1) * mSlotGap)+ mTitleHeight;  
           }
          
          return i;
        }
        
        public int getSlotWidth() {
            return mSlotWidth;
        }

        public int getSlotHeight() {
            return mSlotHeight;
        }
        
        public int getTitleHeight() {
            return mTitleHeight;
        }
        
        public int getContentHeight() {
            return mContentLength > (mWidth > mHeight ? mWidth:mHeight ) ? mContentLength : mHeight;
        }
        
        // Calculate
        // (1) mUnitCount: the number of slots we can fit into one column (or row).
        // (2) mContentLength: the width (or height) we need to display all the
        //     columns (rows).
        // (3) padding[]: the vertical and horizontal padding we need in order
        //     to put the slots towards to the center of the display.
        //
        // The "major" direction is the direction the user can scroll. The other
        // direction is the "minor" direction.
        //
        // The comments inside this method are the description when the major
        // directon is horizontal (X), and the minor directon is vertical (Y).
        private void initLayoutParameters(
                int majorLength, int minorLength,  /* The view width and height */
                int majorUnitSize, int minorUnitSize,  /* The slot width and height */
                int[] padding) {
            int unitCount = (minorLength + mSlotGap) / (minorUnitSize + mSlotGap);
            if (unitCount == 0) unitCount = 1;
            mUnitCount = unitCount;

            // We put extra padding above and below the column.
            int availableUnits = Math.min(mUnitCount, mSlotCount);
            int usedMinorLength = availableUnits * minorUnitSize +
                    (availableUnits - 1) * mSlotGap;
            padding[0] = (minorLength - usedMinorLength) / 2;

            // Then calculate how many columns we need for all slots.
            int count = 0;
            if(mGroup.size()!=0 ){
             	 count = mGroup.get(mGroup.size()-1).endDate;    
             }
            int mtitleHeight=0;
            int mCount = count;
            if(mItemGroup.size()!=0){
            	mtitleHeight=mTitleHeight *(mItemGroup.get(mItemGroup.size()-1).mGroup+1);
            	mCount-=mItemGroup.get(mItemGroup.size()-1).mGroup;
            } 	
            
            mContentLength = count * majorUnitSize + (mCount - 1) * mSlotGap + mtitleHeight + mPaddingBottom;

            // If the content length is less then the screen width, put
            // extra padding in left and right.
            padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
        }

        private void initLayoutParameters() {
            // Initialize mSlotWidth and mSlotHeight from mSpec
            if (mSpec.slotWidth != -1) {
                mSlotGap = 0;
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
            } else {
                 int cols = (mWidth > mHeight) ? mSpec.colsLand : mSpec.colsPort;
                 if(isLand==-1){
                	 isLand=(mWidth > mHeight) ? 0 : 1;
                 }
                 mSlotGap = mSpec.slotGap;
                 mSlotWidth = Math.max(1, (mWidth - (cols - 1) * mSlotGap -140) / cols);
                 mSlotHeight = mSlotWidth; 
            }
            
            mTitleHeight=mSpec.titleHeight;
            mPaddingBottom=mSpec.paddingBottom;
            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }

            int[] padding = new int[2];

            initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
            mVerticalPadding.startAnimateTo(padding[1]);
            mHorizontalPadding.startAnimateTo(padding[0]);
            updateVisibleSlotRange();
            //Log.d("dengjianzhang4", " isLand="+ isLand);
            if(isLand!=((mWidth > mHeight) ? 0 : 1)){
            	//Log.d("dengjianzhang4", " isLand="+ isLand);
            	isLand=(mWidth > mHeight) ? 0 : 1;
            	//Log.d("dengjianzhang4", " isLand="+ isLand);
            	forceSetScrollPosition(0);
            	invalidate();
            }
           
        }

        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
            initLayoutParameters();
        }

        private void updateVisibleSlotRange() {
            int position = mScrollPosition;
            //Log.d("dengjianzhang4", " mScrollPosition="+ mScrollPosition);
                int startCol = position / (mSlotHeight + mSlotGap);            
                int endCol = (position + mHeight + mSlotHeight + mSlotGap - 1) /
                        (mSlotHeight + mSlotGap);
                //Log.d("dengjianzhang4", " mScrollPosition="+ endCol);
                int end=0;            
                int start = 0;
                boolean isStart= true;
                if(mItemGroup.size()!=0){
                	for(int i=0; i<mItemGroup.size(); i++){
                		Rect rect = getSlotRect(i,new Rect());
                		if(rect.top<=position && position<=(rect.bottom+mTitleHeight+ mSlotGap) && isStart){
                			start=i;
                			isStart=false;
                		}
                		if(rect.top<=(position+ mHeight) && (position+ mHeight - 1)<=(rect.bottom+mTitleHeight+ mSlotGap)){
                			
                			endCol = (position + mHeight + mSlotHeight + mSlotGap - 1-(mItemGroup.get(i).mGroup+1)*mTitleHeight) /
                                    (mSlotHeight + mSlotGap) +1;
                			//Log.d("dengjianzhang5", "endCol3"+endCol);
                			break;
                		}
                	}
                	
                }
                if(mGroup.size()!=0 && startCol!=0){
                	for(int i=0; i<mGroup.size(); i++){              
                    	if(endCol>=mGroup.get(i).startDate&& endCol<mGroup.get(i).endDate){ 		
                    		end+=(endCol - mGroup.get(i).startDate-1)*mUnitCount + mUnitCount -1 ;
                    		 //Log.d("dengjianzhang5", " k1="+  end +"endCol"+endCol);
                    		end = Math.min(mSlotCount, end);
                    		setVisibleRange(start, end);
                        	return;
                    	}
                    	if(endCol==mGroup.get(i).endDate){
                    		end+=mGroup.get(i).count+1;
                    		//Log.d("dengjianzhang5", " k2="+  end +"endCol"+endCol);
                    		end = Math.min(mSlotCount, end);
                    		setVisibleRange(start, end);
                        	return;
                    	}
                    	end+=mGroup.get(i).count+1;	
                    }
                	
                	end = Math.min(mSlotCount, mUnitCount * endCol);             	
                	setVisibleRange(start, end);
                }else{
                	end = Math.min(mSlotCount, mUnitCount * endCol);
                	setVisibleRange(start, end);
                }
        }

        public void setScrollPosition(int position) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            //if (mScrollPosition == position) return;
            if (!mForceRefreshFlag && mScrollPosition == position) return;
            /// @}
            mScrollPosition = position;
            updateVisibleSlotRange();
        }

        private void setVisibleRange(int start, int end) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            //if (start == mVisibleStart && end == mVisibleEnd) return;
        	if(end-start>48){
        		Log.d("dengjianzhang ", "setVisibleRange end-start="+  (end-start));
        		start= end - 50;
        	}
            if (!mForceRefreshFlag && start == mVisibleStart && end == mVisibleEnd) return;
            /// @}
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }
            if (mRenderer != null) {
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }

        public int getVisibleStart() {
            return mVisibleStart;
        }

        public int getVisibleEnd() {
            return mVisibleEnd;
        }

        public int getSlotIndexByPosition(float x, float y) {
            int absoluteX = Math.round(x);
            int absoluteY = Math.round(y) + mScrollPosition;
            int index = -1;
            if(mItemGroup.size()!=0){
            	for(int i=0; i<mItemGroup.size(); i++){
            		Rect rect = getSlotRect(i,new Rect());
            		if(rect.left<=absoluteX&& absoluteX<=rect.right
            				&&rect.top<=absoluteY&& absoluteY<=rect.bottom){
            			index=i;
            		}
            	}
            }
            if(index==-1){
            	return INDEX_NONE;
            }

           //Log.d("dengjianzhang10", " index4="+ index);
            return index >= mSlotCount ? INDEX_NONE : index;
        }

        public int getScrollLimit() {
            int limit = mContentLength - mHeight;
            return limit <= 0 ? 0 : limit;
        }

        public boolean advanceAnimation(long animTime) {
            // use '|' to make sure both sides will be executed
            return mVerticalPadding.calculate(animTime) | mHorizontalPadding.calculate(animTime);
        }
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        // We call the listener's onDown() when our onShowPress() is called and
        // call the listener's onUp() when we receive any further event.
        @Override
        public void onShowPress(MotionEvent e) {
            GLRoot root = getGLRoot();
            root.lockRenderThread();
            try {
                if (isDown) return;
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) {
                    isDown = true;
                    mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }
        }

        private void cancelDown(boolean byLongPress) {
            if (!isDown) return;
            isDown = false;
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1,
                MotionEvent e2, float velocityX, float velocityY) {
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            float velocity = velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null) mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1,
                MotionEvent e2, float distanceX, float distanceY) {
            cancelDown(false);
            float distance = distanceY;
            int overDistance = mScroller.startScroll(
                    Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            cancelDown(false);
            if (mDownInScrolling) return true;
            int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            if (index != INDEX_NONE) mListener.onSingleTapUp(index);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }
    }

    public void setStartIndex(int index) {
        mStartIndex = index;
    }

    // Return true if the layout parameters have been changed
    public boolean setSlotCount(int slotCount) {
        boolean changed = mLayout.setSlotCount(slotCount);
        mBackUpSlotCount = slotCount;
        if (mStartIndex != INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(mScrollY);
        return changed;
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public Rect getSlotRect(int slotIndex, GLView rootPane) {
        // Get slot rectangle relative to this root pane.
        Rect offset = new Rect();
        rootPane.getBoundsOf(this, offset);
        Rect r = getSlotRect(slotIndex);
        r.offset(offset.left - getScrollX(),
                offset.top - getScrollY());
        return r;
    }

    private static class IntegerAnimation extends Animation {
        private int mTarget;
        private int mCurrent = 0;
        private int mFrom = 0;
        private boolean mEnabled = false;

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!mEnabled) {
                mTarget = mCurrent = target;
                return;
            }
            if (target == mTarget) return;

            mFrom = mCurrent;
            mTarget = target;
            setDuration(180);
            start();
        }

        public int get() {
            return mCurrent;
        }

        public int getTarget() {
            return mTarget;
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = Math.round(mFrom + progress * (mTarget - mFrom));
            if (progress == 1f) mEnabled = false;
        }
    }
    /// M: [FEATURE.ADD] fancy layout @{
    // slotEntry stands for each slot
    private ArrayList<SlotEntry> mSlotArray = new ArrayList<SlotEntry>();
    private HashMap<Integer, ArrayList<SlotEntry>> mSlotMapByColumn = new HashMap<Integer, ArrayList<SlotEntry>>();
    public static class SlotEntry {
        public int slotIndex;
        public int imageWidth;
        public int imageHeight;
        public int scaledWidth;
        // use these info to judge if slot is changed
        public int oriImageWidth;
        public int oriImageHeight;
        public String mimeType;
        public int rotation;
        public String albumName;

        public int scaledHeight;
        public int inWhichCol;
        public int inWhichRow;
        public Rect slotRect;
        public boolean isLandCameraFolder = false;
        private MediaType mMediaType = MediaType.INVALID;

        public SlotEntry(int index, int oritation, int imageW, int imageH, MediaItem item,
                int imageRotation, int slotW, int gap, boolean isCameraFolder, String name) {
            slotIndex = index;
            imageWidth = imageW;
            imageHeight = imageH;
            rotation = imageRotation;
            oriImageWidth = item.getWidth();
            oriImageHeight = item.getHeight();
            mimeType = item.getMimeType();
            albumName = name;

            if (imageWidth == 0 || imageHeight == 0) {
                // fix JE: item width or height is 0 in db
                imageWidth = slotW;
                imageHeight = slotW;
                isLandCameraFolder = false;
                scaledWidth = scaledHeight = slotW;
            } else {
                if (isCameraFolder && oritation == LAND) {
                    isLandCameraFolder = true;
                    scaledWidth = 2 * slotW + gap;
                    // scaledWidth and scaledHeight must meet 5:2 or 2:5
                    scaledHeight = Utils.clamp(imageHeight * scaledWidth / imageWidth,
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO_LAND),
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO));
                    MediaType mediaType = item.getMediaData().mediaType;
                    if (mediaType != MediaType.VIDEO
                            && mediaType != MediaType.CONTAINER
                            && mediaType != MediaType.PANORAMA) {
                        scaledHeight = Math.round((float) scaledWidth / FancyHelper.FANCY_CROP_RATIO_CAMERA);
                    }
                    mMediaType = mediaType;
                } else {
                    scaledWidth = slotW;
                    // scaledWidth and scaledHeight must meet 5:2 or 2:5
                    scaledHeight = Utils.clamp(imageHeight * scaledWidth / imageWidth,
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO_LAND),
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO));
                }
            }
        }

        public void update(int slotWidth, int slotGap) {
            if (imageWidth == 0 || imageHeight == 0) {
                scaledWidth = scaledHeight = slotWidth;
            } else {
                if (isLandCameraFolder) {
                    scaledWidth = 2 * slotWidth + slotGap;
                    // scaledWidth and scaledHeight must meet 5:2 or 2:5
                    scaledHeight = Utils.clamp(imageHeight * scaledWidth / imageWidth,
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO_LAND),
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO));
                    if (mMediaType != MediaType.VIDEO
                            && mMediaType != MediaType.CONTAINER
                            && mMediaType != MediaType.PANORAMA) {
                        scaledHeight = Math.round((float) scaledWidth / FancyHelper.FANCY_CROP_RATIO_CAMERA);
                    }
                } else {
                    scaledWidth = slotWidth;
                    // scaledWidth and scaledHeight must meet 5:2 or 2:5
                    scaledHeight = Utils.clamp(imageHeight * scaledWidth / imageWidth,
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO_LAND),
                            (int) (scaledWidth * FancyHelper.FANCY_CROP_RATIO));
                }
            }
        }
    }

    // use this Array to collect all slots
    public ArrayList mSlotEntry = new ArrayList<SlotEntry>();
    private AbstractGalleryActivity mActivity = null;

    public static final int COL_NUM = 2;
    public static final int LAND = 0;
    public static final int PORT = 1;
    public static final int INVALID_LAYOUT = -1;
    public static final int DEFAULT_LAYOUT = 0;
    public static final int FANCY_LAYOUT = 1;
    public ArrayList <ListLayout> mLayoutArray = new ArrayList();
    private Spec mBackUpSpec;
    private int mBackUpSlotCount = -1;
    private ListLayout mSwitchFromLayout;
    private com.android.gallery3d.app.GalleryActionBar mActionBar;

    public void setActionBar(GalleryActionBar actionBar) {
        mActionBar = actionBar;
    }

    public void switchLayout(int whichLayout) {
        int visibleIndex = -1;
        int visibleStart = -1;
        Rect visibleStartRect;

        if (mSwitchFromLayout != null) {
            visibleStart = mSwitchFromLayout.getVisibleStart();
            Log.i(TAG, "<switchLayout> <Fancy> visibleStart " + visibleStart);
        }

        switch (whichLayout) {       
            case  DEFAULT_LAYOUT:
            default:
                if (mLayout != null && !mLayout.isFancyLayout()) return;

                Log.i(TAG, "<switchLayout> <Fancy> switch to DEFAULT_LAYOUT");
                mLayout = (DefaultLayout) (mLayoutArray.get(DEFAULT_LAYOUT));
                if (mSwitchFromLayout != null) {
                    mLayout.setForceRefreshFlag(true);
                    setSlotSpec(mBackUpSpec);
                    setSlotCount(Math.max(0, mBackUpSlotCount));
                    if (mBackUpSlotCount <= 0) {
                        forceSetScrollPosition(0);
                    } else {
                        visibleStart = Utils.clamp(visibleStart, 0, mBackUpSlotCount - 1);
                        visibleStartRect = mLayout.getSlotRect(visibleStart, mTempRect);
                        forceSetScrollPosition(visibleStartRect.top);
                    }
                    mLayout.setForceRefreshFlag(false);
                }
                break;
        }
        mSwitchFromLayout = mLayout;
    }

    public void onDataChange(int index, MediaItem item, int size, boolean isCameraFolder, String albumName) {
        if (mSlotArray == null || mLayout == null) return;
        if (size == 0) {
            mSlotArray.clear();
            mLayout.clearColumnArray(0, true);
            mLayout.refreshSlotMap(0);
            if (mLayout.isFancyLayout()) {
                mLayout.setForceRefreshFlag(true);
                mLayout.setScrollPosition(0);
                mLayout.setForceRefreshFlag(false);
            }
            invalidate();
            Log.i(TAG, "<onDataChange> <Fancy> size = 0, no album in Gallery");
            return;
        }

        int slotArraySize  = mSlotArray.size();
        // no need to remove all mSlotArray, cause cluster won't reload all album from dirty album
        if (size > 0 && slotArraySize > size) {
            for (int i = slotArraySize - 1; i >= size; i--) {
                mSlotArray.remove(i);
            }
            mLayout.clearColumnArray(size - 1, false);
            mLayout.onDataChange(size - 1, null, 0, false);
            // refresh scroller bar when data changed
            mScroller.startScroll(0, 0, mLayout.getScrollLimit());
            invalidate();
        }
        if (item == null) {
            // when cover is null or when size changed, it goes here
            Log.i(TAG, "<onDataChange> <Fancy> index " + index + ", item == null, return!!!");
            return;
        }
        if (index < slotArraySize) {
            SlotEntry entry = mSlotArray.get(index);
            // for camera folder, refresh every time
            // for normal folder, skip refreshing if same info
            if (!isCameraFolder && !entry.isLandCameraFolder
                    && entry.oriImageWidth == item.getWidth()
                    && entry.oriImageHeight == item.getHeight()
                    && entry.rotation == item.getRotation()
                    && entry.mimeType.equals(item.getMimeType())) {
                return;
            }
        }

        mLayout.clearColumnArray(index, false);

        addSlot(index, item, size, isCameraFolder, albumName);
        mLayout.onDataChange(index, item, size, isCameraFolder);
        // refresh scroller bar when data changed
        mScroller.startScroll(0, 0, mLayout.getScrollLimit());
        invalidate();
        Log.i(TAG, "<onDataChange> <Fancy> index " + index + ", size " + size + ", isCameraFolder " + isCameraFolder);
    }

    private void addSlot(int index, MediaItem item, int size, boolean isCameraFolder, String albumName) {
        if (item == null || mLayout == null || mSlotArray == null) return;

        int fancyOritation;
        int rotation = item.getRotation();
        if (rotation == 90 || rotation == 270) {
            fancyOritation = item.getWidth() >= item.getHeight() ? PORT : LAND;
        } else {
            fancyOritation = item.getHeight() >= item.getWidth() ? PORT : LAND;
        }

        int slotGap = mLayout.getSlotGap();
        int slotWidth = 0;

        int screenWidth = FancyHelper.getScreenWidthAtFancyMode();
        slotWidth = (screenWidth - (COL_NUM - 1) * slotGap) / 2;
        //int subType = item.getSubType();
        MediaType mediaType = item.getMediaData().mediaType;
        SlotEntry slotEntry;

        //modify by even c687 temp start
        /*
        if (mediaType == MediaType.LIVEPHOTO) {
            slotEntry = new SlotEntry(index, PORT, FancyHelper.LIVEPHOTO_FANCYTHUMB_SIZE,
                    FancyHelper.LIVEPHOTO_FANCYTHUMB_SIZE, item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
        } else if (mediaType == MediaType.MAV) {
            slotEntry = new SlotEntry(index, PORT, FancyHelper.MAV_FANCYTHUMB_SIZE,
                    FancyHelper.MAV_FANCYTHUMB_SIZE, item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
        } else *///end 
        if (mediaType == MediaType.CONTAINER) {
            if (rotation == 90 || rotation == 270) {
                slotEntry = new SlotEntry(index, fancyOritation, item.getHeight(), item.getWidth(), item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
            } else {
                slotEntry = new SlotEntry(index, fancyOritation, item.getWidth(), item.getHeight(), item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
            }
        } else if (mediaType == MediaType.PANORAMA) {
            int height = Math.min(FancyHelper.getWidthPixels(), FancyHelper.getHeightPixels());
            int width = Math.max(FancyHelper.getWidthPixels(), FancyHelper.getHeightPixels());
            slotEntry = new SlotEntry(index, fancyOritation, width, height, item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
        } else {
            int constrainedHeight = item.getHeight();
            int constrainedWidth = item.getWidth();
            if (item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
                // also constraint video width and height, e.g. 16x1080 video
                rotation = ((LocalVideo) item).getOrientation();
                fancyOritation = (rotation == 90 || rotation == 270) ? PORT : LAND;
            }

            if (rotation == 90 || rotation == 270) {
                if ((float) item.getWidth() / (float) item.getHeight() > FancyHelper.FANCY_CROP_RATIO) {
                    constrainedWidth = Math.round((float) item.getHeight() * FancyHelper.FANCY_CROP_RATIO);
                } /*else if ((float)item.getWidth() / (float)item.getHeight() < FancyHelper.FANCY_CROP_RATIO_LAND){
                    // picture will be recognized as panorama, so may not consider this situation
                    constrainedHeight = Math.round((float)item.getWidth() / FancyHelper.FANCY_CROP_RATIO_LAND);
                }*/
                slotEntry = new SlotEntry(index, fancyOritation, constrainedHeight, constrainedWidth, item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
            } else {
                if ((float) item.getHeight() / (float) item.getWidth() > FancyHelper.FANCY_CROP_RATIO) {
                    constrainedHeight = Math.round((float) item.getWidth() * FancyHelper.FANCY_CROP_RATIO);
                } /*else if ((float)item.getHeight() / (float)item.getWidth() < FancyHelper.FANCY_CROP_RATIO_LAND) {
                    // picture will be recognized as panorama, so may not consider this situation
                    constrainedWidth = Math.round((float)item.getHeight() / FancyHelper.FANCY_CROP_RATIO_LAND);
                }*/
                slotEntry = new SlotEntry(index, fancyOritation, constrainedWidth, constrainedHeight, item, rotation, slotWidth, slotGap, isCameraFolder, albumName);
            }
        }
        if (mSlotArray.size() > index) {
            mSlotArray.set(index, slotEntry);
        } else {
            mSlotArray.add(slotEntry);
        }
    }

    public void forceSetScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, true);
    }

    public void setPaddingSpec(int paddingTop, int paddingBottom) {
        if (mLayout != null) {
            mLayout.setPaddingSpec(paddingTop, paddingBottom);
        }
    }
    /// @}
    
    //dengjianzhang@20151222 add start
    public static class ItemCoordinate{
    	
      public int mGroup = -1;
      public int mSubIndex = -1;

      public ItemCoordinate(int group, int subIndex){
    	  mGroup = group;
    	  mSubIndex = subIndex;
      }

      protected ItemCoordinate clone(){
    	  return new ItemCoordinate(mGroup, mSubIndex);
      }

      public boolean equals(Object mObject){
    	  if (mObject == null){
    		  return false;
    	  }else{
      		ItemCoordinate mItemCoordinate = (ItemCoordinate)mObject;
      		if ((mGroup == mItemCoordinate.mGroup) && (mSubIndex == mItemCoordinate.mSubIndex))
        	  return true;
      		else{
        	  return false;
          	}
    	  }
      }

      public boolean isLarge(ItemCoordinate mItemCoordinate){
  
    	  if (mGroup > mItemCoordinate.mGroup){
    		  return true;
          }
          if (mGroup < mItemCoordinate.mGroup){
        	  return false;
          }else if (mSubIndex < mItemCoordinate.mSubIndex){
        	  return false;
          }else{
        	return true;
          }
      }

      public boolean isSmall(ItemCoordinate mItemCoordinate){
       	  if (mGroup < mItemCoordinate.mGroup){
    		  return true;
          }
          if (mGroup > mItemCoordinate.mGroup){
        	  return false;
          }else if (mSubIndex > mItemCoordinate.mSubIndex){
        	  return false;
          }else{
        	return true;
          }
      }

      public boolean isTitle(){
    	  if ((mGroup >= 0) && (mSubIndex == -1)){
    		  return true;
    	  }else{
        	  return false;
          }
      }
    }


//	public void setItemCoordinate(ArrayList<ItemCoordinate> itemCoordinate) {
//		// TODO Auto-generated method stub
//		if(itemCoordinate!=null){
//			mItemCoordinate=itemCoordinate;
//			((DefaultLayout) mLayout).setItemGroup(itemCoordinate);
//		}
//	}
	
	public static class GroupData{
	    public int count;
	    public String Date;
	    public int startDate=-1;
	    public int endDate=-1;
	    
	    public GroupData (int index, String date,int start,int end) {
	    	count=index;
	    	Date =date;
	    	startDate=start;
	    	endDate=end;
		}
	}

//	public void setGroupData(ArrayList<GroupData> groupData) {
//		// TODO Auto-generated method stub
//		if(groupData!=null){
//			mGroupData=groupData;
//			((DefaultLayout) mLayout).setGroup(groupData);
//		}
//	}

	public void onDataChanged(ArrayList<ItemCoordinate> itemCoordinate,
			ArrayList<GroupData> groupData) {
		// TODO Auto-generated method stub
		//Log.d("dengjianzhang2", " mScrollPosition="+ 1);
		if(itemCoordinate!=null&&groupData!=null){			
			mItemCoordinate=itemCoordinate;
			//Log.d("dengjianzhang3", " mScrollPosition1="+ mItemCoordinate.size());
			((DefaultLayout) mLayout).setItemGroup(itemCoordinate);
			
			if(mItemCoordinate.size()==0){
				Log.d("dengjianzhang3", " mScrollPosition="+4);
				mLayout.clearColumnArray(0, true);
				mLayout.refreshSlotMap(0);
				if (mLayout.isFancyLayout()) {
					mLayout.setForceRefreshFlag(true);
	            	mLayout.setScrollPosition(0);
	            	mLayout.setForceRefreshFlag(false);
				}
				invalidate();
			}else if(mSize!=mItemCoordinate.size()){
				//Log.d("dengjianzhang3", " mScrollPosition="+ 3);
					//||(mGroupData.size()!=0&&mEnd!=mGroupData.get(mGroupData.size()-1).endDate)){
				mEnd=mGroupData.get(mGroupData.size()-1).endDate;
				mSize=mItemCoordinate.size();
				int visibleStart=mLayout.getVisibleStart();
				visibleStart=Utils.clamp(visibleStart, 0, mSize - 1);
				if(visibleStart!=0){				
					Rect visibleStartRect = mLayout.getSlotRect(visibleStart, mTempRect);
                	forceSetScrollPosition(visibleStartRect.top);
                	//forceSetScrollPosition(0);
					invalidate();
				}
			}
			mGroupData=groupData;
			((DefaultLayout) mLayout).setGroup(groupData);
		}
	}
	
    //dengjianzhang@20151222 add end
}
