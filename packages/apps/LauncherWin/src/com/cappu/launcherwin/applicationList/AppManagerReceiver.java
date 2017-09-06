package com.cappu.launcherwin.applicationList;

import com.cappu.launcherwin.WorkspaceUpdateReceiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AppManagerReceiver extends BroadcastReceiver {

	private static final String dy_Tag = "dengyingApp";

	private Context mContext;
	private static String mRemovePackageName = "";
	private static String mAddPackageName = "";

	private Thread mUpdateThread;
	private WorkspaceUpdateReceiver mUpdateReceiver;

	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;
		if(mUpdateReceiver == null){  //added by wangyang 2016.8.24
		    mUpdateReceiver = new WorkspaceUpdateReceiver();
		}
		Log.e(dy_Tag, "action=" + intent.getAction().toString());

		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
		    mUpdateReceiver.mCappuHandler.sendEmptyMessage(mUpdateReceiver.VIEW_UPDATE_NET_ISHARE);  //added by wangyang 2016.8.24
			String data = intent.getDataString();

			String packageName = data.substring((data.indexOf(":") + 1), data.length());

			mAddPackageName = packageName;
			
			Log.e(dy_Tag, "安装了:data=" + data + ",packageName=" + packageName + ",mAddPackageName=" + mAddPackageName + ",mRemovePackageName=" + mRemovePackageName);
			
			if(!mRemovePackageName.equals(packageName)){
				DBHelper helper = new DBHelper(context);
				ContentValues values = new ContentValues();

				values.put("app_pkg", packageName);
				values.put("type", 1);
				helper.insert(values);

				helper.close();
				mAddPackageName="";
			}
			
			Intent mIntent = new Intent("cappu.app.action.UPDATE");
			context.sendBroadcast(mIntent);
		} else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
		    mUpdateReceiver.mCappuHandler.sendEmptyMessage(mUpdateReceiver.VIEW_UPDATE_NET_ISHARE);//added by wangyang 2016.8.24
			String data = intent.getDataString();

			String packageName = data.substring((data.indexOf(":") + 1), data.length());

			mRemovePackageName = packageName;

			Log.e(dy_Tag, "卸载了:" + packageName + ",mAddPackageName=" + mAddPackageName + ",mRemovePackageName=" + mRemovePackageName);
			
			if (mUpdateThread == null || !mUpdateThread.isAlive()) {
				mUpdateThread = new Thread() {
					@Override
					public void run() {
						
						Log.e(dy_Tag, "mUpdateThread run");
						try {
							Thread.sleep(5000);//延时5秒
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
				};
				mUpdateThread.start();
			}
			/*DBHelper helper = new DBHelper(context);
			helper.delpkg(packageName);
			helper.close();*/
		} else if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
			String data = intent.getDataString();

			String packageName = data.substring((data.indexOf(":") + 1), data.length());

			Log.e(dy_Tag, "替换了:" + packageName);
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			Log.e(dy_Tag, "Handler mAddPackageName=" + mAddPackageName + ",mRemovePackageName=" + mRemovePackageName);
			
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				if( !"".equals(mRemovePackageName) && !"".equals(mAddPackageName) && mRemovePackageName.equals(mAddPackageName)){
					Log.e(dy_Tag,"AppManagerReceiver update not operation");//升级：删除又添加，啥都不处理
				}else{
					DBHelper helper = new DBHelper(mContext);
					
					if(!"".equals(mRemovePackageName)){
						helper.delpkg(mRemovePackageName);//删除
					}
					
					helper.close();
				}
				mRemovePackageName="";
				mAddPackageName="";
				break;
				
			default:
				mRemovePackageName="";
				mAddPackageName="";
				break;
			}
		}

	};
}