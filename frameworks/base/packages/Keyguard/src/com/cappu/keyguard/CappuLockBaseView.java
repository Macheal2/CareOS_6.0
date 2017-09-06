package com.cappu.keyguard;

import com.android.internal.widget.LockPatternUtils;
import android.os.Handler.Callback;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.Calendar;
import java.util.Date;
import com.android.keyguard.KeyguardUpdateMonitor;
import java.util.Calendar;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class CappuLockBaseView extends RelativeLayout {

    private static final String CAPPU_TAG = "cappu";
    private static final boolean CAPPU_DEBUG = true;
    private Context mContext;

    private final static String M12 = "h:mm";
    private final static String M24 = "kk:mm";

    public final static int CAPPU_STATUS_UNLOCK = 0;
    public final static int CAPPU_STATUS_DIAL = 1;
    public final static int CAPPU_STATUS_MSG = 2;
    public final static int CAPPU_STATUS_BROWSER = 3;
    public final static int CAPPU_STATUS_CAMERA = 4;
    public final static int CAPPU_STATUS_MUSIC = 5;
    public final static int CAPPU_STATUS_CONTENT = 6;
    public final static int CAPPU_STATUS_SCREENON = 100;
    public final static int CAPPU_STATUS_HIDE_VIEW = 900;
    public final static int CAPPU_STATUS_SHOW_VIEW = 901;
    
    public final static String CAPPU_UNREAD_DIAL = "com.android.dialer";
    public final static String CAPPU_UNREAD_MSG = "com.android.mms";
    public final static String CAPPU_UNREAD_EMAIL = "com.android.email";
    public final static String CAPPU_UNREAD_CALENDAR = "com.android.calendar";
    public final static String CAPPU_UNREAD_RCS = "com.orangelabs.rcs";
    public final static String CAPPU_UNREAD_CELLBROAD = "com.android.cellbroadcastreceiver";
    public final static String CAPPU_UNREAD_MTKCELLBROAD = "com.mediatek.cellbroadcastreceiver";
    
    public CappuLockBaseView(Context context) {
        super(context);
		mContext = context;
    }
    
    protected void onScreenTurnedOff() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "onScreenTurnedOff is called");
    }
    
    protected void onScreenTurnedOn() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "onScreenTurnedOn is called");
    }
    
    protected void show() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "show is called");
    }
    
    protected void verifyUnlock() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "verifyUnlock is called");
    }
    
    protected void cleanUp() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "cleanUp is called");
    }
    
    protected void onPause() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "onPause is called");
    }

    protected void onResume(int reason) {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "onResume is called and reasong = " + reason);
    }
    
    protected void reset() {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "reset is called");
    }
    
    protected void onKeyguardVisibilityChanged(boolean showing){
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)
			Log.i(CAPPU_TAG, "onKeyguardVisibilityChanged is called and showing = " + showing);
    }
    
    protected void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batteryStatus, int level){
        Log.i(CAPPU_TAG, "onRefreshBatteryInfo is called and level = " + level);
    } 

    public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batteryStatus){
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)
			Log.i(CAPPU_TAG, "onRefreshBatteryInfo is called and batteryStatus = " + batteryStatus);
      
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = mContext.registerReceiver(null, ifilter);
        //你可以读到充电状态,如果在充电，可以读到是usb还是交流电
        //int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        //mIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        //        status == BatteryManager.BATTERY_STATUS_FULL;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);        
        //mScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        this.onRefreshBatteryInfo(batteryStatus, level);
   }  

   protected void onTimeChanged(){
        // TODO Auto-generated method stub
        /*
		Calendar now = Calendar.getInstance();
        Calendar mDummyDate = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        Date date = mDummyDate.getTime();
		String dummyDate = DateFormat.getTimeFormat(mContext).format(date);
        */

        String format =  android.text.format.DateFormat.is24HourFormat(mContext) ? M24 : M12;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        CharSequence newTime = DateFormat.format(format, calendar);
        Log.i(CAPPU_TAG, "onTimeChanged is called and newtime = " + newTime);
        this.onTimeChanged(newTime.toString());
    }
    
    protected void onTimeChanged(String dateFormat){
        // TODO Auto-generated method stub
    }
        
    private static CharSequence concatenate(CharSequence plmn, CharSequence spn) {
        return (plmn != null) ? plmn : ((spn != null) ? spn : "");
    }
       
    protected void updateUnread(String PackageName, int unreadNum){
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "updateUnread is called");
    }
    
    protected void onRefreshCarrierInfo(int maxSimId, int simId, String simInfo, String DefaultPlmn) {
        // TODO Auto-generated method stub
        if(CAPPU_DEBUG)Log.i(CAPPU_TAG, "onRefreshCarrierInfo is called");
	}
    
    protected void setMainHandler(Callback callback){
        
    }

    public void reflashPhoneUnread(boolean visible, boolean savedMode){

    }

    public void reflashUnreadMessage(boolean visible, boolean savedMode){

    }

}
