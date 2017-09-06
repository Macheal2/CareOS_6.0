package com.android.deskclock;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import com.cappu.app.CareDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ImageButton;

import com.android.deskclock.alarms.AlarmStateManager;
import com.android.deskclock.alarms.PowerOffAlarm;
import com.android.deskclock.provider.Alarm;
import com.android.deskclock.provider.AlarmInstance;
import com.android.deskclock.provider.DaysOfWeek;
import com.android.deskclock.R;
import com.android.deskclock.wheel.OnWheelChangedListener;
import com.android.deskclock.wheel.OnWheelScrollListener;
import com.android.deskclock.wheel.StrericWheelAdapter;
import com.android.deskclock.wheel.WheelView;

import android.view.animation.AnticipateOvershootInterpolator;
import android.util.Log;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import com.cappu.widget.TopBar;
import com.cappu.widget.CareButton;

public class MagcommAddAlarm extends Activity implements LabelDialogFragment.AlarmSetLabelDialogHandler{
	
	private static final String TAG = "AddAlarm";
	private static final String KEY_DEFAULT_RINGTONE = "default_ringtone";
	private static final int REQUEST_CODE_RINGTONE = 1;
	private Context mContext;
	//private MagcommAnalogColckPicker mTimePicker;
	private View mRingtone;
	private TextView mRingtoneName;
	private View mLabel;
	private TextView mLabelName;
	//private TextView mTitle;
	//private ImageButton mDack;
    private TopBar mTopBar;
	private Switch mVibrate;
	private long mAlarmId;
	private Alarm mOriginalAlarm;
	private int mHour;
	private int mMinutes;
	private Uri mAlert;
	private String[] mShortWeekDayStrings;
    private String[] mLongWeekDayStrings;
	private final int[] DAY_ORDER = new int[] {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
    };
	private LinearLayout mRpeatDays;
	//private ViewGroup[] mDayButtonParents = new ViewGroup[7];
	private Button[] mDayButtons = new Button[7];
	private Typeface mRobotoNormal;
	private Typeface mRobotoBold;
	private WheelView mTimeHours;
	private WheelView mTimeMinutes;
	public String[] hour = null;
	public String[] minute = null;
	private CareButton mDelete;
	private CareButton mSave;
    private View mDividView;
  
  	@Override
  	protected void onCreate(Bundle paramBundle){
  		
  		super.onCreate(paramBundle); 

	    getActionBar().setHomeButtonEnabled(true);
	    getActionBar().setDisplayShowHomeEnabled(false);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    getActionBar().hide();
	    
	    setContentView(R.layout.magcomm_alarm_edit);
	    
	    mContext = this;
	    mRobotoBold = Typeface.create("sans-serif-condensed", Typeface.BOLD);
	    mRobotoNormal = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
	    
	    //mTimePicker = (MagcommAnalogColckPicker)findViewById(R.id.TimePicker);
	    init();
	    mTimeHours = (WheelView)findViewById(R.id.hour);
	    mTimeHours.setAdapter(new StrericWheelAdapter(hour));
	    mTimeHours.setCyclic(true);
	    mTimeHours.setLabel(getResources().getString(R.string.hours_label));
	    mTimeHours.setInterpolator(new AnticipateOvershootInterpolator());
        
	    mTimeMinutes  = (WheelView)findViewById(R.id.minute);
	    mTimeMinutes.setAdapter(new StrericWheelAdapter(minute));
	    mTimeMinutes.setCyclic(true);
	    mTimeMinutes.setLabel(getResources().getString(R.string.minutes_label));
	    mTimeMinutes.setInterpolator(new AnticipateOvershootInterpolator());
        
	    mRpeatDays = (LinearLayout)findViewById(R.id.repeat_days);
	    initReapeatDaysButton();
	    
	    mRingtone=(LinearLayout)findViewById(R.id.ringtone);
	    mRingtoneName=(TextView)findViewById(R.id.choose_ringtone);
	    mRingtone.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View view) {
	    		setRingTonePicker();	
	    	}
	    });
		
		mLabel=(LinearLayout)findViewById(R.id.label);
		mLabelName=(TextView)findViewById(R.id.edit_label);
		mLabel.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View view) {
	    		showLabelDialog();
	    	}
	    });
		
		mVibrate=(Switch)findViewById(R.id.vibrate_onoff);
		mVibrate.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						mOriginalAlarm.vibrate=true;		      
					} else {
						mOriginalAlarm.vibrate=false;		      
					}
			}
		});
		
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
                MagcommAddAlarm.this.finish();
            }

            @Override
            public void onRightClick(View v){

            }

            @Override
            public void onTitleClick(View v){

            }
        });
	    
        mDividView = (View) findViewById(R.id.divid_view);

		//mTitle=(TextView)findViewById(R.id.topbar_title);
		mDelete = (CareButton)findViewById(R.id.delete_alarm);
	    
		mAlarmId = getIntent().getIntExtra("alarm_id", -1);
	    Alarm mAlarm;
	    Calendar mCalendar;

	    if (mAlarmId == -1L){
            mTopBar.setText(R.string.button_alarms);
	    	//mTitle.setText(R.string.button_alarms);
	    	mDelete.setVisibility(View.GONE);
            mDividView.setVisibility(View.GONE);

	    	mAlarm = new Alarm();
	    	mCalendar = Calendar.getInstance();
	      	mAlarm.hour = mCalendar.get(Calendar.HOUR_OF_DAY);
	      	mAlarm.minutes = mCalendar.get(Calendar.MINUTE);
	      	mAlarm.label = mContext.getString(R.string.get_up);
	      	String defaultRingtone = getDefaultRingtone(mContext);
            if (isRingtoneExisted(mContext, defaultRingtone)) {
            	mAlarm.alert = Uri.parse(defaultRingtone);
            } else {
            	mAlarm.alert = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                          RingtoneManager.TYPE_ALARM);
            }
            ///@}
            if (mAlarm.alert == null) {
            	mAlarm.alert = Uri.parse("content://settings/system/alarm_alert");
            }
	    }else{
            mTopBar.setText(R.string.magcomm_alarm_edit);
	    	//mTitle.setText(R.string.magcomm_alarm_edit);
            
	    	mDelete.setVisibility(View.VISIBLE);	
            mDividView.setVisibility(View.VISIBLE);
    	
	    	mAlarm = Alarm.getAlarm(getContentResolver(), mAlarmId);
	    }
	    
	    if(mAlarm != null) {
	    	mOriginalAlarm = mAlarm;
	    	updatePrefs(mOriginalAlarm);
	    }
	    
		/*mDack=(ImageButton)findViewById(R.id.topbar_back);
		mDack.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub			
          		finish();
			}
			
		});*/		
	    
	    mDelete.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new CareDialog.Builder(mContext)
				.setTitle(R.string.delete)
				.setMessage(R.string.delete_alarm_confirm)
            	.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				})
            	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						asyncDeleteAlarm(mOriginalAlarm);
						finish();
					}
				}).create().show();
			}
	    	
	    });
	    
		mSave = (CareButton)findViewById(R.id.save_alarm);;
		mSave.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveAlarm();
          		finish();
			}
			
		});
	  }
  	
  	@Override
    protected void onResume(){
    	super.onResume();
    }
    
    @Override
  	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
          	case android.R.id.home:
          		finish();
              	return true;
          	case R.id.save_alarm:
          		saveAlarm();
          		finish();
              	return true;
          	default:
          		break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    @Override
  	public boolean onCreateOptionsMenu(Menu menu) {
      	//getMenuInflater().inflate(R.menu.magcomm_alarm_add, menu);
      	
      	return super.onCreateOptionsMenu(menu);
  	}
    
  	private void initReapeatDaysButton(){
  		DateFormatSymbols dfs = new DateFormatSymbols();
        mShortWeekDayStrings = Utils.getShortWeekdays();
        //mShortWeekDayStrings = new String[]{"","日","一","二","三","四","五","六"};
        mLongWeekDayStrings = dfs.getWeekdays();
	    for (int i = 0; i < 7; i++) {
	    	final Button dayButton = (Button) LayoutInflater.from(mContext).inflate(
                    R.layout.day_button, mRpeatDays, false /* attachToRoot */);
            dayButton.setText(mShortWeekDayStrings[i]);
            dayButton.setContentDescription(mLongWeekDayStrings[DAY_ORDER[i]]);
            mRpeatDays.addView(dayButton);
            mDayButtons[i] = dayButton;
//            final ViewGroup viewgroup = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.day_button,
//            		mRpeatDays, false);
//            final ToggleButton button = (ToggleButton) viewgroup.getChildAt(0);
//            final int dayToShowIndex = DAY_ORDER[i];
//            button.setText(mShortWeekDayStrings[dayToShowIndex]);
//            button.setTextOn(mShortWeekDayStrings[dayToShowIndex]);
//            button.setTextOff(mShortWeekDayStrings[dayToShowIndex]);
//            button.setContentDescription(mLongWeekDayStrings[dayToShowIndex]);
//            mRpeatDays.addView(viewgroup);
//            mDayButtons[i] = button;
//            mDayButtonParents[i] = viewgroup;

            final int buttonIndex = i;
            mDayButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                	final boolean isActivated = mDayButtons[buttonIndex].isActivated();            
                	int day = DAY_ORDER[buttonIndex];
                    mOriginalAlarm.daysOfWeek.setDaysOfWeek(!isActivated, day);
                    if (!isActivated) {
                        turnOnDayOfWeek(buttonIndex);
                    } else {
                        turnOffDayOfWeek(buttonIndex);
                    }
                }
            });
        }
  	}
  	
  	private void updatePrefs(Alarm alarm){
  		
  		//mAlarmId = alarm.id;
  		if (alarm.label == null || alarm.label.length() == 0) {
  			mOriginalAlarm.label=mContext.getString(R.string.get_up);
  			mLabelName.setText(mOriginalAlarm.label);
    	}  else{
    		mLabelName.setText(alarm.label);
    	}
  	    mHour = alarm.hour;
  	    mMinutes = alarm.minutes;
  	    mTimeHours.setCurrentItem(mHour);
  	    mTimeMinutes.setCurrentItem(mMinutes);
  	    //mTimePicker.setCurrentTime(mHour,mMinutes);
  	    mAlert = alarm.alert;
  	    String ringtoneTitle = getRingToneTitle(alarm.alert);
  	    mRingtoneName.setText(ringtoneTitle);
  	    updateDaysOfWeekButtons(alarm.daysOfWeek);
  	    if (alarm.vibrate)
  	    	mVibrate.setChecked(true);
  	    else
  	    	mVibrate.setChecked(false);
  	    
  	  }
  	
  	private void updateDaysOfWeekButtons(DaysOfWeek daysOfWeek) {
        HashSet<Integer> setDays = daysOfWeek.getSetDays();
        for (int i = 0; i < 7; i++) {
            if (setDays.contains(DAY_ORDER[i])) {
                turnOnDayOfWeek(i);
            } else {
                turnOffDayOfWeek(i);
            }
        }
    }
  	
  	private void turnOffDayOfWeek(int dayIndex) {
  		mDayButtons[dayIndex].setActivated(false);
        mDayButtons[dayIndex].setTextColor(getResources().getColor(R.color.magcomm_done_disabled_dark));
        mDayButtons[dayIndex].setTypeface(mRobotoNormal);
    }

    private void turnOnDayOfWeek(int dayIndex) {
    	mDayButtons[dayIndex].setActivated(true);
    	mDayButtons[dayIndex].setTextColor(getResources().getColor(R.color.magcomm_alarm_red));
    	mDayButtons[dayIndex].setTypeface(mRobotoBold);
    }
    
    private void showLabelDialog() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("label_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        /// M:If the LabelEditDialog Existed,do not create again
        //ft.addToBackStack(null);
        /// M:Don't need use the method ft.commit(), because it may cause IllegalStateException
        final LabelDialogFragment newFragment =
                LabelDialogFragment.newInstance(mOriginalAlarm, mOriginalAlarm.label, null);
        ft.add(newFragment, "label_dialog");
        ft.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
    }
    
    private void setLabel(String label) {
    	if (label == null || label.length() == 0) {
    		label=mContext.getString(R.string.get_up);
    	}  
    	mOriginalAlarm.label = label;
    	mLabelName.setText(mOriginalAlarm.label);;
    }
    
    private void setRingTonePicker() {
        Uri oldRingtone = Alarm.NO_RINGTONE_URI.equals(mOriginalAlarm.alert) ? null : mOriginalAlarm.alert;
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, oldRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        startActivityForResult(intent, REQUEST_CODE_RINGTONE);
    }

    private void saveRingtoneUri(Intent intent) {
        Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) {
            uri = Alarm.NO_RINGTONE_URI;
        }
        String ringtoneTitle = getRingToneTitle(uri);
  	    mRingtoneName.setText(ringtoneTitle);
  	    mOriginalAlarm.alert = uri;
    }

//    private void setDefaultRingtone(String defaultRingtone) {
//    	if (TextUtils.isEmpty(defaultRingtone)) {
//            Log.e("setDefaultRingtone fail");
//            return;
//        }
//    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(KEY_DEFAULT_RINGTONE, defaultRingtone);
//        editor.apply();
//    }
    
    private static String getDefaultRingtone(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultRingtone = prefs.getString(KEY_DEFAULT_RINGTONE, "");    
        return defaultRingtone;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RINGTONE:
                    saveRingtoneUri(data);
                    break;
                default:
                    Log.w("TAG","Unhandled request code in onActivityResult: " + requestCode);
            }
        }
    }
    
  	private String getRingToneTitle(Uri uri) {
        // Try the cache first
  		String   title;
  		 if (Alarm.NO_RINGTONE_URI.equals(uri)) {
  			title = mContext.getResources().getString(R.string.silent_alarm_summary);
  	  	    } else {
  	          if (!isRingtoneExisted(mContext, uri.toString())) {
  	        	  uri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
  	                      RingtoneManager.TYPE_ALARM);
  	              if (uri == null) {
  	            	uri = Uri.parse("content://settings/system/alarm_alert");
  	              }
  	          }
  	          
  	        mOriginalAlarm.alert = uri;
  	        Ringtone ringTone = RingtoneManager.getRingtone(mContext, uri);
  	  		title = ringTone.getTitle(mContext);
  	  	}
        return title;
    }
  	
  	public static boolean isRingtoneExisted(Context ctx, String ringtone) {
        boolean result = false;
        if (ringtone != null) {
            if (ringtone.contains("internal")) {
                return true;
            }
            String path = PowerOffAlarm.getRingtonePath(ctx, ringtone);
            
            if (!TextUtils.isEmpty(path)) {
                result = new File(path).exists();
            }
        }      
        return result;
    }
  	
  	private void saveAlarm(){
  		
  		mOriginalAlarm.enabled = true;
  		mOriginalAlarm.hour = mTimeHours.getCurrentItem();//mTimePicker.getCurrentHour();
  		mOriginalAlarm.minutes =mTimeMinutes.getCurrentItem();//mTimePicker.getCurrentMinute();
   
  		if (mAlarmId != -1L){
  			asyncUpdateAlarm(mOriginalAlarm, true);
  		}else{	
  			asyncAddAlarm(mOriginalAlarm);
  		}
  	}
  	
    private static AlarmInstance setupAlarmInstance(Context context, Alarm alarm) {
        ContentResolver cr = context.getContentResolver();
        AlarmInstance newInstance = alarm.createInstanceAfter(Calendar.getInstance());
        newInstance = AlarmInstance.addInstance(cr, newInstance);
        // Register instance to state manager
        AlarmStateManager.registerInstance(context, newInstance, true);
        return newInstance;
    }
    
    private void asyncDeleteAlarm(final Alarm alarm) {

        final AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parameters) {
                // Activity may be closed at this point , make sure data is still valid
                if (mContext != null && alarm != null) {
                    ContentResolver cr = mContext.getContentResolver();
                    AlarmStateManager.deleteAllInstances(mContext, alarm.id);
                    Alarm.deleteAlarm(cr, alarm.id);    
                }
                return null;
            }
        };
        
        deleteTask.execute();
    }
    
    private void asyncAddAlarm(final Alarm alarm) {
        
        final AsyncTask<Void, Void, AlarmInstance> updateTask =
                new AsyncTask<Void, Void, AlarmInstance>() {

            @Override
            protected AlarmInstance doInBackground(Void... parameters) {
                if (mContext != null && alarm != null) {
                    ContentResolver cr = mContext.getContentResolver();
                    // Add alarm to db
                    Alarm newAlarm = Alarm.addAlarm(cr, alarm);                
                    // Create and add instance to db
                    if (newAlarm.enabled) {
                        return setupAlarmInstance(mContext, newAlarm);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(AlarmInstance instance) {
                if (instance != null) {
                    AlarmUtils.popAlarmSetToast(mContext, instance.getAlarmTime().getTimeInMillis());
                }
            }
        };
        updateTask.execute();
    }

    private void asyncUpdateAlarm(final Alarm alarm, final boolean popToast) {
        
        final AsyncTask<Void, Void, AlarmInstance> updateTask =
                new AsyncTask<Void, Void, AlarmInstance>() {
        	
            @Override
            protected AlarmInstance doInBackground(Void ... parameters) {
                ContentResolver cr = mContext.getContentResolver();

                // Dismiss all old instances
                AlarmStateManager.deleteAllInstances(mContext, alarm.id);

                // Update alarm
                Alarm.updateAlarm(cr, alarm);
                if (alarm.enabled) {
                    return setupAlarmInstance(mContext, alarm);
                }

                return null;
            }

            @Override
            protected void onPostExecute(AlarmInstance instance) {
                if (popToast && instance != null) {
                    AlarmUtils.popAlarmSetToast(mContext, instance.getAlarmTime().getTimeInMillis());
                }
            }
        };
        updateTask.execute();
    }
    
    @Override
    public void onDialogLabelSet(String label) {
            setLabel(label);
    }
    
    public void init(){
    	
	    hour = new String[24];
	    for(int i=0;i < 24;i++){
	        hour[i]= String.valueOf(i);
	        if(hour[i].length()<2){
	        	hour[i] = "0"+hour[i];
	        }
	    }
			
	    minute = new String[60];
	    for(int i=0;i<60;i++){
	        minute[i]=String.valueOf(i);
	        if(minute[i].length()<2){
	        	minute[i] = "0" + minute[i];
	        }
	    }
    }
}
