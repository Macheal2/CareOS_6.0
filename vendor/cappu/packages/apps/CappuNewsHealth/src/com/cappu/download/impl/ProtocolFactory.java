
package com.cappu.download.impl;


import android.os.SystemInfo;



/**
 * 协议的工厂类
 * 
 * @author hehangjun
 */
public class ProtocolFactory {

    /**获取APK下载列表*/
    public static final int OP_APKLIST = 2011;
    
    /**add by hehangjun for push.*/
    public static final int OP_PUSH_IMAGE = 9006;

    public static final int OP_PUSH_APK = 9005;

    public static final int OP_PUSH_SETTINGS = 3000;

    public static final int OP_PUSH_LIST = 3001;

    public static final int OP_PUSH_DETAIL = 3002;

    public static final int OP_PUSH_UPDATE = 99999;

    public final String HOST_PUSH = "http://client.ansyspu.com/app/api.do";

    public static String SIGN_KEY = "deskt0pj@y";// 约定字符串
    
    
    /*9005  apk
    9002 圈内apk
    9006 推送图片
    9007 banner图片*/

    SystemInfo mSystemInfo;
    public ProtocolFactory(SystemInfo si) {
        this.mSystemInfo = si;
    }

    public String getSign(String ts, String rs) {

        StringBuffer sb = new StringBuffer(200);
        sb.append(mSystemInfo.encodeContentForUrl(mSystemInfo.md5Encode(mSystemInfo.md5Encode(ts + SIGN_KEY) + rs)));
        return sb.toString();
    }

    public String getSjz(String rs) {
        return mSystemInfo.encodeContentForUrl(rs);
    }

    public String getSign(String ts) {
        String randomString = mSystemInfo.randomString(6);
        StringBuffer sb = new StringBuffer(200);
        sb.append("&sign=").append(mSystemInfo.encodeContentForUrl(mSystemInfo.md5Encode(mSystemInfo.md5Encode(ts + SIGN_KEY) + randomString))).append("&sjz=")
                .append(mSystemInfo.encodeContentForUrl(randomString));
        return sb.toString();
    }

    public Protocol testProtocol(String url) {
        Protocol pw = new Protocol();
        pw.setHost(url);
        return pw;
    }

    // add by huangming for push.
    public Protocol downloadPushApkProtocol(SystemInfo systemInfo,int id,String category) {
        Protocol pw = new Protocol();
        pw.setHost(HOST_PUSH);
        pw.setGetData("?&op=" + OP_PUSH_APK + "&channel=" + systemInfo.getChannel(category) + "&id=" + id);
        pw.setSoTimeout(120000);
        return pw;
    }

    public Protocol downloadPushApkProtocol(SystemInfo systemInfo,String url,String category) {
        Protocol pw = new Protocol();
        pw.setHost(HOST_PUSH);
        pw.setGetData(url + "&channel=" + systemInfo.getChannel(category));
        pw.setSoTimeout(120000);
        return pw;
    }

    /**下载图片的协议
     * 这个基本没用
     * */
    public Protocol downloadPushImageProtocol(int id) {
        Protocol pw = new Protocol();
        pw.setHost(HOST_PUSH);
        pw.setGetData("?&op=" + OP_PUSH_IMAGE + "&id=" + id);
        pw.setSoTimeout(120000);
        return pw;
    }

    /**下载图片的协议*/
    public Protocol downloadPushImageProtocol(String url) {
        Protocol pw = new Protocol();
        pw.setHost(HOST_PUSH);
        pw.setGetData(url);
        pw.setSoTimeout(120000);
        return pw;
    }
    
    /**
     * type  apk的类型，比如游戏，工具，等分类的类别
     * page  分页 的页数
     * num   每页的数量
     * */
    public Protocol getRecommendListProtocal(SystemInfo systemInfo,int apkCategory, int page, int num,String category) {
        Protocol pw = new Protocol();
        pw.setHost(HOST_PUSH);
        pw.setGetData("?op=" + OP_APKLIST + "&channel=" + systemInfo.getChannel(category) + "&category=" + apkCategory + "&pi=" + page + "&ps=" + num);
        return pw;
    }

    /**
     * 获取激活协议
     * 
     * 协议中包涵个体设备信息
     * */
    public Protocol activateProtocol(SystemInfo systemInfo,String category) {
        Protocol pw = new Protocol();

        pw.setHost(HOST_PUSH);
        StringBuffer sb = new StringBuffer(200);
        sb.append("?op=")
                .append(1000)
                .append("&channel=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.getChannel(category)))
                .append("&imei=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.imei))
                .append("&imsi=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.imsi))
                // .append("&mac=").append(Util.encodeContentForUrl(SystemInfo.mac))
                .append("&os=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.os))
                .append("&province=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.province))
                .append("&city=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.city))
                // .append("&sms=").append(Util.encodeContentForUrl(SystemInfo.sms))
                .append("&display=").append(mSystemInfo.encodeContentForUrl(systemInfo.display)).append("&product=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.product)).append("&brand=").append(mSystemInfo.encodeContentForUrl(systemInfo.brand)).append("&model=")
                .append(mSystemInfo.encodeContentForUrl(systemInfo.model)).append("&language=").append(mSystemInfo.encodeContentForUrl(systemInfo.language))
                .append("&operators=").append(systemInfo.operators).append("&network=").append(systemInfo.network).append("&vcode=").append(systemInfo.vcode)
                .append("&vname=").append(mSystemInfo.encodeContentForUrl(systemInfo.vname)).append("&bid=").append(mSystemInfo.encodeContentForUrl(systemInfo.id))
                .append("&board=").append(mSystemInfo.encodeContentForUrl(systemInfo.board)).append("&abi=").append(mSystemInfo.encodeContentForUrl(systemInfo.abi))
                .append("&device=").append(mSystemInfo.encodeContentForUrl(systemInfo.device)).append("&mf=").append(mSystemInfo.encodeContentForUrl(systemInfo.mf))
                .append("&tags=").append(mSystemInfo.encodeContentForUrl(systemInfo.tags)).append("&user=").append(mSystemInfo.encodeContentForUrl(systemInfo.user))
                .append("&btype=").append(mSystemInfo.encodeContentForUrl(systemInfo.type));

        pw.setGetData(sb.toString());
        return pw;
    }

    /**
     * 获取服务器的 设置请求协议*/
    public Protocol pushSettingsProtocol(SystemInfo systemInfo,String category) {
        Protocol pw = new Protocol();
        String data = "?op=" + OP_PUSH_SETTINGS + "&channel=" + systemInfo.getChannel(category);
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }

    /**获取推送（push）的列表（list）*/
    public Protocol pushListProtocol(SystemInfo systemInfo,String category) {
        Protocol pw = new Protocol();
        String data = "?op=" + OP_PUSH_LIST + "&channel=" + systemInfo.getChannel(category) + "&city=" + systemInfo.city + "&network=" + systemInfo.network + "&language="
                + systemInfo.language;
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }

    /**获取推送（push）的单条信息*/
    public Protocol pushDetailProtocol(SystemInfo systemInfo,long id,String category) {
        Protocol pw = new Protocol();
        String data = "?op=" + OP_PUSH_DETAIL + "&channel=" + systemInfo.getChannel(category) + "&id=" + id;
        pw.setHost(HOST_PUSH);
        pw.setGetData(data);
        return pw;
    }
}
