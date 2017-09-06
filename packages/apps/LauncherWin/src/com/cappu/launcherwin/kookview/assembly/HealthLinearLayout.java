package com.cappu.launcherwin.kookview.assembly;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.launcherwin.Launcher;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.widget.CircularRingPercentageView;
import com.cappu.launcherwin.widget.LauncherLog;

public class HealthLinearLayout extends LinearLayout {
	private final static String TAG="HealthLinearLayout";
	private LinearLayout mHealthView;
	private Context mContext;
	private Launcher mLauncher;
	
	private TextView txtHeart;
	private TextView txtStep;
	private CircularRingPercentageView circularProgress;
	private TextView txtVitalityTitle;
	private TextView txtVitalityContent;
	public HealthLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.health_view, this);
		mContext=context;
		if(context instanceof Launcher){
            this.mLauncher = (Launcher) context;
        }
		mHealthView=(LinearLayout)findViewById(R.id.ly_health_view);
		mHealthView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					if (APKInstallTools.checkApkInstall(mContext,
							"com.cappu.healthcenter","com.cappu.healthcenter.ui.activity.MainActivity")) {
						mLauncher.getSpeechTools().startSpeech(
								mContext.getString(R.string.cappu_health),
								mLauncher.getSpeechStatus());
					}
				} catch (Exception e) {
					Log.i("zazaaaaaaaaaaaaa",
							"e             e = " + e.toString());
				}
				openApp("com.cappu.healthcenter","com.cappu.healthcenter.ui.activity.MainActivity");
			}
		});
		txtHeart=(TextView)findViewById(R.id.txt_health_heart);
		txtStep=(TextView)findViewById(R.id.txt_health_step);
		circularProgress=(CircularRingPercentageView)findViewById(R.id.progress);
		txtVitalityTitle=(TextView)findViewById(R.id.vitality_title);
		txtVitalityContent=(TextView)findViewById(R.id.vitality_content);
	}
	public void setStepTxt(String step){
		txtStep.setText(step);
	}
	public void updataHealthData(){
		Uri uri = Uri
				.parse("content://com.cappu.healthcenter.db.HealthCenterProvider/SHARE_DATA");
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(uri, null, null, null,
					null);
			if (cursor.moveToFirst()) {
				String heart=cursor.getString(cursor.getColumnIndex("HEARTRATE_COUT"));
				LauncherLog.v(TAG, "updataHealthData,jeff heart="+heart);
				txtHeart.setText(heart);
				
				String step=cursor.getString(cursor.getColumnIndex("STEP_COUT"));
				LauncherLog.v(TAG, "updataHealthData,jeff step="+step);
				txtStep.setText(step);
				
				String score=cursor.getString(cursor.getColumnIndex("SCORE"));
				LauncherLog.v(TAG, "updataHealthData,jeff score="+score);
				float scoreValue=Float.parseFloat(score);
				if(scoreValue>0){
					circularProgress.setProgress(scoreValue);
				}
				
				String vitalityTitle=cursor.getString(cursor.getColumnIndex("TITLE"));
				LauncherLog.v(TAG, "updataHealthData,jeff vitalityTitle="+vitalityTitle);
				txtVitalityTitle.setText(vitalityTitle);
				
				String vitalityContent=cursor.getString(cursor.getColumnIndex("CONTENT"));
				LauncherLog.v(TAG, "updataHealthData,jeff vitalityContent="+vitalityContent);
				txtVitalityContent.setText(vitalityContent);
				
			}
		} catch (Exception e) {
			LauncherLog.v(TAG, "updataHealthHearts,jeff Exception:"+e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	/** 通过包名去启动一个应用 */
	private void openApp(String packageName, String className) {
		// TODO 把应用杀掉然后再启动，保证进入的是第一个页面
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ComponentName cn = new ComponentName(packageName, className);
		intent.setComponent(cn);
		mContext.getApplicationContext().startActivity(intent);
	}
}
