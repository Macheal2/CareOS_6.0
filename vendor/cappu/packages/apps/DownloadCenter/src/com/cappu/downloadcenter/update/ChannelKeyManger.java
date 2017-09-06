package com.cappu.downloadcenter.update;

import com.cappu.downloadcenter.context.FolderApplication;

import android.text.TextUtils;

public class ChannelKeyManger {

    /** 以下包名所对应的 appkey 及 channel 所放的pos位置 */
    private static String[] PACKAGE_NAME = { "com.cappu.downloadcenter" };
    private static String[] CHANNEL = { "8cb18329e4a6e8c53dd8146f3599d6ad", "6d7ceefff0e3bf8260dab6ac127d1e5e" };
    private static String[] APP_KEY = { "336157e668684e84b4ac56ee8c72f364" };
    private static String[] APP_SECRET = { "e8b1d8162b8e4ab4bc771c917d5b2840" };

    public static String getChannel(String packagename) {
        int index = FolderApplication.mIsChannel;// 0线下,1线上
        return CHANNEL[index];
    }

    public static String getAppKEY(String packagename) {
//        int index = getPKGIndex(packagename);
        return APP_KEY[0];
    }

    public static String getSecret(String packagename) {
//        int index = getPKGIndex(packagename);
        return APP_SECRET[0];
    }
    
    private static int getPKGIndex(String packagename) {
        int index = -1;
        comparedLength();
        if (!TextUtils.isEmpty(packagename)) {
            for (int i = 0; i < PACKAGE_NAME.length; i++) {
                if (packagename.equals(PACKAGE_NAME[i])) {
                    return i;
                } else {
                    continue;
                }
            }
        }
        
        if (index == -1) {
            new Exception("not find packname");
        }
        
        return index;
    }

    private static void comparedLength() {
        if (!(PACKAGE_NAME.length == CHANNEL.length && CHANNEL.length == APP_KEY.length)) {
            new Exception("PACKAGE_NAME CHANNEL APP_KEY all length not equal");
        }
    }
}
