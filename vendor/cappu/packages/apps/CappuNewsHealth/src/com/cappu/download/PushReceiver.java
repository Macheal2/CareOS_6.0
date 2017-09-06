
package com.cappu.download;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RealSystemFacade;
import android.os.SystemFacade;
import android.os.SystemInfo;
import android.os.CaseType.ChannelManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cappu.download.database.PushSettings;
import com.cappu.download.impl.Protocol;
import com.cappu.download.impl.ProtocolFactory;
import com.cappu.download.push.PushService;
import com.cappu.download.utils.PushConstants;


public class PushReceiver extends BroadcastReceiver {
    
    private final static String TAG = "PushReceiver";

    private Context mContext;
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    
    private PushTypeSharedPreferences mPushTypeSharedPreferences;
    private SystemInfo mSystemInfo;
    private ProtocolFactory mProtocolFactory;
    DownloadManager mDownloadManager;
    SystemFacade mSystemFacade;
    
    /**推送列表的时间间隔*/
    private long mPushListTimeInterval;
    
    /**每一次的请求豆是一个单独的 mRequestHeaders 如果同时请求的化注意 clear*/
    private List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();

    int mPushType = -1;
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        Log.e(TAG, "---------------- \n\n\n\n\n\n"+action+" ---------------- \n\n\n\n\n\n");
        
        
        if(mPushTypeSharedPreferences == null){
            mPushTypeSharedPreferences = new PushTypeSharedPreferences(context);
        }
        if(mSystemInfo == null){
            mSystemInfo = SystemInfo.getInstance(context);
        }
        if(mProtocolFactory == null){
            mProtocolFactory = new ProtocolFactory(mSystemInfo);
        }
        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(context);
        }
        
        if(intent != null){
            mPushType = intent.getIntExtra(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES);
        }
        if(mPushType == -1 && mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false)){//如果没有推送类型 同时页激活了 我们默认去获取设置
            mPushType = PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES;
        }
        if(mPushType == -1){
            mPushType = PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES;
        }
        
        if (PushConstants.PUSH_ACTION.equals(action)) {
            startPush(intent, context,mPushType,false);
        }else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startService(context);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null && info.isConnected()) {
                startService(context);
            }
        } else if (action.equals(PushConstants.ACTION_RETRY)) {
            startService(context);
        } else if (action.equals(PushConstants.ACTION_OPEN)
                || action.equals(PushConstants.ACTION_LIST)
                || action.equals(PushConstants.ACTION_HIDE)) {
            //handleNotificationBroadcast(context, intent);
        }else if(PushConstants.PUSH_DOWNLOAD_ACTION.equals(action)){
            if(intent != null){
                startDownload(intent,context);
            }
            
        }else{
            startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES, false);
        }
        
    }
    private void startService(Context context) {
        Log.e(TAG, "重启一个服务");
        context.startService(new Intent(context, PushService.class));
        startPush(null, context,PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES,false);
    }
    
    private void startDownload(Intent intent, Context context){
        Protocol protocol = null;
        int category = intent.getIntExtra(PushTypeSharedPreferences.PUSH_YTPE_DOWNLOAD_APK_LIST_KEY, -1);
        int pushType = intent.getIntExtra(PushTypeSharedPreferences.PUSH_YTPE_DOWNLOAD_APK_LIST_KEY, -1);
        int page = intent.getIntExtra(PushTypeSharedPreferences.APP_PI, -1);
        int num = intent.getIntExtra(PushTypeSharedPreferences.APP_PS, -1);
        
        if(category == -1 || pushType == -1 || page == -1 || num == -1){
            throw new NullPointerException("push download data is category = "+ category+"  pushType = "+pushType+"  page = "+page+"  num = "+num+" ,this all not -1");
        }
        Log.e(TAG, "开始获取 "+mPushTypeSharedPreferences.getTypeCHName(mContext,pushType)+"    type:"+pushType+"    渠道号："+mSystemInfo.getChannel(ChannelManager.PUSH_TYPE_APK_DOWNLOAD));
        ContentValues values = new ContentValues();
        switch (pushType) {
        case PushTypeSharedPreferences.PUSH_TYPE_DOWNLOAD_APK_LIST_VALUES:
            if(!mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false)){//没有激活走这里
                protocol = mProtocolFactory.getRecommendListProtocal(mSystemInfo, category, page, num,ChannelManager.PUSH_TYPE_APK_DOWNLOAD);//激活走的协议
                values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_DOWNLOAD_APK_LIST_VALUES);
                values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT);
                values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_DOWNLOAD_APK_LIST_VALUES));
            }
            break;
        }
        
        if(protocol != null){
            values.put(PushSettings.PushType.DOWNLOAD_CHANNEL, mSystemInfo.getChannel(ChannelManager.PUSH_TYPE_APK_DOWNLOAD));
            transferData(context, protocol,PushSettings.PushType.PUSH_URI, values);
        }
    }
    

    /**attached 这个是为了测试加上的附加值/目的是为了加快推送详情测试*/
    private void startPush(Intent intent, Context context,int type,boolean attached) {
        Log.e(TAG, "开始获取 "+mPushTypeSharedPreferences.getTypeCHName(mContext,type)+"    type:"+type+"    渠道号："+mSystemInfo.getChannel(ChannelManager.PUSH_TYPE_NEWS));
        Protocol protocol = null;
        ContentValues values = new ContentValues();
        switch (type) {
        case PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES:
            Log.e(TAG, "这里去激活  激活值:"+mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false));
            if(!mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false)){//没有激活走这里
                protocol = mProtocolFactory.activateProtocol(mSystemInfo,ChannelManager.PUSH_TYPE_NEWS);//激活走的协议
                values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES);
                values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT);
                values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES));
                Log.e(TAG, "---------------激活  完成-------");
            }else{
                startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES, false);
            }
            break;
        case PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES:
            long lastSettingTime = getLastPushTime(context, PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES);
            if(mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false)){//先判断是否有激活
                int count = queryTypeToData(context,PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES);
                if(Math.abs(mSystemFacade.currentTimeMillis() - lastSettingTime) > PushTypeSharedPreferences.DATE_MILLIS  ||  !(count ==  1)){//这里说明超过一天没有去获取设置了,再次去获取设置
                    Log.e(TAG, "\n\n\n\n设置    这里说明超过一天没有去获取设置了,再次去获取设置");
                    protocol = mProtocolFactory.pushSettingsProtocol(mSystemInfo,ChannelManager.PUSH_TYPE_NEWS);//设置的协议
                    values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES);
                    values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT);
                    values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES));
                }else{
                        Log.e(TAG, "\n\n\n\n设置    在一天内不需要再次请求设置,这里直接请求列表");
                        startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES,false);
                }
            }else{
                Log.e(TAG, "\n\n\n\n这里获取设置的时候 判断没有激活这里先去激活  激活值:"+mPushTypeSharedPreferences.getBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false));
                startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES,false);
            }
            break;
        case PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES:
            long lastListTime = getLastPushTime(context, PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES);
            mPushListTimeInterval = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_LIST_TIME_INTERVAL, 0)*1000;//*1000换算成毫秒
            long exeTime = Math.abs(mSystemFacade.currentTimeMillis() - lastListTime);
            int count = queryTypeToData(context,PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES);
            if(exeTime > mPushListTimeInterval || !(count ==  1)){//在一个固定时间外才去请求一次列表查看是否有新推送
                Log.e(TAG, "\n\n\n\n列表    这里说明该到去请求列表的时间了,再次去获取列表   exeTime:"+exeTime+"    pushListTimeInterval:"+mPushListTimeInterval);
                protocol = mProtocolFactory.pushListProtocol(mSystemInfo,ChannelManager.PUSH_TYPE_NEWS);//列表走的协议
                values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES);
                values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT);
                values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES));
            }else{
                Log.e(TAG, "\n\n\n\n列表    这里说明还没有到去获取列表的时间,这里去直接去请求详情");
                startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES,false);
            }
            break;
        case PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES:
            long lastDetailTime = getLastPushTime(context, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
            long pushTimeInterval = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 0)*1000;//获取单条详情推送时间间隔
            long exeDetailTime = Math.abs(mSystemFacade.currentTimeMillis() - lastDetailTime);// 当前时间  减去 上一次执行 推送详情的时间   得到已间隔时间
            
            Log.e(TAG, "\n\n\n\n 推送详情 获取  最后一次推送详情时间是："+mSimpleDateFormat.format(new Date(lastDetailTime))+"    离上一次的时间"+ exeDetailTime +"    推送详情时间间隔:"+pushTimeInterval+"毫秒");
            if(exeDetailTime > pushTimeInterval){
                Log.e(TAG, "推送详情 获取   ");
                long id = queryDetailToID(context);
                if( id > -1){//id值不为-1说明一次获取的列表没有推送完成
                    protocol = mProtocolFactory.pushDetailProtocol(mSystemInfo,id,ChannelManager.PUSH_TYPE_NEWS);//详情走的协议
                    values.put(PushSettings.PushType.COLUMN_ATTACHED_ID, id);
                    values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_TEXT);
                    values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
                    values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES));
                }else{//这里说明推送列表完成了/去获取列表或者下载图片
                    DetailInfo di = queryDetailToUrl(context);
                    if(di != null){//说明还有图片没有完成去下载图片
                        Log.i(TAG, "这里说明推送列表完成了/    但是还有部分图片没有获取完成   现在去获取图片");
                        startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES, false);
                    }else{//说明全部推送完成了
                        mPushListTimeInterval = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_LIST_TIME_INTERVAL, 0)*1000;//*1000换算成毫秒
                        Log.i(TAG, "这里说明推送列表完成了/    在此后的去  "+mPushListTimeInterval+" 获取列表 ");
                        mSystemFacade.scheduleAlarm(mPushListTimeInterval, mContext, PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES);
                    }
                }
            }else{
                Log.e(TAG, "\n\n\n\n推送详情    这里说明还没有到去获取详情的时间,这里应该发送一个广播去等待请求详情");
                long laveTime = pushTimeInterval - exeDetailTime;// 时间间隔 减与 已间隔时间 得到剩余执行的时间
                mSystemFacade.scheduleAlarm(laveTime, mContext, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
            }
            break;
        case PushTypeSharedPreferences. PUSH_TYPE_DETAIL_ICON_VALUES:
        case PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES:
            DetailInfo detailInfo = queryDetailToUrl(context);
            if(detailInfo != null){
                Log.i(TAG, "推送 icon banner 详情 detailInfo："+detailInfo.toString());
                if(detailInfo.icon_status == PushSettings.PushTextInfo.TEXT_STATUS_FAIL && detailInfo.icon != null){
                    protocol = mProtocolFactory.downloadPushImageProtocol(detailInfo.icon);//下载icon走的协议
                    values.put(PushSettings.PushType.COLUMN_ATTACHED_ID, detailInfo.id);
                    values.put(PushSettings.PushType.COLUMN_FILE_NAME_HINT, detailInfo.icon);
                    values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES);
                    values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_PNG);
                    values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES));
                    transferData(context, protocol,PushSettings.PushType.PUSH_URI, values);
                }
                if(detailInfo.banner_status == PushSettings.PushTextInfo.TEXT_STATUS_FAIL && detailInfo.banner != null){
                    if(mRequestHeaders != null){
                        mRequestHeaders.clear();
                    }
                    protocol = mProtocolFactory.downloadPushImageProtocol(detailInfo.banner);//下载icon走的协议
                    values.put(PushSettings.PushType.COLUMN_ATTACHED_ID, detailInfo.id);
                    values.put(PushSettings.PushType.COLUMN_FILE_NAME_HINT, detailInfo.banner);
                    values.put(PushSettings.PushType.PUSH_TYPE, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES);
                    values.put(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, PushSettings.BasePushColumns.PUSH_DOWNLOAD_TYPE_PNG);
                    values.put(PushSettings.PushType.PUSH_TYPE_NAME, mPushTypeSharedPreferences.getTypeCHName(mContext,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES));
                    transferData(context, protocol,PushSettings.PushType.PUSH_URI, values);
                }
                return;
            }else{
                Log.i(TAG, "detailInfo is null, 有可能列表 推送列表详情里面所有的 icon 以及 banner都获取完成了,也有可能是 还有的推送详情没有下来");
                
                /*这里为了加快详情推送就开启去下载详情*/
                //Log.i(TAG, "------------------------这里为了加快详情推送就开启去下载详情--------------------------------");
                //startPush(intent, context, PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES,true);
            }
            
        default:
            break;
        }
        if(protocol != null){
            values.put(PushSettings.PushType.DOWNLOAD_CHANNEL, mSystemInfo.getChannel(ChannelManager.PUSH_TYPE_NEWS));
            transferData(context, protocol,PushSettings.PushType.PUSH_URI, values);
        }
    }
    
    public long getLastPushTime(Context context,int type){
        long exeTime = -1;
        String where = PushSettings.PushType.PUSH_TYPE +" = " + type;
        String[] projection = new String[] { PushSettings.COLUMN_LAST_MODIFICATION};
        Cursor cursor = context.getContentResolver().query(PushSettings.PushType.PUSH_URI, projection, where, null, null);
        while (cursor != null && cursor.moveToNext()) {
            exeTime = cursor.getLong(0);
            Log.i(TAG, mPushTypeSharedPreferences.getTypeCHName(mContext,type)+"    上一次推送的时间是:"+mSimpleDateFormat.format(new Date(exeTime)));
        }
        if(cursor != null){
            cursor.close();
        }
        return exeTime;
        
    }
    
    
    /**查询数据是否存在*/
    private int queryTypeToData(Context context,int type) {
        String where = PushSettings.PushType.PUSH_TYPE + "=" +type;
        Cursor cursor = context.getContentResolver().query(PushSettings.PushType.PUSH_URI, null, where, null, null);
        int count = -1;
        
        if(count == -1){
            count = cursor.getCount();
            Log.i(TAG, "当前全部推送完成  :"+cursor.getCount());
        }
        if(cursor != null){
            cursor.close();
        }
        
        return count;
    }
    
    /**推送详情数据中没有完成的数据*/
    private long queryDetailToID(Context context) {
        String where = PushSettings.PushTextInfo.TEXT_STATUS + "=" + PushSettings.PushTextInfo.TEXT_STATUS_FAIL+" or "+ PushSettings.PushTextInfo.TEXT_ICON_STATUS +"="+PushSettings.PushTextInfo.TEXT_STATUS_FAIL+
                " or " +PushSettings.PushTextInfo.TEXT_BANNER +" = " +PushSettings.PushTextInfo.TEXT_STATUS_FAIL;
        String[] projection = new String[] { PushSettings.PushTextInfo.TEXT_ID};
        Cursor cursor = context.getContentResolver().query(PushSettings.PushTextInfo.PUSH_TEXT_URI, projection, where, null, null);
        long id = -1;
        while (cursor != null && cursor.moveToNext()) {
            id = cursor.getLong(0);
            Log.i(TAG, "当前没有推送完成 id:"+id);
        }
        
        if(id == -1){
            Log.i(TAG, "当前全部推送完成  :"+cursor.getCount());
        }
        if(cursor != null){
            cursor.close();
        }
        
        return id;
    }
    
    /**推送详情数据中没有完成的数据*/
    private DetailInfo queryDetailToUrl(Context context) {
        DetailInfo di = null;// = new DetailInfo();
        int COLUMNS_TEXT_ID = 0;
        int COLUMNS_TEXT_STATUS = 1;
        int COLUMNS_TEXT_ICON = 2;
        int COLUMNS_TEXT_ICON_STATUS = 3;
        int COLUMNS_TEXT_BANNER = 4;
        int COLUMNS_TEXT_BANNER_STATUS = 5;
        
        String where = PushSettings.PushTextInfo.TEXT_STATUS + " = " + PushSettings.PushTextInfo.TEXT_STATUS_FAIL
              +" or "+ PushSettings.PushTextInfo.TEXT_ICON_STATUS +" = "+PushSettings.PushTextInfo.TEXT_STATUS_FAIL
              +" or " +PushSettings.PushTextInfo.TEXT_BANNER_STATUS +" = " +PushSettings.PushTextInfo.TEXT_STATUS_FAIL;
        
        String[] projection = new String[] {PushSettings.PushTextInfo.TEXT_ID,PushSettings.PushTextInfo.TEXT_STATUS,
                                            PushSettings.PushTextInfo.TEXT_ICON,PushSettings.PushTextInfo.TEXT_ICON_STATUS,
                                            PushSettings.PushTextInfo.TEXT_BANNER,PushSettings.PushTextInfo.TEXT_BANNER_STATUS};
        Cursor cursor = context.getContentResolver().query(PushSettings.PushTextInfo.PUSH_TEXT_URI, projection, where, null, null);
        long id = -1;
        int id_status = 0;
        String icon = null;
        int icon_status = 0;
        String banner = null;
        int banner_status = 0;
        try {
            while (cursor.moveToNext()) {
                id = cursor.getLong(COLUMNS_TEXT_ID);
                id_status = cursor.getInt(COLUMNS_TEXT_STATUS);
                icon = cursor.getString(COLUMNS_TEXT_ICON);
                icon_status = cursor.getInt(COLUMNS_TEXT_ICON_STATUS);
                banner = cursor.getString(COLUMNS_TEXT_BANNER);
                banner_status = cursor.getInt(COLUMNS_TEXT_BANNER_STATUS);
                
                if(!TextUtils.isEmpty(icon) || !TextUtils.isEmpty(banner)){
                    di = new DetailInfo();
                    di.id = id;
                    di.icon_status = icon_status;
                    di.icon = icon;
                    di.banner = banner;
                    di.banner_status = banner_status;
                    
                    if(di.icon_status == PushSettings.PushTextInfo.TEXT_STATUS_FAIL || di.banner_status == PushSettings.PushTextInfo.TEXT_STATUS_FAIL){
                        return di;
                    }
                }
                
                Log.i(TAG, "查询推送图片     id："+id+"    icon_status:"+icon_status+"    icon:"+icon+"    banner_status:"+banner_status+"    banner:"+banner);
            }
        } catch (Exception e) {
            Log.e(TAG, "queryDetailToUrl "+e.toString());
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        
        return di;
    }
    
    class DetailInfo{
        long id = -1;
        String icon = null;
        int icon_status = 0;
        String banner = null;
        int banner_status = 0;
        @Override
        public String toString() {
            return "DetailInfo [id=" + id + ", icon=" + icon + ", icon_status=" + icon_status + ", banner=" + banner + ", banner_status=" + banner_status + "]";
        }
        
    }
    
    /**添加头属性 到构建容器内*/
    public void addRequestHeader(String header, String value) {
        if (header == null) {
            throw new NullPointerException("header cannot be null");
        }
        if (header.contains(":")) {
            throw new IllegalArgumentException("header may not contain ':'");
        }
        if (value == null) {
            value = "";
        }
        mRequestHeaders.add(Pair.create(header, value));
    }
    
    /**构建容器内将属性与值保存*/
    private void encodeHttpHeaders(ContentValues values) {
        int index = 0;
        for (Pair<String, String> header : mRequestHeaders) {
            String headerString = header.first + ": " + header.second;
            values.put(PushSettings.RequestHeaders.INSERT_KEY_PREFIX + index, headerString);
            index++;
        }
    }
    
    private void transferData(Context context,Protocol protocol,Uri uri,ContentValues values){
        String postUrl = protocol.getHost();
        if (protocol.getGetData() != null) {
            postUrl += protocol.getGetData() + mProtocolFactory.getSign(mSystemInfo.getRandomTS());
        }
        String randomTS = mSystemInfo.getRandomTS();
        
        values.put(PushSettings.PushType.COLUMN_URI, protocol.getHost()+protocol.getGetData()+mProtocolFactory.getSign(randomTS));
        values.put(PushSettings.PushType.PUSH_PROTOCOL, protocol.getGetData());
        
        addRequestHeader("ts", randomTS);
        addRequestHeader("deviceId", mSystemInfo.deviceid);
        addRequestHeader("Accept-Encoding", "gzip");
        addRequestHeader("Content-Type", "text/json;charset=UTF-8");
        
        int startPos = protocol.getStartPos();
        int endPos = protocol.getEndPos();
        if (startPos != -1 && endPos != -1) {
            addRequestHeader("Range", "bytes=" + startPos + "-");
        }
        if (!mRequestHeaders.isEmpty()) {
            encodeHttpHeaders(values);
        }
        context.getContentResolver().insert(uri, values);
    }
}
