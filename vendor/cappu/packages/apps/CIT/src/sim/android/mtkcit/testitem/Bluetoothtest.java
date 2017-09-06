package sim.android.mtkcit.testitem;

import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.R;
import sim.android.mtkcit.CITActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Bluetoothtest extends TestBase {
	/** Called when the activity is first created. */
	private TextView allNetWork;
	private Button btn_success;
	private Button btn_fail;
	// 扫描结果列表
	private StringBuffer sb = new StringBuffer();
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private int num;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_bt);
		
		allNetWork = (TextView) findViewById(R.id.allNetWork);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);
		
		// 设置广播信息过滤
		IntentFilter intentFilter = new IntentFilter();   
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND); 
		intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED); 
//		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); 
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
		
		// 注册广播接收器，接收并处理搜索结果
		this.registerReceiver(mReceiver, intentFilter); 
		
		// open bt
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mAdapter.isEnabled())
			mAdapter.enable();
		
		if (mAdapter.getState() != mAdapter.STATE_ON)
			mAdapter.enable();
		
		// 延迟2s，否则BT扫描不到设备，why???
		SystemClock.sleep(3000L);
        
        // modified by bruce for translate string begin
		//allNetWork.setText("搜索中...");
        allNetWork.setText(R.string.bt_searching);
        // modified by bruce for translate string end
		getAllNetWorkList();
	}

	// 搜索周围的蓝牙设备
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			Log.i("Bluetoothtest000","11111111111111111111111");
		    //找到设备
			if (action.equals(BluetoothDevice.ACTION_FOUND) ||
					action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // added by bruce for fix exception begin
                if (device == null){
                    Log.i("bruce_nan", "CIT_Bluetoothtest: device is null");
                return;
                }
                // added by bruce for fix exception end
				
				num++;	
				btn_success.setEnabled(true);
				
				Log.i("Bluetoothtest000","num=" + num);
				
				//执行更新列表的代码
                // modified by bruce for translate string begin
                /*
				sb = sb.append("【" + device.getName() + "】 地址:   ")
						.append(device.getAddress() + "\n\n");
				allNetWork.setText("扫描到的蓝牙设备：\n" + sb.toString());
                */              
                sb = sb.append("【" + device.getName() + "】 ")
                        .append(getString(R.string.bt_address))
						.append(device.getAddress() + "\n\n");
				allNetWork.setText(getString(R.string.bt_search_result) + sb.toString());
                // modified by bruce for translate string end
			}
			//搜索完成
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (num == 0) {
                    // modified by bruce for translate string begin
					//Toast.makeText(context, "搜索完成,未搜索到任何设备", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, R.string.bt_no_device, Toast.LENGTH_SHORT).show();
                    // modified by bruce for translate string end
				} else {
                    // modified by bruce for translate string begin
					//Toast.makeText(context, "搜索完成", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, R.string.bt_search_complete, Toast.LENGTH_SHORT).show();
                    // modified by bruce for translate string end
				}
			}
		}
	}; 
	
	public void getAllNetWorkList() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// 每次点击扫描之前清空上一次的扫描结果
		if (sb != null) {
			sb = new StringBuffer();
		}
		
		// close first
//		mAdapter.disable();
		
		// open bt
		if (!mAdapter.isEnabled())
			mAdapter.enable();
		
		if (mAdapter.getState() != mAdapter.STATE_ON)
			mAdapter.enable();
			
		// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去 
		if (!mAdapter.isDiscovering()) {
			mAdapter.startDiscovery();
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
		
		// close 
		mAdapter.disable();
		
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	protected void onStop() {
		Log.i("Bluetoothtest000", "onStop");
		
		// close 
		mAdapter.disable();
		
		super.onStop();
	}
}
