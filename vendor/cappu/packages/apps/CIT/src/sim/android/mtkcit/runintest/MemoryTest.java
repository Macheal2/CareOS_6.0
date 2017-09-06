package sim.android.mtkcit.runintest;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.R;
import sim.android.mtkcit.RunInTest;

public class MemoryTest extends RuninBaseActivity {
	private Button beginButton;
	private Button StopButton;

	private final String fileName = "test.txt";
	private Spinner mSpinner;
	private boolean writeflag;
	private boolean readflag;
	private boolean debugFlag = true;
	private final String TAG = "MemoryTest";
	private static int times;
	private boolean testFlas = true;

	@Override
	protected void onCreate(Bundle bundle) {

		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.memorytest);

	}

	@Override
	protected void onResume() {
		initRec();
		super.onResume();
	}

	private void initRec() {
		times++;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		LOGV(debugFlag, TAG, "initRec()");

		new Thread(new mThread()).start();
		ct = CITTools.getInstance(this);
		ct.getSDPaths();
		if (ct.checkSDCardMount(ct.SD1Path))
			creatFile();
		TextView cycleTimes = (TextView) findViewById(R.id.tv_cycle_times);
		cycleTimes.setText("");
		TextView testResule = (TextView) findViewById(R.id.tv_test_result);

		testResule.setText("");

		LOGV(debugFlag, TAG, " showResult()");
		String writeResult;
		writeflag = write2File();
		if (writeflag) {
			writeResult = getString(R.string.success);
		} else
			writeResult = getString(R.string.fail);
		String ResuleString = testResule.getText() + " " + writeResult;
		testResule.setText(ResuleString);
		String cycleString = cycleTimes.getText() + " " + readFromFile();
		cycleTimes.setText(cycleString);

		// Intent intent = new Intent();
		// intent.putExtra("stopTime", stopTime);
		// intent.setClass(this, SpeakerReceiverTest.class);
		// startActivity(intent);
	}

	private String readFromFile() {
		return ct.readFileByLines(RunInTest.RUNIN + "/" + fileName);
	}

	private boolean creatFile() {
		return ct.creatFile(fileName, RunInTest.RUNIN);
	}

	/*
	 * private void stopTest() { LOGV(debugFlag, TAG, "stopTest()");
	 * SharedPreferences settings = getSharedPreferences(RunInTest.PREFS_NAME,
	 * 0); SharedPreferences.Editor editor = settings.edit();
	 * editor.putBoolean("test_complate", true); editor.commit(); finish();
	 * 
	 * }
	 * 
	 * private void cyleTest() { LOGV(debugFlag, TAG, "cyleTest()"); Intent
	 * intent = new Intent(); intent.setClass(this, SpeakerReceiverTest.class);
	 * startActivity(intent); this.finish();
	 * 
	 * }
	 */
	private boolean write2File() {
		LOGV(debugFlag, TAG, "write2File() ");

		return ct.writeMethod(RunInTest.RUNIN + "/" + fileName, times + "");

	}

	@Override
	void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, SpeakerReceiverTest.class);
		startActivity(intent);
		this.finish();
	}
	
	class mThread implements Runnable {
		@Override
		public void run() {
			LOGV(debugFlag, TAG, "run() ");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGV(debugFlag, TAG, "mThread error ");
				return;
			}
//			currentTime = System.currentTimeMillis();
//			LOGV(debugFlag, TAG, "currentTime=" + currentTime);
//			if (currentTime > RunInTest.stopTime) {
//				stopTest();
//
//			} else
				cyleTest();

		}
	}
}
