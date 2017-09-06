package com.cappu.cleaner.update;

import android.text.TextUtils;


public class ChannelKeyManger {
    public static final int LOCAL_CHANNEL = 0;
    public static final int NETWORK_CHANNEL = 1;

    /** 以下包名所对应的 appkey 及 channel 所放的pos位置 */
    private static String[] PACKAGE_NAME = { "com.cappu.downloadcenter" };
    private static String[] CHANNEL = { "06b7801c9a3f1916a4d86ca374e1c3c9", "06b7801c9a3f1916a4d86ca374e1c3c9" };
    private static String[] APP_KEY = { "d9527ec4037c42748c4f78138347aff2" };
    private static String[] APP_SECRET = { "4e1bec5b03ba46d2b4c6a6de9dda8c2a" };

    public static String getChannel(String packagename) {
        int index = NETWORK_CHANNEL;// 0线下,1线上
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
