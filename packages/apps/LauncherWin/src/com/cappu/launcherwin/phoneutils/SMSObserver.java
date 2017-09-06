package com.cappu.launcherwin.phoneutils;

import android.content.ContentResolver;

import android.database.ContentObserver;

import android.database.Cursor;

import android.os.Handler;

import android.os.Message;

import android.util.Log;

public class SMSObserver extends ContentObserver {

    public static final String TAG = "hehangjun";
    private static final String[] PROJECTION = new String[] { SMS._ID,// 0
            SMS.TYPE,// 1
            SMS.ADDRESS,// 2
            SMS.BODY,// 3
            SMS.DATE,// 4
            SMS.THREAD_ID,// 5
            SMS.READ,// 6
            SMS.PROTOCOL // 7
    };

    private static final String SELECTION = SMS._ID + " > %s" +
    // " and " + SMS.PROTOCOL + " = null" +
    // " or " + SMS.PROTOCOL + " = " + SMS.PROTOCOL_SMS + ")" +
            " and (" + SMS.TYPE + " = " + SMS.MESSAGE_TYPE_INBOX + " or " + SMS.TYPE + " = " + SMS.MESSAGE_TYPE_SENT + ")";

    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_TYPE = 1;
    private static final int COLUMN_INDEX_PHONE = 2;
    private static final int COLUMN_INDEX_BODY = 3;
    private static final int COLUMN_INDEX_PROTOCOL = 7;
    private static final int MAX_NUMS = 10;
    private static int MAX_ID = 0;
    private ContentResolver mResolver;
    private Handler mHandler;
    
    private PhoneUtil mPhoneUtil;

    public SMSObserver(ContentResolver contentResolver, Handler handler,PhoneUtil phoneUtil) {
        super(handler);
        this.mResolver = contentResolver;
        this.mHandler = handler;
        this.mPhoneUtil = phoneUtil;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = mResolver.query(SMS.CONTENT_URI, PROJECTION, String.format(SELECTION, MAX_ID), null, null);
        int id, type, protocol;
        String phone, body;
        Message message;
        MessageItem item;
        int iter = 0;
        boolean hasDone = false;
        
        String providerName = mPhoneUtil.getProvidersName();
        String[] sendNumber = mPhoneUtil.getSendNumber(providerName);
        
        Log.i(TAG, "onChange : " + selfChange + "; " + MAX_ID + "; " + SELECTION +"   cursor:"+cursor.getCount());
        while (cursor.moveToNext()) {
            id = cursor.getInt(COLUMN_INDEX_ID);
            type = cursor.getInt(COLUMN_INDEX_TYPE);
            phone = cursor.getString(COLUMN_INDEX_PHONE);
            body = cursor.getString(COLUMN_INDEX_BODY);
            protocol = cursor.getInt(COLUMN_INDEX_PROTOCOL);
            
            Log.i(TAG, "onChange id: " + id+"  type:"+type+"  phone: "+phone+"   body:"+body+"   protocol:"+protocol);
            if (hasDone) {
                MAX_ID = id;
                break;
            }
            if (protocol == SMS.PROTOCOL_SMS && body != null && phone.equals(sendNumber[1].toString())/*&& body.startsWith(SMS.FILTER)*/) {
                hasDone = true;
                item = new MessageItem();
                item.setId(id);
                item.setType(type);
                item.setPhone(phone);
                item.setBody(body);
                item.setProtocol(protocol);
                message = new Message();
                message.obj = item;
                
                Log.i(TAG, "onChange item: " + item.toString());
                mHandler.sendMessage(message);
            } else {
                if (id > MAX_ID)
                    MAX_ID = id;
            }
            if (iter > MAX_NUMS)
                break;
            iter++;
        }
        cursor.close();
    }
}