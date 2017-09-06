package com.cappu.launcherwin.applicationList.activity;

import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;
import com.cappu.launcherwin.applicationList.DBHelper;
import com.cappu.launcherwin.applicationList.adapter.MainAdapter;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
public class ToolActivity extends BasicActivity {
	
    private static final String dy_Tag = "dengyingApp";	
	
    private ListView mList;

    private List<AppInfo> appList;
    private MainAdapter adapter;

    private List<ResolveInfo> mApps;    
    private PackageManager mPackageManager;

    private List<ResolveInfo> mCappuApps; 
    private static final String CAPPU_ACTION = "android.intent.cappu.LAUNCHER";    
    
    private int mType = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.good_use_activity);
        
        registerBoradcastReceiver();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);    
        
        Intent i = new Intent(CAPPU_ACTION);
        mCappuApps = mPackageManager.queryIntentActivities(i,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        
        mType = 4;
        
        Log.e(dy_Tag,"mType = " + mType);
        
        adapter = new MainAdapter(this);
        adapter.setList(initList());

        mList = (ListView) findViewById(R.id.listView);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(mOnItemClickListener);
    }

    private List<AppInfo> initList() {
        appList = new LinkedList<AppInfo>();

        List<AppInfo> al = new LinkedList<AppInfo>();

        int type = ThemeManager.getInstance().getCurrentThemeType(this);
        
        for(int i = 0; i < mCappuApps.size(); i++){
            ResolveInfo resolveInfo = mCappuApps.get(i);
            String name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager));
            al.add(new AppInfo(name,icon, packageName,className));
        }          
        
        for(int i = 0; i < mApps.size(); i++){
            ResolveInfo resolveInfo = mApps.get(i);
            String name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager));
            //屏蔽应用
            if(packageName.equals("com.android.development")){
                continue;
            }
            
            //替换应用的名称
            if(packageName.equals("com.android.settings") && className.equals("com.android.settings.Settings")){
                name = getResources().getString(R.string.gooduse_android_setting); 
            }else if(packageName.equals("com.baidu.searchbox")){
                name = getResources().getString(R.string.gooduse_baidu_search);
            }else if(packageName.equals("com.android.dialer")){
                name = getResources().getString(R.string.gooduse_dialor);//通话记录->电话
            }else{
                name = resolveInfo.loadLabel(mPackageManager).toString();
            }
            
            
            al.add(new AppInfo(name,icon, packageName,className));
        }       
        
        DBHelper helper=new DBHelper(this);

        Cursor cursor = helper.queryByType(mType);
        
        while (cursor.moveToNext()) {
            String packname = cursor.getString(1);// 获取第二列的值
            String activityName = cursor.getString(2);
            
            for (AppInfo mi : al) {
            	//新安装的没法获取到activity
            	if(packname.equals(mi.getPackage_name())){
	            	if(activityName != null && !activityName.equals("")){
	            		if(activityName.equals(mi.getActivity_name())){
	        				appList.add(mi);
	        				break; 
	            		}
	            	}else{
	    				appList.add(mi);
	    				break; 
	            	}
            	}
            }
        }

        cursor.close();
        helper.close();
        al.clear();
        al = null;

        return appList;
    }


    //modified by yzs begin
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
        @Override
		public void onItemClick(AdapterView<?> adapter, View view,int position, long id) {
        	
            PackageManager pm = getPackageManager();
            String packageName = appList.get(position).getPackage_name();
            String activityName = appList.get(position).getActivity_name();
            
            Log.e(dy_Tag,"onItemClick="+"packageName="+packageName+",activityName="+activityName);
            
             
            if(activityName != null && !activityName.equals("")){
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName(packageName, activityName);
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
                
                if("com.cappu.launcherwin".equals(packageName)){
                    intent.removeCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction("");
                    intent.setFlags(0);
                }
                
                startActivity(intent); 
        	}else{
                Intent i = pm.getLaunchIntentForPackage(packageName);

                if (i != null){
                    startActivity(i);
                }
        	}               
        }
    };

    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        if(mApps != null){
            mApps.clear();
            mApps = null;
        }
        
        if(mCappuApps!= null){
            mCappuApps.clear();
            mCappuApps = null;
        }
        
        unregisterBoradcastReceiver();
        if(appList != null){
            appList.clear();
            appList = null;
        }
    }

    private void update(){
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);    
        
        Intent i = new Intent(CAPPU_ACTION);
        mCappuApps = mPackageManager.queryIntentActivities(i,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        
        Log.e(dy_Tag,"ToolActivity,update mType = " + mType);
        
        adapter = new MainAdapter(this);
        adapter.setList(initList());
        
        mList.setAdapter(adapter);
    }
    
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		
		 Log.e(dy_Tag,"ToolActivity,registerBoradcastReceiver");
		myIntentFilter.addAction("cappu.app.action.UPDATE");
		
		this.registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	public void unregisterBoradcastReceiver() {
		
		 Log.e(dy_Tag,"ToolActivity,unregisterBoradcastReceiver");
		
		this.unregisterReceiver(mBroadcastReceiver);
	}    

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
	        Log.e(dy_Tag,"ToolActivity,action=" + action);
			if (action.equals("cappu.app.action.UPDATE")) {
				update();
			}
		}
	};	
	
}
