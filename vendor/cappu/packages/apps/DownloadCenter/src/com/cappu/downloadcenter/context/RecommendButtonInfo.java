package com.cappu.downloadcenter.context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.ex.HttpException;

import com.cappu.downloadcenter.R;
import com.cappu.downloadcenter.adapter.AppExpandableListAdapter.MyImgAdapter;
import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.cache.BitmapCaches;
import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.common.view.AlertDialog;
import com.cappu.downloadcenter.common.cache.ImageDownLoadCallback;
import com.cappu.downloadcenter.download.DownloadInfo;
import com.cappu.downloadcenter.download.DownloadManager;
import com.cappu.downloadcenter.download.DownloadState;
import com.cappu.downloadcenter.download.DownloadViewHolder;
import com.cappu.downloadcenter.install.SecretlyInstallReceiver;

import com.joy.network.RecommendShortcutInfo;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;

public class RecommendButtonInfo implements ImageDownLoadCallback {
    private Context mContext;
    private FolderApplication mApp;
    private IntentFilter mInstallBackgroundfilter;
    private IntentFilter mInstallSystemfilter;
    private RecommendShortcutInfo mRecommendInfo;
    private DownloadInfo mDownloadInfo;
    private int mMaskIconStatus = 0;
    private Dialog remindDialog = null;
    private DownloadItemViewHolder mItemCallBack;
    private IconBitmap mIconBitmap = new IconBitmap();
    
    private View mConvertView;
    private ImageView mImageView;
    private TextView title;
    private LinearLayout item;

    private static final int MIN_UPDATE_TIME = 2000;//间隔2S更新数据
    private static final int ICON_FINISH = 1;
    private static final int ICON_DOWNLOAD = 2;
    private static final int ICON_PAUSE = 3;
    private static final int ICON_NONE = 4;

    class IconBitmap{
        Bitmap iconBmp;
        boolean change;
        boolean isShowDefaultImg;
    }
    
    public RecommendButtonInfo(Context context, final RecommendShortcutInfo recommendInfo) {
        this.mContext = context;
        this.mRecommendInfo = recommendInfo;
        this.mApp = (FolderApplication) context.getApplicationContext();
        mDownloadInfo = DownloadManager.getInstance().getDownloadInfo(recommendInfo.id);
        
        if (mDownloadInfo == null) {
            mDownloadInfo = new DownloadInfo(recommendInfo.id, recommendInfo.name, recommendInfo.name, recommendInfo.packageName, recommendInfo.url, recommendInfo.size);
        }
        if(Util.DEBUG)Log.e(Util.TAG,"init "+mDownloadInfo.toStater());
        
        mInstallBackgroundfilter = new IntentFilter();
        mInstallBackgroundfilter.addAction(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL_SUCCESS);//接收到安装的广播
        mInstallBackgroundfilter.addAction(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL_FAIL);//接受安装失败广播
        
        mInstallSystemfilter = new IntentFilter();
        mInstallSystemfilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mInstallSystemfilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mInstallSystemfilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        mInstallSystemfilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        mInstallSystemfilter.addDataScheme("package");
        reloaderBitmap();
    }
    
    BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            
            if (intent == null)
                return;
            
            if (intent.getAction().equals(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL_SUCCESS)) {
                String packageName = (String)intent.getExtra("packageName", "");
                if (packageName.equals(mDownloadInfo.getPackageName())) {
                    if(Util.DEBUG)Log.e(Util.TAG, "安装成功" + packageName);
                    title.setText(mDownloadInfo.getLabel());
                    item.setEnabled(true);
                    mDownloadInfo.setInstallStatus(false);
                    removeDownloadfile();
//                    getBitmap(mImageView);
                    refresh();
                    mContext.unregisterReceiver(myReceiver);
                }
            } else if (intent.getAction().equals(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL_FAIL)){
                String packageName = (String)intent.getExtra("packageName", "");
                if(Util.DEBUG)Log.e(Util.TAG, "安装失败 packageName="+ packageName+"; mDownloadInfo="+mDownloadInfo.getLabel());
                if (packageName.equals(mDownloadInfo.getLabel())) {
                    title.setText(mDownloadInfo.getLabel());
                    item.setEnabled(true);
                    removeDownloadfile();
                    String notifyStr = mContext.getResources().getText(R.string.uu_download_apk_is_error_repeat_again)+"";
                    resumeOrAddNewDownload(notifyStr, false);
//                    getBitmap(mImageView);
                    refresh();
                    mContext.unregisterReceiver(myReceiver);
                    
                }
            }
            
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName.equals(mDownloadInfo.getPackageName())){
                    if(Util.DEBUG)Log.e(Util.TAG, "卸载成功" + packageName);
                    removeDownload("");
                    mContext.unregisterReceiver(myReceiver);
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
                String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName.equals(mDownloadInfo.getPackageName())){
                    if(Util.DEBUG)Log.e(Util.TAG, "收到广播 安装好了");
                    removeDownload("");
                    mContext.unregisterReceiver(myReceiver);
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
                String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName.equals(mDownloadInfo.getPackageName())){
                    if(Util.DEBUG)Log.e(Util.TAG, "收到广播 替换好了");
                    removeDownload("");
                    mContext.unregisterReceiver(myReceiver);
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED)){
                String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName.equals(mDownloadInfo.getPackageName())){
                    if(Util.DEBUG)Log.e(Util.TAG, "收到广播 更新好了 ACTION_PACKAGE_RESTARTED");
                    removeDownload("");
                    mContext.unregisterReceiver(myReceiver);
                }
            }
        }
    };

    /**
     * 是否已经安装应用
     * @return
     */
    public boolean getIsInstall() {
        if (mDownloadInfo != null) {
            return Util.isInstallApplication(mContext, mRecommendInfo.packageName);
        }
        return false;
    }

    public boolean getIsCompleted(){
        if (mDownloadInfo.getFilesize() == 0){
            return false;
        }
        long sizeKB = Util.getAutoFileOrFilesSize(mDownloadInfo.getFileSavePath(), Util.SIZE_KB);
        if (sizeKB >= mDownloadInfo.getFilesize()) {
            return true;
        }
        return false;
    }
    
    public RecommendShortcutInfo getShorcutInfo() {
        return mRecommendInfo;
    }

    public void setView(View convert){
        this.mConvertView = convert;
        this.title = (TextView) convert.findViewById(R.id.recommend_title);//title;
        this.item = (LinearLayout) convert.findViewById(R.id.item);//item
        this.mImageView = (ImageView) convert.findViewById(R.id.recommend_icon);
        if (mItemCallBack == null){
            mItemCallBack = new DownloadItemViewHolder(null, mDownloadInfo);
        }else{
            mItemCallBack.update(mDownloadInfo);
        }
        item.setOnClickListener(itemOnClickListener);
        item.setOnLongClickListener(itemOnLongClickListener);
        
    }
    
    OnLongClickListener itemOnLongClickListener = new OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            if (Util.fileIsExist(new File(mDownloadInfo.getFileSavePath()).getAbsolutePath())) {
                String notifyStr = mContext.getResources().getText(R.string.config_delete) + "";
                removeDownload(notifyStr);
            }
            return false;
        }
    };
    
    OnClickListener itemOnClickListener = new OnClickListener() {
        public void onClick(View v) {
          //启动
            if (getIsInstall()) {// 已经安装了这个应用
                Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mRecommendInfo.packageName);
                if(Util.DEBUG)Log.e(Util.TAG,"启动已经安装的应用 intent="+(intent == null));
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(mContext.getApplicationContext(), mContext.getResources().getText(R.string.run_error), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            //安装
            if (getIsCompleted()) {// 返回绝对路径，文件是否存在
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mContext);
                String tip = mContext.getString(R.string.config_install);
                alertBuilder.setTitle(R.string.uu_folder_black_list_title).setMessage(tip + " " + mDownloadInfo.getLabel() + "?")
                .setPositiveButton(R.string.uu_folder_confirm_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadInfo.setInstallStatus(true);
                        onInstallAPK();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.uu_folder_cancel_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                remindDialog = alertBuilder.create();
                remindDialog.show();
                return;
            }

            // 没有安装
            if (mApp.getConnectedType().equals(mApp.NETWORK_TYPE_NULL)) {// 没有网络
                Toast.makeText(mContext.getApplicationContext(), mContext.getResources().getText(R.string.uu_download_no_networking_text), Toast.LENGTH_SHORT).show();
                return;
            }

            if (remindDialog != null && remindDialog.isShowing() || mContext == null) {
                return;
            }

            /*暂停*/
            if (DownloadManager.getInstance().getDownloadInfo(mDownloadInfo.getId()) != null && mDownloadInfo.getState() == DownloadState.STARTED) {// 正在下载，要去暂停
                if(Util.DEBUG)Log.e(Util.TAG, "click 暂停下载 " + mDownloadInfo.toString());
                DownloadManager.getInstance().stopDownload(mDownloadInfo);
            } else if(DownloadManager.getInstance().getDownloadInfo(mDownloadInfo.getId()) != null){//继续
                if(Util.DEBUG)Log.e(Util.TAG, "click 继续下载 "+mDownloadInfo.toString());
                String sAgeFormat = mContext.getResources().getString(R.string.config_resume);
                String notifyStr = String.format(sAgeFormat, mDownloadInfo.getLabel(), Util.formetFileSizeMB(mDownloadInfo.getFilesize() - mDownloadInfo.getCompletesize()));
                resumeOrAddNewDownload(notifyStr, true);
            } else {// 新下载
                if(Util.DEBUG)Log.e(Util.TAG, "click 新下载 " + mDownloadInfo.toString());
                String sAgeFormat = mContext.getResources().getString(R.string.config_new);
                String notifyStr = String.format(sAgeFormat, mDownloadInfo.getLabel(), Util.formetFileSizeMB(mDownloadInfo.getFilesize()));
                resumeOrAddNewDownload(notifyStr, false);
            }
        }
    };
    
    public void reloaderBitmap() {
        mIconBitmap.iconBmp = BitmapCaches.getInstance().haveImg(mContext, mRecommendInfo);
        mIconBitmap.change = false;
        mIconBitmap.isShowDefaultImg = false;
        if (mIconBitmap.iconBmp == null || mIconBitmap.iconBmp.isRecycled()) {
            mIconBitmap.isShowDefaultImg = true;
            BitmapCaches.getInstance().setSaveAppIcon(mContext, mRecommendInfo, this);
        }
    }

    public boolean getShowDefauleImg() {
        return mIconBitmap.isShowDefaultImg;
    }

    private void setDownloadInfo(DownloadInfo dInfo) {
        mDownloadInfo = dInfo;
    }

    public DownloadInfo getDownloadInfo() {
        if (mDownloadInfo != null)
            return mDownloadInfo;
        return null;
    }
    
    private void onInstallAPK(){
        try {
            mContext.unregisterReceiver(myReceiver);
        } catch (IllegalArgumentException e) {
            if(Util.DEBUG)Log.e(Util.TAG, "error没有可以注销的广播，请重新注册广播" + e.getMessage());
        }
        if (mDownloadInfo.getPackageName().equals("com.tencent.mm") || mDownloadInfo.getPackageName().equals("com.android.dazhihui")) {
            mDownloadInfo.setInstallStatus(false);
            title.setText(mDownloadInfo.getLabel());
            item.setEnabled(true);
            Util.installAPK(mContext, mDownloadInfo.getFileSavePath(), mDownloadInfo.getLabel(), false);
            mContext.registerReceiver(myReceiver, mInstallSystemfilter);
        } else {
            Util.installAPK(mContext, mDownloadInfo.getFileSavePath(), mDownloadInfo.getLabel(), true);
            mDownloadInfo.setInstallStatus(true);
            mContext.registerReceiver(myReceiver, mInstallBackgroundfilter);
        }
    }
    
    private void resumeOrAddNewDownload(String notifyStr, final boolean newOrResume) {
        if (mApp.getConnectedType().equals(mApp.NETWORK_TYPE_NULL)) {// 没有网络
            Toast.makeText(mContext.getApplicationContext(), mContext.getResources().getText(R.string.uu_download_no_networking_text), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (notifyStr.equals("")) {// 不需要对话框
            try {
                DownloadManager.getInstance().startDownload(mItemCallBack);
            } catch (DbException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.e(Util.TAG, e.getMessage());
            }
//            getBitmap(mImageView);
            refresh();
        } else {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mContext);
            alertBuilder.setTitle(R.string.uu_folder_black_list_title).setMessage(notifyStr).setPositiveButton(R.string.uu_folder_confirm_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DownloadManager.getInstance().startDownload(mItemCallBack);
                    } catch (DbException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        if(Util.ERROR_DEBUG)Log.e(Util.TAG, e.getMessage());
                    }
//                    getBitmap(mImageView);
                    refresh();
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.uu_folder_cancel_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            remindDialog = alertBuilder.create();
            remindDialog.show();
        }
    }
    
    private void removeDownloadDB() {
        if (DownloadManager.getInstance().getDownloadInfo(mDownloadInfo.getId()) != null) {
            try {
                DownloadManager.getInstance().removeDownload(mDownloadInfo);
            } catch (DbException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.e(Util.TAG, e.getMessage());
            }
        }
        mDownloadInfo.setState(DownloadState.STOPPED);
        mDownloadInfo.setCompletesize(0L);
        mDownloadInfo.setInstallStatus(false);
    }
    
    private boolean removeDownloadfile(){
        boolean ret = false;
        // 如果文件存在就删除
        File localfile = new File(mDownloadInfo.getFileSavePath());
        if (localfile.exists()) {
            ret = localfile.delete();
        }
        return ret;
    }
    
    /**
     * 清除下载信息
     */
    private void removeDownload(String notifyStr){
        if (notifyStr.equals("")) {// 不需要对话框
            removeDownloadDB();
            removeDownloadfile();
            refresh();
        } else {// 有对话框
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mContext);
            alertBuilder.setTitle(R.string.uu_folder_black_list_title).setMessage(notifyStr)
            .setPositiveButton(R.string.uu_folder_confirm_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    removeDownloadDB();
                    removeDownloadfile();
                    refresh();
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.uu_folder_cancel_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            remindDialog = alertBuilder.create();
            remindDialog.show();
        }
    }
    
    public void refresh() {
        if (item == null || title == null || mImageView == null) {
            return;
        }
        title.setText(mDownloadInfo.getLabel());
        if (mIconBitmap.isShowDefaultImg && (mIconBitmap.iconBmp == null)) {
            return;
        }
        Bitmap tempBmp;
        tempBmp = BitmapCaches.getInstance().haveImg(mContext, mRecommendInfo);
        if (tempBmp == null) {
            mIconBitmap.iconBmp = tempBmp;
            mIconBitmap.change = false;
            mIconBitmap.isShowDefaultImg = true;
            if(mDownloadInfo.getInstallStatus()){
                mConvertView.setEnabled(false);
                title.setText(R.string.uu_download_installing);
            }
            return;
        }
        
        mIconBitmap.isShowDefaultImg = false;
        mConvertView.setEnabled(true);
        
        if(mDownloadInfo.getInstallStatus()){
            mConvertView.setEnabled(false);
            title.setText(R.string.uu_download_installing);
            mIconBitmap.iconBmp = tempBmp;
            mImageView.setImageBitmap(mIconBitmap.iconBmp);
            return;
        }
        
        double completesize;
        double filesize;
        double percent;

        DownloadState state = mDownloadInfo.getState();
        if(Util.DEBUG)Log.e(Util.TAG, "refresh state=" + mDownloadInfo.toStater());
        switch (state) {
        case WAITING:
            title.setText(R.string.uu_download_wait);
            mIconBitmap.iconBmp = Util.ImageIconFermodeUndownload(mContext, tempBmp);
            mIconBitmap.change = true;
            break;
        case STARTED:
            completesize = mDownloadInfo.getCompletesize();
            filesize = mDownloadInfo.getFilesize();
            percent = Math.min(1.0, completesize / filesize);
            if(Util.DEBUG)Log.e(Util.TAG, "completesize=" + completesize + "; filesize=" + filesize + "; percent=" + percent);
            mIconBitmap.iconBmp = Util.ImageIconFermodeRotateLoading(mContext, tempBmp, false, percent);
            mIconBitmap.change = true;
            break;
        case ERROR:
        case STOPPED:
            if (DownloadManager.getInstance().getDownloadInfo(mDownloadInfo.getId()) != null) {// 暂停
                completesize = mDownloadInfo.getCompletesize();
                filesize = mDownloadInfo.getFilesize();
                percent = Math.min(1.0, completesize / filesize);
                mIconBitmap.iconBmp = Util.ImageIconFermodeRotateLoading(mContext, tempBmp, true, percent);
                mIconBitmap.change = true;
            } else {// stop && error
                if (getIsInstall() || getIsCompleted()) {// 完成
                    if (mIconBitmap.change) {
                        mIconBitmap.iconBmp = tempBmp;
                        mIconBitmap.change = false;
                    }
                } else {
                    if (!mIconBitmap.change) {
                        mIconBitmap.iconBmp = Util.ImageIconFermodeUndownload(mContext, tempBmp);
                        mIconBitmap.change = true;
                    }
                }
            }
            break;
        case FINISHED:
            if (mIconBitmap.change) {
                mIconBitmap.iconBmp = tempBmp;
                mIconBitmap.change = false;
            }
            break;
        default:
            if (mIconBitmap.change) {
                mIconBitmap.iconBmp = tempBmp;
                mIconBitmap.change = false;
            }
            break;
        }
        mImageView.setImageBitmap(mIconBitmap.iconBmp);
    }
    
    /**
     * 下载状态回调函数
     */
    public class DownloadItemViewHolder extends DownloadViewHolder {
        View view;
        DownloadInfo downloadInfo;

        public DownloadItemViewHolder(View view, DownloadInfo downloadInfo) {
            super(view, downloadInfo);
            this.view = view;
            this.downloadInfo = downloadInfo;
            refresh();
        }

        @Override
        public void update(DownloadInfo downloadInfo) {
            super.update(downloadInfo);
            refresh();
        }

        @Override
        public void onWaiting() {
            downloadInfo.setState(DownloadState.WAITING);
            refresh();
        }

        @Override
        public void onStarted() {
            downloadInfo.setState(DownloadState.STARTED);
            refresh();
        }

        @Override
        public void onLoading(long total, long current) {
            if(Util.DEBUG)Log.e(Util.TAG,"DownloadItemViewHolder --onLoading: "+total+"; "+current+"; downloadInfo="+downloadInfo.getLabel());
            downloadInfo.setState(DownloadState.STARTED);
            downloadInfo.setCompletesize(current / 1024);
            refresh();
        }

        @Override
        public void onSuccess(File result) {
            downloadInfo.setState(DownloadState.FINISHED);
            if (getIsCompleted()) {
                removeDownloadDB();
                onInstallAPK();
            } else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mContext);
                String tip = mContext.getString(R.string.config_install_error_redownload);
                alertBuilder.setTitle(R.string.uu_folder_black_list_title).setMessage(tip + " " + mRecommendInfo.name + "?")
                        .setPositiveButton(R.string.uu_folder_confirm_action, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDownload("");
                                resumeOrAddNewDownload("", false);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.uu_folder_cancel_action, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDownload("");
                                dialog.dismiss();
                            }
                        });
                remindDialog = alertBuilder.create();
                remindDialog.show();
            }
            refresh();
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            if(Util.DEBUG)Log.e(Util.TAG, "DownloadItemViewHolder --onError:" + ex.toString());
            downloadInfo.setState(DownloadState.ERROR);
            String failString = "";
            if (ex.toString().indexOf("org.apache.http.conn.ConnectTimeoutException") != -1) {
                failString = mContext.getString(R.string.downloading_timeout);
            } else if (ex.toString().indexOf("java.net.UnknownHostException") != -1) {
                failString = mContext.getString(R.string.downloading_not_network);
            } else if (ex.toString().indexOf("maybe the file has downloaded completely") != -1) {
                failString = "下载失败重新下载";
                removeDownload("");
            } else if (ex.toString().indexOf("java.net.SocketTimeoutException") != -1) {
                failString = mContext.getString(R.string.downloading_timeout);
            } else {
                failString = "下载失败,请重试！";
            }

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mContext);
            alertBuilder.setTitle(R.string.uu_folder_black_list_title).setMessage(failString).setPositiveButton(R.string.uu_folder_confirm_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    refresh();
                    dialog.dismiss();
                }
            });
            remindDialog = alertBuilder.create();
            remindDialog.show();
            refresh();
        }

        @Override
        public void onCancelled(Callback.CancelledException cex) {
            if(Util.DEBUG)Log.e(Util.TAG,"DownloadItemViewHolder --onCancelled: "+cex.toString()+cex.getMessage());
            downloadInfo.setState(DownloadState.STOPPED);
            refresh();
        }
    }
    
    /**
     * 异步加载软件ICON的回调函数
     */
    @Override
    public void imageDownLoaded(int status, Bitmap bm) {
        // TODO Auto-generated method stub
        if (status == 3) {
            if (bm == null) {
                mIconBitmap.isShowDefaultImg = true;
                mIconBitmap.iconBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.onlinefolder_app_icon_default);
            } else {
                mIconBitmap.isShowDefaultImg = false;
                mIconBitmap.iconBmp = bm;
            }

            mMaskIconStatus = ICON_FINISH;

            if (mImageView != null) {
//                getBitmap(mImageView);
                refresh();
            }
        }
    }

}