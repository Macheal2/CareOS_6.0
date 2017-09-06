package com.cappu.keyguard;

import com.android.internal.R;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.app.IBatteryStats;

import android.database.ContentObserver;
import android.content.ContentResolver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.os.Bundle;
import android.os.Message;
import android.os.ServiceManager;

import android.provider.ContactsContract;
import android.database.Cursor;

import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.provider.ContactsContract;

import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.content.BroadcastReceiver;

import android.os.PowerManager;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.KeyguardSecurityContainer.SecurityCallback;

public class CappuBootUnlockView extends FrameLayout implements SecurityCallback{

    private boolean DEBUG = false;
    private String TAG = "CappuBootUnlockView";
    private ViewMediatorCallback mCallback = null;
    private boolean mIsRegisted = false;
    public boolean mBInit = false;
    public CappuLockBaseView view;
    private Context mContext;
    private final Receiver mReceiver = new Receiver();
    private PowerManager mPowerManager;

    public CappuBootUnlockView(Context context) {
         super(context);
    }

    final static int MSG_NEW_SMS_COUNT = 2;
    final static int MSG_NEW_CALL_COUNT = 1;

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NEW_SMS_COUNT:
                boolean sms = (Boolean) msg.obj;
                // Log.i("yzs1111", "sms = " + sms);
                if (sms && !mSavedMode) {
                    if (view != null)
                        view.reflashUnreadMessage(true, mSavedMode);
                    unregisterMessageObserverMms();
                } else {
                    if (view != null)
                        view.reflashUnreadMessage(false, mSavedMode);
                    registerMessageObserverMms();
                }
                break;
            case MSG_NEW_CALL_COUNT:
                boolean call = (Boolean) msg.obj;
                if (call && !mSavedMode) {
                    if (view != null)
                        view.reflashPhoneUnread(true, mSavedMode);
                    unregisterMessageObserverPhone();
                } else {
                    if (view != null)
                        view.reflashPhoneUnread(false, mSavedMode);
                    registerMessageObserverPhone();
                }
                break;
            }
        }
    };

    public class newMmsContentObserver extends ContentObserver {
        private Context ctx;
        private Handler m_handler;
        int newMmsCount = 0;
        int newSmsCount = 0;

        public newMmsContentObserver(Context context, Handler handler) {
            super(handler);
            ctx = context;
            m_handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            newMmsCount = getNewSmsCount();
            newSmsCount = getNewMmsCount();
            m_handler.obtainMessage(MSG_NEW_SMS_COUNT, (newMmsCount + newSmsCount) > 0).sendToTarget();
        }
    }

    public class newPhoneContentObserver extends ContentObserver {

        private Context ctx;
        int missedCallCount = 0;
        private Handler m_handler;
        private static final String TAG = "MissedCallContentObserver";

        public newPhoneContentObserver(Context context, Handler handler) {
            super(handler);
            ctx = context;
            m_handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            missedCallCount = getUnreceivedCallCount();
            m_handler.obtainMessage(MSG_NEW_CALL_COUNT, missedCallCount > 0).sendToTarget();
        }
    }

    private newMmsContentObserver observerMms = new newMmsContentObserver(mContext, myHandler);
    private newPhoneContentObserver observerPhone = new newPhoneContentObserver(mContext, myHandler);

    private void registerMessageObserverMms() {
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, observerMms);
    }

    private void registerMessageObserverPhone() {
        mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, observerPhone);
    }

    private synchronized void unregisterMessageObserverMms() {
        try {
            if (observerMms != null) {
                mContext.getContentResolver().unregisterContentObserver(observerMms);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
    }

    private synchronized void unregisterMessageObserverPhone() {
        try {
            if (observerPhone != null) {
                mContext.getContentResolver().unregisterContentObserver(observerPhone);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
    }

    public CappuBootUnlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        mCallback = viewMediatorCallback;
    }

    public CappuBootUnlockView(Context context, Configuration configuration) {
        super(context);
        mContext = context;

        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mReceiver.init();

        view = new CappuLockScreen(context);// dengying@20160822 lockscreen
        view.setMainHandler(mViewCallback);
        if (view != null) {
            addView(view);
        }

        if (!mIsRegisted) {
            KeyguardUpdateMonitor.getInstance(context).registerCallback(mCappuUpdateMonitorCallback);
            mIsRegisted = true;
        }

        // registerMessageObserverMms();
        // registerMessageObserverPhone();

        myHandler.obtainMessage(MSG_NEW_SMS_COUNT, ((getNewSmsCount() + getNewMmsCount()) > 0)).sendToTarget();
        myHandler.obtainMessage(MSG_NEW_CALL_COUNT, getUnreceivedCallCount() > 0).sendToTarget();

        // view.reflashPhoneUnread(getUnreceivedCallCount() > 0, mSavedMode);
        // view.reflashUnreadMessage((getNewSmsCount() + getNewMmsCount()) > 0,
        // mSavedMode);
    }

    private boolean mSavedMode;

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            mContext.registerReceiver(this, filter);
            updateSaverMode();
        }

        private void updateSaverMode() {
            if (DEBUG)
                Log.i(TAG, " updateSaverMode is called and " + mPowerManager.isPowerSaveMode());
            mSavedMode = mPowerManager.isPowerSaveMode();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
                updateSaverMode();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGING.equals(intent.getAction())) {
                mSavedMode = intent.getBooleanExtra(PowerManager.EXTRA_POWER_SAVE_MODE, false);
            }
        }
    };

    private int getUnreceivedCallCount() {
        ContentResolver localContentResolver = mContext.getContentResolver();
        Uri localUri = CallLog.Calls.CONTENT_URI;
        String[] arrayOfString = new String[1];
        arrayOfString[0] = "_id";
        Cursor localCursor = localContentResolver.query(localUri, arrayOfString, "type=3 and new<>0", null, null);

        int number;
        if (localCursor == null) {
            return -1;
        } else {
            try {
                number = localCursor.getCount();
                if (localCursor != null) {
                    localCursor.close();
                }
                // localCursor.close();
            } finally {
                if (localCursor != null) {
                    localCursor.close();
                }
            }
        }
        return number;
    }

    private int getNewSmsCount() {
        int result = 0;
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://sms"), null, "type = 1 and read = 0", null, null);
        if (csr != null) {
            result = csr.getCount();
            csr.close();
        }
        return result;
    }

    private int getNewMmsCount() {
        int result = 0;
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://mms/inbox"), null, "read = 0", null, null);
        if (csr != null) {
            result = csr.getCount();
            csr.close();
        }
        return result;
    }

    public void onScreenTurnedOff() {
        // TODO Auto-generated method stub
        view.onScreenTurnedOff();
    }

    public void onScreenTurnedOn() {
        // TODO Auto-generated method stub
        view.onScreenTurnedOn();
    }

    public void show() {
        // TODO Auto-generated method stub
        view.show();
    }

    public void verifyUnlock() {
        // TODO Auto-generated method stub
        view.verifyUnlock();
    }

//    @Override
    public void cleanUp() {
        // TODO Auto-generated method stub
        Log.i(TAG, "cleanUp");
        KeyguardUpdateMonitor.getInstance(getContext()).removeCallback(mCappuUpdateMonitorCallback);
        view.cleanUp();
        mIsRegisted = false;
    }

//    @Override
    public long getUserActivityTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void onDestroy() {
        unregisterMessageObserverMms();
        unregisterMessageObserverPhone();
        if (view != null) {
            this.removeView(view);
            view = null;
        }
    }

    private Callback mViewCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Intent intent = null;
            switch (msg.what) {
            case CappuLockBaseView.CAPPU_STATUS_UNLOCK:
                break;
            case CappuLockBaseView.CAPPU_STATUS_DIAL:
                intent = new Intent(Intent.ACTION_DIAL);
                break;
            case CappuLockBaseView.CAPPU_STATUS_MSG:
                intent = new Intent(Intent.ACTION_MAIN);
                intent.setType("vnd.android-dir/mms-sms");
                break;
            case CappuLockBaseView.CAPPU_STATUS_BROWSER:
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
            case CappuLockBaseView.CAPPU_STATUS_CAMERA:
                intent = new Intent();
                intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
                break;

            case CappuLockBaseView.CAPPU_STATUS_MUSIC:
                intent = new Intent();
                intent.setAction("android.intent.action.MUSIC_PLAYER");
                break;

            case CappuLockBaseView.CAPPU_STATUS_CONTENT:
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                break;

            case CappuLockBaseView.CAPPU_STATUS_SCREENON:
                mCallback.userActivity();
                return false;

            default:
                break;
            }
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // mActivityLauncher.launchActivityWithAnimation(intent, false,
                // null, null, null);
            }
            mCallback.dismiss(false);
            return false;
        }
    };

    private KeyguardUpdateMonitorCallback mCappuUpdateMonitorCallback = new CappuKeyguardUpdateMonitorCallback();

    class CappuKeyguardUpdateMonitorCallback extends KeyguardUpdateMonitorCallback {

        @Override
        public void onTimeChanged() {
            super.onTimeChanged();
            if (view != null)
                view.onTimeChanged();
        }

        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            super.onRefreshBatteryInfo(status);
            if (view != null) {
                view.onRefreshBatteryInfo(status);
            }
        }
    }

    public boolean isInit() {
        return mBInit;
    }

    public boolean isAlarmUnlockScreen() {
        return false;
    }

    public void wakeWhenReadyTq(int arg0) {

    }

//    @Override
    protected void onExternalMotionEvent(MotionEvent event) {

    }

    protected void onUserSwitching(boolean switching) {

    }

    protected void onCreateOptions(Bundle options) {

    }

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
