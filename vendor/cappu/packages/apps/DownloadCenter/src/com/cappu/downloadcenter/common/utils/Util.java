package com.cappu.downloadcenter.common.utils;

import static android.os.Environment.MEDIA_MOUNTED;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cappu.downloadcenter.common.entity.DownloadCenterTitle;
import com.joy.network.RecommendShortcutInfo;
import com.joy.util.Logger;
import com.cappu.downloadcenter.R;
import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.entity.AppInfo;
import com.cappu.downloadcenter.context.FolderApplication;
import com.cappu.downloadcenter.install.SecretlyInstallReceiver;

/**
 * 工具类 提供随机数生成（数字和字符），加密相关（mod5），字符串操作（追加和替换），图片操作（生成、缩放、裁剪）这些分类方法。
 * 
 * @author hao.wang
 * 
 */
public class Util {

    public static final String TAG = "DownloadCentent";
    public static final boolean DEBUG = false;
    public static final boolean ERROR_DEBUG = true;
    private static Random mRandom = new Random();

    public final static String APP_TITLE_ARRAY = "app_title_array";
    public final static String APP_LIST_ARRAY = "shorcutList";

    /**
     * 随机数
     * 
     * @return
     */
    public static String getTS() {
        String str = String.valueOf(mRandom.nextInt(9999));
        return str;
    }

    /**
     * 获取随机字符串
     */

    public static final String randomString(int length) {
        if (length < 1) {
            return null;
        }
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[mRandom.nextInt(71)];
            // randBuffer[i] = numbersAndLetters[randGen.nextInt(35)];
        }
        return new String(randBuffer);
    }

    /**
     * 将字符串编码为md5格式
     * 
     * @param value
     * @return
     */
    public static String md5Encode(String value) {
        String tmp = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(value.getBytes("utf8"));
            byte[] md = md5.digest();
            tmp = binToHex(md);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public static String binToHex(byte[] md) {
        StringBuffer sb = new StringBuffer("");
        int read = 0;
        for (int i = 0; i < md.length; i++) {
            read = md[i];
            if (read < 0)
                read += 256;
            if (read < 16)
                sb.append("0");
            sb.append(Integer.toHexString(read));
        }
        return sb.toString();
    }

    /**
     * 构造String字符串
     * 
     * @param args
     * @return
     */
    public static String buildString(String... args) {
        StringBuffer buffer = new StringBuffer();
        for (String arg : args) {
            buffer.append(arg);
        }
        return buffer.toString();
    }

    /**
     * 字符串转换
     * 
     * @param content
     * @return
     */
    public static String encodeContentForUrl(String content) {

        try {
            return (content == null ? "" : URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 字符串转换
     * 
     * @param content
     * @return
     */
    public static String encodeDevice(String value) {
        StringBuilder sb = new StringBuilder(32);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(value.getBytes("utf-8"));

            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase(Locale.CHINA).substring(1, 3));
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
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

    /**
     * 将流转换成字节数组
     * 
     * @param is
     * @return
     */
    public static byte[] getBytes(InputStream is) {

        ByteArrayOutputStream bab = new ByteArrayOutputStream();
        byte[] datas = new byte[8192];
        int count = -1;

        try {
            while ((count = is.read(datas, 0, datas.length)) != -1) {
                bab.write(datas, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bab.toByteArray();
    }

    /**
     * generate bitmap rely on ID
     * 
     * @author yongjian.he
     * @param id
     * @return
     */
    public static Bitmap getBitmapById(Context context, int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    /**
     * bitmap to drawable
     * 
     * @author yongjian.he
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable != null) {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } else {
            if (DEBUG) Log.e(TAG, "ERROR ---drawableToBitmap  --error: the src drawable is null!");
            return null;
        }
    }

    /**
     * zoom drawable
     * 
     * @author yongjian.he
     */
    public static Drawable zoomDrawable(Context context, Drawable drawable, int w, int h) {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap oldbmp = drawableToBitmap(drawable);
            Matrix matrix = new Matrix();
            float sx = ((float) w / width);
            float sy = ((float) h / height);
            matrix.postScale(sx, sy);
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
            return new BitmapDrawable(context.getResources(), newbmp);
        } else {
            if (DEBUG) Log.e(TAG, "Error ---zoomDrawable " + "error: the src drawable is null!");
            return null;
        }
    }

    /**
     * generate RoundedCornerBitmap
     * 
     * @author yongjian.he
     * @param roundPx
     *            : corner radius.
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * 图片的遮罩
     * 
     * @param context
     * @param bitmap
     * @return
     */
    public static Bitmap ImageIconFermodeRotateLoading(Context context, Bitmap bitmap, boolean isPause, double percent) {
        if (bitmap == null) {
            return null;
        }
        Bitmap markbg = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_status)).getBitmap();
//        Bitmap downloading = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_status_downloading)).getBitmap();
        Bitmap pause = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_status_pause)).getBitmap();
//        Bitmap pause_dstout = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_status_pause_dstout1)).getBitmap();
        
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Paint paint = new Paint();
        paint.setAntiAlias(true);// 抗锯齿
        paint.setDither(true);// 防抖动
        paint.setFilterBitmap(true);// 滤波
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x80000000);
        
        Bitmap returnbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);//复制一个可编辑的bitmap
        Canvas canvas = new Canvas(returnbitmap);//创建在bitmap上的画布
        float mark_offsetX = (width - markbg.getWidth()) / 2;
        float mark_offsetY = (height - markbg.getHeight()) / 2;
        canvas.drawBitmap(markbg, mark_offsetX, mark_offsetY, paint);//最外圈遮罩覆盖
        
        //绘制下载百分比
        float percent_diameter = (float) (width * 0.66);
        float sweepAngle = (float) percent * 360;
        float download_offsetX = (float) (width - percent_diameter) / 2;
        float download_offsetY = (float) (height - percent_diameter) / 2;
        RectF rect = new RectF(download_offsetX, download_offsetY, width - download_offsetX, height - download_offsetY);
        canvas.drawArc(rect, -90f, sweepAngle - 360, true, paint);//-360逆时针旋转
        
        if (isPause) {//绘制暂停
//            Bitmap circle = Bitmap.createBitmap(pause_dstout.getWidth(), pause_dstout.getHeight(), Bitmap.Config.ARGB_8888);
            float pause_diameter = (float) (percent_diameter * 0.8);
            float pause_offsetX = (float) (width - pause_diameter) / 2;
            float pause_offsetY = (float) (height - pause_diameter) / 2;
            
            rect = new RectF(pause_offsetX, pause_offsetY, width - pause_offsetX, height - pause_offsetY);
            canvas.drawArc(rect, -90f, sweepAngle, true, paint);//顺时针
            int x = (width - pause.getWidth()) / 2;
            int y = (height - pause.getHeight()) / 2;
            canvas.drawBitmap(pause, x, y, paint);
        }
        return returnbitmap;
    }

    /**
     * 图片的遮罩
     * 
     * @param context
     * @param bitmap
     * @return
     */
    public static Bitmap ImageIconFermodeUndownload(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap undownload = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_status_undownload)).getBitmap();

        Paint paint = new Paint();
        paint.setAntiAlias(true);// 抗锯齿
        paint.setDither(true);// 防抖动
        paint.setFilterBitmap(true);// 滤波

        Bitmap returnbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(returnbitmap);
        canvas.drawBitmap(undownload, 0f, 0f, null);
        return returnbitmap;
    }
    
    /**
     * 判断是否有网络连接
     * 
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static int getConnectedType(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }
        return -1;
    }
    
    /**
     * 判断网络是否畅通
     * 
     */
    public static boolean isNetworkReachable(String str) {
        Process p;
        String url = "www.baidu.com";
        if (str != null)
            url = str;
        try {
            // ping 3次,超时间隔为100毫秒.
            p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + url);
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }

            if (status == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static int SIZE_B = 0;
    public final static int SIZE_KB = 1;
    public final static int SIZE_MB = 2;
    
    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * 
     * @param filePath
     *            文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static long getAutoFileOrFilesSize(String filePath, int filesStype) {
        File file = new File(filePath);
        long blockSize = 0;
        if (!fileIsExist(file.getAbsolutePath())){
            return blockSize;
        }
        
        try {
            blockSize = getFileSize(file);
            if (filesStype == SIZE_KB){
                blockSize = blockSize / 1024;
            }
            if (filesStype == SIZE_MB){
                blockSize = blockSize / 1048576;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(ERROR_DEBUG)Log.e(TAG, "获取失败!");
        }
        return blockSize;
    }

    /**
     * 获取指定文件大小
     * 
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = -1;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        }
        return size;
    }
    
    /**
     * 转换文件大小
     * 
     * @param fileS
     * @return
     */
    public static String formetFileSizeMB(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "K";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "M";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "G";
        }
        return fileSizeString;
    }
     
    /**
     * 判断SD卡剩余内存是否够用。
     */
    public static boolean isSdcardMemonyAvailable(int size) {
        if (!hasSdcard()) {
            return false;
        }
        File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize > size * 1024; // 统一成byte
    }

    /**
     * 判断data目录剩余空间是否够用
     * 
     * @return
     */
    public static boolean isRomSpaceAvailable(int size) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize > size * 1024;
    }

    /**
     * 删除一些应用来腾出data下的空间
     * 
     * @param context
     * @param size
     * @return
     * @throws Exception
     */
    // public static boolean makeDataRoom(Context context, int size) throws
    // Exception {
    // if (isMainThread()) {
    // Log.w(TAG,
    // "Warning: Must run in the child thread becuse uninstall the application needs to have a waiting time");
    // throw new Exception("Warning: Must run in the child thread...");
    // }
    // ArrayList<String> userAppList = Util.userAppList(context);
    // do {
    // if (userAppList.size() == 0)
    // return false;
    // Util.uninstall(userAppList.get(0), new IPackageDeleteObserver() {
    //
    // @Override
    // public IBinder asBinder() {
    // return null;
    // }
    //
    // @Override
    // public void packageDeleted(String arg0, int arg1) throws RemoteException
    // {
    //
    // }
    // });
    // try {
    // Thread.sleep(20000);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // } while (isRomSpaceAvailable(size));
    // return true;
    // }

    /**
     * 判断SD卡是否存在
     * 
     * @return
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(status)) {
            return true;
        } else {
            if (DEBUG) Log.e(TAG, "sdcard not found...");
            return false;
        }
    }

    /**
     * 把文件放到SD卡中,如果存在则删除
     * 
     * @param bm
     */
    public static boolean saveFileToSD(InputStream is, String path) {
        if (!hasSdcard()) {
            return false;
        }
        if (is == null) {
            return false;
        }
        FileOutputStream fos = null;
        boolean success = false;
        try {
            File file = new File(path).getParentFile();
            if (!file.exists()) {
                file.mkdirs();
            }
            fos = new FileOutputStream(path);
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            success = true;
        } catch (Exception e) {
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return success;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * 
     * @param sPath
     *            被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } // 删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     * 
     * @param sPath
     *            被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 将字符串以utf8保存到sd卡 hao.wang
     * 
     * @param filename
     *            路径+文件名
     * @param content
     *            内容
     * @throws Exception
     */
    public static void saveString(String filename, String content) {
        if (!hasSdcard()) {
            return;
        }
        FileOutputStream fos = null;
        OutputStreamWriter writer = null;
        try {
            File file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            writer = new OutputStreamWriter(fos, "utf-8");
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (writer != null) {
                    writer.close();
                }

            } catch (Exception e1) {
            }
        }
    }

    /**
     * 读取本地文本 utf8 hao.wang
     * 
     * @param filename
     *            路径+文件名
     * @return
     */
    public static String readString(String filename) {
        if (!hasSdcard()) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                // FileContent += "\r\n"; // 补上换行符
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    /**
     * get current API Versions
     * 
     * @return
     */
    public static int getCurrentApiVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 检测文件是否有重名 若重名则在名字后面加数字来区别 eg：a.txt --> a(1).txt --> a(2).txt
     * 
     * @param file
     * @return
     */
    public static File getCleverFileName(File file) {
        if (file == null) {
            return null;
        }
        String fileName = file.getName();
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            return file;
        } else {
            int index = fileName.lastIndexOf(".");
            if (index == -1) {
                index = fileName.length();
            }
            boolean end = false;
            String numStr = "";
            String name = file.getName().substring(0, index);
            for (int i = name.length(); i > 0; i--) {
                String c = name.substring(i - 1, i);
                if ("(".equals(c)) {
                    break;
                }
                if (end) {
                    numStr += c;
                }
                if (")".equals(c)) {
                    end = true;
                }
            }
            if (end) {
                int x = 1;
                try {
                    x = Integer.parseInt(numStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int y = name.length() - ("(" + x + ")").length();
                name = name.substring(0, y);
                fileName = name + "(" + (x + 1) + ")" + file.getName().substring(index);
            } else {
                fileName = name + "(1)" + file.getName().substring(index);
            }

            file = new File(file.getParentFile() + "/" + fileName);
            if (file.exists()) {
                return getCleverFileName(file);
            } else {
                return file;
            }
        }
    }

    // /**
    // * @param apkPath
    // * @param apkName
    // * @param isSecretly是否静默安装
    // */
    // public static void installAPK(String apkPath, boolean isSecretly) {
    //
    // // TODO 静默安装的开关
    // if (Constants.TURN_OFF_SECRETLY)
    // isSecretly = false;
    // File file = new File(apkPath, info.getLocalname());
    // if (Constants.DEBUG)
    // Log.i("OpenFile",
    // "file.getName(): " + file.getName() + " apkPath: " + apkPath +
    // " apkName: " + info.getLocalname());
    // if (!fileIsExist(file.getAbsolutePath())) {
    // return;
    // }
    // // 手动安装
    // if (!isSecretly || (SystemInfo.installType == PushUtils.USER_APP)) {
    // Intent intent = new Intent();
    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // intent.setAction(android.content.Intent.ACTION_VIEW);
    // intent.setDataAndType(Uri.fromFile(file),
    // "application/vnd.android.package-archive");
    // PushApplication.mContext.startActivity(intent);
    // } else {// 静默安装
    // Intent intent = new
    // Intent(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL);
    // intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_NAME,
    // info.getLocalname());
    // intent.putExtra(PushUtils.PUSH_DETAIL_TYPE, info.getDownloadType());
    // intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_PATCH, apkPath);//
    // 从assets中安装
    // PushApplication.mContext.sendBroadcast(intent);
    // }
    // }

    /**
     * 是否已经安装APP应用程序
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstallApplication(Context context, String packageName) {
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return pkg != null;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 删除文件 add by wanghao
     * 
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                // File files[] = file.listFiles();
                // for (int i = 0; i < files.length; i++) {
                // deleteFile(files[i]);
                // }
                deleteDirectory(file.getPath());
            }
            // file.delete();
        }
    }

    // get string from assert
    public static String getStringFromAssets(Context context, String fileName) {
        String result = "";
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            int lenght = in.available();
            byte[] buffer = new byte[lenght];
            in.read(buffer);
            result = EncodingUtils.getString(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据文件名获得sd file
     * 
     * @param fileName
     * @return
     */
    public static File getSdBackupFile(String fileName) {
        File sdBackupFile = null;
        if (Environment.getExternalStorageState().equals(MEDIA_MOUNTED)) {
            sdBackupFile = new File(Environment.getExternalStorageDirectory(), fileName);
            if (!sdBackupFile.exists()) {
                try {
                    if (!sdBackupFile.createNewFile()) {
                        sdBackupFile = null;
                    }
                } catch (IOException e) {
                    sdBackupFile = null;
                    e.printStackTrace();
                }
            }
        }
        return sdBackupFile;
    }

    /**
     * 将文件内容copy到另一文件
     * 
     * @param formFile
     * @param toFile
     * @return
     */
    public static boolean copyFile(File formFile, File toFile) {
        boolean success = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(formFile);
            fos = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fis.read(bt)) > 0) {
                fos.write(bt, 0, c);
            }
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    /**
     * 从Assets中读取图片 eg:getImageFromAssetsFile("img/cat_blink0000.png");
     */
    public static Bitmap getBitmapFromAssets(Context context, String fileName) {
        if (fileName == null) {
            return null;
        }
        Bitmap image = null;
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            image = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 判断文件是否存在
     * 
     * @param fileAbsolutePath
     *            绝对路径
     * @return
     */
    public static boolean fileIsExist(String fileAbsolutePath) {
        String fileName = fileAbsolutePath;
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 比较时间
     * 
     * @param s1
     * @param s2
     * @return 相差多少天
     * @throws Exception
     */
    public static long dateCompare(String s1, String s2) {
        long differ = 0;
        try {
            // 设定时间的模板
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            // 得到指定模范的时间
            Date d1 = sdf.parse(s1);
            Date d2 = sdf.parse(s2);
            // 比较
            differ = (int) Math.abs(((d1.getTime() - d2.getTime()) / (24 * 3600 * 1000)));
        } catch (Exception e) {
        }
        return differ;
    }

    /**
     * 当前时间
     * 
     * @return
     */
    public static String getCurrentDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String time = sDateFormat.format(new java.util.Date());
        return time;
    }

    /**
     * 获取版本号(内部识别号)
     * 
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取版本名(内部识别号)
     * 
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            // if (SystemInfo.isDexLoader) {
            // try {
            // DexLoader loader = DexLoader.getInstance(context);
            // Class<?> mLoadClass =
            // loader.loadClass("com.update.loaddex.DexLoader");
            //
            // // 获取需要调用的方法
            // Method setActivity = mLoadClass.getMethod("getVersion", new
            // Class[] {});
            // setActivity.setAccessible(true);
            // String versionName = setActivity.invoke(mLoadClass, new Object[]
            // {}).toString();
            // if (Constants.DEBUG)
            // Log.e(TAG, "getVersion：" + versionName);
            // return versionName;
            // } catch (Exception e) {
            // e.printStackTrace();
            // if (Constants.DEBUG)
            // Log.e(TAG, "getVersion error..");
            // }
            // }
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 保存图片文件到SD卡中
     * 
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public static String saveFile(Bitmap bm, String fileName) throws IOException {
        String sdFile = Constants.ICON_PATH.substring(0, Constants.ICON_PATH.length() - 1);
        File dirFile = new File(sdFile);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(sdFile + File.separator + fileName);
        if (DEBUG) Log.e(TAG, "savefile: " + sdFile + File.separator + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
        return sdFile + File.separator + fileName;
    }

    /**
     * 是否是系统软件或者是系统软件的更新软件
     * 
     * @return
     */
    public static boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static boolean isSystemUpdateApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static boolean isUserApp(PackageInfo pInfo) {
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }

    /**
     * 检查app是否是系统rom集成的
     * 
     * @param pname
     * @return
     */
    public static int checkAppType(Context context, String pname) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(pname, 0);
            // 是系统软件或者是系统软件更新
            if (isSystemApp(pInfo) || isSystemUpdateApp(pInfo)) {
                return Constants.SYSTEM_REF_APP;
            } else {
                return Constants.USER_APP;
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return Constants.UNKNOW_APP;
    }

    /**
     * get user app
     * 
     * @param context
     * @return userApp list
     */
    public static ArrayList<String> userAppPackageList(Context context) {
        ArrayList<String> appList = new ArrayList<String>(); // 用来存储获取的应用信息数据
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (isUserApp(packageInfo)) {
                appList.add(packageInfo.packageName);// 如果非系统应用，则添加至appList
            }
        }
        return appList;
    }

    /**
     * get user app list
     * 
     * @param context
     * @return
     */
    public static HashMap<String, AppInfo> userAppList(Context context) {
        HashMap<String, AppInfo> appList = new HashMap<String, AppInfo>(); // 用来存储获取的应用信息数据
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (isUserApp(packageInfo)) {
                AppInfo info = new AppInfo();
                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                info.setAppName(appName == null ? "" : appName);
                info.setPackageName(packageInfo.packageName);
                info.setIcon(packageInfo.applicationInfo.loadIcon(pm));
                appList.put(packageInfo.packageName, info);// 如果非系统应用，则添加至appList
            }
        }
        return appList;
    }

    /**
     * 获取有可能被系统删除的但是还在应用程序列表中保留的appname.
     * 因为有些apk可能被删掉 但是数据还在 所以用GET_UNINSTALLED_PACKAGES 这个flag 
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean checkAppExists(Context context, String packageName) {
        if (packageName == null || "" == packageName)
            return false;
        try {
            @SuppressWarnings("unused")
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * open app by packageName
     * 
     * @param packageName
     * @param context
     * @throws NameNotFoundException
     */
    public static void openApp(String packageName, Context context) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(packageName, 0);
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null) {
            String packageName2 = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            Log.e("ml", "className: " + className);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // intent.setData(Uri.parse("com.mazi.privacies://mainactivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            ComponentName cn = new ComponentName(packageName2, className);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 得到图片字节流 数组大小
     * */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 压缩图片
     * 
     * @param data图片的字节文件
     * @param size图片长宽的最大值
     * @return图片
     */
    public static Bitmap compressImg(byte[] data, float size) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        // BitmapFactory.decodeStream(is, null, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        if (DEBUG) Log.e(TAG, "icon width: " + width + " height: " + height);
        int inSampleSize = Math.max(1, (int) Math.ceil(Math.max((double) width / size, (double) height / size)));
        if (DEBUG) Log.e(TAG, "icon inSampleSize: " + inSampleSize);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;
        // icon = BitmapFactory.decodeStream(is, null, opts);
        return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
    }

    /**
     * 卸载一个应用
     * 
     * @param packageName包名
     * @param observer卸载成功与否的回调对象
     */
    // public static void uninstall(String packageName, IPackageDeleteObserver
    // observer) {
    // PackageManager pManager = PushApplication.mContext.getPackageManager();
    // pManager.deletePackage(packageName, observer, 0);
    // }

    public static void installAPK(final Context mContext, final String apkPath, String apkName, boolean background) {
        final File file = new File(apkPath);
        Logger.info("Util", "file.getName(): " + file.getName());
        Logger.info("Util", "apkPath: " + apkPath);

        if (!fileIsExist(file.getAbsolutePath())) {
            Toast.makeText(mContext, "file not exist", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkAppType(mContext, mContext.getPackageName()) == Constants.USER_APP || !background) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        } else {// 静默安装
            Intent intent = new Intent(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL);
            intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_NAME, apkName);
            intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_PATCH, apkPath);// 从assets中安装
            FolderApplication.mContext.sendBroadcast(intent);
            // new Thread() {
            // @Override
            // public void run() {
            // try {
            // secretlyInstall(apkPath + "/" + apkName);
            // } catch (IOException e) {
            // e.printStackTrace();
            // Intent intent = new Intent();
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // intent.setAction(android.content.Intent.ACTION_VIEW);
            // intent.setDataAndType(Uri.fromFile(file),
            // "application/vnd.android.package-archive");
            // mContext.startActivity(intent);
            // }
            // };
            // }.start();
        }
    }

    public static void delectApk(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    /**
     * 执行linux命令
     * 
     * @param command
     */
    public static void execCommand(String... command) {
        Process process = null;
        try {
            process = new ProcessBuilder().command(command).start();
            // 对于命令的执行结果我们可以通过流来读取
            // InputStream in = process.getInputStream();
            // OutputStream out = process.getOutputStream();
            // InputStream err = process.getErrorStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null)
                process.destroy();
        }
    }

    /**
     * pm 方式静默安装
     * 
     * @param apkAbsolutePath
     * @return
     * @throws IOException
     */
    public static String secretlyInstall(String apkAbsolutePath) throws IOException {
        String[] args = { "pm", "install", "-r", apkAbsolutePath };
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            // baos.write('/n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                // process.destroy();
            }
        }
        return result;
    }

    /**
     * 判断是否是主线程
     * 
     * @return true:主线程 false:子线程
     */
    public static boolean isMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper())
            return true;
        return false;
    }
}
