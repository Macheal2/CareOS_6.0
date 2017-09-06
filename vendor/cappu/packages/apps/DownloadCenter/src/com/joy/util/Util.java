package com.joy.util;

import static android.os.Environment.MEDIA_MOUNTED;

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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;

import com.joy.network.RecommendShortcutInfo;
import com.joy.network.impl.Service;

/**
 * ������
 * �ṩ�������ɣ����ֺ��ַ�������أ�mod5�����ַ������׷�Ӻ��滻����ͼƬ��������ɡ����š��
 * ü�����Щ���෽����
 * 
 * @author hao.wang
 * 
 */
public class Util {

    private static String deviceid;
    private static int sBitmapTextureWidth = 48;
    private static int sBitmapTextureHeight = 48;
    private static Random mRandom = new Random();

    /**
     * �����
     * 
     * @return
     */
    public static String getTS() {
        String str = String.valueOf(mRandom.nextInt(9999));
        return str;
    }

    /**
     * ��ȡ����ַ�
     */

    public static final String randomString(int length) {
        if (length < 1) {
            return null;
        }
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[mRandom.nextInt(71)];
        }
        return new String(randBuffer);
    }

    /**
     * ���ַ����Ϊmd5��ʽ
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
     * ����String�ַ�
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
     * �ַ�ת��
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
     * �ַ�ת��
     * 
     * @param content
     * @return
     */
    public static String decodeContentFromUrl(String content) {

        try {
            return (content == null ? "" : URLDecoder.decode(URLDecoder.decode(content, "UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * �ַ�ת��
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
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }

    /**
     * ���url�����ļ���
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
     * ����ת�����ֽ�����
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
    public static Bitmap getBitmapById(int id) {
        return BitmapFactory.decodeResource(Service.mContext.getResources(), id);
    }

    /**
     * bitmap to drawable
     * 
     * @author yongjian.he
     */
    public static Drawable bitmapToDrawable(Bitmap bp) {
        Bitmap bitmap = bp;
        if (bitmap != null) {
            return new BitmapDrawable(Service.mContext.getResources(), bitmap);
        } else {
            Logger.info("Util", "---bitmapToDrawable " + "error: the src bitmap is null!");
            bitmap = Bitmap.createBitmap(sBitmapTextureWidth, sBitmapTextureHeight, Bitmap.Config.ARGB_8888);
            return new BitmapDrawable(Service.mContext.getResources(), bitmap);
        }
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
            Logger.info("Util", "---drawableToBitmap " + "error: the src drawable is null!");
            return null;
        }
    }
    
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }
    
    /**
     * zoom drawable
     * 
     * @author yongjian.he
     */
    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap oldbmp = drawableToBitmap(drawable);
            Matrix matrix = new Matrix();
            float sx = ((float) w / width);
            float sy = ((float) h / height);
            matrix.postScale(sx, sy);
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
            return new BitmapDrawable(Service.mContext.getResources(), newbmp);
        } else {
            Logger.info("Util", "---zoomDrawable " + "error: the src drawable is null!");
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
     * �ж��Ƿ�����������
     * 
     * @return
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) Service.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * �ж�SD���Ƿ����
     * 
     * @return
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(status)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ���ļ��ŵ�SD����
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
     * ���ַ���utf8���浽sd��
     * 
     * @param filename
     *            ·��+�ļ���
     * @param content
     *            ����
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
     * ��ȡ�����ı� utf8
     * 
     * @param filename
     *            ·�� +�ļ���
     * @return
     */
    public static String readString(String filename) {
        if (!hasSdcard()) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
     * ����ļ��Ƿ������� �������������ֺ������������� eg��a.txt --> a(1).txt -->
     * a(2).txt
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

    /**
     * ��װapk
     * 
     * @param apkPath
     * @param apkName
     * @param isSecretly
     *            �Ƿ�Ĭ��װ
     */
    // public static void installAPK(String apkPath, String apkName, boolean
    // isSecretly) {
    // File file = new File(apkPath, apkName);
    // Logger.info("Util", "file.getName(): " + file.getName());
    // Logger.info("Util", "apkPath: " + apkPath);
    // Logger.info("Util", "apkName: " + apkName);
    //
    // if (!fileIsExist(file.getAbsolutePath())) {
    // return;
    // }
    //
    // if (!isSecretly) {
    // Intent intent = new Intent();
    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // intent.setAction(android.content.Intent.ACTION_VIEW);
    // intent.setDataAndType(Uri.fromFile(file),
    // "application/vnd.android.package-archive");
    // Service.mContext.startActivity(intent);
    // } else {
    // // Intent intent = new
    // // Intent(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL);
    // // intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_NAME,
    // // apkName);
    // // intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_PATCH,
    // // apkPath);
    // // Service.mContext.sendBroadcast(intent);
    // }
    // }

    /**
     * �Ƿ��Ѿ���װAPPӦ�ó���
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
     * ɾ���ļ�
     * 
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }
    }

    /**
     * get string from assert
     * 
     * @param fileName
     * @return
     */
    public static String getStringFromAssets(String fileName) {
        String result = "";
        try {
            InputStream in = Service.mContext.getResources().getAssets().open(fileName);
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
     * ����ļ�����sd file
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
     * ���ļ�����copy����һ�ļ�
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
     * ��Assets�ж�ȡͼƬ eg:getImageFromAssetsFile("img/cat_blink0000.png");
     */
    public static Bitmap getBitmapFromAssets(String fileName) {
        if (fileName == null) {
            return null;
        }
        Bitmap image = null;
        try {
            InputStream in = Service.mContext.getResources().getAssets().open(fileName);
            image = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * �ж��ļ��Ƿ����
     * 
     * @param fileAbsolutePath
     *            ���·��
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
     * �Ƚ�ʱ��
     * 
     * @param s1
     * @param s2
     * @return ��������
     * @throws Exception
     */
    public static long dateCompare(String s1, String s2) {
        long differ = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date d1 = sdf.parse(s1);
            Date d2 = sdf.parse(s2);

            differ = (int) Math.abs(((d1.getTime() - d2.getTime()) / (24 * 3600 * 1000)));
        } catch (Exception e) {
        }
        return differ;
    }

    /**
     * ��ǰʱ��
     * 
     * @return
     */
    public static String getCurrentDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sDateFormat.format(new java.util.Date());
        return time;
    }

    /**
     * �Ƿ�ΪϵͳӦ�ó���
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isSystemApplication(Context context, String packageName) {
        boolean ret = false;
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (pkg != null) {
                if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                        || (pkg.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                } else {
                    ret = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * ��ȡ�汾��(�ڲ�ʶ���)
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

}
