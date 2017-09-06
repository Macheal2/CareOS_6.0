package com.cappu.download.push;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

import com.cappu.download.PushTypeSharedPreferences;
import com.cappu.download.database.PushSettings;
import com.cappu.download.utils.PushConstants;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemFacade;
import android.os.SystemInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

/**
 * Runs an actual download
 */
public class PushThread extends Thread {

    String TAG = "PushThread";
    private Context mContext;
    private PushInfo mInfo;
    private SystemFacade mSystemFacade;
    PushTypeSharedPreferences mPushTypeSharedPreferences;

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    
    public PushThread(Context context, SystemFacade systemFacade, PushInfo info,PushTypeSharedPreferences pushTypeSharedPreferences) {
        mContext = context;
        mSystemFacade = systemFacade;
        mInfo = info;
        this.mPushTypeSharedPreferences = pushTypeSharedPreferences;
    }

    /**
     * Returns the user agent provided by the initiating app, or use the default
     * one 返回由启动应用程序所提供的用户代理，或使用默认的
     */
    private String userAgent() {
        String userAgent = null;//mInfo.mUserAgent;
        if (userAgent != null) {
        }
        if (userAgent == null) {
            userAgent = PushConstants.DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    /**
     * State for the entire run() method.
     */
    private static class State {
        public String mFilename;
        public FileOutputStream mStream;
        public String mMimeType;
        public boolean mCountRetry = false;
        public int mRetryAfter = 0;
        public int mRedirectCount = 0;
        public String mNewUri;
        public boolean mGotData = false;
        public String mRequestUri;
        public int mPushType;

        public State(PushInfo info) {
            mMimeType = sanitizeMimeType(info.mMimeType);
            mRequestUri = info.mUri;
            mFilename = info.mFileName;
            mPushType = info.mPushType;
        }
    }

    /**
     * State within executeDownload()
     * 内部状态
     */
    private static class InnerState {
        /**下载到里多少的长度*/
        public int mBytesSoFar = 0;
        public String mHeaderETag;
        /**是否继续下载 这个值将断点下载一样*/
        public boolean mContinuingDownload = false;
        /**这个是文件总大小的长度*/
        public String mHeaderContentLength;
        public String mHeaderContentDisposition;
        public String mHeaderContentLocation;
        public int mBytesNotified = 0;
        public long mTimeLastNotification = 0;
    }

    /**
     * Raised from methods called by run() to indicate that the current request
     * should be stopped immediately.
     * 
     * Note the message passed to this exception will be logged and therefore
     * must be guaranteed not to contain any PII, meaning it generally can't
     * include any information about the request URI, headers, or destination
     * filename.
     */
    private class StopRequest extends Throwable {
        private static final long serialVersionUID = 1L;

        public int mFinalStatus;

        public StopRequest(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }

    /**
     * Raised from methods called by executeDownload() to indicate that the
     * download should be retried immediately.
     */
    private class RetryDownload extends Throwable {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Executes the download in a separate thread
     * 执行下载在单独的线程
     */
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        State state = new State(mInfo);
        AndroidHttpClient client = null;
        //DefaultHttpClient client = new DefaultHttpClient();
        PowerManager.WakeLock wakeLock = null;
        int finalStatus = PushSettings.STATUS_UNKNOWN_ERROR;//最终状态

        try {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PushConstants.TAG);//保持CPU 运转，屏幕和键盘灯有可能是关闭的
            wakeLock.acquire();

            if (PushConstants.LOGVV) {
                Log.v(PushConstants.TAG, "initiating download for " + mInfo.mUri);
            }

            client = AndroidHttpClient.newInstance(userAgent(), mContext);

            boolean finished = false;
            while (!finished) {//这里循环表示支持断点下载
                //Log.i(PushConstants.TAG, "Initiating request for download " + mInfo.mId);
                
                Log.i(TAG, "Initiating request for download " + mInfo.mId+"   获取类型是:"+mPushTypeSharedPreferences.getTypeCHName(mContext,state.mPushType)+"    mInfo.mUri:"+mInfo.mUri);
                HttpGet request = new HttpGet(state.mRequestUri);
                try {
                    executeDownload(state, client, request);
                    finished = true;
                } catch (RetryDownload exc) {
                    Log.i(TAG, "RetryDownload exc:"+exc.toString());
                } finally {
                    request.abort();
                    request = null;
                }
            }

            if (PushConstants.LOGVV) {
                Log.v(PushConstants.TAG, "download completed for " + mInfo.mUri);
            }
            if(!state.mMimeType.equals(PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT)){//如果是文本文件则不用检测文件读取权限了
                finalizeDestinationFile(state);
            }
            
            finalStatus = PushSettings.STATUS_SUCCESS;
        } catch (StopRequest error) {
            // remove the cause before printing, in case it contains PII
            //Log.w(PushConstants.TAG, "Aborting request for download " + mInfo.mId + ": " + error.getMessage());
            Log.w(TAG, "Aborting request for download " + mInfo.mId + ": " + error.getMessage());
            finalStatus = error.mFinalStatus;
            // fall through to finally block
        } catch (Throwable ex) { // sometimes the socket code throws unchecked
            // exceptions
            //Log.w(PushConstants.TAG, "Exception for id " + mInfo.mId + ": " + ex);
            Log.w(TAG, "Exception for id " + mInfo.mId + ": " + ex);
            finalStatus = PushSettings.STATUS_UNKNOWN_ERROR;
            // falls through to the code that reports an error
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
            if (client != null) {
                client.close();
                client = null;
            }
            cleanupDestination(state, finalStatus);
            
            notifyUpdateDatabase(state.mPushType,state.mFilename,finalStatus);
            
            notifyDownloadCompleted(finalStatus, state.mCountRetry, state.mRetryAfter, state.mGotData, state.mFilename, state.mNewUri,state.mMimeType);
            mInfo.mHasActiveThread = false;
            
            Log.e(TAG, "下载完最后处理的状态 finalStatus:" +finalStatus+"        如果失败查看一下失败的次数  mInfo:"+mInfo.mNumFailed);
            
        }
    }

    /**
     * Fully execute a single download request - setup and send the request,
     * handle the response, and transfer the data to the destination file.
     * 充分执行单个下载请求 - 建立和发送请求，处理响应，并将数据传输到目标文件。
     * 
     */
    private void executeDownload(State state, AndroidHttpClient client, HttpGet request) throws StopRequest, RetryDownload {
        long startTime = System.currentTimeMillis();
        //Log.v(TAG, "startTime:" + (startTime));
        InnerState innerState = new InnerState();//内部状态
        byte data[] = new byte[PushConstants.BUFFER_SIZE];//意思是每一次去下载的文件大小是多少

        setupDestinationFile(state, innerState);//这里是检测当前要下载的文件是否存在，以前是在文件后面 加 “- 数字”，可以在这里改掉
        addRequestHeaders(innerState, request);//这里是下载的http请求的一些头

        // check just before sending the request to avoid using an invalid
        // connection at all    检查只是在发送请求之前避免使用无效连接在所有
        checkConnectivity(state);//检测网络链接状态

        HttpResponse response = sendRequest(state, client, request);//请求返回来的结果
        handleExceptionalStatus(state, innerState, response);//对请求码做处理
        
        
        Header encodeHader = response.getLastHeader("Content-Encoding");
        Header typeHeader = response.getLastHeader("Content-Type");

        if (PushConstants.LOGVV) {
            Log.v(PushConstants.TAG, "received response for " + mInfo.mUri);
            Log.v(TAG, "请求的URI:" + mInfo.mUri+"   请求的类型是:"+mPushTypeSharedPreferences.getTypeCHName(mContext,state.mPushType));
        }

        //processResponseHeaders(state, innerState, response);//如果是文本类不用这一句 构造 文件路径
        
        InputStream entityStream = null;
        if(/*state.mPushType > 0*/state.mMimeType.equals(PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT)){//如果此类型存在说明是push一类
            boolean isZip = false;//如果是文本类一般需要解压
            if( state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES || state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES || state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES || state.mPushType==PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES){
                isZip = true;
            }
            if(state.mMimeType == PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT){
                isZip = true;
            }
            try {
                entityStream = handleReponse(response, isZip);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(entityStream != null){
                String str = convertStreamToString(entityStream);
                transferData(state, innerState,str);//将数据写入数据库（这里不是文件不用写入文件）
            }
            
        }else{
            processResponseHeaders(state, innerState, response);
            //entityStream = openResponseEntity(state, response);//获取网络数据中的数据流
            if(state.mMimeType.equals("text/html")){
                try {
                    Log.i(TAG, "类型出现异常以后来到这里了");
                    entityStream = handleReponse(response, true);
                    if(entityStream != null){
                        String str = convertStreamToString(entityStream);
                        Log.i(TAG, "类型出现异常以后来到这里了  str："+str);
                    }
                } catch (IOException e) {
                    Log.i(TAG, "exception 类型出现异常以后来到这里了");
                }
            }else{
                entityStream = openResponseEntity(state, response);//获取网络数据中的数据流
            }
            transferData(state, innerState, data, entityStream,null);//将数据写入文件
        }
    }
    
    public JSONObject stringToJSON(String str){
        if(str != null){
            try {
                return new JSONObject(str);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "JSON exception:"+e.toString());
            }
        }
        return null;
    }
    
    public boolean isActivate(JSONObject result) {
        int state = 0;
        try {
            state = result.getInt("state");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state == 1;
    }
    
    /**
     * 转换方法 HttpResponse->InputStream
     * 
     * @param response
     * @param gzip
     * @return
     * @throws IOException
     */
    private InputStream handleReponse(HttpResponse response, boolean gzip) throws IOException {
        InputStream is = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            if (gzip) {
                is = new GZIPInputStream(entity.getContent());
                BufferedInputStream bis = new BufferedInputStream(is);
                bis.mark(2);
                // 取前两个字节
                byte[] header = new byte[2];
                int result = bis.read(header);
                // reset输入流到开始位置
                bis.reset();
                // 判断是否是GZIP格式
                int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
                if (result != -1 && ss == GZIPInputStream.GZIP_MAGIC) {
                    is = new GZIPInputStream(bis);
                } else {
                    // 取前两个字节
                    is = bis;
                    // bis.close();
                }
            } else {
                is = new BufferedInputStream(entity.getContent());
            }
        }
        return is;
    }
    
    public String convertStreamToString(InputStream is) {
        StringBuffer buffer = new StringBuffer();
        String line = null;
        
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "IOException e:" + (e.toString()));
        }
        return buffer.toString();
    }

    /**
     * Check if current connectivity is valid for this request.
     * 检查当前的网络连接是有效的申请
     */
    private void checkConnectivity(State state) throws StopRequest {
        int networkUsable = mInfo.checkCanUseNetwork();
        if (networkUsable != PushInfo.NETWORK_OK) {
            int status = PushSettings.STATUS_WAITING_FOR_NETWORK;
            if (networkUsable == PushInfo.NETWORK_UNUSABLE_DUE_TO_SIZE) {
                status = PushSettings.STATUS_QUEUED_FOR_WIFI;
                mInfo.notifyPauseDueToSize(true);
            } else if (networkUsable == PushInfo.NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE) {
                status = PushSettings.STATUS_QUEUED_FOR_WIFI;
                mInfo.notifyPauseDueToSize(false);
            }
            throw new StopRequest(status, mInfo.getLogMessageForNetworkError(networkUsable));
        }
    }

    /**
     * Transfer as much data as possible from the HTTP response to the
     * destination file.
     * 从该HTTP 响应传输尽可能多的数据可能目标文件。
     * @param data
     *            buffer to use to read data   缓冲用于读取数据
     * @param entityStream
     *            stream for reading the HTTP response entity  流以读取HTTP响应实体
     */
    private void transferData(State state, InnerState innerState, byte[] data, InputStream entityStream,String fountData) throws StopRequest {
        Log.v(TAG, "\n\n\n获取数据前  当前数据:" + innerState.mBytesSoFar +"   总共量:" + innerState.mHeaderContentLength +"    "+mPushTypeSharedPreferences.getTypeCHName(mContext,state.mPushType)+ " for " + mInfo.mUri);
        for (;;) {
            int bytesRead = readFromResponse(state, innerState, data, entityStream);
            if (bytesRead == -1) { // success, end of stream already reached 等于-1 的时候说明读取到数据的尾部
                handleEndOfStream(state, innerState);
                return;
            }

            state.mGotData = true;
            writeDataToDestination(state, data, bytesRead);//将数据写入文件
            innerState.mBytesSoFar += bytesRead;//累加下载数据有多少了
            reportProgress(state, innerState);//更新数据库进度

            if (PushConstants.LOGVV) {
                Log.v(TAG, "downloaded  当前数据:" + innerState.mBytesSoFar +"   总共量:" + innerState.mHeaderContentLength +"    "+mPushTypeSharedPreferences.getTypeCHName(mContext,state.mPushType)+ " for " + mInfo.mUri);
            }
            checkPausedOrCanceled(state);
        }
    }
    
    private void transferData(State state, InnerState innerState, String fountData) throws StopRequest {
        Log.v(TAG, "transferData:" + (fountData)+"   state.mPushType:"+state.mPushType);
        long now = mSystemFacade.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(PushSettings.BasePushColumns.COLUMN_TITLE, fountData);
        values.put(PushSettings.PushType.PUSH_TYPE, mInfo.mPushType);
        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);

        if (fountData != null && state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES) {// 激活
            boolean activate = isActivate(stringToJSON(fountData));
            Log.v(TAG, "激活:" + (activate));
            mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, activate);
        } else if (fountData != null && state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES) {// 设置
            saveSetting(fountData);
        } else if(fountData != null && state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES){
            savePushList(mContext,fountData);
        }else if(fountData != null && state.mPushType == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES){
            savePushDetail(mContext, fountData);
        }
        Log.v(TAG, "得到的字符串是:" + (fountData));
    }
    public void savePushDetail(Context context,String fountData){
        try {
            JSONObject detailJSON = new JSONObject(fountData);
            Log.i(TAG, "判断 :"+(detailJSON.getInt("state") == 1)+"    "+(detailJSON.isNull("item")));
            if (detailJSON.getInt("state") == 1) {
                if (!detailJSON.isNull("item")) {
                    detailJSON = detailJSON.optJSONObject("item");
                    int flag = detailJSON.isNull("flag") ? 0 : detailJSON.getInt("flag");
                    String iconUrl = detailJSON.isNull("icon") ? null : detailJSON.getString("icon");
                    String bannerUrl = detailJSON.optString("banner");
                    int type = detailJSON.isNull("type") ? 0 : detailJSON.getInt("type");
                    String title = detailJSON.isNull("title") ? null : detailJSON.getString("title");
                    String introduce = detailJSON.isNull("introduce") ? null : detailJSON.getString("introduce");
                    String packageName = detailJSON.isNull("packageName") ? null : detailJSON.getString("packageName");
                    String url = detailJSON.isNull("url") ? null : detailJSON.getString("url");
                    int size = detailJSON.isNull("size") ? 0 : detailJSON.getInt("size");
                    String site = detailJSON.isNull("site") ? null : detailJSON.getString("site");

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PushSettings.PushTextInfo.TEXT_FLAG, flag);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_ICON, iconUrl);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_BANNER, bannerUrl);

                    contentValues.put(PushSettings.PushTextInfo.TEXT_TYPE, type);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_TITLE, title);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_INTRODUCE, introduce);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_PACKAGENAME, packageName);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_URL, url);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_SITE, site);
                    contentValues.put(PushSettings.PushTextInfo.TEXT_SIZE, size);
                    
                    contentValues.put(PushSettings.PushTextInfo.TEXT_DATE, mSimpleDateFormat.format(mSystemFacade.currentTimeMillis()));
                    
                    Log.i(TAG, "单条的插入数据 :"+(detailJSON.toString()));
                    context.getContentResolver().update(PushSettings.PushTextInfo.PUSH_TEXT_URI, contentValues, PushSettings.PushTextInfo.TEXT_ID +" = "+mInfo.mOtherAttachedId, null);//(PushSettings.PushTextInfo.PUSH_TEXT_URI, contentValues);
                }
            }else{
                mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "savePushList :"+fountData+"    "+e.toString());
        }

    }
    
    public void savePushList(Context context,String fountData){
            try {
                JSONObject listJSON = new JSONObject(fountData);
                Log.i(TAG, "判断 savePushList :"+(listJSON.getInt("state") == 1)+"    "+(listJSON.isNull("item")));
                if (listJSON.getInt("state") == 1) {
                    if (!listJSON.isNull("item")) {
                    	trimDatabase(context);
                        String listStr = listJSON.getString("item");
                        String[] items = listStr.split(",");
                        ContentValues contentValues = new ContentValues();
                        for (int i = 0; i < items.length && !listStr.equals(""); i++) {
                            contentValues.clear();
                            contentValues.put(PushSettings.PushTextInfo.TEXT_ID, Integer.parseInt(items[i]));
                            contentValues.put(PushSettings.PushTextInfo.TEXT_ICON_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_FAIL);
                            contentValues.put(PushSettings.PushTextInfo.TEXT_BANNER_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_FAIL);
                            contentValues.put(PushSettings.PushTextInfo.TEXT_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_FAIL);
                            context.getContentResolver().insert(PushSettings.PushTextInfo.PUSH_TEXT_URI, contentValues);
                        }
                    }
                }else{
                    mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "savePushList :"+fountData+"    "+e.toString());
            }
    }
    
    /**
     * Drops old rows from the database to prevent it from growing too large
     * 从数据库中删除旧行，以防止过大,当数据库数据超过一千行的时候将自动瘦身数据库
     * 
     */
    private void trimDatabase(Context context ) {
    	try {
    		Log.e(TAG, "裁剪数据库");
    		 Cursor cursor = context.getContentResolver().query(PushSettings.PushTextInfo.PUSH_TEXT_URI, new String[] { PushSettings.BasePushColumns._ID,PushSettings.PushTextInfo.TEXT_ICON_PATH,PushSettings.PushTextInfo.TEXT_BANNER_PATH },
    	                null, null, null);
    	        if (cursor == null) {
    	            // This isn't good - if we can't do basic queries in our database,
    	            // nothing's gonna work
    	            Log.e(TAG, "null cursor in trimDatabase");
    	            return;
    	        }
    	        if (cursor.moveToFirst()) {
    	            int numDelete = cursor.getCount() - PushConstants.MAX_DOWNLOADS;
    	            int columnId = cursor.getColumnIndexOrThrow(PushSettings.BasePushColumns._ID);
    	            int columnIcon = cursor.getColumnIndexOrThrow(PushSettings.PushTextInfo.TEXT_ICON_PATH);
    	            int columnBanner = cursor.getColumnIndexOrThrow(PushSettings.PushTextInfo.TEXT_BANNER_PATH);
    	            while (numDelete > 0) {
    	                Uri downloadUri = ContentUris.withAppendedId(PushSettings.PushTextInfo.PUSH_TEXT_URI, cursor.getLong(columnId));
    	                context.getContentResolver().delete(downloadUri, null, null);
    	                
    	                deletedrawable(cursor.getString(columnIcon));
    	                deletedrawable(cursor.getString(columnBanner));
    	                if (!cursor.moveToNext()) {
    	                    break;
    	                }
    	                numDelete--;
    	            }
    	        }
    	        cursor.close();
		} catch (Exception e) {
			Log.e(TAG, "裁剪数据库 e:"+e.toString());
		}
       
    }
    
    /**删除*/
    public void deletedrawable(String imgpath) {
        File f=new File(imgpath);
        if(f!= null && f.exists()){
            boolean b = f.delete();
            Log.i("HHJ", "删除图片 b:" + b + "   imgpath：" +imgpath);
        }
    }
    
    
    private void saveSetting(String fountData){
        mPushTypeSharedPreferences.setStringToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_KEY, fountData);
        if(fountData != null){
            try {
                JSONObject settingsJson = new JSONObject(fountData);
                int stateSettings = settingsJson.getInt("state");
                if (stateSettings == -4 || stateSettings == -5 || stateSettings == -7) {//说明推送关闭
                    mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false);
                }
                JSONObject item = settingsJson.getJSONObject("item");
                int timeIntervalType = item.isNull("unittp") ? 0 : item.optInt("unittp");//推送时间间隔 类型，有按天推送，安小时推送，安分推送
                int timeInterval = item.isNull("unit") ? 30 : item.getInt("unit");
                int pushTimeInterval = getPushInterval(timeInterval, timeIntervalType);//long 推送间隔，默认3分钟，如果有推送就每隔这么长时间弹出一条
                int pushListTimeInterval = item.isNull("heartbeat") ? 1800 : item.getInt("heartbeat");//long 心跳间隔，默认3分钟，每次心跳都拉取一遍推送列表，判断有没有新推送
                int pushCurrentDayNumMax = item.isNull("maxnum") ? 5 : item.getInt("maxnum");//每日推送上限，到上限后推送将关闭，第二天启动
                int firstIntervalType = item.isNull("recunittp") ? 0 : item.getInt("recunittp");
                int firstInterval = item.isNull("recunit") ? 1 : item.getInt("recunit");
                int pushFirstInterval = getPushInterval(firstInterval, firstIntervalType) * 1000;//long 首次联网到首次推送的时间间隔，默认半小时
                
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_FIRST_INTERVAL, pushFirstInterval);//首次安装开始推送的时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, pushTimeInterval);//每条的推送时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_LIST_TIME_INTERVAL, pushListTimeInterval);//列表获取的时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_CURRENT_DAY_NUM_MAX, pushCurrentDayNumMax);//每日推送上限
                
                /*
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_FIRST_INTERVAL, 60);//首次安装开始推送的时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 60);//每条的推送时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_LIST_TIME_INTERVAL, 60);//列表获取的时间间隔
                mPushTypeSharedPreferences.setLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_CURRENT_DAY_NUM_MAX, 60);//每日推送上限
                */
            } catch (Exception e) {
                Log.v(TAG, " JSONObject exception e:" + e.toString());
            }
        }
    }

    public int getPushInterval(int interval, int type) {
        int pushInterval = 0;
        // 1:day,2:hour,other:minute
        if (type == 1) {
            pushInterval = interval * 24 * 60 * 60;
        } else if (type == 2) {
            pushInterval = interval * 60 * 60;
        } else {
            pushInterval = interval * 60;
        }
        return pushInterval;
    }
    
    /**
     * Called after a successful completion to take any necessary action on the
     * downloaded file.
     */
    private void finalizeDestinationFile(State state) throws StopRequest {
        // make sure the file is readable 确保该文件是可读的
        FileUtils.setPermissions(state.mFilename, 0644, -1, -1);
        syncDestination(state);
    }

    /**
     * Called just before the thread finishes, regardless of status, to take any
     * necessary action on the downloaded file.
     * 
     * 这里在对码处理的时候容易出现416 错误/从而走来这里将文件删除/这里后续查找错误为什么容易删除
     */
    private void cleanupDestination(State state, int finalStatus) {
        closeDestination(state);
        if (state.mFilename != null && PushSettings.isStatusError(finalStatus)) {
            //new File(state.mFilename).delete();
            //state.mFilename = null;
        }
    }

    /**
     * Sync the destination file to storage.
     */
    private void syncDestination(State state) {
        FileOutputStream downloadedFileStream = null;
        try {
            downloadedFileStream = new FileOutputStream(state.mFilename, true);
            downloadedFileStream.getFD().sync();
        } catch (FileNotFoundException ex) {
            Log.w(TAG, "file " + state.mFilename + " not found: " + ex);
        } catch (SyncFailedException ex) {
            Log.w(TAG, "file " + state.mFilename + " sync failed: " + ex);
        } catch (IOException ex) {
            Log.w(TAG, "IOException trying to sync " + state.mFilename + ": " + ex);
        } catch (RuntimeException ex) {
            Log.w(TAG, "exception while syncing file: ", ex);
        } finally {
            if (downloadedFileStream != null) {
                try {
                    downloadedFileStream.close();
                } catch (IOException ex) {
                    Log.w(TAG, "IOException while closing synced file: ", ex);
                } catch (RuntimeException ex) {
                    Log.w(TAG, "exception while closing file: ", ex);
                }
            }
        }
    }

    /**
     * Close the destination output stream.
     * 关闭目标输出流。
     */
    private void closeDestination(State state) {
        try {
            // close the file
            if (state.mStream != null) {
                state.mStream.close();
                state.mStream = null;
            }
        } catch (IOException ex) {
                Log.v(TAG, "exception when closing the file after download : " + ex);
            // nothing can really be done if the file can't be closed
        }
    }

    /**
     * Check if the download has been paused or canceled, stopping the request
     * appropriately if it has been.
     * 检查下载已暂停或取消，停止请求适当地，如果它已
     */
    private void checkPausedOrCanceled(State state) throws StopRequest {
        synchronized (mInfo) {
            if (mInfo.mControl == PushSettings.CONTROL_PAUSED) {
                throw new StopRequest(PushSettings.STATUS_PAUSED_BY_APP, "download paused by owner");
            }
        }
        if (mInfo.mStatus == PushSettings.STATUS_CANCELED) {
            throw new StopRequest(PushSettings.STATUS_CANCELED, "download canceled");
        }
    }

    /**
     * Report download progress through the database if necessary.
     * 通过在必要的数据库报表下载进度
     */
    private void reportProgress(State state, InnerState innerState) {
        long now = mSystemFacade.currentTimeMillis();
        if (innerState.mBytesSoFar - innerState.mBytesNotified > PushConstants.MIN_PROGRESS_STEP && now - innerState.mTimeLastNotification > PushConstants.MIN_PROGRESS_TIME) {
            ContentValues values = new ContentValues();
            values.put(PushSettings.BasePushColumns.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
            mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
            innerState.mBytesNotified = innerState.mBytesSoFar;
            innerState.mTimeLastNotification = now;
        }
    }

    /**
     * Write a data buffer to the destination file.
     * 
     * @param data
     *            buffer containing the data to write
     * @param bytesRead
     *            how many bytes to write from the buffer
     */
    private void writeDataToDestination(State state, byte[] data, int bytesRead) throws StopRequest {
        for (;;) {
            try {
                if (state.mStream == null) {
                    state.mStream = new FileOutputStream(state.mFilename, true);
                }
                state.mStream.write(data, 0, bytesRead);
                //if (mInfo.mDestination == PushSettings.DESTINATION_EXTERNAL) {
                    closeDestination(state);
                //}
                return;
            } catch (IOException ex) {
                if (!PushHelpers.isExternalMediaMounted()) {
                    throw new StopRequest(PushSettings.STATUS_DEVICE_NOT_FOUND_ERROR, "external media not mounted while writing destination file");
                }

                long availableBytes = PushHelpers.getAvailableBytes(PushHelpers.getFilesystemRoot(state.mFilename));
                if (availableBytes < bytesRead) {
                    throw new StopRequest(PushSettings.STATUS_INSUFFICIENT_SPACE_ERROR, "insufficient space while writing destination file", ex);
                }
                throw new StopRequest(PushSettings.STATUS_FILE_ERROR, "while writing destination file: " + ex.toString(), ex);
            }
        }
    }

    /**
     * Called when we've reached the end of the HTTP response stream, to update
     * the database and check for consistency.
     */
    private void handleEndOfStream(State state, InnerState innerState) throws StopRequest {
        ContentValues values = new ContentValues();
        values.put(PushSettings.BasePushColumns.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
        if (innerState.mHeaderContentLength == null) {
            values.put(PushSettings.BasePushColumns.COLUMN_TOTAL_BYTES, innerState.mBytesSoFar);
        }
        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);

        boolean lengthMismatched = (innerState.mHeaderContentLength != null) && (innerState.mBytesSoFar != Integer.parseInt(innerState.mHeaderContentLength));
        if (lengthMismatched) {
            /*if (cannotResume(innerState)) {
                throw new StopRequest(PushSettings.STATUS_CANNOT_RESUME, "mismatched content length");
            } else {*/
                throw new StopRequest(getFinalStatusForHttpError(state), "closed socket before end of file  :"+"    innerState.mHeaderContentLength:"+innerState.mHeaderContentLength+"    innerState.mBytesSoFar:"+innerState.mBytesSoFar);
            //}
        }
    }

    private boolean cannotResume(InnerState innerState) {
        return innerState.mBytesSoFar > 0 /*&& !mInfo.mNoIntegrity*/ && innerState.mHeaderETag == null;
    }

    /**
     * Read some data from the HTTP response stream, handling I/O errors.
     * 
     * @param data
     *            buffer to use to read data
     * @param entityStream
     *            stream for reading the HTTP response entity
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached
     */
    private int readFromResponse(State state, InnerState innerState, byte[] data, InputStream entityStream) throws StopRequest {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            logNetworkState();
            ContentValues values = new ContentValues();
            values.put(PushSettings.BasePushColumns.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
            mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
            /*if (cannotResume(innerState)) {//这个是不能恢复重试下载的/这里我们占时不需要
                String message = "while reading response: " + ex.toString() + ", can't resume interrupted download with no ETag";
                throw new StopRequest(PushSettings.STATUS_CANNOT_RESUME, message, ex);
            } else {*/
                throw new StopRequest(getFinalStatusForHttpError(state), "while reading response: " + ex.toString(), ex);
            //}
        }
    }

    /**
     * Open a stream for the HTTP response entity, handling I/O errors.
     * 
     * @return an InputStream to read the response entity
     */
    private InputStream openResponseEntity(State state, HttpResponse response) throws StopRequest {
        try {
            return response.getEntity().getContent();
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while getting entity: " + ex.toString(), ex);
        }
    }

    private void logNetworkState() {
            Log.i(TAG, "Net " + (PushHelpers.isNetworkAvailable(mSystemFacade) ? "Up" : "Down"));
    }

    /**
     * Read HTTP response headers and take appropriate action, including setting
     * up the destination file and updating the database.
     * 查看HTTP 响应头，并采取适当的行动，包括设置目标文件和更新数据库。
     */
    private void processResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest {
        if (innerState.mContinuingDownload) {
            // ignore response headers on resume requests
            return;
        }

        readResponseHeaders(state, innerState, response);//将网络上读取到的数据保存在本地 State 里面

        try {
            Log.i("hehangjun", "state.mFilename:"+state.mFilename+"    mInfo.mHint:"+mInfo.mHint);
            state.mFilename = PushHelpers.generateSaveFile(mContext, mInfo.mUri, mInfo.mHint, innerState.mHeaderContentDisposition,
                    innerState.mHeaderContentLocation, state.mMimeType, /*mInfo.mDestination*/0,
                    (innerState.mHeaderContentLength != null) ? Long.parseLong(innerState.mHeaderContentLength) : 0, false/*mInfo.mIsPublicApi*/);
            Log.i("hehangjun", "state.mFilename:"+state.mFilename);
        } catch (PushHelpers.GenerateSaveFileError exc) {
            throw new StopRequest(exc.mStatus, exc.mMessage);
        }
        try {
            state.mStream = new FileOutputStream(state.mFilename);
        } catch (FileNotFoundException exc) {
            throw new StopRequest(PushSettings.STATUS_FILE_ERROR, "while opening destination file: " + exc.toString(), exc);
        }
        if (PushConstants.LOGVV) {
            Log.v(PushConstants.TAG, "writing " + mInfo.mUri + " to " + state.mFilename);
        }

        updateDatabaseFromHeaders(state, innerState);//更新数据库里面的数据
        // check connectivity again now that we know the total size
        checkConnectivity(state);
    }

    /**
     * Update necessary database fields based on values of HTTP response headers
     * that have been read.
     */
    private void updateDatabaseFromHeaders(State state, InnerState innerState) {
        ContentValues values = new ContentValues();
        values.put(PushSettings.BasePushColumns._DATA, state.mFilename);
        if (innerState.mHeaderETag != null) {
            values.put(PushConstants.ETAG, innerState.mHeaderETag);
        }
        if (state.mMimeType != null) {
            values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, state.mMimeType);
        }
        values.put(PushSettings.BasePushColumns.COLUMN_TOTAL_BYTES, mInfo.mTotalBytes);
        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
    }

    /**
     * Read headers from the HTTP response and store them into local state.
     * 
     * 从HTTP 响应读取头，并将它们存储到本地状态。
     * 
     */
    private void readResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest {
        Header header = response.getFirstHeader("Content-Disposition");
        if (header != null) {
            innerState.mHeaderContentDisposition = header.getValue();
        }
        header = response.getFirstHeader("Content-Location");
        if (header != null) {
            innerState.mHeaderContentLocation = header.getValue();
        }
        
        
        if (state.mMimeType == null) {
            header = response.getFirstHeader("Content-Type");
            if (header != null) {
                state.mMimeType = sanitizeMimeType(header.getValue());
            }
        }else{
            if(response != null && response.getFirstHeader("Content-Type") != null){
                Header contentType = response.getFirstHeader("Content-Type");
                if(contentType != null){
                    state.mMimeType = sanitizeMimeType(contentType.getValue());;
                }
            }
        }
        header = response.getFirstHeader("ETag");
        if (header != null) {
            innerState.mHeaderETag = header.getValue();
        }
        String headerTransferEncoding = null;
        header = response.getFirstHeader("Transfer-Encoding");
        if (header != null) {
            headerTransferEncoding = header.getValue();
        }
        if (headerTransferEncoding == null) {
            header = response.getFirstHeader("Content-Length");
            if (header != null) {
                innerState.mHeaderContentLength = header.getValue();
                mInfo.mTotalBytes = Long.parseLong(innerState.mHeaderContentLength);
            }
        } else {
            // Ignore content-length with transfer-encoding - 2616 4.4 3
            if (PushConstants.LOGVV) {
                Log.v(TAG, "ignoring content-length because of xfer-encoding");
            }
        }
        if (PushConstants.LOGVV) {
            Log.v(PushConstants.TAG, "Content-Disposition: " + innerState.mHeaderContentDisposition);
            Log.v(PushConstants.TAG, "Content-Length: " + innerState.mHeaderContentLength);
            Log.v(PushConstants.TAG, "Content-Location: " + innerState.mHeaderContentLocation);
            Log.v(PushConstants.TAG, "Content-Type: " + state.mMimeType);
            Log.v(PushConstants.TAG, "ETag: " + innerState.mHeaderETag);
            Log.v(PushConstants.TAG, "Transfer-Encoding: " + headerTransferEncoding);
        }

        boolean noSizeInfo = innerState.mHeaderContentLength == null
                && (headerTransferEncoding == null || !headerTransferEncoding.equalsIgnoreCase("chunked"));
        if (/*!mInfo.mNoIntegrity &&  */noSizeInfo) {
            throw new StopRequest(PushSettings.STATUS_HTTP_DATA_ERROR, "can't know size of download, giving up");
        }
    }

    /**
     * Check the HTTP response status and handle anything unusual (e.g. not
     * 200/206).
     * 检查HTTP 响应状态并处理任何异常（检测响应状态 不为200 以及 206）。
     * 
     * 
     * 普及以下http请求返回码知识
     *    200 返回成功
     *    503 返回失败
     *    416 是由于 断点串送的 起始点问题造成的
     * 
     */
    private int handleExceptionalStatus(State state, InnerState innerState, HttpResponse response) throws StopRequest, RetryDownload {
        int statusCode = response.getStatusLine().getStatusCode();
        Log.i(TAG, "handleExceptionalStatus statusCode:"+statusCode);
        if (statusCode == 503 && mInfo.mNumFailed < PushConstants.MAX_RETRIES) {
            handleServiceUnavailable(state, response);
        }
        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) {
            handleRedirect(state, response, statusCode);
        }

        int expectedStatus = innerState.mContinuingDownload ? 206 : PushSettings.STATUS_SUCCESS;
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, innerState, statusCode);
        }
        return statusCode;
    }

    /**
     * Handle a status that we don't know how to deal with properly.
     */
    private void handleOtherStatus(State state, InnerState innerState, int statusCode) throws StopRequest {
        int finalStatus;
        if (PushSettings.isStatusError(statusCode)) {
            finalStatus = statusCode;
        } else if (statusCode >= 300 && statusCode < 400) {
            finalStatus = PushSettings.STATUS_UNHANDLED_REDIRECT;
        } else if (innerState.mContinuingDownload && statusCode == PushSettings.STATUS_SUCCESS) {
            finalStatus = PushSettings.STATUS_CANNOT_RESUME;
        } else {
            finalStatus = PushSettings.STATUS_UNHANDLED_HTTP_CODE;
        }
        throw new StopRequest(finalStatus, "http error " + statusCode);
    }

    /**
     * Handle a 3xx redirect status.
     * 当请求 出现statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307
     * 时从定向操作
     * 
     */
    private void handleRedirect(State state, HttpResponse response, int statusCode) throws StopRequest, RetryDownload {
            Log.v(TAG, "got HTTP redirect " + statusCode);
        if (state.mRedirectCount >= PushConstants.MAX_REDIRECTS) {
            throw new StopRequest(PushSettings.STATUS_TOO_MANY_REDIRECTS, "too many redirects");
        }
        Header header = response.getFirstHeader("Location");
        if (header == null) {
            return;
        }
        if (PushConstants.LOGVV) {
            Log.v(PushConstants.TAG, "Location :" + header.getValue());
        }

        String newUri;
        try {
            newUri = new URI(mInfo.mUri).resolve(new URI(header.getValue())).toString();
        } catch (URISyntaxException ex) {
            if (PushConstants.LOGVV) {
                Log.d(PushConstants.TAG, "Couldn't resolve redirect URI " + header.getValue() + " for " + mInfo.mUri);
            }
            throw new StopRequest(PushSettings.STATUS_HTTP_DATA_ERROR, "Couldn't resolve redirect URI");
        }
        ++state.mRedirectCount;
        state.mRequestUri = newUri;
        if (statusCode == 301 || statusCode == 303) {
            // use the new URI for all future requests (should a retry/resume be
            // necessary)
            state.mNewUri = newUri;
        }
        throw new RetryDownload();
    }

    /**
     * Handle a 503 Service Unavailable status by processing the Retry-After
     * header.
     * 
     * 当出现503的时候向服务器重试请求
     */
    private void handleServiceUnavailable(State state, HttpResponse response) throws StopRequest {
            Log.v(PushConstants.TAG, "got HTTP response code 503");
        state.mCountRetry = true;
        Header header = response.getFirstHeader("Retry-After");
        if (header != null) {
            try {
                if (PushConstants.LOGVV) {
                    Log.v(TAG, "Retry-After :" + header.getValue());
                }
                state.mRetryAfter = Integer.parseInt(header.getValue());
                if (state.mRetryAfter < 0) {
                    state.mRetryAfter = 0;
                } else {
                    if (state.mRetryAfter < PushConstants.MIN_RETRY_AFTER) {
                        state.mRetryAfter = PushConstants.MIN_RETRY_AFTER;
                    } else if (state.mRetryAfter > PushConstants.MAX_RETRY_AFTER) {
                        state.mRetryAfter = PushConstants.MAX_RETRY_AFTER;
                    }
                    state.mRetryAfter += PushHelpers.sRandom.nextInt(PushConstants.MIN_RETRY_AFTER + 1);
                    state.mRetryAfter *= 1000;
                }
            } catch (NumberFormatException ex) {
                // ignored - retryAfter stays 0 in this case.
            }
        }
        throw new StopRequest(PushSettings.STATUS_WAITING_TO_RETRY, "got 503 Service Unavailable, will retry later");
    }

    /**
     * Send the request to the server, handling any I/O exceptions.
     * 发送请求到服务器，处理任何I / O异常。
     */
    private HttpResponse sendRequest(State state, AndroidHttpClient client, HttpGet request) throws StopRequest {
        try {
            
            request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
            return client.execute(request);
        } catch (IllegalArgumentException ex) {
            Log.v(TAG,  "IllegalArgumentException while trying to execute request: " + ex.toString());
            throw new StopRequest(PushSettings.STATUS_HTTP_DATA_ERROR, "while trying to execute request: " + ex.toString(), ex);
        } catch (IOException ex) {
            Log.v(TAG,  "IOException while trying to execute request: " + ex.toString());
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while trying to execute request: " + ex.toString(), ex);
        }
    }

    private int getFinalStatusForHttpError(State state) {
        if (!PushHelpers.isNetworkAvailable(mSystemFacade)) {
            return PushSettings.STATUS_WAITING_FOR_NETWORK;
        } else if (mInfo.mNumFailed < PushConstants.MAX_RETRIES) {
            state.mCountRetry = true;
            return PushSettings.STATUS_WAITING_TO_RETRY;
        } else {
            Log.w(PushConstants.TAG, "reached max retries for " + mInfo.mId);
            return PushSettings.STATUS_HTTP_DATA_ERROR;
        }
    }

    /**
     * Prepare the destination file to receive data. If the file already exists,
     * we'll set up appropriately for resumption.
     * 制备目标文件来接收数据。如果该文件已经存在，我们会为恢复适当的位置开始下载。
     */
    private void setupDestinationFile(State state, InnerState innerState) throws StopRequest {
        if (!TextUtils.isEmpty(state.mFilename)) { // only true if we've already
            // run a thread for this
            // download
            if (!PushHelpers.isFilenameValid(state.mFilename)) {
                // this should never happen
                throw new StopRequest(PushSettings.STATUS_FILE_ERROR, "found invalid internal destination filename");
            }
            // We're resuming a download that got interrupted  我们从此恢复被中断下载
            File f = new File(state.mFilename);
            
            //Log.i(TAG, "delete File:"+state.mFilename+"    743"+"    f.exists():"+f.exists());
            if (f.exists()) {
                long fileLength = f.length();
                if (fileLength == 0) {//如果文件里面的数据为0 说明是一个空文件
                    // The download hadn't actually started, we can restart from scratch
                    //下载还没有真正开始，我们可以从//从头开始
                    f.delete();
                    state.mFilename = null;
                }/* else if (mInfo.mETag == null && !mInfo.mNoIntegrity) {
                    // This should've been caught upon failure 这应该是在失败的时候 抛出异常
                    f.delete();
                    throw new StopRequest(PushSettings.STATUS_CANNOT_RESUME, "Trying to resume a download that can't be resumed");
                } */else {
                    // All right, we'll be able to resume this download  如果全部是正常的，那么我就从此开始下载
                    try {
                        state.mStream = new FileOutputStream(state.mFilename, true);
                    } catch (FileNotFoundException exc) {
                        throw new StopRequest(PushSettings.STATUS_FILE_ERROR, "while opening destination for resuming: " + exc.toString(), exc);
                    }
                    
                    Log.v(TAG, "\n\n\n 这里是断点续传:" + innerState.mBytesSoFar +"    "+ innerState.mContinuingDownload+"    fileLength:"+fileLength+"    state.mFilename:"+state.mFilename+"   mInfo.mFileName:"+mInfo.mFileName+"   \n\n\n");
                    innerState.mBytesSoFar = (int) fileLength;//将当前文件长度记录
                    if (mInfo.mTotalBytes != -1) {
                        innerState.mHeaderContentLength = Long.toString(mInfo.mTotalBytes);
                    }
                    //innerState.mHeaderETag = mInfo.mETag;
                    innerState.mContinuingDownload = true;
                    
                }
            }
        }

        if (state.mStream != null/* && mInfo.mDestination == Downloads.DESTINATION_EXTERNAL*/) {
            closeDestination(state);
        }
    }

    /**
     * Add custom headers for this download to the HTTP request.
     * 添加自定义页眉此下载HTTP请求。
     */
    private void addRequestHeaders(InnerState innerState, HttpGet request) {
        for (Pair<String, String> header : mInfo.getHeaders()) {
            request.addHeader(header.first, header.second);
            //Log.i(TAG, "添加http请求头:"+header.first+"    "+header.second+"        innerState.mContinuingDownload:"+innerState.mContinuingDownload);
        }
        if (innerState.mContinuingDownload) {
            if (innerState.mHeaderETag != null) {
                request.addHeader("If-Match", innerState.mHeaderETag);
            }
            request.addHeader("Range", "bytes=" + innerState.mBytesSoFar + "-");
        }
    }

    /**
     * Stores information about the completed download, and notifies the
     * initiating application.
     */
    private void notifyDownloadCompleted(int status, boolean countRetry, int retryAfter, boolean gotData, String filename, String uri, String mimeType) {
        notifyThroughDatabase(status, countRetry, retryAfter, gotData, filename, uri, mimeType);//这里是插入下载的数据库
        if (PushSettings.isStatusCompleted(status)) {
            mInfo.sendIntentIfRequested();
        }
    }

    /**如果下载的是图片就将数据状态保存起来*/
    private void notifyUpdateDatabase(int type,String filename,int finalStatus) {
        if(type == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES || type == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES){
            if(PushSettings.isStatusCompleted(finalStatus) && !TextUtils.isEmpty(filename)){
                if(type == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES){
                    ContentValues values = new ContentValues();
                    values.put(PushSettings.PushTextInfo.TEXT_ICON_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_SUCCESS);
                    values.put(PushSettings.PushTextInfo.TEXT_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_SUCCESS);
                    values.put(PushSettings.PushTextInfo.TEXT_ICON_PATH, filename);
                    mContext.getContentResolver().update(PushSettings.PushTextInfo.PUSH_TEXT_URI, values, PushSettings.PushTextInfo.TEXT_ID +" = "+mInfo.mOtherAttachedId, null);
                }else if(type == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES){
                    ContentValues values = new ContentValues();
                    values.put(PushSettings.PushTextInfo.TEXT_BANNER_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_SUCCESS);
                    values.put(PushSettings.PushTextInfo.TEXT_STATUS, PushSettings.PushTextInfo.TEXT_STATUS_SUCCESS);
                    values.put(PushSettings.PushTextInfo.TEXT_BANNER_PATH, filename);
                    mContext.getContentResolver().update(PushSettings.PushTextInfo.PUSH_TEXT_URI, values, PushSettings.PushTextInfo.TEXT_ID +" = "+mInfo.mOtherAttachedId, null);
                }
            }else if(mInfo.mNumFailed >= PushConstants.MAX_RETRIES){//如果图片获取次数获取失败太多/我们继续推送下一条图片详情
                long defTime = 1000;
                defTime = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 3 * defTime)*1000;//定时去获取下一条详情
                mSystemFacade.scheduleAlarm(defTime, mContext,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
            }
        }
    }

    private void notifyThroughDatabase(int status, boolean countRetry, int retryAfter, boolean gotData, String filename, String uri, String mimeType) {
        ContentValues values = new ContentValues();
        values.put(PushSettings.BasePushColumns.COLUMN_STATUS, status);
        values.put(PushSettings.BasePushColumns._DATA, filename);
        if (uri != null) {
            values.put(PushSettings.BasePushColumns.COLUMN_URI, uri);
        }
        values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, mimeType);
        values.put(PushSettings.PushType.PUSH_TYPE, mInfo.mPushType);
        values.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
        values.put(PushConstants.RETRY_AFTER_X_REDIRECT_COUNT, retryAfter);
        if (!countRetry) {
            values.put(PushConstants.FAILED_CONNECTIONS, 0);
        } else if (gotData) {
            values.put(PushConstants.FAILED_CONNECTIONS, 1);
        } else {
            values.put(PushConstants.FAILED_CONNECTIONS, mInfo.mNumFailed + 1);
        }

        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
    }

    /**
     * Clean up a mimeType string so it can be used to dispatch an intent to
     * view a downloaded asset.
     * 
     * @param mimeType
     *            either null or one or more mime types (semi colon separated).
     * @return null if mimeType was null. Otherwise a string which represents a
     *         single mimetype in lowercase and with surrounding whitespaces
     *         trimmed.
     */
    private static String sanitizeMimeType(String mimeType) {
        try {
            mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);

            final int semicolonIndex = mimeType.indexOf(';');
            if (semicolonIndex != -1) {
                mimeType = mimeType.substring(0, semicolonIndex);
            }
            return mimeType;
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
