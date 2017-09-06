package com.cappu.launcherwin.phoneutils;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SMSHandler extends Handler {
    public static final String TAG = "hehangjun";
    private Context mContext;
    
    private UIInterface mUIInterface;
    private PhoneUtil mPhoneUtil;

    public SMSHandler(Context context, UIInterface uUIInterface,PhoneUtil phoneUtil) {
        super();
        this.mContext = context;
        this.mUIInterface = uUIInterface;
        this.mPhoneUtil = phoneUtil;
    }

    public void handleMessage(Message message) {
        //Log.i(TAG, "handleMessage: " + message);
        MessageItem item = (MessageItem) message.obj;
        String providerName = mPhoneUtil.getProvidersName();
        String[] sendNumber = mPhoneUtil.getSendNumber(providerName);
                
        if(item.getPhone().equals(sendNumber[1])){//判断是查询话费的短信进行拦截
            mUIInterface.updateUi(item);
            // delete the sms
            Uri uri = ContentUris.withAppendedId(SMS.CONTENT_URI, item.getId());
            mContext.getContentResolver().delete(uri, null, null);
            //Log.i(TAG, "delete sms item: " + item);
        }else{
            Log.i(TAG, "不是查询短信号码: " + item+"   item.getPhone():"+item.getPhone()+"   mPhoneUtil.getProvidersName():"+mPhoneUtil.getProvidersName()+"   mPhoneUtil.getSendNumber(mPhoneUtil.getProvidersName())[1]:"+mPhoneUtil.getSendNumber(mPhoneUtil.getProvidersName())[1]);
        }
        
    }

}