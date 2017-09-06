package com.cappu.cleaner.update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cappu.cleaner.Util;


public class UpdateCenter {
    
    public static String BASE_URL = "http://upgrade.cappu.cn";// http://192.168.0.51:8081/client
    private static String POSH_URL = BASE_URL+"/v1/upgrade";//protocol.getHost();
    private static Random mRandom = new Random();
    private static String SIGN_KEY = "cappu-g@od";
    private static final ApkUpdate mApkUpdate = new ApkUpdate();
    
    public static final long DAY_MILS = 24*60*60*1000;
    
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator+"upgrade"+ File.separator;
    
    /**调用检测是否有新版本*/
    public static void onResume(Activity activity) {
//        x.Ext.init(activity.getApplication());
        if(activity == null) {
            if(Util.DEBUG)Log.e(Util.TAG, "unexpected null context in onResume");
        } else {
            mApkUpdate.setContext(activity);
        }
        
        String postUrl = POSH_URL +getAppKeyToHeader(activity)+getSign(activity)+getChannel(activity)+getVCode(activity)+getPack(activity);
        //http://upgrade.cappu.cn/v1/upgrade？appkey=com.cappu.downloadcenter&sign=md5(appsecret+"cappu-g@od")&channel=getChannel&vcode=vcode&pack=pack
        mApkUpdate.startDownload(postUrl);
    }
    
    /**更新检测*/
    public static void updateDetect(Activity activity){
        
        if(checkNetworkState(activity)){
            String postUrl = POSH_URL +getAppKeyToHeader(activity)+getSign(activity)+getChannel(activity)+getVCode(activity)+getPack(activity);
            mApkUpdate.setContext(activity);
            mApkUpdate.detectVersion(postUrl,"isdetect");
        }else{
            Toast.makeText(activity, "网络异常", Toast.LENGTH_LONG).show();
        }
    }
    
     /** 
     * 检测网络是否连接
     * @return 
     */
    public static boolean checkNetworkState(Activity activity) {
        boolean flag = false;
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        return flag;
    }
    
    static String getAppKeyToHeader(Activity activity){
        return "?"+getAppKey(activity);
    }
    
    static String getAppKeyToOther(Activity activity){
        return "&"+getAppKey(activity);
    }
    
    private static String getAppKey(Activity activity){
        return "appkey="+ChannelKeyManger.getAppKEY(activity.getPackageName());
    }
    
    static String getChannel(Activity activity){
        return "&channel=" + ChannelKeyManger.getChannel(activity.getPackageName());
    }
    
    static String getSign(Activity activity) {
        return "&sign="+encodeContentForUrl(md5EncodeToString(ChannelKeyManger.getSecret(activity.getPackageName()) + SIGN_KEY));//md5EncodeToString(values)"appsecret" + SIGN_KEY
    }
    
    static String getVCode(Context context){
        if(context != null && context.getPackageName().equals("com.cappu.cleaner")){
            return "&vcode="+0;
        }
        return "&vcode="+ApkInfoUtil.getAppVersionCode(context, null);
    }
    
    static String getPack(Context context){
        if(context != null && context.getPackageName().equals("com.cappu.cleaner")){
            return "&pack="+"com.cappu.cleaner";
        }
        return "&pack="+context.getPackageName();
    }
    
    /**
     * 字符串转换
     * 
     * @param content
     * @return
     */
    static String encodeContentForUrl(String content) {

        try {
            return (content == null ? "" : URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }
    
    static String md5EncodeToFile(File file) {
        if (file == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return md5Encode(out.toByteArray());
        } catch (IOException e) {
        }
        return null;
    }
    
    private static String md5EncodeToString(String values){
        try {
            return md5Encode(values.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            if(Util.DEBUG)Log.e(Util.TAG, "ERROR ----UpdateCenter ----UnsupportedEncodingException :"+e.toString());
        }
        return null;
    }
    
    /**
     * 将字符串编码为md5格式
     * 
     * @param value
     * @return
     */
    private static String md5Encode(byte[] value) {
        String tmp = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(value);
            byte[] md = md5.digest();
            tmp = binToHex(md);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return tmp;
    }
    
    static String binToHex(byte[] md) {
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


}
