package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.testitem.LCDTest.timetask;

public class TwinkleTest extends TestBase{
	private boolean debugflag = true;
	private String TAG = "TwinkleTest";
	
	private Vibrator vibrator;
//	private NotificationMgr mNotificationMgr;
	private NotificationManager mNotificationManager;
	private Notification notification;

	private TextView tv;
	private Button btn_success;
	private Button btn_fail;
	private CITTools ct;

	/*  COMING_CALL_NOTIFICATION 定义在NotificationMgr.java文件中，
	 *  这里直接使用数字11代替，清注意。
	 *  
	 *  static final int COMING_CALL_NOTIFICATION = 11;// shaoxinbo add for
	 *												// 2012-6-18
     *	static final int MISSED_CALL_LED_NOTIFICATION = 12;//caorange
	 * 
	 * */
	
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.simple_test);
		tv = (TextView) findViewById(R.id.test_title);
		ct = CITTools.getInstance(this);
		
		notification = new Notification();
		mNotificationManager =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

//		String testItem = ct.getStringFromRes(this, 35);
		String testItem = getString(R.string.twinkle_test_text);
		tv.setText(testItem);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		initProc();
		super.onStart();
	}

	private void initProc() {
		startTest();
	}

	@Override
	protected void onStop() {
		releaseProc();
		super.onStop();
	}

	private void startTest() {	
		LOGV(debugflag, TAG, "startTest, 00000");
//        notification.ledPriority = 0;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xffff0000;		//只有这一种颜色
        notification.ledOnMS = 500;		//亮的时间
        notification.ledOffMS = 500;		//灭的时间
        mNotificationManager.notify(11/*COMING_CALL_NOTIFICATION*/, notification);
	}

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		releaseProc();
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void releaseProc() {
		LOGV(debugflag, TAG, "releaseProc, 00000");
		mNotificationManager.cancel(11/*COMING_CALL_NOTIFICATION*/);
	}

}
