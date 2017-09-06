package sim.android.mtkcit;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import sim.android.mtkcit.cittools.CITTools;

public class MachineMain extends ListActivity {
	private ArrayList<String> Items = new ArrayList<String>();
	public static final int MACHINE_AUTO_TEST = 0;
	public static final int MACHINE_SENSOR_AUTO_TEST = 2;
	public static final int MACHINE_ITEMS_TEST = 1;
	public static final int WBG_TEST = 3;

	public static final int RUNIN_TEST = 4;

	public static int item_test_type = -1;
	public static int auto_test_type = -1;

	// public static final String CIT_TYPE = "CIT type";
	// public static final String CIT_ITEM_IDX = "CIT idex";
	// public static final String CIT_ITEM_CNT = "CIT count";
	private String as[];
	boolean m_bBatteryTesting;
	boolean m_bTemperature;
	AudioManager am;
	String batteryStatus;
	int nCurrentMusicVolume;
	int bUsbDebug;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		Log.v("fengxuanyang", "onListItemClick--------position=" + position);
		switch (position) {
		case CITBroadcastReceiver.MACHINE_AUTO_TEST:
			auto_test_type = CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO;
			intent.setClass(this, AutoTestActivity.class);
			intent.putExtra(AutoTestActivity.AUTO_TEST_TYPE, auto_test_type);
			startActivity(intent);
			break;
		case CITBroadcastReceiver.MACHINE_SENSOR_AUTO_TEST:
			auto_test_type = CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO;
			intent.setClass(this, AutoTestActivity.class);
			intent.putExtra(AutoTestActivity.AUTO_TEST_TYPE, auto_test_type);
			startActivity(intent);
			break;

		case CITBroadcastReceiver.WBG_TEST:
			auto_test_type = CITBroadcastReceiver.TEST_TYPE_WBG_AUTO;
			intent.setClass(this, AutoTestActivity.class);
			intent.putExtra(AutoTestActivity.AUTO_TEST_TYPE, auto_test_type);
			startActivity(intent);
			break;
		case CITBroadcastReceiver.MACHINE_ITEMS_TEST:
			intent.setClass(this, ItemTestActivity.class);
			item_test_type = CITBroadcastReceiver.TEST_TYPE_MACHINE_ITEMS_TEST;
			 intent.putExtra(ItemTestActivity.ITEM_TEST_TYPE, item_test_type);
			startActivity(intent);
			break;
		case CITBroadcastReceiver.RUNIN_TEST:
			intent.setClass(this, RunInTest.class);
			startActivity(intent);
			break;
		}

		super.onListItemClick(l, v, position, id);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.pcbamain);
		getListItems();

		// bUsbDebug = Settings.Secure.getInt(getContentResolver(),
		// Settings.Secure.ADB_ENABLED, 0);
		// if (bUsbDebug == 0)
		// Settings.Secure.putInt(getContentResolver(),
		// Settings.Secure.ADB_ENABLED, 1);
	}

	// @Override
	// protected void onDestroy() {
	// if (bUsbDebug == 0)
	// Settings.Secure.putInt(getContentResolver(),
	// Settings.Secure.ADB_ENABLED, 0);
	// super.onDestroy();
	// }

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onStart() {
		getWindow().setType(WindowManager.LayoutParams.TYPE_BASE_APPLICATION);
		super.onStart();
	}

	private void getListItems() {
		as = getResources().getStringArray(R.array.MachineTest);
		int j = 0;
		do {
			if (j < as.length) {
				Items.add(as[j]);
				j++;
			} else {
				setListAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, Items));
				getListView().setTextFilterEnabled(true);
				return;
			}
		} while (true);
	}
}
