package com.magcomm.keyguard;

import com.android.internal.R;
import com.android.internal.widget.LockPatternUtils;
import android.app.Application;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.os.Handler;

import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.KeyguardSecurityContainer.SecurityCallback;
	
public class MagcommBootUnlockView extends FrameLayout implements SecurityCallback{

    private boolean DEBUG = true;
    private String TAG = "MagcommBootUnlockView";
    private ViewMediatorCallback mCallback = null;
    private boolean mIsRegisted = false;
    public boolean mBInit = false;
    public MagcommLockBaseView mView;

    private Context mContext;
    private int mPhoneUnread, mMessageUnread;

    public MagcommBootUnlockView(Context context) {
        super(context);
    }

    public MagcommBootUnlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }    

    private ContentObserver newMmsContentObserver = new ContentObserver(new Handler()) { 
        public void onChange(boolean selfChange) { 
            mMessageUnread = getNewSmsCount() + getNewMmsCount();
            Log.i(TAG, "mMessageUnread = " + mMessageUnread);
            if(mView != null){
                mView.reflashUnreadMessage(mMessageUnread);
            }
        } 
    }; 

    private void registerMessageObserver() { 
        unregisterMessageObserver();
		//mContext.getContentResolver().registerContentObserver(
		//		Uri.parse("content://sms"), true, newMmsContentObserver);
		

        mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, 
        newMmsContentObserver); 
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, 
        newMmsContentObserver); 
	
		mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true,
				newPhoneContentObserver); 
    } 

    private synchronized void unregisterMessageObserver() { 
        try { 
            if (newMmsContentObserver != null) { 
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver); 
            } 
            if (newMmsContentObserver != null) { 
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver); 
            } 

            /*if (newMmsContentObserver != null) { 
               mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver); 
            }*/ 

            if (newPhoneContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newPhoneContentObserver);
            }
        } catch (Exception e) { 
                Log.e(TAG, "unregisterObserver fail"); 
        } 
    }

    private int getNewSmsCount() { 
        int result = 0; 
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://sms"), null, 
            "type = 1 and read = 0", null, null); 
        if (csr != null) { 
            result = csr.getCount(); 
            csr.close(); 
        } 
        return result; 
    } 

    private int getNewMmsCount() { 
        int result = 0; 
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://mms/inbox"), 
        null, "read = 0", null, null); 
        if (csr != null) { 
            result = csr.getCount(); 
            csr.close(); 
        } 
        return result; 
    }

    public int getUnreceivedCallCount() {
		ContentResolver localContentResolver = mContext.getContentResolver();
		Uri localUri = CallLog.Calls.CONTENT_URI;
		String[] arrayOfString = new String[1];
		arrayOfString[0] = "_id";
		Cursor localCursor = localContentResolver.query(localUri,
				arrayOfString, "type=3 and new<>0", null, null);

		int number;
		if (localCursor == null) {
			return -1;
		} else {
			try {
				number = localCursor.getCount();
                if(localCursor != null){
				    localCursor.close();
                }
				//localCursor.close();
			} finally {
                if(localCursor != null){
    				localCursor.close();
                }
			}
		}
		return number;
	}

    private ContentObserver newPhoneContentObserver = new ContentObserver(
			new Handler()) {
		public void onChange(boolean selfChange) {
			int phoneUnread = getUnreceivedCallCount();
			//int phoneUnread2 = readMissCall();
            if(mView != null){
    			mView.reflashPhoneUnread(phoneUnread);
            }
		}
	};
	
    public MagcommBootUnlockView(Context context, Configuration configuration) {
        super(context);
        mContext = context;
        mView = new MagcommLockscreen(context);		
        mView.setMainHandler(mViewCallback); 
        if (mView != null) {
            addView(mView);
        }
        registerMessageObserver();
        mView.reflashPhoneUnread(getUnreceivedCallCount());
        mView.reflashUnreadMessage(getNewSmsCount());  
    }

    public void show() {
        // TODO Auto-generated method stub
        if(mView != null){
            mView.show();
        }
    } 

	public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
		mCallback= viewMediatorCallback;
	}

	public void onDestroy() { 
	    if(mView != null){
	        this.removeView(mView);
	        mView = null;
	    }        
    } 

    private Callback mViewCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Intent intent = null;
            switch (msg.what) {
            case MagcommLockBaseView.MAGCOMM_STATUS_UNLOCK:
                break;
            case MagcommLockBaseView.MAGCOMM_STATUS_DIAL:
                //modify for liukun 20170831
                intent = new Intent();
                intent.setClassName("com.android.dialer","com.android.dialer.calllog.CallLogActivity");
                intent.setData(Uri.parse("tel:"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
            case MagcommLockBaseView.MAGCOMM_STATUS_MSG:
                //modify for liukun 20170831
                intent = new Intent();
                intent.setClassName("com.android.mms","com.android.mms.ui.BootActivity");
                intent.setType("vnd.android-dir/mms-sms");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
            case MagcommLockBaseView.MAGCOMM_STATUS_BROWSER:
                Bundle bundle = msg.getData();
                String string = bundle.getString("url");
                if (string != null) {
                    Uri uri = Uri.parse(string);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                } else {
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                }
                break;
            case MagcommLockBaseView.MAGCOMM_STATUS_CAMERA:
                intent = new Intent();
                intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
                break;

            case MagcommLockBaseView.MAGCOMM_STATUS_MUSIC:
                intent = new Intent();
                intent.setAction("android.intent.action.MUSIC_PLAYER");
                break;

            case MagcommLockBaseView.MAGCOMM_STATUS_CONTENT:
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                break;

            case MagcommLockBaseView.MAGCOMM_STATUS_SCREENON:
                mCallback.userActivity();
                return false;

            default:
                break;
            }
            mCallback.dismiss(false);
            return false;
        }
    };

//	@Override
    public void cleanUp() {
        // TODO Auto-generated method stub
        Log.i(TAG,"cleanUp");
        KeyguardUpdateMonitor.getInstance(getContext()).removeCallback(mMagcommUpdateMonitorCallback);
        if(mView != null){
            mView.cleanUp();
        }
        mIsRegisted = false;
    }

//	@Override
    public long getUserActivityTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

//	@Override
	protected void onExternalMotionEvent(MotionEvent event){

	}
	protected void onUserSwitching(boolean switching){}
	
    protected void onCreateOptions(Bundle options){

	}

	private KeyguardUpdateMonitorCallback mMagcommUpdateMonitorCallback = new KeyguardUpdateMonitorCallback();

    @Override
    public boolean dismiss(boolean authenticated) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void userActivity() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSecurityModeChanged(SecurityMode securityMode, boolean needsInput) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finish(boolean strongAuth) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOnDismissAction(OnDismissAction action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasOnDismissAction() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateNavbarStatus() {
        // TODO Auto-generated method stub
        
    }
}
