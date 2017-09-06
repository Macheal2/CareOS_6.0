package com.cappu.launcherwin.phoneutils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

public class PhoneUtil {
    
    private String TAG = "PhoneUtil";
    
    TelephonyManager tm;
    Context mContext = null;
    String imsi;
    static PhoneUtil mPhoneUtil = null;
    
    private static boolean IsInquiry = false;

    public PhoneUtil(Context context) {
        this.mContext = context;
        this.tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.tm.getCallState();
    }

    public static void init(Context context) {
        if(mPhoneUtil == null){
            mPhoneUtil = new PhoneUtil(context);
        }
    }

    public static PhoneUtil getInstance() {
        if (mPhoneUtil == null) {
            throw new IllegalStateException("Uninitialized");
        }
        return mPhoneUtil;
    }
    
    public void setInquiryType(boolean type){
        IsInquiry = type;
    }
    
    public boolean getInquiryType(){
        return IsInquiry;
    }
    
    /**
     * 获取手机屏幕分辨率
     * 
     * @return
     */
    public String getScreenPix() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return Integer.toString(dm.widthPixels) + "*" + Integer.toString(dm.heightPixels);
    }

    /**
     * 获取手机屏幕尺寸
     * 
     * @return
     */
    public String getDisplayMetrics() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;// 屏幕宽（像素，如：480px）
        int screenHeight = dm.heightPixels;// 屏幕高（像素，如：800px）
        return String.valueOf(screenWidth) + "*" + String.valueOf(screenHeight);
    }

    /*
     * NETWORK_TYPE_CDMA 网络类型为CDMA NETWORK_TYPE_EDGE 网络类型为EDGE
     * NETWORK_TYPE_EVDO_0 网络类型为EVDO0 NETWORK_TYPE_EVDO_A 网络类型为EVDOA
     * NETWORK_TYPE_GPRS 网络类型为GPRS NETWORK_TYPE_HSDPA 网络类型为HSDPA
     * NETWORK_TYPE_HSPA 网络类型为HSPA NETWORK_TYPE_HSUPA 网络类型为HSUPA
     * NETWORK_TYPE_UMTS 网络类型为UMTS
     * 
     * 在中国，联通的3G为UMTS或HSDPA，移动和联通的2G为GPRS或EGDE，电信的2G为CDMA，电信的3G为EVDO
     */
    /**
     * 获取网络类型
     * 
     * @return
     */
    public String getNetworkType() {
        int netType = tm.getNetworkType();
        if (TelephonyManager.NETWORK_TYPE_CDMA == netType || TelephonyManager.NETWORK_TYPE_EVDO_0 == netType
                || TelephonyManager.NETWORK_TYPE_EVDO_A == netType) {
            return "CDMA";
        } else {
            return "GSM";
        }
    }

    public String getNetworkNG() {
        int netType = tm.getNetworkType();
        if (TelephonyManager.NETWORK_TYPE_CDMA == netType || TelephonyManager.NETWORK_TYPE_GPRS == netType) {
            return "GPRS";
        } else if (TelephonyManager.NETWORK_TYPE_EDGE == netType) {
            return "EDGE";
        } else if (TelephonyManager.NETWORK_TYPE_HSDPA == netType || TelephonyManager.NETWORK_TYPE_UMTS == netType) {
            return "WCDMA_3G";
        } else if (TelephonyManager.NETWORK_TYPE_EVDO_0 == netType || TelephonyManager.NETWORK_TYPE_EVDO_A == netType) {
            return "EVDO_3G";
        }
        return "";
    }

    /**
     * 通过IMSI获取运营商名称 参考文献：http://baike.baidu.com/view/715091.htm
     * 
     * @return 运营商名称
     */
    public String getProvidersName() {
        String ProvidersName = null;
        // 返回唯一的用户ID;就是这张卡的编号神马的
        String IMSI = tm.getSubscriberId();
        if(IMSI == null){
            return null;
        }
        // IMSI号前面3位460是国家，紧接着后面2位00 02 07是中国移动，01是中国联通，03是中国电信。
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }
        return ProvidersName;
    }

    /**
     * 返回手机号码，对于GSM网络来说即MSISDN
     * 
     * @return
     */
    public String getLine1Number() {
        return tm.getLine1Number();
    }

    /**
     * 获取手机IMEI
     * 
     * @return
     */
    public String getImei() {
        return tm.getDeviceId();
    }

    /**
     * 判断手机是否漫游
     * 
     * @return
     */
    public boolean getRoam() {
        if (tm.isNetworkRoaming()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回SIM卡提供商的国家代码
     * 
     * @return
     */
    public String getSimCountryIso() {
        return tm.getSimCountryIso();
    }

    /**
     * 返回SIM卡的序列号(IMEI)
     * 
     * @return
     */
    public String getSimSerialNumber() {
        return tm.getSimSerialNumber();
    }

    /**
     * 返回移动网络运营商的名字(SPN)
     * 
     * @return
     */
    public String getNetworkOperatorName() {
        return tm.getNetworkOperatorName();
    }

    /**
     * * 返回电话状态
     * 
     * CALL_STATE_IDLE 无任何状态时 CALL_STATE_OFFHOOK 接起电话时 CALL_STATE_RINGING 电话进来时
     * 
     * @return
     */
    public int getCallState() {
        return tm.getCallState();
    }

    /**
     * 获取数据活动状态
     * 
     * DATA_ACTIVITY_IN 数据连接状态：活动，正在接受数据 DATA_ACTIVITY_OUT 数据连接状态：活动，正在发送数据
     * DATA_ACTIVITY_INOUT 数据连接状态：活动，正在接受和发送数据 DATA_ACTIVITY_NONE
     * 数据连接状态：活动，但无数据发送和接受
     */
    public int getDataActivity() {
        return tm.getDataActivity();
    }

    /**
     * 获取数据连接状态
     * 
     * DATA_CONNECTED 数据连接状态：已连接 DATA_CONNECTING 数据连接状态：正在连接 DATA_DISCONNECTED
     * 数据连接状态：断开 DATA_SUSPENDED 数据连接状态：暂停
     */
    public int getDataState() {
        return tm.getDataState();
    }

    /**
     * 获取Android系统版本号
     */
    public String getVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取Android系统对应的SDK版本号
     */
    public String getVersionSDK() {
        return android.os.Build.VERSION.SDK;
    }

    /**
     * 获取手机型号
     */
    public String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取所安装的APP版本号
     * 
     * @return 返回APP版本号
     * @throws Exception
     */
    public int getAppVersion() throws Exception {
        return this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionCode;
    }

    /**
     * 获取设置得 电话号码(T328W 手机无法获取，这个函数不一定能获取到)
     * 
     * @return 手机号码
     */
    public String getNativePhoneNumber() {
        String NativePhoneNumber = null;
        NativePhoneNumber = tm.getLine1Number();
        return NativePhoneNumber;
    }

    /**
     * 查询余额
     * 
     * @param ProvidersName
     */
    public boolean sendSms(String ProvidersName) {
        String msg = "";
        String number = "";
        /*if (ProvidersName.equals("中国移动")) {
            msg = "YE";
            number = "10086";
        } else if (ProvidersName.equals("中国联通")) {
            msg = "YE";
            number = "10010";
        } else if (ProvidersName.equals("中国电信")) {
            // 101实时话费 102账户余额 103上月账单 104历史账单 105积分查询 107积分消费记录 108套餐使用情况
            // 111欠费查询 151手机上网流量查询
            msg = "102";
            number = "10001";
        }*/
        String[] info = getSendNumber(ProvidersName);
        msg = info[0];
        number = info[1];
        
        if (TextUtils.isEmpty(msg) || TextUtils.isEmpty(number)) {
            return false;
        } else {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, msg, null, null);
            Log.i(TAG, "number:" + number + "    msg:" + msg);
            return true;
        }
    }
    
    /**获取当前当前查询余额运营商的号码*/
    public String[] getSendNumber(String ProvidersName){
        String[] info = new String[2];
        if (ProvidersName.equals("中国移动")) {
            info[0] = "YE";
            info[1] = "10086";
        } else if (ProvidersName.equals("中国联通")) {
            info[0] = "YE";
            info[1] = "10010";
        } else if (ProvidersName.equals("中国电信")) {
            // 101实时话费 102账户余额 103上月账单 104历史账单 105积分查询 107积分消费记录 108套餐使用情况
            // 111欠费查询 151手机上网流量查询
            info[0] = "102";
            info[1] = "10001";
        }else{
            info[0] = "";
            info[1] = "";
        }
        return info;
    }

    /**
     * 获取手机短信
     * 
     * @return
     */
    public String getSmsInPhone() {
        final String SMS_URI_ALL = "content://sms/";
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_SEND = "content://sms/sent";
        final String SMS_URI_DRAFT = "content://sms/draft";
        final String SMS_URI_OUTBOX = "content://sms/outbox";
        final String SMS_URI_FAILED = "content://sms/failed";
        final String SMS_URI_QUEUED = "content://sms/queued";
        StringBuilder smsBuilder = new StringBuilder();
        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[] { "_id", "body", "date" };
            Cursor cur = mContext.getContentResolver().query(uri, projection, null, null, "date desc"); // 获取手机内部短信
            if (cur.moveToFirst()) {
                // int index_Address = cur.getColumnIndex("address");
                // int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                // int index_Type = cur.getColumnIndex("type");
                // do {
                // String strAddress = cur.getString(index_Address);
                // int intPerson = cur.getInt(index_Person);
                String strbody = cur.getString(index_Body);
                long longDate = cur.getLong(index_Date);
                // int intType = cur.getInt(index_Type);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = new Date(longDate);
                String strDate = dateFormat.format(d);
                String strType = "";
                // if (intType == 1) {
                // strType = "接收";
                // } else if (intType == 2) {
                // strType = "发送";
                // } else {
                // strType = "null";
                // }
                // smsBuilder.append("[ ");
                // smsBuilder.append(strAddress + ", ");
                // smsBuilder.append(intPerson + ", ");
                smsBuilder.append(strbody + ", ");
                smsBuilder.append(strDate + ", ");
                // smsBuilder.append(strType);
                smsBuilder.append(" ]\n\n");
                // } while (cur.moveToNext());
                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
                smsBuilder.append("no result!");
            }
            smsBuilder.append("getSmsInPhone has executed!");
        } catch (SQLiteException ex) {
            Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
        }
        return smsBuilder.toString();
    }
}
