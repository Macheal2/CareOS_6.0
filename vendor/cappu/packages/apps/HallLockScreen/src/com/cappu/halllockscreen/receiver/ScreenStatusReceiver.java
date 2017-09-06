package com.cappu.halllockscreen.receiver;

import java.util.Calendar;

import com.cappu.halllockscreen.DigitalClock;
import com.cappu.halllockscreen.HallLockScreenActivity;
import com.cappu.halllockscreen.util.DateUtils;

import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStatusReceiver extends BroadcastReceiver {
	// 关闭霍尔窗口广播
	private static String LID_OFF = "magcomm.action.LID_OFF";
	// 打开霍尔窗口广播
	private static String LID_ON = "magcomm.action.LID_ON";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("AAAAA", "intent.getAction()="+intent.getAction());
		// 盖上盖子，打开霍尔窗口广播
		if (LID_ON.equals(intent.getAction())) {
			Log.i("AAAAA", "apk_start");
			if (HallLockScreenActivity.get_mg_instance() == null) {
				Intent wealthIntent = new Intent();
				wealthIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				wealthIntent.setClass(context, HallLockScreenActivity.class);
				context.startActivity(wealthIntent);
			}

		}
		// 打开盖子，关闭霍尔窗口广播
		else if (LID_OFF.equals(intent.getAction())) {
			Log.i("AAAAA", "apk_finish");
			if (HallLockScreenActivity.get_mg_instance() != null) {
				HallLockScreenActivity.get_mg_instance().finshWindowLife();
			}
		}else if(Intent.ACTION_TIME_TICK.equals(intent.getAction())){
			 Calendar cal = Calendar.getInstance();  
	         int min = cal.get(Calendar.MINUTE); 
	         if(min==0){
	        	 DigitalClock.mDateString = DigitalClock.getDateString();
	        	 DigitalClock.mWeekString = DigitalClock.getWeek();
	        	 DigitalClock.mLunarString = DateUtils.getLunarMonth()
						+ DateUtils.getLunarDay();
	         }
		}
	}
}
