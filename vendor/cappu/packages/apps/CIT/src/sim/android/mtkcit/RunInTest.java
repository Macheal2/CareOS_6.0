package sim.android.mtkcit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import sim.android.mtkcit.R;
import sim.android.mtkcit.cittools.CITShellExe;
import sim.android.mtkcit.runintest.*;
import sim.android.mtkcit.cittools.CITTools;
import android.content.SharedPreferences;

public class RunInTest extends Activity implements OnItemSelectedListener,
		View.OnClickListener {
	private Button beginButton;
	private Button StopButton;
	private Spinner mSpinner;
	private ArrayAdapter<CharSequence> mSpnAdapter;
	public static long diruction;
	public static long beginTime;
	public static long stopTime;
	public static long currentTime;
	private boolean mThreadFlag;
	private boolean debugFlag = true;
	private final String TAG = "RunInTest";
	public static String RUNIN = "RunIn";

	private CITTools ct;
	private String RunInCmdBegin = "echo 1 > /sys/class/power_supply/battery/ChargerTest";
	private String RunInCmdStop = "echo 0 > /sys/class/power_supply/battery/ChargerTest";
	public static String PREFS_NAME = "runin_test";
	private int RUNIN_RESULT = 1;

	@Override
	protected void onCreate(Bundle bundle) {
		CITTools.LOGV(debugFlag, TAG, "onCreate");
		super.onCreate(bundle);
		setContentView(R.layout.runin);
		initRec();

	}

	private void initRec() {
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		//
		// SharedPreferences.Editor editor = settings.edit();
		// editor.putBoolean("first", true);
		// editor.commit();
		mSpinner = (Spinner) findViewById(R.id.spinner_duration);
		mSpnAdapter = ArrayAdapter.createFromResource(this,
				R.array.runin_diruction, android.R.layout.simple_spinner_item);
		mSpinner.setAdapter(mSpnAdapter);
		mSpinner.setOnItemSelectedListener(this);
		beginButton = (Button) findViewById(R.id.btn_begin);
		beginButton.setOnClickListener(this);
		StopButton = (Button) findViewById(R.id.btn_stop);
		StopButton.setOnClickListener(this);

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		CITTools.LOGV(debugFlag, TAG, "position=" + position);
		//diruction = Long.parseLong(mSpnAdapter.getItem(position).toString()) * 1000;
		diruction = Long.parseLong(mSpnAdapter.getItem(position).toString()) * 1000	*60*60;

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		CITTools.LOGV(debugFlag, TAG, " onResume() =");
		ct = CITTools.getInstance(this);
		ct.stopAutoBrightness(this);
		ct.setBrightness(20);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean first = settings.getBoolean("first", true);
		CITTools.LOGV(debugFlag, TAG, "first=" + first);

		if (!first) {
			boolean test_complate = settings.getBoolean("test_complate", false);
			CITTools.LOGV(debugFlag, TAG, "test_complate=" + test_complate);

			if (test_complate) {
				showDialog();
			} else {
				showDialogFail();

			}
		}

		super.onResume();
	}

	private void showDialog() {
		Log.v(TAG, "showDialog");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		long testdiruction = settings.getLong("diruction", 0);
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("RUN IN 测试完成，测试时间 :\n" + testdiruction + "小时");

		// / (1000 * 3600) + "小时");

		builder.setTitle("RUN IN");
		// builder.setPositiveButton(
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						finish();
					}
				});

		builder.create().show();
	}

	private void showDialogFail() {
		Log.v(TAG, "showDialogFail");
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("测试未能完成");
		builder.setTitle("RUN IN");
		// builder.setPositiveButton(
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						finish();
					}
				});

		builder.create().show();
	}

	private void releaseResource() {
		ct.setBrightness(100);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("first", true);
		editor.putBoolean("test_complate", false);
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		releaseResource();
		try {
			String[] cmd = { "/system/bin/sh", "-c", RunInCmdStop };
			int ret = CITShellExe.execCommand(cmd);
			CITTools.LOGV(debugFlag, TAG, cmd[0] + cmd[1] + cmd[2]);
			if (0 == ret) {
				CITTools.LOGV(debugFlag, TAG, "execCommand success "
						+ CITShellExe.getOutput());

			} else {
				CITTools.LOGV(debugFlag, TAG, "execCommand error ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();
		beginTime = System.currentTimeMillis();
		stopTime = beginTime + diruction;
		CITTools.LOGV(debugFlag, TAG, "beginTime=" + beginTime);
		CITTools.LOGV(debugFlag, TAG, "stopTime=" + stopTime);
		String[] cmdx = { "/system/bin/sh", "-c", "echo" };
		int ret;
		if (id == R.id.btn_begin) { // must
			ct.getSDPaths();
			CITTools.LOGV(debugFlag, TAG, "ct.SD1Path=" + ct.SD1Path);
			RUNIN = ct.SD1Path + File.separator + "RUNIN";
			ct.creatFile("test.txt", RUNIN);
			InputStream is = getResources().openRawResource(R.raw.fhzy);
			InputStream is2 = getResources().openRawResource(R.raw.testvideo);
			ct.cpFileToSD("fhzy.mp3", is, RUNIN);
			ct.cpFileToSD("test.3gp", is2, RUNIN);
			if (!ct.checkFile("test.3gp", RUNIN)) {
				CITTools.LOGV(debugFlag, TAG, RUNIN);

				Toast.makeText(this, R.string.no_test3gp, 1).show();
				return;

			}
			if (!ct.checkFile("fhzy.mp3", RUNIN)) {
				CITTools.LOGV(debugFlag, TAG, RUNIN);
				Toast.makeText(this, R.string.no_testmp3, 1).show();
				return;

			}
			try {
				String[] cmd = { "/system/bin/sh", "-c", RunInCmdBegin };
				ret = CITShellExe.execCommand(cmd);
				CITTools.LOGV(debugFlag, TAG, cmd[0] + cmd[1] + cmd[2]);

				if (0 == ret) {
					CITTools.LOGV(debugFlag, TAG,
							" btn_begin execCommand success ");

				} else {
					CITTools.LOGV(debugFlag, TAG, "execCommand error ");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("diruction", diruction / (1000 * 3600));
			editor.putBoolean("first", false);
			editor.putBoolean("test_complate", false);
			editor.commit();
			CITTools.LOGV(debugFlag, TAG, "diruction=" + diruction);

			editor.commit();

			intent.setClass(this, MemoryTest.class);
			startActivity(intent);
		} else if (id == R.id.btn_stop) {
			try {
				/**
				 * "/system/bin/sh"; String desktopSh = "/bin/sh";
				 */

				String[] cmd = { "/system/bin/sh", "-c", RunInCmdStop };
				ret = CITShellExe.execCommand(cmd);
				CITTools.LOGV(debugFlag, TAG, cmd[0] + cmd[1] + cmd[2]);

				if (0 == ret) {
					CITTools.LOGV(
							debugFlag,
							TAG,
							"btn_stop  execCommand success  "
									+ CITShellExe.getOutput());

				} else {
					CITTools.LOGV(debugFlag, TAG, "execCommand error ");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finish();
		}
	}

}
