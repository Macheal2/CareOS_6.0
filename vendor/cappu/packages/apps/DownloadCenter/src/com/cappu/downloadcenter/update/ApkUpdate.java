package com.cappu.downloadcenter.update;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.common.view.AlertDialog;
import com.cappu.downloadcenter.common.view.AlertDialog.Builder;
import com.joy.network.util.SystemInfo;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.AsyncTask.Status;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.cappu.downloadcenter.R;

public class ApkUpdate {
    private Context mContext = null;
    private Activity mActivity;

    private String mPackName;
    private String mClassName;

    /** 图片 */
    private static final String UPGRADE_ICON_KEY = "icon";
    /** 是否必须升级 */
    private static final String UPGRADE_ISFORCE_KEY = "isforce";
    /** 软件名字 */
    private static final String UPGRADE_NAME_KEY = "name";
    /** 文件的md5码 */
    private static final String UPGRADE_MD5_KEY = "md5";
    /** 摘要 升级的内容摘要 */
    private static final String UPGRADE_DESCRIPTION_KEY = "description";
    /** apk下载路径 */
    private static final String UPGRADE_PATH_KEY = "path";
    /** 升级数据返回结果 */
    private static final String UPGRADE_RT_KEY = "rt";
    /** 服务器上的版本号 对应的名字 */
    private static final String UPGRADE_VERSION_KEY = "version";
    /** 服务器上的版本号 */
    private static final String UPGRADE_VCODE_KEY = "vcode";
    /** 当前检测的时间 */
    private static final String UPGRADE_TIME_KEY = "time";
    /** 是否忽略此版本 */
    private static final String UPGRADE_IGNORE_KEY = "ignore";

    AsyncTask<String, Void, ApkInfo> mAsyncTask;

    AlertDialog mAlertDialog;
    // 通知栏
    private NotificationManager mNotificationManager = null;
    NotificationCompat.Builder mBuilder;
    private int NOTIFICATION_ID = 10;

    class ApkInfo {
        /** 图片 */
        String icon;
        boolean isforce;
        String name;
        String md5;
        String description;
        /** apk下载路径 */
        String path;
        int rt;
        String version;
        int vcode;
        long currenTime;
        boolean ignore;

        private void put() {
            KookSharedPreferences.putString(mContext, UPGRADE_ICON_KEY, icon);
            KookSharedPreferences.putBoolean(mContext, UPGRADE_ISFORCE_KEY, isforce);
            KookSharedPreferences.putString(mContext, UPGRADE_NAME_KEY, name);
            KookSharedPreferences.putString(mContext, UPGRADE_MD5_KEY, md5);
            KookSharedPreferences.putString(mContext, UPGRADE_DESCRIPTION_KEY, description);
            KookSharedPreferences.putString(mContext, UPGRADE_PATH_KEY, path);
            KookSharedPreferences.putInt(mContext, UPGRADE_RT_KEY, rt);
            KookSharedPreferences.putString(mContext, UPGRADE_VERSION_KEY, version);
            KookSharedPreferences.putInt(mContext, UPGRADE_VCODE_KEY, vcode);
            KookSharedPreferences.putLong(mContext, UPGRADE_TIME_KEY, System.currentTimeMillis());
            KookSharedPreferences.putBoolean(mContext, UPGRADE_IGNORE_KEY, false);
        }

        public ApkInfo get(ApkInfo apkInfo) {
            apkInfo.icon = KookSharedPreferences.getString(mContext, UPGRADE_ICON_KEY);
            apkInfo.isforce = KookSharedPreferences.getBoolean(mContext, UPGRADE_ISFORCE_KEY);
            apkInfo.name = KookSharedPreferences.getString(mContext, UPGRADE_NAME_KEY);
            apkInfo.md5 = KookSharedPreferences.getString(mContext, UPGRADE_MD5_KEY);
            apkInfo.description = KookSharedPreferences.getString(mContext, UPGRADE_DESCRIPTION_KEY);
            apkInfo.path = KookSharedPreferences.getString(mContext, UPGRADE_PATH_KEY);
            apkInfo.rt = KookSharedPreferences.getInt(mContext, UPGRADE_RT_KEY);
            apkInfo.version = KookSharedPreferences.getString(mContext, UPGRADE_VERSION_KEY);
            apkInfo.vcode = KookSharedPreferences.getInt(mContext, UPGRADE_VCODE_KEY);
            apkInfo.currenTime = KookSharedPreferences.getLong(mContext, UPGRADE_TIME_KEY);
            apkInfo.ignore = KookSharedPreferences.getBoolean(mContext, UPGRADE_IGNORE_KEY);
            return apkInfo;
        }
    }

    ApkUpdate() {

    }

    private void initThread() {
        mAsyncTask = new AsyncTask<String, Void, ApkInfo>() {

            private boolean isdetect = false;

            @Override
            protected ApkInfo doInBackground(String... params) {
                String postUrl = params[0];
                isdetect = params[1] == null ? false : true;

                if (TextUtils.isEmpty(postUrl)) {
                    return null;
                }

                ApkInfo apkInfo = null;
                HttpGet httpget = new HttpGet(params[0]);
                HttpResponse httpResponse = null;
                try {
                    httpResponse = new DefaultHttpClient().execute(httpget);
                    if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+"-------------1result:" + httpResponse.getStatusLine().getStatusCode());
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        // 第三步，使用getEntity方法活得返回结果
                        String result = EntityUtils.toString(httpResponse.getEntity());
                        if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate ---------2result:" + result);
                        JSONObject json = new JSONObject(result);

                        int rus = json.isNull("rt") ? -1 : json.getInt("rt");
                        if (rus == 1) {
                            apkInfo = new ApkInfo();
                            apkInfo.icon = json.isNull(UPGRADE_ICON_KEY) ? "" : json.getString(UPGRADE_ICON_KEY);
                            apkInfo.isforce = json.isNull(UPGRADE_ISFORCE_KEY) ? false : json.getBoolean(UPGRADE_ISFORCE_KEY);
                            apkInfo.description = json.isNull(UPGRADE_DESCRIPTION_KEY) ? "" : json.getString(UPGRADE_DESCRIPTION_KEY);
                            apkInfo.name = json.isNull(UPGRADE_NAME_KEY) ? null : json.getString(UPGRADE_NAME_KEY);
                            apkInfo.md5 = json.isNull(UPGRADE_MD5_KEY) ? null : json.getString(UPGRADE_MD5_KEY);
                            apkInfo.path = json.isNull(UPGRADE_PATH_KEY) ? null : json.getString(UPGRADE_PATH_KEY);
                            apkInfo.rt = json.isNull(UPGRADE_RT_KEY) ? -1 : json.getInt(UPGRADE_RT_KEY);
                            apkInfo.version = json.isNull(UPGRADE_VERSION_KEY) ? null : json.getString(UPGRADE_VERSION_KEY);
                            apkInfo.vcode = json.isNull(UPGRADE_VCODE_KEY) ? -1 : json.getInt(UPGRADE_VCODE_KEY);
                            apkInfo.put();

                            /*
                             * ：{"icon":
                             * "/v1/getFile?path=/upload/clientfile/20160628103400953.png"
                             * , "isforce":false,
                             * "description":"就斤斤计较斤斤计较斤斤计较斤斤计较斤斤计较斤斤计较经济",
                             * "name":"爱分享",
                             * "md5":"8856d075cfaab89c6a9dd9ade60768a3", "path":
                             * "/v1/getFile?path=/upload/clientfile/20160628103400953.apk"
                             * , "rt":1, "version":"1.0.3", "vcode":2}
                             */
                        }

                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ApkUpdate"+"ERROR ----ClientProtocolException :" + e.toString());
                } catch (IOException e) {
                    if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ApkUpdate"+"ERROR ----IOException :" + e.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ApkUpdate"+"ERROR ----JSONException :" + e.toString());
                }
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----postUrl:" + postUrl);
                return apkInfo;
            }

            /** doInBackground 之后 */
            @Override
            protected void onPostExecute(ApkInfo apkInfo) {
                if (apkInfo != null) {
                    if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onPostExecute  服务器获取到的versionCode:" + apkInfo.vcode + "    当前软件 versionCode:" + ApkInfoUtil.getAppVersionCode(mContext, null));
                    if (apkInfo.vcode > ApkInfoUtil.getAppVersionCode(mContext, null)) {
                        tips(apkInfo);
                    } else {
                        if (isdetect) {
                            Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }

            /** doInBackground 之前 */
            @Override
            protected void onPreExecute() {

            }
        };
    }

    @SuppressLint("NewApi")
    private void tips(final ApkInfo apkInfo) {
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new Builder(mActivity);
            builder.setMessage(R.string.uu_folder_update_dialog_content);

            if (apkInfo.isforce) {
                builder.setTitle("请更新最新软件、否则无法使用");
            } else {
                builder.setTitle("软件更新");
            }

            builder.setPositiveButton(R.string.uu_folder_confirm_action, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File filedirs = new File(UpdateCenter.BASE_PATH);
                    if (!filedirs.exists()) {
                        filedirs.mkdirs();
                    }
                    if (!mActivity.isFinishing()) {
                        String url = UpdateCenter.BASE_URL + apkInfo.path + UpdateCenter.getSign(mActivity) + UpdateCenter.getAppKeyToOther(mActivity);
                        String fileName = getFileNameByUrl(apkInfo.path);
                        String apkAbsPath = UpdateCenter.BASE_PATH + fileName;
                        File file = new File(apkAbsPath);
                        if (file.exists()) {// 当本地下载以后如果有此apk
                            if (ApkInfoUtil.getAppVersionCode(mContext, apkAbsPath) == apkInfo.vcode) {
                                ApkInfoUtil.install(mContext, apkAbsPath);
                                dialog.dismiss();
                                return;
                            }
                        }
                        downloadFile(apkInfo, url, apkAbsPath);
                        dialog.dismiss();
                    }
                }
            });

            builder.setNegativeButton(R.string.uu_folder_cancel_action, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (apkInfo.isforce) {
                        mActivity.finish();
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            mAlertDialog = builder.create();
        }

        if (!mActivity.isDestroyed() && !mActivity.isFinishing()) {
            mAlertDialog.show();
        }

    }

    /**
     * 根据url返回文件名
     * 
     * @param content
     * @return
     */
    public static String getFileNameByUrl(String url) {
        if (url == null || "".equals(url.trim())) {
            return null;
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    void setContext(final Activity activity) {
        if (activity == null) {
            if(Util.DEBUG)Log.e(Util.TAG, "unexpected null context in onResume");
        } else {
            mContext = activity.getApplicationContext();
            this.mActivity = activity;
            mClassName = activity.getClass().getName();
            mPackName = mContext.getPackageName();
        }
    }

    @SuppressWarnings("deprecation")
    public void createNotification(final ApkInfo apkInfo) {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                // .setContentIntent(getDefalutIntent(0))
                // .setNumber(number)//显示数量
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                // .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                // .setDefaults(Notification.DEFAULT_VIBRATE)//
                // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                // Notification.DEFAULT_ALL Notification.DEFAULT_SOUND 添加声音 //
                // requires VIBRATE permission
                .setSmallIcon(android.R.drawable.stat_sys_download);// android.R.drawable.stat_sys_download;android.R.drawable.stat_sys_download;
    }

    public void startDownload(String postUrl) {
        long time = System.currentTimeMillis() - KookSharedPreferences.getLong(mContext, UPGRADE_TIME_KEY);
        if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----System.currentTimeMillis():" + System.currentTimeMillis() + "    " + KookSharedPreferences.getLong(mContext, UPGRADE_TIME_KEY) + "    time:" + time);
        if (time > UpdateCenter.DAY_MILS || mContext.getPackageName().equals("com.cappu.download")) {// 更新时间必须是超过一天
            detectVersion(postUrl, null);
        } else {
            if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----当前时间没有超过一天没必要去更新");
        }
    }

    public void detectVersion(String postUrl, String isdetect) {
        Status status = null;
        if (mAsyncTask != null) {
            status = mAsyncTask.getStatus();
            if (Status.FINISHED == status) {
                mAsyncTask = null;
            }
        }
        if (mAsyncTask == null) {
            initThread();
        }

        if (status != Status.RUNNING) {
            mAsyncTask.execute(postUrl, isdetect);
        }

    }
    
    private void downloadFile(final ApkInfo apkInfo,final String url, String apkAbsPath) {
        if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----downloadFile  url:"+url+"    fileName:"+apkAbsPath);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setSaveFilePath(apkAbsPath);
        
       // x.http().
        
        x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onWaiting");
                if(mNotificationManager != null){
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
            }
            @Override
            public void onStarted() {
                if(mNotificationManager == null){
                    createNotification(apkInfo);
                }
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onStarted");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                int percent = (int) (current * 100 / total);
                
                
                if(mNotificationManager != null){
                    mBuilder.setContentTitle(apkInfo.name);
                    mBuilder.setContentText(percent+"%");
                    mBuilder.setProgress(100, percent, false); // 这个方法是显示进度条
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                }
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onLoading total:"+total+"   current:"+current+"    "+percent+"%");
            }
            
            @Override
            public void onSuccess(File result) {
                String fileMd5 = UpdateCenter.md5EncodeToFile(result);
                String serverMd5 = KookSharedPreferences.getString(mContext, UPGRADE_MD5_KEY);
                
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onSuccess result:"+result.getAbsolutePath()+"   fileMd5:"+fileMd5+"    serverMd5:"+serverMd5);
                if(serverMd5.equals(fileMd5)){
                    if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----下载成功");
                    ApkInfoUtil.install(mContext,result.getAbsolutePath());
                }else{
                    if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----下载失败");
                }
                if(mNotificationManager != null){
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
            }
            
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onError  ex:"+ex.toString());
                ex.printStackTrace();
                if(mNotificationManager != null){
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
                
            }

            @Override
            public void onCancelled(CancelledException cex) {
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onCancelled  cex:"+cex.toString());
                if(mNotificationManager != null){
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
            }
            
            @Override
            public void onFinished() {
                if(Util.DEBUG)Log.i(Util.TAG, "ApkUpdate"+" ----onFinished  ");
                if(mNotificationManager != null){
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
            }
        });
    }
}
