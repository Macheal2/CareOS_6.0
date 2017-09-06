package cappu.sos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.AudioManager; //add by zxy
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
//import android.telephony.gemini.GeminiSmsManager;//not web version
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationData;

import android.os.SystemProperties;
//import cappu.sos.widget.TopBar;
//import cappu.sos.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;

public class SOSShowActivity extends Activity {

	private SharedPreferences sharedPreferences;
	private MediaPlayer mp;
	private boolean sos_isSounding = false;
	
	private SOSPhoneCount mPhoneCount;
	private SOSSMSCount mSMSCount;

	private int msgcounter;
	// private String curSmsSendNumber;
	private String SENT = "SMS_SENT";
	private String DELIVERED = "SMS_DELIVERED";
	
	private boolean b_sos_sound_state;
	private boolean b_sos_phone_state;
	private boolean b_sos_sms_state;
	private boolean b_sos_sms_location;
	private String sos_mobile_number;
	private String sos_sms_mobile_number;
	private String sos_sms_content;

	private TextView txt_phone_title;
	private TextView txt_call_state;
	
	private TextView txt_sms_state;
	private TextView txt_sms_title;;

	private ImageView img_sound_icon;
	private TextView txt_sound_info;
	
	private RelativeLayout sms_layout;

	private boolean sim1_ok = false;
	private boolean sim2_ok = false;
	private boolean default_send_sim_ok = false;
	private int default_send_sim = -1;
	
	// 定位相关 begin
	LocationClient mLocClient;
	MyLocationData locData = null;
	String sms_body_local = "";

	private IntentFilter filter;
	private SendReceiver sendReceiver;
	private DeliveredReceiver deliveredReceiver;
	
	public MyLocationListenner myListener = new MyLocationListenner();

	// 定位相关 end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_show);

		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// not web version begin
		//sim begin
		/*try {
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			
			default_send_sim = manager.getSmsDefaultSim();
			default_send_sim_ok = (TelephonyManager.SIM_STATE_READY == manager.getSimStateGemini(default_send_sim));
			
			sim1_ok = (TelephonyManager.SIM_STATE_READY == manager.getSimStateGemini(PhoneConstants.GEMINI_SIM_1));
			sim2_ok = (TelephonyManager.SIM_STATE_READY == manager.getSimStateGemini(PhoneConstants.GEMINI_SIM_2));
			
			Log.e("dengying", "sim1_ok="+sim1_ok+" sim2_ok="+sim2_ok +" manager.getSimState()="+manager.getSimState()+" default_send_sim="+default_send_sim+" default_send_sim_ok="+default_send_sim_ok);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		//sim end
		// not web version end
		
		sharedPreferences = getSharedPreferences("sospref", Context.MODE_PRIVATE);
		
		filter = new IntentFilter();
		filter.addAction(SENT);
		filter.addAction(DELIVERED);

		sendReceiver = new SendReceiver();
		deliveredReceiver = new DeliveredReceiver();

		b_sos_sound_state = sharedPreferences.getBoolean("sos_sound_state", true);
		
		b_sos_phone_state = sharedPreferences.getBoolean("sos_phone_state", false);
		sos_mobile_number = sharedPreferences.getString("sos_mobile_number", "");

		b_sos_sms_state = sharedPreferences.getBoolean("sos_sms_state", false);
		sos_sms_mobile_number = sharedPreferences.getString("sos_sms_mobile_number", "");
		sos_sms_content = sharedPreferences.getString("sos_sms_content", getString(R.string.sos_sms_content));

		Log.e("dengying", "sos_sound_state="+b_sos_sound_state+" sos_phone_state="+b_sos_phone_state+" sos_sms_state="+b_sos_sms_state);
		
		if (b_sos_phone_state && !sos_mobile_number.equals("") && isCanUseSim()) {
			mPhoneCount = new SOSPhoneCount(10000, 1000);
			mPhoneCount.start();
			
			Log.e("dengying","mPhoneCount.start()");
		}

		if (b_sos_sms_state && !sos_sms_mobile_number.equals("") && !sos_sms_content.equals("") && isCanUseSim()) {
			mSMSCount = new SOSSMSCount(5000, 1000);
			mSMSCount.start();
		}
//add by zxy for pause bgm when SOS begin
		AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE); 
		if (audioManager.isMusicActive()){
			audioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT); 
		}else{
			audioManager.abandonAudioFocus(null);
		}
//add end
		//play sound begin
		if(b_sos_sound_state){
			sos_isSounding = true;
		}
		
		mp = MediaPlayer.create(SOSShowActivity.this, R.raw.warningtone_buzz);
		//deleted  by jiangyan for the alarm cannot be stopped immediately   start
	/*	try {
		//	mp.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
			//deleted  by jiangyan for the alarm cannot be stopped immediately   end
		mp.setLooping(true);
		//play sound end
		
		initLocation();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		registerReceiver(sendReceiver, filter);
		registerReceiver(deliveredReceiver, filter);
		
		//play sound begin
		img_sound_icon = (ImageView) findViewById(R.id.sound_icon);
		txt_sound_info = (TextView) findViewById(R.id.sound_info);
		if (sos_isSounding){
			mp.start();
			img_sound_icon.setImageResource(R.drawable.icon_sos_voice_off);
			txt_sound_info.setText(getString(R.string.sos_alarm_close));
		}else{
			img_sound_icon.setImageResource(R.drawable.icon_sos_voice_on);
			txt_sound_info.setText(getString(R.string.sos_alarm_open));
		}
		//play sound end		
		
		initView();
	}

	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		unregisterReceiver(sendReceiver);
		unregisterReceiver(deliveredReceiver);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if (sos_isSounding) {
			mp.pause();
		}		
		//added by jiangyan for SOS Countdown start
	/*	try {
			if (mPhoneCount != null) {
				mPhoneCount.cancel();
			}
			if (mSMSCount != null) {
				mSMSCount.cancel();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		//added by jiangyan for SOS Countdown end
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	
		if (sos_isSounding){
			mp.stop();
			
			sos_isSounding = false;
		}

		mp.release();
		
		//added by jiangyan for SOS Countdown start
			try {
		if (mPhoneCount != null) {
			mPhoneCount.cancel();
		}
		if (mSMSCount != null) {
			mSMSCount.cancel();
		}
	} catch (Exception e) {
		// TODO: handle exception
	}
		//added by jiangyan for SOS Countdown end

		// 退出时销毁定位
		if (mLocClient != null)
			mLocClient.stop();
		//add by zxy for pause bgm when SOS begin
		AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE); 
		if (audioManager.isMusicActive()){
			audioManager.abandonAudioFocus(null);
		}
		//add end
	}

	// 实例化监听
	private onTopBarListener mTopBarListener = new onTopBarListener(){
	    public void onLeftClick(View v){
	    	finish();
	    }
	    
	    public void onRightClick(View v){
	    }   
	    
	    public void onTitleClick(View v){
	    }
	};
	
	private void initView() {

		b_sos_phone_state = sharedPreferences.getBoolean("sos_phone_state", false);
		sos_mobile_number = sharedPreferences.getString("sos_mobile_number", "");

		b_sos_sms_state = sharedPreferences.getBoolean("sos_sms_state", false);
		sos_sms_mobile_number = sharedPreferences.getString("sos_sms_mobile_number", "");
		sos_sms_content = sharedPreferences.getString("sos_sms_content", getString(R.string.sos_sms_content));

		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);		
		
		/*RelativeLayout title_layout = (RelativeLayout) findViewById(R.id.title_layout);

		ImageButton btnCancel = (ImageButton) title_layout.findViewById(R.id.cancel);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/

		/*TextView txtTitle = (TextView) title_layout.findViewById(R.id.title);
		txtTitle.setText(R.string.desktop_title_sos);*/

		RelativeLayout sound_layout = (RelativeLayout) findViewById(R.id.sound_layout);

		sound_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (sos_isSounding){
					
					if (mp.isPlaying()) {
						mp.pause();
					}

					img_sound_icon.setImageResource(R.drawable.icon_sos_voice_on);
					txt_sound_info.setText(getString(R.string.sos_alarm_open));

					sos_isSounding = false;
				} else {
					mp.start();

					img_sound_icon.setImageResource(R.drawable.icon_sos_voice_off);
					txt_sound_info.setText(getString(R.string.sos_alarm_close));
					
					sos_isSounding = true;
				}
			}
		});

		RelativeLayout cancel_layout = (RelativeLayout) findViewById(R.id.cancel_layout);

		cancel_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		RelativeLayout call_layout = (RelativeLayout) findViewById(R.id.call_layout);

		call_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!b_sos_phone_state || sos_mobile_number.equals("")) {
					Intent i = new Intent(SOSShowActivity.this, SOSSetting.class);
					startActivity(i);
				} else {

					try {
						if (null != mPhoneCount) {

							mPhoneCount.cancel();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					txt_call_state.setText("");
					callSosNumber();
				}
			}
		});

		sms_layout = (RelativeLayout) findViewById(R.id.sms_layout);

		sms_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!b_sos_sms_state || sos_sms_mobile_number.equals("") || sos_sms_content.equals("")) {
					Intent i = new Intent(SOSShowActivity.this, SOSSetting.class);
					startActivity(i);
				} else {

					try {
						if (null != mSMSCount) {
							mSMSCount.cancel();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					txt_sms_state.setText("");
					sendSosSms();
				}
			}
		});

		txt_call_state = (TextView) findViewById(R.id.call_state);
		txt_phone_title = (TextView) findViewById(R.id.phone_title);

		if (b_sos_phone_state && !sos_mobile_number.equals("")) {
			txt_call_state.setText("");
			txt_phone_title.setTextColor(android.graphics.Color.BLACK);
		} else {
			txt_phone_title.setTextColor(android.graphics.Color.GRAY);
		}

		txt_sms_state = (TextView) findViewById(R.id.sms_state);
		txt_sms_title = (TextView) findViewById(R.id.sms_title);

		if (b_sos_sms_state && !sos_sms_mobile_number.equals("") && !sos_sms_content.equals("")) {
			txt_sms_state.setText("");
			txt_sms_title.setTextColor(android.graphics.Color.BLACK);
		} else {
			txt_sms_title.setTextColor(android.graphics.Color.GRAY);
		}

		b_sos_sms_location = sharedPreferences.getBoolean("sos_sms_location", false);

	}

	class SOSPhoneCount extends CountDownTimer {

		public SOSPhoneCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			txt_call_state.setText("0" + getString(R.string.second));
			txt_call_state.setText("");

			callSosNumber();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			txt_call_state.setText(String.valueOf(millisUntilFinished / 1000) + getString(R.string.second));
		}
	}

	class SOSSMSCount extends CountDownTimer {

		public SOSSMSCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			txt_sms_state.setText("0" + getString(R.string.second));
			txt_sms_state.setText("");

			sendSosSms();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			txt_sms_state.setText(String.valueOf(millisUntilFinished / 1000) + getString(R.string.second));
		}
	}

	private void callSosNumber() {

		if (!isCanUseSim()) {
			showToast(getString(R.string.no_sim));
			return;
		}

		if (sos_isSounding){
			
			if (mp.isPlaying()) {
				mp.pause();
				
			}

			sos_isSounding = false;
		}	
		
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://" + sos_mobile_number));
		// ACTION_CALL ACTION_DIAL ACTION_CALL_PRIVILEGED
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void sendSosSms() {

		if (!isCanUseSim()) {
			showToast(getString(R.string.no_sim));
			return;
		}

		txt_sms_title.setText(R.string.sms_sending);

		sms_layout.setClickable(false);

		if (b_sos_sms_location) {
			if(sms_body_local.trim().equals("")){
				sos_sms_content = sos_sms_content + "," + getString(R.string.sos_sms_body_pre) + getString(R.string.no_location);
			}else{
				sos_sms_content = sos_sms_content + "," + getString(R.string.sos_sms_body_pre) + sms_body_local;
			}
		}

		String[] temp = sos_sms_mobile_number.split("\\,");
		int numberCount = 0;
		for (String number : temp) {
			if (number != null && (!number.equals("")))
				numberCount++;
			    Log.e("dengying", "sms number="+number);
		}

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(sos_sms_content);
		msgcounter = parts.size() * numberCount;

		for (int i = 0; i < temp.length; i++) {
			String number = temp[i];
			if (number != null && (!number.equals(""))) {
				// doSendSMSTo(number, sos_sms_content);
				// doSendSMSTo(number, sms_body_local);
				sendSMS(number, sos_sms_content);

				//showToast(getString(R.string.sms_sent_number) + number);
			}

			if (i == temp.length) {
				txt_sms_title.setText(R.string.sos_sms);
				sms_layout.setClickable(true);
			}
		}
	}

	// 定位相关 begin
	private void initLocation() {
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			try {
				if (location == null)
					return;
			 locData = new MyLocationData.Builder().accuracy(location.getRadius())
		                    // 此处设置开发者获取到的方向信息，顺时针0-360
		                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
		            
//				locData.latitude = location.getLatitude();
//				locData.longitude = location.getLongitude();
//
//				// 如果不显示定位精度圈，将accuracy赋值为0即可
//				locData.accuracy = location.getRadius();
//				// 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
//				locData.direction = location.getDerect();

				sms_body_local = "http://api.map.baidu.com/staticimage?zoom=17&markers=" + locData.longitude + "," + locData.latitude;

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	// 定位相关 end

	public void doSendSMSTo(String phoneNumber, String message) {
		/*
		 * if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){ Intent intent
		 * = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
		 * intent.putExtra("sms_body", message); startActivity(intent); }
		 */

		SmsManager smsManager = SmsManager.getDefault();

		List<String> contents = smsManager.divideMessage(message);
		/*
		 * for (String sms : contents) { smsManager.sendTextMessage(phoneNumber,
		 * null, sms, null, null); }
		 */

		smsManager.sendMultipartTextMessage(phoneNumber, null, (ArrayList<String>) contents, null, null);

		/*
		 * if (message.length() > 140) { List<String> contents =
		 * smsManager.divideMessage(message); for (String sms : contents) {
		 * smsManager.sendTextMessage(phoneNumber, null, sms, null, null); } }
		 * else { smsManager.sendTextMessage(phoneNumber, null, message, null,
		 * null); }
		 */
	}

	private void sendSMS(final String phoneNumber, String message) {

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		//web version begin
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		//web version end
		
		//ArrayList<String> parts = GeminiSmsManager.divideMessage(message);//not web version
		
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
		for (int number = 0; number < parts.size(); number++) {
			sentIntents.add(sentPI);
			deliveryIntents.add(deliveredPI);
		}
		
		sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);//web version
		
		//not web version begin
		/*int sendSim = -1;
		
		if(default_send_sim_ok){
			sendSim = default_send_sim;
		}else if(sim1_ok){
			sendSim =PhoneConstants.GEMINI_SIM_1;
		}else if(sim2_ok){
			sendSim =PhoneConstants.GEMINI_SIM_2;	
		}
		
		if(sendSim >= 0){
			GeminiSmsManager.sendMultipartTextMessageGemini(phoneNumber, null, parts, sendSim,sentIntents, deliveryIntents);
		}*/
		//not web version end
	}

	/*
	 * SIM_STATE_UNKNOWN 未知状态 0
	 * 
	 * SIM_STATE_ABSENT 没插卡 1
	 * 
	 * SIM_STATE_PIN_REQUIRED 锁定状态，需要用户的PIN码解锁 2
	 * 
	 * SIM_STATE_PUK_REQUIRED 锁定状态，需要用户的PUK码解锁 3
	 * 
	 * SIM_STATE_NETWORK_LOCKED 锁定状态，需要网络的PIN码解锁 4
	 * 
	 * SIM_STATE_READY 就绪状态 5
	 */
	public boolean isCanUseSim() {
		
		//web version begin
		try {
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

			return TelephonyManager.SIM_STATE_READY == manager.getSimState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false; 
		//web version end 
		
		//not web version
		//return (sim1_ok || sim2_ok);
	}

	public void showToast(String msg) {
		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.toast_normal_layout, (ViewGroup) findViewById(R.id.toast_layout));

		Toast toast = new Toast(getApplicationContext());
		((TextView) toastLayout.findViewById(R.id.prompt_text)).setText(msg);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastLayout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	class SendReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(SENT))
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					if (msgcounter > 0) {
						msgcounter = msgcounter - 1;
					}

					if (msgcounter == 0) {
						showToast(getString(R.string.sms_sent_number));
						txt_sms_title.setText(R.string.sos_sms);
						sms_layout.setClickable(true);

						Log.e("dengying", "all msg sent");
					}
					break;

				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					txt_sms_title.setText(R.string.sos_sms);
					sms_layout.setClickable(true);
					break;

				case SmsManager.RESULT_ERROR_RADIO_OFF:
					txt_sms_title.setText(R.string.sos_sms);
					sms_layout.setClickable(true);
					break;

				case SmsManager.RESULT_ERROR_NULL_PDU:
					txt_sms_title.setText(R.string.sos_sms);
					sms_layout.setClickable(true);
					break;
				}
		}

	}

	class DeliveredReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(DELIVERED)) {
				int resultCode = getResultCode();
				if (resultCode == Activity.RESULT_OK) {
					Log.d("dengying", "SMS delivered");
				} else {
					Log.d("dengying", "SMS not delivered");
				}
			}
		}
	}
}
