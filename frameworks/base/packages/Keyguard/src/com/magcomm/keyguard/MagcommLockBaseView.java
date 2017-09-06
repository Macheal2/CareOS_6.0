package com.magcomm.keyguard;

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

public class MagcommLockBaseView extends RelativeLayout {

    private static final String MAGCOMM_TAG = "magcomm";
    private static final boolean MAGCOMM_DEBUG = true;
    private Context mContext;

    public final static int MAGCOMM_STATUS_UNLOCK = 0;
    public final static int MAGCOMM_STATUS_DIAL = 1;
    public final static int MAGCOMM_STATUS_MSG = 2;
    public final static int MAGCOMM_STATUS_BROWSER = 3;
    public final static int MAGCOMM_STATUS_CAMERA = 4;
    public final static int MAGCOMM_STATUS_MUSIC = 5;
    public final static int MAGCOMM_STATUS_CONTENT = 6;
    public final static int MAGCOMM_STATUS_SCREENON = 100;
    public final static int MAGCOMM_STATUS_HIDE_VIEW = 900;
    public final static int MAGCOMM_STATUS_SHOW_VIEW = 901;
    
    public final static String MAGCOMM_UNREAD_DIAL = "com.android.dialer";
    public final static String MAGCOMM_UNREAD_MSG = "com.android.mms";
    public final static String MAGCOMM_UNREAD_EMAIL = "com.android.email";
    public final static String MAGCOMM_UNREAD_CALENDAR = "com.android.calendar";
    public final static String MAGCOMM_UNREAD_RCS = "com.orangelabs.rcs";
    public final static String MAGCOMM_UNREAD_CELLBROAD = "com.android.cellbroadcastreceiver";
    public final static String MAGCOMM_UNREAD_MTKCELLBROAD = "com.mediatek.cellbroadcastreceiver";
    
    public MagcommLockBaseView(Context context) {
        super(context);
	    mContext = context;
    }
    
    protected void onScreenTurnedOff() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onScreenTurnedOff is called");
    }
    
    protected void onScreenTurnedOn() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onScreenTurnedOn is called");
    }
    
    protected void show() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "show is called");
    }
    
    protected void verifyUnlock() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "verifyUnlock is called");
    }
    
    protected void cleanUp() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "cleanUp is called");
    }
    
    protected void onPause() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onPause is called");
    }

    protected void onResume(int reason) {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onResume is called and reasong = " + reason);
    }
    
    protected void reset() {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "reset is called");
    }
    
    protected void onKeyguardVisibilityChanged(boolean showing){
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)
			Log.i(MAGCOMM_TAG, "onKeyguardVisibilityChanged is called and showing = " + showing);
    }
   
    protected void onTimeChanged(){
        // TODO Auto-generated method stub
        //this.onAlarmChanged(refreshAlarmStatus());

	    Calendar now = Calendar.getInstance();
        Calendar mDummyDate = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        Date date = mDummyDate.getTime();

		String dummyDate = DateFormat.getTimeFormat(mContext).format(date);
        this.onTimeChanged(dummyDate);
    }
    
    protected void onTimeChanged(String dateFormat){
        // TODO Auto-generated method stub
    }
    
    protected void onAlarmChanged(String AlarmString){
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onAlarmChanged is called and AlarmString = " + AlarmString);
    }
    
    private static CharSequence concatenate(CharSequence plmn, CharSequence spn) {
        return (plmn != null) ? plmn : ((spn != null) ? spn : "");
    }
    
    protected void onRefreshCarrierInfo(CharSequence plmn, CharSequence DefaultPlmn, CharSequence spn, int simId) {
        
    }
    
    public void reflashPhoneUnread(int number){

    }

    public void reflashUnreadMessage(int number){

    }

    protected void onRefreshCarrierInfo(int maxSimId, int simId, String simInfo, String DefaultPlmn) {
        // TODO Auto-generated method stub
        if(MAGCOMM_DEBUG)Log.i(MAGCOMM_TAG, "onRefreshCarrierInfo is called");
	}
    
    protected void setMainHandler(Callback callback){
        
    }
}
