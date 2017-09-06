package com.cappu.launcherwin;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.LauncherLog;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ThemePreviewActivity extends Activity{
	private int currentres;
	private int currentPosition;
	private RelativeLayout ryPreview;
	private Button btnCancel;
	private Button btnSet;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_preview);
		ryPreview=(RelativeLayout)findViewById(R.id.ry_theme_preview);
		currentres=getIntent().getIntExtra("setWallpaper", 0);
		currentPosition=getIntent().getIntExtra("currentPosition", 0);
		if(currentPosition==-1){
			ryPreview.setBackground(new BitmapDrawable(ThemeManager.getInstance().getCurWallpaperBmp()));
		}else{
			ryPreview.setBackgroundResource(currentres);
		}
		btnCancel=(Button)findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		btnSet=(Button)findViewById(R.id.btn_set);
		btnSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LauncherLog.e("hejianfeng", "jeffhejianfeng setOnClickListener");
				startActivity(new Intent(ThemePreviewActivity.this,Launcher.class).setAction(BasicKEY.THEME_WALLPAPER_ACTION));
				Settings.Global.putInt(getContentResolver(), "setWallpaper", currentres);
				Settings.Global.putInt(getContentResolver(), "current_theme_bg", currentPosition);
				finish();
			}
		});
		
	}
	
}
