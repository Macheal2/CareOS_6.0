package com.cappu.cleaner;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.cappu.cleaner.context.fileCleanInfo;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hmq on 17-1-6.
 */

public class Util {
    public static final String TAG = "hmq";//"cleaner";
    public static final boolean DEBUG =true;
    private static ArrayList<File>  mFileList = new ArrayList<File>();
    public static Drawable mWechatIcon;

    public final static String ACTION_INSTALL_SHORTCUT = "com.cappu.cleaner.action.INSTALL_SHORTCUT";

    /*********************************file****************************************************/
    public static void initWechatIcon(Context context) {
        //获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : packageInfos) {
            //拿到包名
            String packageName = info.packageName;
            //拿到应用程序的信息
            ApplicationInfo appInfo = info.applicationInfo;
            //拿到应用程序的图标
            mWechatIcon = appInfo.loadIcon(pm);
            String appName = appInfo.loadLabel(pm).toString();
            if (packageName.equals("com.tencent.mm")){
                mWechatIcon = appInfo.loadIcon(pm);
                Log.e("hmq","initWechatIcon packageName="+packageName+"; appName="+appName);
            }
        }

    }

    /** Calculates the constrast between two colors, using the algorithm provided by the WCAG v2. */
    public static float computeContrastBetweenColors(int bg, int fg) {
        float bgR = Color.red(bg) / 255f;
        float bgG = Color.green(bg) / 255f;
        float bgB = Color.blue(bg) / 255f;
        bgR = (bgR < 0.03928f) ? bgR / 12.92f : (float) Math.pow((bgR + 0.055f) / 1.055f, 2.4f);
        bgG = (bgG < 0.03928f) ? bgG / 12.92f : (float) Math.pow((bgG + 0.055f) / 1.055f, 2.4f);
        bgB = (bgB < 0.03928f) ? bgB / 12.92f : (float) Math.pow((bgB + 0.055f) / 1.055f, 2.4f);
        float bgL = 0.2126f * bgR + 0.7152f * bgG + 0.0722f * bgB;

        float fgR = Color.red(fg) / 255f;
        float fgG = Color.green(fg) / 255f;
        float fgB = Color.blue(fg) / 255f;
        fgR = (fgR < 0.03928f) ? fgR / 12.92f : (float) Math.pow((fgR + 0.055f) / 1.055f, 2.4f);
        fgG = (fgG < 0.03928f) ? fgG / 12.92f : (float) Math.pow((fgG + 0.055f) / 1.055f, 2.4f);
        fgB = (fgB < 0.03928f) ? fgB / 12.92f : (float) Math.pow((fgB + 0.055f) / 1.055f, 2.4f);
        float fgL = 0.2126f * fgR + 0.7152f * fgG + 0.0722f * fgB;

        return Math.abs((fgL + 0.05f) / (bgL + 0.05f));
    }

    /**
     * Loads an integer array asset into a list.
     */
    public static ArrayList<Integer> loadIntegerArray(Resources r, int resNum) {
        int[] vals = r.getIntArray(resNum);
        int size = vals.length;
        ArrayList<Integer> list = new ArrayList<Integer>(size);

        for (int i = 0; i < size; i++) {
            list.add(vals[i]);
        }

        return list;
    }

    /*********************************calculate************************************************/
    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }


    /*********************************file****************************************************/
    public static final int SIZETYPE_B = 1;// 获取文件大小单位为B的double值
    public static final int SIZETYPE_KB = 2;// 获取文件大小单位为KB的double值
    public static final int SIZETYPE_MB = 3;// 获取文件大小单位为MB的double值
    public static final int SIZETYPE_GB = 4;// 获取文件大小单位为GB的double值


    private File hasSdCare(Context mContext) {
        File path = null;
        //检测SD卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory();
        } else {
            Toast.makeText(mContext, "没有SD卡", Toast.LENGTH_LONG).show();
        }
        return path;
    }
    /**
     *
     * @return 获得根目录/data 内部存储路径
     */
    public static File getDataFile(){
        return Environment.getDataDirectory();
    }

    /**
     *
     * @return 获得缓存目录/cache
     */
    public static File getCacheFile(){
        return android.os.Environment.getDownloadCacheDirectory();
    }

    /**
     *
     * @return 获得SD卡目录/mnt/sdcard（获取的是手机外置sd卡的路径）
     */
    public static File getExternalFile(){
        return Environment.getExternalStorageDirectory();
    }

    /**
     *
     * @return 获得系统目录/system
     */
    public static File getRootDirFile(){
        return Environment.getRootDirectory();
    }

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath
     *            文件路径
     * @param sizeType
     *            获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return FormatFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath
     *            文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return FormatFileSize(blockSize);
    }

    /**
     * 调用此方法计算指定文件夹的大小
     *
     * @param filePath
     *            文件路径
     * @return 返回B单位的文件大小
     */
    public static long getFilesSizeForLong(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return blockSize;
    }
    /**
     * 获取指定文件大小
     *
     * @param
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }
    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }
    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    public static double FormatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    /**
     * Formats a content size to be in the form of bytes, kilobytes, megabytes, etc.
     *
     * If the context has a right-to-left locale, the returned string is wrapped in bidi formatting
     * characters to make sure it's displayed correctly if inserted inside a right-to-left string.
     * (This is useful in cases where the unit strings, like "MB", are left-to-right, but the
     * locale is right-to-left.)
     *
     * @param context Context to use to load the localized units
     * @param sizeBytes size value to be formatted, in bytes
     * @return formatted string with the number
     */
    public static String FormatFileSize(Context context, long sizeBytes){
        return Formatter.formatFileSize(context, sizeBytes);
    }

    /**
     * 或得文件信息
     * @param type    3.微信 2.无效文件
     * @param name    数组
     * @param apkPath 文件夹路径
     * @return 文件信息
     */
    public static fileCleanInfo getFileInfo(Context context, int type, int name, String apkPath) {
        long fileSizeTypeB = 0;
        fileCleanInfo file_info = new fileCleanInfo();
        Drawable icon = null;

        file_info.setFileSavePath(apkPath);
        fileSizeTypeB = Util.getFilesSizeForLong(apkPath);
        file_info.setFilesize(fileSizeTypeB);//得到文件大小

        if (type == FileClean.RESULT_WECHAT) {
            file_info.setLabel(NameList.WechatNameList[name]);
            icon = Util.mWechatIcon;
        } else if (type == FileClean.RESULT_AD) {
            file_info.setLabel(NameList.ADNameList[name]);
            icon = context.getResources().getDrawable(R.drawable.ic_useless);
        }
        file_info.setType(type);
        file_info.setIcon(icon);
        return file_info;
    }

    /**
     * 获得APK信息
     * @param context
     * @param apkPath
     * @return
     */
    public static fileCleanInfo getPackageInfo(Context context, String apkPath) {
        long fileSizeTypeB = 0;
        fileCleanInfo file_info = new fileCleanInfo();
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

        if (info != null) {
            file_info.setFileSavePath(apkPath);
            fileSizeTypeB = Util.getFilesSizeForLong(apkPath);
            file_info.setFilesize(fileSizeTypeB);//得到文件大小
            ApplicationInfo appInfo = info.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            file_info.setLabel(appInfo.loadLabel(pm).toString());//(pm.getApplicationLabel(appInfo).toString());
            file_info.setPackageName(appInfo.packageName);//得到安装包名称
            String version = info.versionName;//String.valueOf(info.versionCode);       //得到版本信息
            file_info.setVersion(version);
            file_info.setType(FileClean.RESULT_WECHAT);
            Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
            file_info.setIcon(icon);

            Log.e("hmq", "version=" + version + "; versioncode=" + info.versionCode + "   file_info:" + file_info.toString());
        }
        return file_info;
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            }else if (file.isDirectory()) {
                deleteDirWihtFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }
    /********************************* log **************************************************/
    public static void MyLog(String level, String msg) {
        if (level.equals("d") || level.equals("D")) {
            Log.d(TAG, msg);
        } else if (level.equals("i") || level.equals("I")) {
            Log.i(TAG, msg);
        } else if (level.equals("w") || level.equals("W")) {
            Log.w(TAG, msg);
        } else if (level.equals("v") || level.equals("V")) {
            Log.v(TAG, msg);
        } else if (level.equals("e") || level.equals("E")) {
            Log.e(TAG, msg);
        }
    }
}
