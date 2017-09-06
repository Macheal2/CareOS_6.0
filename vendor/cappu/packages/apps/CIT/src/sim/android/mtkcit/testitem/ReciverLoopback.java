package sim.android.mtkcit.testitem;

import sim.android.mtkcit.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;



public class ReciverLoopback extends Activity {
	private TextView tvMes;
	private AudioManager am;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		isAutoPassOrFail = false;
//		layoutId = R.layout.test_loopback;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_loopback);

		tvMes = (TextView)findViewById(R.id.tv_mes);
//		
		am = (AudioManager) getSystemService(AUDIO_SERVICE);
//		
//		tvMes.setText(R.string.headset_illustration1);
//		
	}
	
	@Override
	protected void onPause() {
		am.setParameter("loopback", "reciver");
		unregisterReceiver(headsetReceiver);
		SystemClock.sleep(1000);
		super.onPause();
	}

	@Override
	protected void onResume() {
		am.setParameter("loopback", "reciver");
		registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		super.onResume();
	}
	
	private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
				int state = intent.getIntExtra("state", 0);
				if (state == 0) {
					tvMes.setText(R.string.reciver_loopback);
					am.setParameter("loopback", "reciver");
				}else{
					tvMes.setText(R.string.pull_heatset);
					am.setParameter("loopback", "off");
				}
			}
		}
	};
}
