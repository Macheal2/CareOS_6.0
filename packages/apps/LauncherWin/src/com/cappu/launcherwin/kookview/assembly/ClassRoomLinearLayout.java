package com.cappu.launcherwin.kookview.assembly;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cappu.launcherwin.Launcher;
import com.cappu.launcherwin.NoNetActivity;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.install.APKInstallTools;

public class ClassRoomLinearLayout extends LinearLayout {
    private static final int IMAGE_PICTURE_CYCLE=1001;
    private static final int IMAGE_PICTURE_UPDTATE=1002;
    private ImageView imgvClassRoom;
    private TextView txtTitle;
    private int cycleTime=5000;
    private int updateTime=600000;
    private int currentPosition=0;
    private Context mContext;
    private int mBorderRadius=10;
    private Launcher mLauncher;
	public ClassRoomLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext=context;
		if(context instanceof Launcher){
            this.mLauncher = (Launcher) context;
        }
		LayoutInflater.from(context).inflate(R.layout.class_room_view, this);
		imgvClassRoom=(ImageView)findViewById(R.id.imgv_class_room);
		txtTitle=(TextView)findViewById(R.id.txt_class_room_title);
		imgvClassRoom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					if (APKInstallTools.checkApkInstall(mContext,
							"com.tencent.mtt","com.tencent.mtt.SplashActivity")) {
						
						mLauncher.getSpeechTools().startSpeech(
								mContext.getString(R.string.cappu_classroom),
								mLauncher.getSpeechStatus());
					}
				} catch (Exception e) {
					Log.i("zazaaaaaaaaaaaaa",
							"e             e = " + e.toString());
				}
				ClassRoomInfo classInfo=ClassRoomManager.getInstance().getListMap().get(currentPosition-1);
				//hejianfeng add start
				if(isNetworkAvailable()){
					netStartActivity(classInfo);
				}else{
					noNetStartActivity(classInfo);
				}
				//hejianfeng add end
			}
		});
		ClassRoomManager.getInstance().updateListMap();
		mHandler.sendEmptyMessage(IMAGE_PICTURE_UPDTATE);
		mHandler.sendEmptyMessage(IMAGE_PICTURE_CYCLE);
	}
	private void updateView(int position){
		ClassRoomInfo classInfo=ClassRoomManager.getInstance().getListMap().get(position);
		imgvClassRoom.setBackgroundDrawable(new BitmapDrawable(classInfo.getImageBmp()));
		txtTitle.setText(classInfo.getTitle());
	}
	//hejianfeng add start
	private void noNetStartActivity(ClassRoomInfo classInfo){
		Intent intent = new Intent(mContext, NoNetActivity.class);  
		intent.putExtra("address_uri", classInfo.getPath());  
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.getApplicationContext().startActivity(intent); 
	}
	private void netStartActivity(ClassRoomInfo classInfo){
		Uri uri = Uri.parse(classInfo.getPath());    
		Intent it = new Intent(Intent.ACTION_VIEW, uri); 
		it.setPackage("com.tencent.mtt");
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.getApplicationContext().startActivity(it); 
	}
	private boolean isNetworkAvailable() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}
	//hejianfeng add end
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case IMAGE_PICTURE_CYCLE:
				if(currentPosition==ClassRoomManager.mCarouselNum){
					currentPosition=0;
				}
				updateView(currentPosition);
				this.sendEmptyMessageDelayed(IMAGE_PICTURE_CYCLE, cycleTime);
				currentPosition++;
				break;
			case IMAGE_PICTURE_UPDTATE:
				ClassRoomManager.getInstance().updateData();
				this.sendEmptyMessageDelayed(IMAGE_PICTURE_UPDTATE, updateTime);
				break;
			}
		}
	};
}
