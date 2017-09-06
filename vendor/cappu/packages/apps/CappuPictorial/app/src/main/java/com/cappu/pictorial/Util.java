/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.cappu.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@cappu.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial;

/**
 * Created by hmq on 16-12-1.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Util {
    private static String DATE_FORMAT_YMD_STR = "yyyy-MM-dd";
    public static final String MAIN_FOLDER_NAME = "CAPPU";
    public static final String SUB_FOLDER_NAME = "pictorial";
    public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + MAIN_FOLDER_NAME + "/" + SUB_FOLDER_NAME;
    public static final String CAPPU_SP = "cappu_shared_preferences";
    public static final String CAPPU_SP_TIME_KEY = "cappu_sp_key_time";//最后更新的时间
    public static final String CAPPU_SP_VERSION_KEY = "cappu_sp_key_version";//更新下载版本号
    public static final String CAPPU_SP_COUNT_KEY = "cappu_sp_key_count";//需要循环图片的总数
    public static final String CAPPU_SP_JSON_KEY = "cappu_sp_key_json";
    public static String CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL = "cappu_settings_pictorial";//画报墙纸开关
    public static final int CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_ON = 0;//画报墙纸开
    public static final int CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_OFF = 1;//画报墙纸关
    public static String CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL = "cappu_settings_pictorial_random";//画报墙纸随机开关
    public static final int CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_ON = 0;//画报墙纸随机开
    public static final int CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_OFF = 1;//画报墙纸随机关
    public static final String TAG = "CappuPictorialLog";
    private static final String LOG_MSG = "Util  ";
	private static final boolean LOG_DEBUG = false;

    /*NETWORK*/
    public static final int TIMEOUT = 15000;
    public final static int DOWNLOAD_APK_HTTP_OK = 206;

    private static Random mRandom = new Random();

    /**
     * 当前时间
     *
     * @return
     */
    public static long currentTimeLong() {
        return new Date().getTime();
    }

    /**
     * 是否同一天
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDate(long date1, long date2) {
        long days1 = date1 / (1000 * 60 * 60 * 24);
        long days2 = date2 / (1000 * 60 * 60 * 24);
        return days1 == days2;
    }

    /**
     *
     * @param hour 心跳间隔(小时)
     * @return 是否超高间隔时间 true:需要起心跳 false:不需要起心跳
     */
    public static boolean isHeartbeatTimeH(long startDate, int hour){
        if (startDate == 0) return false;
        long days1 = startDate + (hour * 1000 * 60 * 60);
        long days2 = currentTimeLong();
        return (days2 > days1) ? true : false;
    }

    /**
     * long 转 date
     *
     * @param millis
     * @return
     */
    public static String GetStringFromLong(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YMD_STR);
        java.util.Date dt = new Date(millis);
        return sdf.format(dt);
    }

    /**
     * 资源图片转成bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitamp(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    /**
     * 将文件生成位图
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static Bitmap getImageDrawable(String path) throws IOException {
        //打开文件
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] bt = new byte[10 * 1024 * 1024];

        //得到文件的输入流
        InputStream in = new FileInputStream(file);

        //将文件读出到输出流中
        int readLength = in.read(bt);
        while (readLength != -1) {
            outStream.write(bt, 0, readLength);
            readLength = in.read(bt);
        }

        //转换成byte 后 再格式化成位图
        byte[] data = outStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// 生成位图

        return bitmap;
    }

    /**
     * @param
     */
    public static boolean hasFile(String path) {
        // TODO Auto-generated method stub
        if (path == null)
            return false;

        File dirFirstFolder = new File(path);
        return hasFile(dirFirstFolder);
    }

    /**
     * @param
     */
    public static boolean hasFile(File file) {
        // TODO Auto-generated method stub
        if (file == null)
            return false;

        if (!file.exists()) { //如果该文件夹不存在
            return false;
        }
        return true;
    }

    public static void deleteFile(String path) {
        if (path == null)
            return;

        File file = new File(path);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (file == null)
            return;

        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
        } else {
            MyLog("e", LOG_MSG + " ERROR --deleteFile  文件不存在！");
        }
    }

    /*
     * searchFile 查找文件并加入到ArrayList 当中去
     * @String keyword 查找的关键词
     * @File filepath 查找的目录
     */
    public static String searchFile(String keyword, File filepath) {
        // 判断SD卡是否存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (filepath != null && !filepath.exists()) {
                return null;
            }

            File[] files = filepath.listFiles();
            if (files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果目录可读就执行（一定要加，不然会挂掉）
                        if (file.canRead()) {
                            return searchFile(keyword, file); // 如果是目录，递归查找
                        }
                    } else {
                        // 判断是文件，则进行文件名判断
                        try {
                            if (file.getName().indexOf(keyword) > -1 || file.getName().indexOf(keyword.toUpperCase()) > -1) {
                                return file.getAbsolutePath();
                            }
                        } catch (Exception e) {
                            MyLog("e", LOG_MSG + "ERROR searchFile Exception msg:" + e.getMessage());
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
 * searchFile 查找文件并加入到ArrayList 当中去
 * @String keyword 查找的关键词
 * @File filepath 查找的目录
 */
    public static void deleteFolderOtherFile(List<PictorialInfo> keyword, File filepath) {
        // 判断SD卡是否存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (filepath != null && !filepath.exists()) {
                return;
            }
            File[] files = filepath.listFiles();
            if (files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果目录可读就执行（一定要加，不然会挂掉）
                        if (file.canRead()) {
                            deleteFolderOtherFile(keyword, file); // 如果是目录，递归查找
                        }
                    } else {
                        // 判断是文件，则进行文件名判断
                        try {
                            int intexInt = -1;
                            for (int i = 0; i < keyword.size(); i++){
                                String key = keyword.get(i).getMd5();
                                intexInt = file.getName().indexOf(key);
                                if (intexInt != -1){
                                    break;
                                }
                            }
                            if (intexInt == -1) {
                                MyLog("i","找到文件夹中需要删除的多余文件："+file.getPath());
                                deleteFile(file);
                            }
                        } catch (Exception e) {
                            MyLog("e", LOG_MSG + "ERROR deleteFolderOtherFile Exception msg:" + e.getMessage());
                        }
                    }
                }
            }
        }
        return;
    }

    /**
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

    public static String savePath(String key, String suffix) {
        if (suffix == null){
            suffix = "png";
        }
        return DOWNLOAD_IMAGE_DIR + "/" + key + "." + suffix;
    }

    public static String saveBitmapToSD(String key, Bitmap bm, String suffix) {
        if (suffix == null){
            suffix = "png";
        }
        if (hasSdcard() && bm != null) {
            try {
                String fileName = savePath(key, suffix);
                File file = new File(fileName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                if (suffix.toUpperCase().equals("PNG")) {
                    bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } else {
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
                fos.flush();
                fos.close();
                return fileName;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                MyLog("e", LOG_MSG + "ERROR saveBitmapToSD IOException msg:" + e);
            }
        }
        return null;
    }

    public static Boolean renameFile(String oldPath, String newPath) {
        if (oldPath == null || newPath == null) {
            return false;
        }
        if (oldPath.equals(newPath)) {
            return true;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        return renameFile(oldFile, newFile);
    }

    public static Boolean renameFile(File oldPath, File newPath) {
        if (oldPath == null || newPath == null) {
            return false;
        }
        if (oldPath.getAbsolutePath().equals(newPath.getAbsolutePath())) {//两个地址相同
            return true;
        }
        if (hasFile(oldPath)) {
            oldPath.renameTo(newPath);//如果新地址存在文件会被覆盖
        } else {
            return false;
        }
        return true;
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

    public static String getTS() {
        String str = String.valueOf(mRandom.nextInt(9999));
        return str;
    }

    public static void MyLog(String level, String msg) {
        if (!LOG_DEBUG)
            return;
        
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
