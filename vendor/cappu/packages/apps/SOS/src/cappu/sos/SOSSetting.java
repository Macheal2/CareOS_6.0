package cappu.sos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.provider.Settings;
import android.content.ContentResolver;
//import cappu.sos.widget.TopBar;
//import cappu.sos.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.net.Uri;
import android.util.Log;

public class SOSSetting extends Activity {

	private SharedPreferences sharedPreferences;
	private boolean b_sos_key_state;
	private boolean b_sos_sound_state;
	private boolean b_sos_phone_state;
	private boolean b_sos_sms_state;
	private boolean b_sos_sms_location;

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
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_setting_main);
	}

	private void initView() {

		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);		
		
		/*ImageButton btnCancel = (ImageButton) findViewById(R.id.cancel);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/

		//TextView txtTitle = (TextView) findViewById(R.id.title);
		//txtTitle.setText(R.string.main_setting_sos_title);

		RelativeLayout layoutSosPhoneNumber = (RelativeLayout) findViewById(R.id.sos_phone_number_layout);

		layoutSosPhoneNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSSetting.this, SOSNumberSetting.class);
				startActivity(i);
			}
		});

		RelativeLayout layoutSosSmsNumber = (RelativeLayout) findViewById(R.id.sos_sms_number_layout);

		layoutSosSmsNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSSetting.this, SOSSmsNumberSetting.class);
				startActivity(i);
			}
		});

		RelativeLayout layoutSosSmsContent = (RelativeLayout) findViewById(R.id.sos_sms_content_layout);

		layoutSosSmsContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSSetting.this, SOSSmsContentSetting.class);
				startActivity(i);
			}
		});

		sharedPreferences = getSharedPreferences("sospref", Context.MODE_PRIVATE);
		final Editor editor = sharedPreferences.edit();

		upDateSosPhoneState();

		ImageButton btn_sos_phone_state_switch = (ImageButton) findViewById(R.id.sos_phone_state_switch);
		btn_sos_phone_state_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				b_sos_phone_state = !b_sos_phone_state;
				editor.putBoolean("sos_phone_state", b_sos_phone_state);
				editor.commit();

				upDateSosPhoneState();
			}
		});

		TextView sos_phone_number = (TextView) findViewById(R.id.sos_phone_number);

		String sos_mobile_number = sharedPreferences.getString("sos_mobile_number", "");

		if (sos_mobile_number.equals("")) {
			sos_mobile_number = this.getString(R.string.phone);
		}

		sos_phone_number.setText(sos_mobile_number);

		ImageButton btn_sos_sms_state_switch = (ImageButton) findViewById(R.id.sos_sms_state_switch);

		upDateSosSmsState();

		btn_sos_sms_state_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				b_sos_sms_state = !b_sos_sms_state;
				editor.putBoolean("sos_sms_state", b_sos_sms_state);
				editor.commit();
				upDateSosSmsState();
			}
		});

		TextView txt_sos_sms_phone_count = (TextView) findViewById(R.id.sos_sms_number);

		String sos_sms_mobile_number = sharedPreferences.getString("sos_sms_mobile_number", "");

		String[] temp = sos_sms_mobile_number.split("\\,");

		int sos_sms_phone_count = temp.length;

		if (temp[sos_sms_phone_count - 1].equals("")) {
			sos_sms_phone_count = sos_sms_phone_count - 1;
		}

		txt_sos_sms_phone_count.setText(String.valueOf(sos_sms_phone_count) + "人");

		ImageButton btn_sos_sms_location_switch = (ImageButton) findViewById(R.id.sos_sms_location_switch);
		upDateSosSmsLocationState();
		btn_sos_sms_location_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				b_sos_sms_location = !b_sos_sms_location;
				editor.putBoolean("sos_sms_location", b_sos_sms_location);
				editor.commit();
				upDateSosSmsLocationState();
			}
		});

		//sound
		ImageButton btn_sos_sound_state_switch = (ImageButton) findViewById(R.id.sos_sound_state_switch);
		upDateSosSoundState();
		btn_sos_sound_state_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				b_sos_sound_state = !b_sos_sound_state;
				editor.putBoolean("sos_sound_state", b_sos_sound_state);
				editor.commit();
				upDateSosSoundState();
			}
		});	
		
		//key
		ImageButton btn_sos_key_state_switch = (ImageButton) findViewById(R.id.sos_key_state_switch);
		upDateSosKeyState();
		btn_sos_key_state_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				b_sos_key_state = !b_sos_key_state;
				ContentResolver resolver = getContentResolver();
				Settings.Global.putInt(resolver,"sos_key_state",b_sos_key_state?1:0);
				
				//editor.putBoolean("sos_key_state", b_sos_key_state);
				//editor.commit();
				upDateSosKeyState();
			}
		});	
	}

	private void upDateSosKeyState() {
		ImageButton btn_sos_key_state_switch = (ImageButton) findViewById(R.id.sos_key_state_switch);

		//b_sos_key_state = sharedPreferences.getBoolean("sos_key_state", true);
		ContentResolver resolver = getContentResolver();
		int i_sos_key_state = Settings.Global.getInt(resolver,"sos_key_state",0); 

		b_sos_key_state = (i_sos_key_state == 1)?true:false;
		
		if (b_sos_key_state) {
			btn_sos_key_state_switch.setBackgroundResource(R.drawable.setting_switch_on);
		} else {
			btn_sos_key_state_switch.setBackgroundResource(R.drawable.setting_switch_off);
		}
	}	
	
	private void upDateSosSoundState() {
		ImageButton btn_sos_sound_state_switch = (ImageButton) findViewById(R.id.sos_sound_state_switch);

		b_sos_sound_state = sharedPreferences.getBoolean("sos_sound_state", true);
		if (b_sos_sound_state) {
			btn_sos_sound_state_switch.setBackgroundResource(R.drawable.setting_switch_on);
		} else {
			btn_sos_sound_state_switch.setBackgroundResource(R.drawable.setting_switch_off);
		}
	}
	
	private void upDateSosPhoneState() {
		ImageButton btn_sos_phone_state_switch = (ImageButton) findViewById(R.id.sos_phone_state_switch);

		b_sos_phone_state = sharedPreferences.getBoolean("sos_phone_state", false);
		if (b_sos_phone_state) {
			btn_sos_phone_state_switch.setBackgroundResource(R.drawable.setting_switch_on);
		} else {
			btn_sos_phone_state_switch.setBackgroundResource(R.drawable.setting_switch_off);
		}
	}

	private void upDateSosSmsState() {
		ImageButton btn_sos_sms_state_switch = (ImageButton) findViewById(R.id.sos_sms_state_switch);

		b_sos_sms_state = sharedPreferences.getBoolean("sos_sms_state", false);
		if (b_sos_sms_state) {
			btn_sos_sms_state_switch.setBackgroundResource(R.drawable.setting_switch_on);
		} else {
			btn_sos_sms_state_switch.setBackgroundResource(R.drawable.setting_switch_off);
		}
	}

	private void upDateSosSmsLocationState() {
		ImageButton btn_sos_sms_location_switch = (ImageButton) findViewById(R.id.sos_sms_location_switch);

		b_sos_sms_location = sharedPreferences.getBoolean("sos_sms_location", false);
		if (b_sos_sms_location) {
			btn_sos_sms_location_switch.setBackgroundResource(R.drawable.setting_switch_on);
		} else {
			btn_sos_sms_location_switch.setBackgroundResource(R.drawable.setting_switch_off);
		}
	}
}
