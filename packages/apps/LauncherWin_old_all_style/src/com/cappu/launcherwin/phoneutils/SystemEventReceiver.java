package com.cappu.launcherwin.phoneutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.os.Handler;
//import android.os.Message;
//import android.telephony.gsm.SmsManager;
import android.util.Log;

public class SystemEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(Globals.IMICHAT_SERVICE));
        } else if (intent.getAction().equals(Globals.ACTION_SEND_SMS)) {
            MessageItem mItem = (MessageItem) intent.getSerializableExtra(Globals.EXTRA_SMS_DATA);
            if (mItem != null && mItem.getPhone() != null && mItem.getBody() != null) {
                //SmsManager.getDefault().sendTextMessage(mItem.getPhone(), null, mItem.getBody(), null, null);
                // new Thread(mTasks).start();
                
                //Log.i("hehangjun", "mItem.getBody():"+mItem.getBody()+"   mItem.getPhone():"+mItem.getPhone());
            }
        }
    }
}
