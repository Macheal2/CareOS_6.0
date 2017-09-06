package sim.android.mtkcit.testitem;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.R;
import sim.android.mtkcit.CITActivity;
import android.os.SystemClock;
import android.util.Log;

// added by bruce for translate string begin
import android.content.Context;
// added by bruce for translate string end

public class Wifirssitest extends TestBase {
	/** Called when the activity is first created. */
	private TextView allNetWork;
	private Button scan;
	private Button start;
	private Button stop;
	private Button check;
	private WifiAdmin mWifiAdmin;
	private Button btn_success;
	private Button btn_fail;
	// 扫描结果列表
	private List<ScanResult> list;
	private ScanResult mScanResult;
	private StringBuffer sb = new StringBuffer();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifirssi);
		mWifiAdmin = new WifiAdmin(Wifirssitest.this);
		
		// open wifi for test.
		mWifiAdmin.closeWifi();
		SystemClock.sleep(500L);	//Bug 16168

		mWifiAdmin.openWifi();
		SystemClock.sleep(200L);
		
		init();
	}

	public void init() {
		allNetWork = (TextView) findViewById(R.id.allNetWork);
//		scan = (Button) findViewById(R.id.scan);
//		start = (Button) findViewById(R.id.start);
//		stop = (Button) findViewById(R.id.stop);
//		check = (Button) findViewById(R.id.check);
//		scan.setOnClickListener(new MyListener());
//		start.setOnClickListener(new MyListener());
//		stop.setOnClickListener(new MyListener());
//		check.setOnClickListener(new MyListener());
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);
		
		getAllNetWorkList();
	}

//	private class MyListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			switch (v.getId()) {
//			case R.id.scan:// 扫描网络
//				getAllNetWorkList();
//				break;
//			case R.id.start:// 打开Wifi
//				mWifiAdmin.openWifi();
//				Toast.makeText(Wifirssitest.this,
//						"当前wifi状态为：" + mWifiAdmin.checkState(), 1).show();
//				break;
//			case R.id.stop:// 关闭Wifi
//				mWifiAdmin.closeWifi();
//				Toast.makeText(Wifirssitest.this,
//						"当前wifi状态为：" + mWifiAdmin.checkState(), 1).show();
//				break;
//			case R.id.check:// Wifi状态
//				Toast.makeText(Wifirssitest.this,
//						"当前wifi状态为：" + mWifiAdmin.checkState(), 1).show();
//				break;
//			default:
//				break;
//			}
//		}
//
//	}

	public void getAllNetWorkList() {
		boolean flag = true;
		
		// 每次点击扫描之前清空上一次的扫描结果
		if (sb != null) {
			sb = new StringBuffer();
		}
		
		// close first
		//mWifiAdmin.closeWifi();
		
		// open wifi
		mWifiAdmin.openWifi();
		
		// 开始扫描网络
		while(flag) {	// 循环搜索
			mWifiAdmin.startScan();
			list = mWifiAdmin.getWifiList();
			if (list != null) {
//				Log.i("WifirssiTest", "11111");
				int num = list.size();
				
				if (num > 0)
					flag = false;
				
				if (num > 5)
					num = 5;
				
				for (int i = 0; i < num/*list.size()*/; i++) {
					// 得到扫描结果
					mScanResult = list.get(i);
	//				sb = sb.append(mScanResult.BSSID + "  ")
	//						.append(mScanResult.SSID + "   ")
	//						.append(mScanResult.capabilities + "   ")
	//						.append(mScanResult.frequency + "   ")
	//						.append(mScanResult.level + "\n\n");

                    // modified by bruce for translate string begin
					//sb = sb.append("【" + mScanResult.SSID + "】 信号强度:   ")
					//		.append(mScanResult.level + "\n\n");
                    sb = sb.append("【" + mScanResult.SSID + "】")
                            .append(getString(R.string.wifi_signal_strength))
                            .append(mScanResult.level + "\n\n");
                    // modified by bruce for translate string end
					
//					Log.i("WifirssiTest", "sb=" + sb.toString());
				}
				
                // modified by bruce for translate string begin
				//allNetWork.setText("扫描到的wifi网络(前五个)：\n" + sb.toString());
				allNetWork.setText(getString(R.string.wifi_search_result) + sb.toString());
				// modified by bruce for translate string end

				btn_success.setEnabled(true);
			}
		}
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}

		
		// close wifi for test.
		mWifiAdmin.closeWifi();
		
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	protected void onStop() {
//		Log.i("WifirssiTest", "onStop()");
		
		// close wifi for test.
		mWifiAdmin.closeWifi();
		
 		super.onStop();
	}
	
}
