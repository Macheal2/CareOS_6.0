package com.cappu.launcherwin.phoneutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
public class SMSReceiver extends BroadcastReceiver implements UIInterface{

    public final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private final static String TAG = "hehangjun";
    
    private PhoneUtil mPhoneUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(mPhoneUtil == null){
            PhoneUtil.init(context);
            mPhoneUtil = PhoneUtil.getInstance();
        }

        if (intent.getAction().equals(SMS_ACTION)) {// 判断是系统短信
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String providerName = mPhoneUtil.getProvidersName();
                String[] sendNumber = mPhoneUtil.getSendNumber(providerName);
                
                String sender = null;
                String content = null;

                Object[] pdus = (Object[]) bundle.get("pdus");// 通过pdus获得接收到的所有短信消息，获取短信内容；

                SmsMessage[] mMessages = new SmsMessage[pdus.length];// 构建短信对象数组；
                for (int i = 0; i < pdus.length; i++) {
                    mMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);// 获取单条短信内容，以pdu格式存,并生成短信对象；
                }
                for (SmsMessage mMessage : mMessages) {
                    sender = mMessage.getDisplayOriginatingAddress();
                    content = mMessage.getMessageBody();
                    
                    if(content != null && content.indexOf("元") != -1 && sender.equals(sendNumber[1].toString()) && mPhoneUtil.getInquiryType()){
                        Intent it = new Intent();
                        it.setClass(context, PhoneUtilActivity.class);
                        it.putExtra("content", content);
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        
                        //Log.i(TAG, "短信内容 content: " + content);
                        context.startActivity(it);
                        this.abortBroadcast();// 不再往下传递消息
                        //Log.e(TAG, "-------------SmsReceiver sender=" + sender + " AES content=" + content);
                    }

                    //Log.e(TAG, "***********SmsReceiver sender=" + sender + " AES content=" + content);
                    
                }
            }
        }
    }

    @Override
    public void updateUi(MessageItem item) {
        // TODO Auto-generated method stub
        
    }

    
    
    /*
    public static final String TAG = "HHJ";
    // android.provider.Telephony.Sms.Intents

    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = getMessagesFromIntent(intent);
            for (SmsMessage message : messages)           {
                Log.i(TAG, message.getOriginatingAddress() + " : " +
                message.getDisplayOriginatingAddress() + " : " +
                message.getDisplayMessageBody() + " : " +
                message.getTimestampMillis());
            }
        }
    }

    public final SmsMessage[] getMessagesFromIntent(Intent intent)  {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }

        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        this.abortBroadcast();//不再往下传递消息
        return msgs;
        
    }*/

}
