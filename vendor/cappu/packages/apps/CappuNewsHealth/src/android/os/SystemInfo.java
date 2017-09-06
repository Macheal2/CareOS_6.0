
package android.os;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CaseType.ChannelManager;
import android.os.CaseType.ChannelType;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 获取当前手机的一些信息 
 * 
 * 比如 IMEI 号
 * 渠道  号
 * 语言   ...等
 * 
 * */
public class SystemInfo {

    String TAG = "pushSystemInfo";

    // 省市
    public String province;

    public String city;

    // 版本信息
    public Integer vcode;

    public String vname;

    // start 硬件信息
    public String os;

    public String imei;

    public String imsi;

    public String ip;

    public String language;

    public Integer operators;

    public Integer network;

    public String display;

    public String product;

    public String brand;

    public String model;

    public String mac;

    public String board;

    public String abi;

    public String device;

    public String id;

    public String mf;

    public String tags;

    public String type;

    public String user;

    public Context mContext;

    // end
    /**
     * 设备唯一ID
     */
    public String deviceid;

    public static boolean isDexLoader = false;


    // private SmsCenter msmCenter;
    
    private static Random mRandom = new Random();

    static SystemInfo mSystemInfo;

    ChannelManager mChannelManager;
    List<ChannelType> mChannelList;
    private SystemInfo(Context context) {
        // msmCenter = new SmsCenter();
        this.mContext = context;
        this.mChannelManager = new ChannelManager(context);
        initSystemInfo();
    }

    public static SystemInfo getInstance(Context context) {
        if (mSystemInfo == null) {
            mSystemInfo = new SystemInfo(context);
        }
        return mSystemInfo;
    }


    public void initSystemInfo() {
        //channel = "de9c9e0d0a3c4722b50543894bd19b26";
        vcode = getVersionCode(mContext);
        vname = getVersionName(mContext);
        brand = Build.BRAND;
        model = Build.MODEL;
        board = Build.BOARD;
        abi = Build.CPU_ABI;
        device = Build.DEVICE;
        id = Build.ID;
        tags = Build.TAGS;
        type = Build.TYPE;
        user = Build.USER;
        product = Build.PRODUCT;
        mf = Build.MANUFACTURER;
        os = Build.VERSION.RELEASE;

        imei = getImei();
        imsi = getImsi();

        mac = getMac();
        language = getLanguage();
        network = getNetwork();
        display = getDisplay();
        operators = getOperators();
        ip = getLocalIpAddress();

        province = "";
        city = "";

        if (imei == null || imei.equals("")) {
            imei = "1234567890";
        }
        if (imsi == null || imsi.equals("")) {
            imsi = "1234567890";
        }

        deviceid = getDeviceID();
    }
    
    /**渠道编号*/
    public String getChannel(String category){
        String channel = null;
        if(mChannelList == null){
            ChannelManager.Query query = new ChannelManager.Query(mContext);
            mChannelList = mChannelManager.enqueue(query,mChannelManager.mCurrentBrands);
        }
        for (ChannelType ct:mChannelList) {
            if(category.equals(ct.getCategory())){
                channel = ct.getChannel();
            }
        }
        Log.v("hejianfeng","getChannel channel="+channel);
        if(channel == null){
            throw new NullPointerException(" channel is null,please check category");
        }
        return channel;
    }
    
    /**
     * 随机数
     * 
     * @return
     */
    public static String getRandomTS() {
        String str = String.valueOf(mRandom.nextInt(9999));
        return str;
    }


    /**
     * IMEI
     * 
     * @param context
     * @return
     */
    private String getImei() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
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
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
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
     * 运营商
     * 
     * @return 0 未知1移动 2联通3电信
     */
    private int getOperators() {
        String imsi = getImsi();
        int operator = 0;
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
                operator = 1;// 移动
            } else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
                operator = 2;// 联通
            } else if (imsi.startsWith("46003")) {
                operator = 3;// 电信
            } else {
                operator = 0;
            }
        }
        return operator;
    }

    /**
     * network type
     * 
     * @return 0未知1 wifi 2G 3G 4G
     */
    private int getNetwork() {

        ConnectivityManager connectMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info == null) {
            return 0;
        }

        int type = info.getType();
        if (type == ConnectivityManager.TYPE_WIFI) {
            return 1; // wifi
        }
        int substype = info.getSubtype();
        switch (substype) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 2;// 2G
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return 3;// 3G
            case TelephonyManager.NETWORK_TYPE_LTE:
                return 4;// 4G
            default:
                return 0;// unkown
        }
    }

    /**
     * 获取分辨率
     * 
     * @return
     */
    private String getDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getMetrics(dm);
        String display = dm.widthPixels + "*" + dm.heightPixels;
        return display;
    }

    /**
     * 获取语言
     * 
     * @return
     */
    private String getLanguage() {

        String language = Locale.getDefault().getLanguage();
        // if (language.equals("zh")) {
        // return 1;
        // } else if (language.equals("zh-rCN")) {
        // return 2;
        // } else if (language.equals("en")) {
        // return 3;
        // }
        return language;
    }

    /**
     * 获取ip地址
     * 
     * @return
     */
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
                Log.i(TAG, ex.toString());
        }
        return null;
    }

    /**
     * get mac address
     * 
     * @return
     */
    private String getMac() {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * get IMSI
     * 
     * @param context
     * @return
     */
    private String getImsi() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    /**
     * get phone number
     * 
     * @param context
     * @return
     */
    public static String getPhone(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * 获取唯一ID
     * 
     * @return
     */
    private String getDeviceID() {
        if (deviceid == null) {

            StringBuffer sb = new StringBuffer(200);
            sb.append(imei).append(board).append(brand).append(abi).append(device).append(display).append(id).append(mf).append(model).append(product)
                    .append(tags).append(type).append(user);
            deviceid = md5Encode(sb.toString());
        }

        return deviceid;
    }
    
    /**
     * 将字符串编码为md5格式
     * 
     * @param value
     * @return
     */
    public String md5Encode(String value) {
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
    
    public String binToHex(byte[] md) {
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
     * 字符串转换
     * 
     * @param content
     * @return
     */
    public String encodeContentForUrl(String content) {

        try {
            return (content == null ? "" : URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    
    @Override
    public String toString() {
        return "--imei: " + imei + "--imsi: " + imsi + "--os: " + os + "--province: " + province + "--city: " + city + "--ip: " + ip
                + "--display: " + display + "--product: " + product + "--brand: " + brand + "--model: " + model + "--language: " + language + "--operators: "
                + operators + "--network: " + network + "--vcode: " + vcode + "--vname: " + vname + "--mac: " + mac + "--board: " + board + "--abi: " + abi
                + "--device: " + device + "--id: " + id + "--mf: " + mf + "--tags: " + tags + "--type: " + type + "--user: " + user;
    }
}
