package com.cappu.launcherwin;

import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		
		TextView txt_version = (TextView) findViewById(R.id.txt_version);
		
		TextView txt_android = (TextView) findViewById(R.id.txt_android);
		
		TextView txt_model = (TextView) findViewById(R.id.txt_model);
		
		txt_version.setText(android.os.Build.DISPLAY);
		
		txt_model.setText(android.os.Build.BRAND);
		
		txt_android.setText(android.os.Build.VERSION.RELEASE);
		
		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);	
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
}
