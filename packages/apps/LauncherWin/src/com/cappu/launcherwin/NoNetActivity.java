package com.cappu.launcherwin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
/* Cappu:liukun on: Fri, 25 Aug 2017 18:31:40 +0800
 * TODO: 修改云课堂没有连网时界面
 */
import android.view.MotionEvent;
import android.graphics.Color;
// End of Cappu:liukun

import android.widget.Button;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.kookview.assembly.ClassRoomInfo;

public class NoNetActivity extends BasicActivity {
	private Button btnReload;
	private Uri addressUri;
	private void netStartActivity(Uri addressUri){
		Intent it = new Intent(Intent.ACTION_VIEW, addressUri); 
		it.setPackage("com.tencent.mtt");
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(it); 
	}
	private boolean isNetworkAvailable() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_net);
		Intent intent = getIntent();

		addressUri = Uri.parse(intent.getStringExtra("address_uri"));
		btnReload = (Button) findViewById(R.id.btn_reload);
		btnReload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(isNetworkAvailable()){
					netStartActivity(addressUri);
				}
			}
                        });
/* Cappu:liukun on: Fri, 25 Aug 2017 18:32:48 +0800
 * TODO: 修改云课堂没有连网时界面
 */
                btnReload.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                view.setBackgroundResource(R.drawable.cloud_classrom_btn_press);
                                btnReload.setTextColor(Color.parseColor("#ffffff"));
                            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                view.setBackgroundResource(R.drawable.cloud_classrom_btn_normal);
                                btnReload.setTextColor(Color.parseColor("#009dde"));
                            }
                        return false;
                        }                                                                                                                                                                                                       });
// End of Cappu:liukun
        }
}
