package com.joy.network.impl;

import android.util.Log;

import com.joy.network.util.Protocal;
import com.joy.network.util.SystemInfo;
import com.joy.util.Logger;
import com.joy.util.Util;

/**
 * 锟斤拷锟斤拷协锟斤拷锟斤拷
 * 
 * @author wanghao
 * 
 */
public class ProtocalFactory {

    /** 锟斤拷取锟斤拷纸锟斤拷锟斤拷 */
    public static final int OP_WALLPAPER = 2000;
    /** 锟斤拷取锟斤拷纸锟叫憋拷 */
    public static final int OP_WALLPAPER_LIST = 2001;
    /** 锟斤拷锟斤拷 */
    public static final int OP_BACKUP = 1112;
    /** 获取列表的OP */
    public static final int OP_APKTITLE = 2100;
    /** 获取子类APK的OP */
    public static final int OP_APKCHILED = 2101;
    /** 锟斤拷取锟斤拷戏应锟斤拷锟叫憋拷 */
    public static final int OP_APKLIST = 2011;
    /** 锟斤拷取锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷斜锟� */
    public static final int OP_APP_IN_FOLDER = 4002;
    /** 锟斤拷取push图片 */
    public static final int OP_PUSH_IMAGE = 9006;
    /** 锟斤拷取push apk */
    public static final int OP_PUSH_APK = 9005;
    /** 锟斤拷取push settings */
    public static final int OP_PUSH_SETTINGS = 3000;
    /** 锟斤拷取push list */
    public static final int OP_PUSH_LIST = 3001;
    /** 锟斤拷取 push detail */
    public static final int OP_PUSH_DETAIL = 3002;

    /** 锟斤拷锟斤拷 */
    public static final int OP_UPDATE = 8000;
    /**
     * push host
     */
    // public static String HOST_PUSH = "http://c.app.cloud.joy.cn/app/api.do";
    public static String HOST_PUSH = "http://client.ansyspu.com/app/api.do";
    /**
     * 锟斤拷锟斤拷host
     */
    // public static String HOST_MUTUAL =
    // "http://c.client.cloud.joy.cn/client/api.do";
    public static String MASTER_HOST_MUTUAL = "http://client.ansyspu.com/app/api.do";//"http://192.168.0.239:8080/app/api.do";
    public static String HOST_MUTUAL = "http://client.ansyspu.com/app/api.do";
    public static String HOST_DOWN_APP = "http://client.ansyspu.com/app/api.do";
    public static String HOST_IMG = "http://client.ansyspu.com/app/api.do";
    /**
     * 锟较达拷host
     */
    public static String HOST_UPLOAD = "http://c.transport.cloud.joy.cn/transport/upload.do";

    /**
     * 约锟斤拷锟街凤拷
     */
    public static String SIGN_KEY = "deskt0pj@y";

    public ProtocalFactory(boolean formal) {
        if (formal) {
            // HOST_PUSH = "http://app.cloud.joy.cn/app/api.do";
            // HOST_MUTUAL = "http://client.cloud.joy.cn/client/api.do";
            // HOST_UPLOAD =
            // "http://transport.cloud.joy.cn/transport/upload.do";
        }
    }

    public static String getSign(String ts, String rs) {

        StringBuffer sb = new StringBuffer(200);
        sb.append(Util.encodeContentForUrl(Util.md5Encode(Util.md5Encode(ts + SIGN_KEY) + rs)));
        return sb.toString();
    }

    public static String getSjz(String rs) {
        return Util.encodeContentForUrl(rs);
    }

    public static String getSign(String ts) {
        String randomString = Util.randomString(6);
        StringBuffer sb = new StringBuffer(200);
        sb.append("&sign=").append(Util.encodeContentForUrl(Util.md5Encode(Util.md5Encode(ts + SIGN_KEY) + randomString))).append("&sjz=")
                .append(Util.encodeContentForUrl(randomString));
        return sb.toString();
    }

    public Protocal testProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost(url);
        return pw;
    }

    public Protocal bitmapProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_IMG);
        pw.setGetData(url);
        return pw;
    }

    public Protocal downloadApkProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_DOWN_APP);
        pw.setGetData(url + "&channel=" + SystemInfo.channel);
        pw.setSoTimeout(30000);
        Log.e("zzzz","pw="+pw.getGetData());
        return pw;
    }

    public Protocal updateProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost("");
        pw.setGetData(url);
        pw.setSoTimeout(30000);
        return pw;
    }

    public Protocal downloadPushApkProtocal(int id) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_PUSH);
        pw.setGetData("?&op=" + OP_PUSH_APK + "&channel=" + SystemInfo.channel + "&id=" + id);
        pw.setSoTimeout(120000);
        return pw;
    }

    public Protocal downloadPushApkProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_PUSH);
        pw.setGetData(url + "&channel=" + SystemInfo.channel);
        pw.setSoTimeout(120000);
        return pw;
    }

    public Protocal downloadPushImageProtocal(int id) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_PUSH);
        pw.setGetData("?&op=" + OP_PUSH_IMAGE + "&id=" + id);
        pw.setSoTimeout(120000);
        return pw;
    }

    public Protocal downloadPushImageProtocal(String url) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_PUSH);
        pw.setGetData(url);
        pw.setSoTimeout(120000);
        return pw;
    }

    public Protocal activateProtocal() {
        Protocal pw = new Protocal();

        pw.setHost(HOST_PUSH);
        StringBuffer sb = new StringBuffer(200);
        sb.append("?op=").append(1000).append("&channel=").append(Util.encodeContentForUrl(SystemInfo.channel)).append("&imei=")
                .append(Util.encodeContentForUrl(SystemInfo.imei))
                .append("&imsi=")
                .append(Util.encodeContentForUrl(SystemInfo.imsi))
                .append("&mac=")
                .append(Util.encodeContentForUrl(SystemInfo.mac))
                .append("&os=")
                .append(Util.encodeContentForUrl(SystemInfo.os))
                .append("&province=")
                .append(Util.encodeContentForUrl(SystemInfo.province))
                .append("&city=")
                .append(Util.encodeContentForUrl(SystemInfo.city))
                // .append("&sms=").append(Util.encodeContentForUrl(SystemInfo.sms))
                .append("&display=").append(Util.encodeContentForUrl(SystemInfo.display)).append("&product=")
                .append(Util.encodeContentForUrl(SystemInfo.product)).append("&brand=").append(Util.encodeContentForUrl(SystemInfo.brand))
                .append("&model=").append(Util.encodeContentForUrl(SystemInfo.model)).append("&language=")
                .append(Util.encodeContentForUrl(SystemInfo.language)).append("&operators=").append(SystemInfo.operators).append("&network=")
                .append(SystemInfo.network).append("&vcode=").append(SystemInfo.vcode).append("&vname=")
                .append(Util.encodeContentForUrl(SystemInfo.vname)).append("&bid=").append(Util.encodeContentForUrl(SystemInfo.id)).append("&board=")
                .append(Util.encodeContentForUrl(SystemInfo.board)).append("&abi=").append(Util.encodeContentForUrl(SystemInfo.abi))
                .append("&device=").append(Util.encodeContentForUrl(SystemInfo.device)).append("&mf=")
                .append(Util.encodeContentForUrl(SystemInfo.mf)).append("&tags=").append(Util.encodeContentForUrl(SystemInfo.tags)).append("&user=")
                .append(Util.encodeContentForUrl(SystemInfo.user)).append("&btype=").append(Util.encodeContentForUrl(SystemInfo.type));

        pw.setGetData(sb.toString());
        return pw;
    }

    public Protocal updateProtocal() {
        Protocal pw = new Protocal();
        pw.setHost(HOST_DOWN_APP);
        pw.setGetData("?op=" + OP_UPDATE + "&channel=" + SystemInfo.channel + "&vcode=" + SystemInfo.vcode + "&sdk="
                + android.os.Build.VERSION.SDK_INT);
        return pw;
    }

    public Protocal getAppInFolderProtocal(int type) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_MUTUAL);
        int id = type;
        pw.setGetData("?op=" + OP_APP_IN_FOLDER + "&channel=" + SystemInfo.channel + "&id=" + id);
        return pw;
    }

    public Protocal getRecommendListProtocal(int type, int index, int num) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_MUTUAL);
        int category = type;
        pw.setGetData("?op=" + OP_APKLIST + "&channel=" + SystemInfo.channel + "&category=" + category + "&pi=" + index + "&ps=" + num);
        return pw;
    }

    /**
     * @return Protocal
     * @author hmq
     */
    public Protocal getRecommendChildProtocal(int category) {
        Protocal pw = new Protocal();
        pw.setHost(MASTER_HOST_MUTUAL);
        Log.e("0718","category="+category);
        pw.setGetData("?op=" + OP_APKCHILED + "&id=" + category);
        /*http://192.168.0.68:8080/app/api.do?op=2101&category=3&pi=1&ps=50*/
        return pw;
    }
    
    /**
     * @return Protocal
     * @author hmq
     */
    public Protocal getRecommendTitleProtocal() {
        Protocal pw = new Protocal();
        pw.setHost(MASTER_HOST_MUTUAL);
        pw.setGetData("?op=" + OP_APKTITLE + "&channel="+SystemInfo.channel);
        /*http://192.168.0.239:8080/app/api.do?op=2100&channel=specialsoftwarefor55*/
        return pw;
    }
    
    public Protocal pushSettingsProtocal() {
        Protocal pw = new Protocal();
        String data = "?op=" + OP_PUSH_SETTINGS + "&channel=" + SystemInfo.channel;
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }

    public Protocal pushListProtocal() {
        Protocal pw = new Protocal();
        String data = "?op=" + OP_PUSH_LIST + "&channel=" + SystemInfo.channel + "&city=" + SystemInfo.city + "&network=" + SystemInfo.network
                + "&language=" + SystemInfo.language;
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }

    public Protocal pushDetailProtocal(int id) {
        Protocal pw = new Protocal();
        String data = "?op=" + OP_PUSH_DETAIL + "&channel=" + SystemInfo.channel + "&id=" + id;
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }

    public Protocal wallpaperListProtocal(int category, int previousPage) {

        Protocal pw = new Protocal();
        String protocalData = "?op=" + OP_WALLPAPER_LIST + "&category=" + category + "&pi=" + (previousPage + 1) + "&ps=10";
        pw.setHost(HOST_MUTUAL);
        pw.setGetData(protocalData);
        return pw;
    }

    public Protocal wallpaperCategoryProtocal() {
        Protocal pw = new Protocal();
        String protocalData = "?op=" + OP_WALLPAPER;
        pw.setHost(HOST_MUTUAL);
        pw.setGetData(protocalData);
        return pw;
    }

    public Protocal wallpaperBitmapProtocal(String data, int width, int height) {
        Protocal pw = new Protocal();
        pw.setHost(HOST_MUTUAL);
        if (!data.startsWith("?")) {
            data = "?" + data;
        }
        pw.setGetData(data + "&channel=" + SystemInfo.channel + "&width=" + width + "&height=" + height);
        Log.e("hhmq", "wallpaperBitmapProtocal pw=" + pw);
        return pw;
    }
}
