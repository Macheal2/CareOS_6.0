package com.magcomm.keyguard;

import android.os.Handler.Callback;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.provider.Settings;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;
import com.cappu.pictorial.CappuPictorialTool;
import com.cappu.pictorial.ICappuPictorial;

import java.util.Calendar;
import android.graphics.drawable.AnimationDrawable;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Typeface;

public class MagcommLockscreen extends MagcommLockBaseView implements OnTouchListener {
    public static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";
    private static final String MAGCOMM_FZHJ_FONT_FILE = "/system/fonts/Cappu-font-fzhj.ttf";
    public static final String CAPPU_BOLD_FONT_STYLE = "/system/fonts/CappuPingFangBold.ttf";
    public static final String CAPPU_MED_FONT_STYLE = "/system/fonts/CappuPingFangMedium.ttf";
    public static final String CAPPU_REGULAR_FONT_STYLE = "/system/fonts/CappuPingFangRegular.ttf";

    private Callback mainHandler = null;
    private Context mContext;

    private MagcommClockViewType mMagcommClockViewType;
    private Animation alpha;
    private boolean misAnimationStatus;
    private final static String TAG = "MagcommLockscreen";

    private View mBackground, mParentView;
    private RelativeLayout mScrollView, mLeftBtn, mRightBtn;
    private ImageView mMidBtn;
    private TextView mNotice;
    private boolean isMoving = false;

    private MagcommUnreadView mPhoneView, mMsgView;
    private ImageView mNarrowView;
    private AnimationDrawable anim;

    private GestureDetector mGestureDetector;
    private final int EFFECTIVE_DISTANCE;
    private static final int EFFECTIVE_SPEED = 3000;

    private int mMasterStreamType;
    private AudioManager mAudioManager;
    private SoundPool mLockSounds;
    private int mLockSoundId;
    private int mUnlockSoundId;
    private int mLockSoundStreamId;
    private ICappuPictorial mIsService;// by hmq 20161108 Modify for cappu pictorial

    private String[] mDateMonth, mDateDay;
    
    private View mCurrentTouchView;
    private int mUnreadMsg, mUnreadCall;
    
    /**
     * The volume applied to the lock/unlock sounds.
     */
    private final float mLockSoundVolume;

    private boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public MagcommLockscreen(Context context) {
        super(context);
        mContext = context;
        mGestureDetector = new GestureDetector(context, new MagcommGestureListener());

        final LayoutInflater inflater = LayoutInflater.from(mContext);

        View rootView = inflater.inflate(R.layout.magcomm_layout_screen, this, true);

        mBackground = (RelativeLayout) rootView.findViewById(R.id.container_background);

        mParentView = (View) mBackground.getParent();
        mScrollView = (RelativeLayout) mBackground.findViewById(R.id.scrollview);
        mLeftBtn = (RelativeLayout) mBackground.findViewById(R.id.magcomm_left);
        mMidBtn = (ImageView) mBackground.findViewById(R.id.magcomm_unlock);
        mRightBtn = (RelativeLayout) mBackground.findViewById(R.id.magcomm_right);
        
        mParentView.setOnTouchListener(this);
        //mParentView.setLongClickable(true);
        mLeftBtn.setOnTouchListener(this);
        mMidBtn.setOnTouchListener(this);
        mRightBtn.setOnTouchListener(this);
        
        mNotice = (TextView) mBackground.findViewById(R.id.magcomm_notice);
        mNotice.setTypeface(getClockTypeface());
        
        mNarrowView = (ImageView) rootView.findViewById(R.id.magcomm_narrow);
        mNarrowView.setBackgroundResource(R.anim.magcomm_narrow_anim);

        if (anim == null) {
            anim = (AnimationDrawable) mNarrowView.getBackground();
            anim.start();
        } else {
            anim.start();
        }
        
        mMagcommClockViewType = (MagcommClockViewType)rootView.findViewById(R.id.magcomm_clock_view);
        mPhoneView = (MagcommUnreadView) rootView.findViewById(R.id.magcomm_left_unread);
        mMsgView = (MagcommUnreadView) rootView.findViewById(R.id.magcomm_right_unread);
        
        anim = (AnimationDrawable) mNarrowView.getBackground();
        final ContentResolver cr = mContext.getContentResolver();
        mLockSounds = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        String soundPath = Settings.Global.getString(cr, Settings.Global.LOCK_SOUND);
        if (soundPath != null) {
            mLockSoundId = mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || mLockSoundId == 0) {
            Log.w(TAG, "failed to load lock sound from " + soundPath);
        }
        soundPath = Settings.Global.getString(cr, Settings.Global.UNLOCK_SOUND);
        if (soundPath != null) {
            mUnlockSoundId = mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || mUnlockSoundId == 0) {
            Log.w(TAG, "failed to load unlock sound from " + soundPath);
        }
        int lockSoundDefaultAttenuation = context.getResources().getInteger(com.android.internal.R.integer.config_lockSoundVolumeDb);
        mLockSoundVolume = (float) Math.pow(10, (float) lockSoundDefaultAttenuation / 20);
        EFFECTIVE_DISTANCE = getScreenHeight(context) / 3;

        /* begin: edit by hmq 20161108 Modify for cappu pictorial */
        if (SystemProperties.getInt("ro.com.cappu.keyguard.pictorial", 0) == 1) {
            Log.e("hmq", "MagcommLockscreen  ro.com.cappu.keyguard.pictorial= 1");
            mIsService = CappuPictorialTool.getCappuPictorialService();
            if (mIsService == null) {
                mIsService = CappuPictorialTool.setBindService(mContext);
            }
            Log.e("hmq", "MagcommLockscreen mIsService?=null " + (mIsService == null));
            int mode = Settings.System.getInt(context.getContentResolver(), Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL, Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_ON);

            if (mode == Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_ON) {
                try {
                    Log.e("hmq", "MagcommLockscreen connection mIsService != null");
                    if (mIsService != null) {
                        // 调用远程服务中的方法
                        long currentTime = System.currentTimeMillis();
                        String path = mIsService.getCappuPictorialPath();
                        Log.e("hmq", "MagcommLockscreen connection path=" + path);
                        Bitmap bp = CappuPictorialTool.getDiskBitmap(path);
                        Log.e("hmq", "MagcommLockscreen connection bp?=null" + (bp == null));
                        if (bp != null) {
                            mBackground.setBackground(CappuPictorialTool.bitmap2Drawable(bp));
                        }
                        Log.e("hmq", "MagcommLockscreen connection overtime=" + (System.currentTimeMillis() - currentTime));
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    mBackground.setBackgroundResource(R.drawable.magcomm_lockscreen_background);
                    e.printStackTrace();
                    Log.e("hmq", "MagcommLockscreen connection error=" + e.getMessage());
                }
            } else {
                mBackground.setBackgroundResource(R.drawable.magcomm_lockscreen_background);
            }
        } else {
            Log.e("hmq", "MagcommLockscreen  ro.com.cappu.keyguard.pictorial= 0");
            mBackground.setBackgroundResource(R.drawable.magcomm_lockscreen_background);
        }
        /* end: edit by hmq 20161108 Modify for cappu pictorial */
    }

    private String getCurrentDayOfWeek() {
        Resources mResources = mContext.getResources();
        String[] weeks = mResources.getStringArray(R.array.array_week);
        return weeks[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    public void reflashPhoneUnread(int number) {
        mUnreadCall = number;
        if (number > 0) {
            mLeftBtn.setVisibility(View.VISIBLE);
            mPhoneView.setNumber(number);
        } else {
            mLeftBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void reflashUnreadMessage(int number) {
        mUnreadMsg = number;
        if (number > 0) {
            mRightBtn.setVisibility(View.VISIBLE);
            mMsgView.setNumber(number);
        } else {
            mRightBtn.setVisibility(View.INVISIBLE);
        }
    }

    protected void onTimeChanged(String dateFormat) {
        // TODO Auto-generated method stub
        super.onTimeChanged(dateFormat);
        mMagcommClockViewType.updateTime();
    }

    @Override
    protected void onAlarmChanged(String AlarmString) {
        // TODO Auto-generated method stub
        super.onAlarmChanged(AlarmString);

    }

    @Override
    protected void onRefreshCarrierInfo(int maxSimId, int simId, String simInfo, String defaultInfo) {
        super.onRefreshCarrierInfo(maxSimId, simId, simInfo, defaultInfo);
        // TODO Auto-generated method stub
    }

    public static final int BATTERY_STATUS_UNKNOWN = 1;
    public static final int BATTERY_STATUS_CHARGING = 2;
    public static final int BATTERY_STATUS_DISCHARGING = 3;
    public static final int BATTERY_STATUS_NOT_CHARGING = 4;
    public static final int BATTERY_STATUS_FULL = 5;

    public static final int BATTERY_HEALTH_UNKNOWN = 1;
    public static final int BATTERY_HEALTH_GOOD = 2;
    public static final int BATTERY_HEALTH_OVERHEAT = 3;
    public static final int BATTERY_HEALTH_DEAD = 4;
    public static final int BATTERY_HEALTH_OVER_VOLTAGE = 5;
    public static final int BATTERY_HEALTH_UNSPECIFIED_FAILURE = 6;
    public static final int BATTERY_HEALTH_COLD = 7;

    public static final int BATTERY_PLUGGED_AC = 1;
    public static final int BATTERY_PLUGGED_USB = 2;
    public static final int BATTERY_PLUGGED_WIRELESS = 4;

    boolean mShowingBatteryInfo[];

    protected void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
        // TODO Auto-generated method stub
        // super.onRefreshBatteryInfo(status);
        // final int idx = status.index;//yzs del
        String BatteryString = "";

        if (status.isPluggedIn()) {
            switch (status.plugged) {
            case BATTERY_PLUGGED_AC:
                BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_charger);
                break;
            case BATTERY_PLUGGED_USB:
                BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_usb);
                break;
            case BATTERY_PLUGGED_WIRELESS:
                BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_connect);
                break;
            }
            if (status.isCharged()) {
                BatteryString = BatteryString + ";" + mContext.getString(R.string.magcomm_keyguard_charge_full);
            } else {
                BatteryString = BatteryString + ";" + status.level + "\u0025";
            }
        } else if (status.isBatteryLow()) {
            BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_fell);
        } else if (status.isCharged() && (status.level == 100)) {
            BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_full);
        } else {
            BatteryString = mContext.getString(R.string.magcomm_keyguard_charge_value) + status.level + "\u0025";
        }
        // BatteryString = BatteryString + status.health;
        switch (status.health) {
        case BATTERY_HEALTH_UNKNOWN:
            BatteryString = BatteryString + mContext.getString(R.string.magcomm_keyguard_charge_health) + mContext.getString(R.string.magcomm_keyguard_charge_health_unknow);
            break;
        case BATTERY_HEALTH_GOOD:
            BatteryString = BatteryString + mContext.getString(R.string.magcomm_keyguard_charge_health) + mContext.getString(R.string.magcomm_keyguard_charge_health_good);
            break;
        case BATTERY_HEALTH_OVERHEAT:
            BatteryString = BatteryString + mContext.getString(R.string.magcomm_keyguard_charge_health) + mContext.getString(R.string.magcomm_keyguard_charge_health_subhealthy);
            break;
        case BATTERY_HEALTH_DEAD:
            BatteryString = BatteryString + mContext.getString(R.string.magcomm_keyguard_charge_health) + mContext.getString(R.string.magcomm_keyguard_charge_health_bad);
            break;
        }
        // mBatteryInfo.setText(BatteryString);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method STUB
        super.onPause();
        anim.stop();

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (view.getId() == mLeftBtn.getId()){
                mLeftBtn.setVisibility(View.VISIBLE);
                mMidBtn.setVisibility(View.INVISIBLE);
                mRightBtn.setVisibility(View.INVISIBLE);
                mLeftBtn.setPressed(true);
            }else if (view.getId() == mRightBtn.getId()){
                mLeftBtn.setVisibility(View.INVISIBLE);
                mMidBtn.setVisibility(View.INVISIBLE);
                mRightBtn.setVisibility(View.VISIBLE);
                mRightBtn.setPressed(true);
            }else{
                mLeftBtn.setVisibility(View.INVISIBLE);
                mMidBtn.setVisibility(View.VISIBLE);
                mRightBtn.setVisibility(View.INVISIBLE);
                mMidBtn.setPressed(true);
            }
            mCurrentTouchView = view;
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            int scrollY = mScrollView.getScrollY();
            mScrollView.scrollTo(0, 0);
            mScrollView.setAlpha(1.0f);
            if (EFFECTIVE_DISTANCE <= scrollY) {
                if (!isMoving) {
                    startTranslate(mCurrentTouchView, 550, -scrollY, -mScrollView.getHeight(), true);
                }
            } else {
                if (!isMoving) {
                    startTranslate(mCurrentTouchView, 400, -scrollY, 0, false);
                }
            }
            mLeftBtn.setPressed(false);
            mMidBtn.setPressed(false);
            mRightBtn.setPressed(false);
//            mLeftBtn.setVisibility(View.VISIBLE);
//            mMidBtn.setVisibility(View.VISIBLE);
//            mRightBtn.setVisibility(View.VISIBLE);
            mCurrentTouchView = null;
            break;
        }
        
        mGestureDetector.onTouchEvent(event);
        return true;
//        if (mGestureDetector.onTouchEvent(event)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    private class MagcommGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            mNarrowView.setVisibility(View.INVISIBLE);
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TODO Auto-generated method stub
            
            int scrollY = mScrollView.getScrollY();
            int moveY = (int) (e2.getRawY() - e1.getRawY());
            if (EFFECTIVE_SPEED <= Math.abs(velocityY) && velocityY < 0 && !isMoving) {
                mScrollView.scrollTo(0, -moveY);
                startTranslate(mScrollView, 900, -scrollY, -mScrollView.getHeight(), true);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            keepScreenOn();
            int moveY = (int) (e2.getRawY() - e1.getRawY());
            float alpha = ((float)getHeight()/ 2.5f - (float)mScrollView.getScrollY()) / ((float)getHeight()/2.5f);
            mScrollView.setAlpha(Math.max(0.2f, Math.min(1.0f, alpha)));
            if (moveY > 0 && !isMoving) {
                mScrollView.scrollTo(0, 0);
                return false;
            }
            if (Math.abs(moveY) > 10 && !isMoving) {
                mScrollView.scrollTo(0, -moveY);
            }
            return false;

        }
    }

    private void startTranslate(final View view, int duration, float startY, float endY, boolean toUnlock) {
        isMoving = true;
        final boolean unLock = toUnlock;
        TranslateAnimation tansAnimatiion = new TranslateAnimation(0.0f, 0.0f, startY, endY);
        AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator() {

            @Override
            public float getInterpolation(float input) {
                // TODO Auto-generated method stub
                float result;
                if (mFactor == 1.0f) {
                    result = (1.0f - ((1.0f - input) * (1.0f - input)));
                } else {
                    result = (float) (1.0f - Math.pow((1.0f - input), 2 * mFactor));
                }
                return result;
            }

            private float mFactor = 1.0f;
        };

        tansAnimatiion.setDuration(duration);

        tansAnimatiion.setInterpolator(mAccelerateDecelerateInterpolator);
        tansAnimatiion.setFillAfter(true);
        tansAnimatiion.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (unLock) {
                    // playSounds(true);
                    if (view.getId() == mLeftBtn.getId()) {
                        gotoIntent(MAGCOMM_STATUS_DIAL);
                    } else if (view.getId() == mRightBtn.getId()) {
                        gotoIntent(MAGCOMM_STATUS_MSG);
                    } else {
                        gotoIntent(MAGCOMM_STATUS_UNLOCK);
                    }
                }
                mNarrowView.setVisibility(View.VISIBLE);
                if(mUnreadMsg > 0){
                    mRightBtn.setVisibility(View.VISIBLE);
                }else{
                    mRightBtn.setVisibility(View.INVISIBLE);
                }
                if(mUnreadCall > 0){
                    mLeftBtn.setVisibility(View.VISIBLE);
                }else{
                    mLeftBtn.setVisibility(View.INVISIBLE);
                }
                mMidBtn.setVisibility(View.VISIBLE);
                isMoving = false;
            }
        });

        mScrollView.startAnimation(tansAnimatiion);
    }

    private void keepScreenOn() {
        Message m = new Message();
        m.what = MagcommLockBaseView.MAGCOMM_STATUS_SCREENON;
        mainHandler.handleMessage(m);
    }

    public void gotoIntent(int msg) {
        Message m = new Message();
        m.what = msg;
        mainHandler.handleMessage(m);
    }

    public void setMainHandler(Callback handler) {
        mainHandler = handler;
    }

    private void playSounds(boolean locked) {

        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.System.getInt(cr, Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) == 1) {
            final int whichSound = locked ? mLockSoundId : mUnlockSoundId;
            mLockSounds.stop(mLockSoundStreamId);
            // Init mAudioManager
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                if (mAudioManager == null)
                    return;
                mMasterStreamType = mAudioManager.getUiSoundsStreamType();
            }
            // If the stream is muted, don't play the sound
            if (mAudioManager.isStreamMute(mMasterStreamType))
                return;

            mLockSoundStreamId = mLockSounds.play(whichSound, mLockSoundVolume, mLockSoundVolume, 1/* priortiy */, 0/* loop */, 1.0f/* rate */);
        }
    }

    // 获取屏幕高度
    private int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    private Typeface getClockTypeface() {
        return Typeface.createFromFile(MAGCOMM_FZHJ_FONT_FILE);
    }
}
