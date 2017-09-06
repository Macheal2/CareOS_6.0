package com.cappu.downloadcenter.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.context.FolderApplication;

import android.content.SharedPreferences;
import java.util.HashSet;

//end

public class SecretlyInstallReceiver extends BroadcastReceiver {

    boolean isDebug = true;
    static String TAG = "SecretlyInstallReceiver";

    public static final String ACTION_SECRETLY_INSTALL = "com.cappu.downloadcenter.ACTION_SECRETLY_INSTALL";
    public static final String ACTION_SECRETLY_INSTALL_SUCCESS = "com.cappu.downloadcenter.ACTION_SECRETLY_INSTALL_SUCCESS";
    public static final String ACTION_SECRETLY_INSTALL_FAIL = "com.cappu.downloadcenter.ACTION_SECRETLY_INSTALL_FAIL";

    public static final String INSTALL_APK_NAME = "install_apk_name";
    public static final String INSTALL_APK_PATCH = "install_apk_patch";

    // added by yzs 20141113
    private static Context mContext;
    private static String mApkName;
    private static HashSet<String> mSet = new HashSet<String>();

    // end

    @Override
    public void onReceive(Context context, Intent intent) {
        String actiongString = intent.getAction();
        if (!ACTION_SECRETLY_INSTALL.equals(actiongString)) {
            return;
        }
        Bundle bundle = intent.getExtras();

        final String apkPatch = bundle.getString(INSTALL_APK_PATCH);
        final String apkName = bundle.getString(INSTALL_APK_NAME);

        mContext = context;
        mApkName = apkName.trim();

        new Thread(new Runnable() {

            @Override
            public void run() {
                SecretlyInstall(apkPatch, apkName);
            }
        }).start();

    }

    public static void SecretlyInstall(String apkPatch, String apkName) {

        if (apkName != null && apkPatch != null) {
        }
        if (apkPatch.equals("assets")) {
            String toPath = "/data/data/" + FolderApplication.mContext.getPackageName();
            CopyApkFromAssets(toPath, apkName);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            toPath = toPath + "/files/";
            install(toPath + apkName, apkName, true);
        } else {
            install(apkPatch, apkName, true);//路径，包名，是否安装完删除
        }
    }

    /**
     * copy apk from assets add by wanghao
     * 
     * @param apkName
     */
    private static void CopyApkFromAssets(String toPath, String apkName) {

        File file = new File(toPath, apkName);
        try {
            InputStream is = FolderApplication.mContext.getAssets().open(apkName);
            if (is == null) {
                return;
            }
            if (!file.exists()) {
                {
                    File folder = new File(toPath);
                    if (!folder.exists())
                        folder.mkdirs();
                }

                file.createNewFile();
                FileOutputStream os = FolderApplication.mContext.openFileOutput(file.getName(), Context.MODE_WORLD_WRITEABLE);
                byte[] bytes = new byte[512];
                @SuppressWarnings("unused")
                int i = -1;
                while ((i = is.read(bytes)) > 0) {
                    os.write(bytes);
                }

                os.close();
                is.close();
                if (Util.DEBUG) Log.i(Util.TAG, TAG+" CopyApkFromAssets----coif(Constants.DEBUG)if(Constants.DEBUG)Logsucceed");
            } else {
                if (Util.DEBUG) Log.i(Util.TAG, TAG+" CopyApkFromAssets----exist");
            }
            String permission = "666";

            try {
                String command = "chmod " + permission + " " + toPath + "/files/" + apkName;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            if (Util.DEBUG) Log.e(Util.TAG, TAG + "  CopyApkFromAssets---"+e.toString());
        }

    }

    /**
     * 
     * @param apkPath
     * @param apkName
     */
    private static void install(String apkPath, String apkName, boolean delete) {
        if (Util.DEBUG) Log.i(Util.TAG, TAG+" SecretlyInstallReceiver ----install,apkPath " + apkPath);
        File file = new File(apkPath);
        if (Util.DEBUG) Log.i(Util.TAG, TAG+" SecretlyInstallReceiver ----install,file.exists() " + file.exists());
        if (!file.exists())
            return;
        Uri mPackageURI = Uri.fromFile(file);
        int installFlags = 0;
        PackageManager pm = FolderApplication.mContext.getApplicationContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (Util.DEBUG) Log.i(Util.TAG, TAG+" SecretlyInstallReceiver ----install,info " + info);
        if (info != null) {
            try {
                PackageInfo pi = pm.getPackageInfo(info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                if (pi != null) {
                    // modify by even
                    // start
                    // just for compile.
                    // end
                    installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
                }
            } catch (NameNotFoundException e) {
                if (Util.DEBUG) Log.i(Util.TAG, TAG+" SecretlyInstallReceiver ----install, e " + e);
            }

            IPackageInstallObserver observer = new PackageInstallObserver(file, delete);
            PackageManager pManager = FolderApplication.mContext.getPackageManager();
            // modify by even
            // start
            // just for compile.
            // end
            installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
            pManager.installPackage(mPackageURI, observer, installFlags, info.packageName);
        }else{
            //解析包出错
            Log.e("eee", "解析包出错，发送广播" + apkName);
            Intent intent = new Intent(ACTION_SECRETLY_INSTALL_FAIL);
            intent.putExtra("packageName", apkName);
            FolderApplication.mContext.sendBroadcast(intent);
        }
    }

    /**
     * package install listener delete the file when it is installed
     * 
     * @author wanghao
     */
    private static class PackageInstallObserver extends MyPackageInstallObserver {
        File file;
        boolean isDeleteFile = true;

        // end
        public PackageInstallObserver(File f, boolean delete) {
            file = f;
            isDeleteFile = delete;
        }

        @Override
        public void packageInstalled(String packageName, int arg1) throws RemoteException {
            if (isDeleteFile) {
                Util.deleteFile(file);
            }
            if (Util.DEBUG) Log.i(Util.TAG, TAG+" packageInstalled ----packageInstalled:" + packageName);
            // added by yzs 20141113
            boolean checkAppExists = false;
            boolean installApplication = false;
            if (!Util.checkAppExists(mContext, packageName)) {
                checkAppExists = true;
            }
            
            if (Util.isInstallApplication(mContext, packageName)) {
                mSet.add(packageName);
                installApplication = true;
            }
            
            if (!checkAppExists && !installApplication) {
                mSet.remove(packageName);
                Intent intent = new Intent(ACTION_SECRETLY_INSTALL_FAIL);
                intent.putExtra("packageName", packageName);
                mContext.sendBroadcast(intent);
            }else{
                Intent intent = new Intent(ACTION_SECRETLY_INSTALL_SUCCESS);
                intent.putExtra("packageName", packageName);
                mContext.sendBroadcast(intent);
            }
            saveHashSet(mSet);
            // end
        }
    }

    // added by yzs for save ArrayList
    private static boolean saveHashSet(HashSet set) {
        SharedPreferences sp = mContext.getSharedPreferences("APPINFO", Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("ApkInfo", set);
        return editor.commit();
    }
    // end
}
