package cappu.sos;

import cappu.sos.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
   
public class SOSMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_main);
		
		initView();  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		
		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);
		
		/*ImageButton mBtnCancel = (ImageButton) findViewById(R.id.cancel);

		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/
		
		RelativeLayout location_layout = (RelativeLayout) findViewById(R.id.location_layout);
		
		location_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSMainActivity.this, LocationOverlayDemo.class);//SOSMap LocationOverlayDemo
				startActivity(i);
			}
		});		
		
		RelativeLayout sos_layout = (RelativeLayout) findViewById(R.id.sos_layout);
		
		sos_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSMainActivity.this, SOSShowActivity.class);
				startActivity(i);
			}
		});			
		
		Button sos_setting = (Button) findViewById(R.id.sos_setting);
		
		sos_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSMainActivity.this, SOSSetting.class);
				startActivity(i);				
			}
		});			
		
		Button personal = (Button) findViewById(R.id.personal);
		
		personal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SOSMainActivity.this, SOSPersonalEdit.class);
				startActivity(i);					
			}
		});			
		
	}
	
}
