package com.magcomm.settings;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.lang.reflect.Method;
import android.util.Log;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.app.Service;

/*
*zhaogangzhu add for reveived intent to chang mobile_data on/off
*/
public class CareGPRSIntentReceiver extends BroadcastReceiver {

	   @Override
	   public void onReceive(Context context, Intent intent) {
	     	ConnectivityManager   mConnectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
			TelephonyManager      mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

			String action = intent.getAction();
			boolean wifiConn = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
			boolean isGprs = mTelephonyManager.getDataEnabled();
			boolean	isSECRETEnable = Settings.Global.getInt(context.getContentResolver(),"enable_data_conn_secretcode", 1)==1;
			boolean	isDataon = Settings.Global.getInt(context.getContentResolver(),Settings.Global.MOBILE_DATA, 1) == 1;
			Log.i("zhaogangzhu","received ACTION GPRS intent on/off");

			if("android.net.conn.SETDATA_OFF".equals(action)){

				mTelephonyManager.setDataEnabled(false);
				intent = new Intent("android.net.conn.CONNECTIVITY_CANCEL");
				context.sendBroadcast(intent);
				Log.i("zhaogangzhu","receiver SETDATA_OFF intent off");				
			}else if("android.net.conn.SETDATA_ON".equals(action)){
				mTelephonyManager.setDataEnabled(true);
				if(!isSECRETEnable){
				intent = new Intent("android.net.conn.CONNECTIVITY_CHANGEING");
				context.sendBroadcast(intent);
					}
				Log.i("zhaogangzhu","receiver SETDATA_ON intent on");
			}
		}
 
}


