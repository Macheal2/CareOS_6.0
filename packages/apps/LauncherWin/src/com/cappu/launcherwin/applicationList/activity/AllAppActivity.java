package com.cappu.launcherwin.applicationList.activity;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.content.ComponentName;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;
import com.cappu.launcherwin.applicationList.adapter.AllAppAdapter;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.widget.TopBar;

public class AllAppActivity extends BasicActivity {
    private ListView lv;
    private List<AppInfo> appList;
    
    LauncherApplication mLauncherApplication;
    List<ResolveInfo> mApps;
    private AllAppAdapter adapter;
    PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_app);

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);

        adapter = new AllAppAdapter(this);
        lv = (ListView) findViewById(R.id.listView1);
        lv.setOnItemClickListener(myOnItemClickListener);
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApps.clear();
        mApps = null;
        if(appList != null){
            appList.clear();
            appList = null;
        }
    }


    @Override
    public void onResume(){
        super.onResume();    
        adapter.setList(initList());  
        lv.setAdapter(adapter);
    }

    private List<AppInfo> initList() {
        String name = "";

        appList = new LinkedList<AppInfo>();
        for(int i = 0; i < mApps.size(); i++){
            ResolveInfo resolveInfo = mApps.get(i);
            
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Bitmap icon = ThemeRes.getInstance().getDefModeIcon(packageName+"/"+className+"/",resolveInfo.loadIcon(mPackageManager));
            
            if(packageName.equals("com.android.development") || packageName.equals("com.cappu.launcherwin")){
                continue;   
            } 
    
            if(packageName.equals("com.iflytek.speechsuite")){
                continue;
            }
    
            if(packageName.equals("com.android.settings")){
                name = getResources().getString(R.string.gooduse_android_setting); 
            }else if(packageName.equals("com.baidu.searchbox")){
                name = getResources().getString(R.string.gooduse_baidu_search);
            }else if(packageName.equals("com.android.dialer")){
                name = getResources().getString(R.string.gooduse_dialor);//通话记录->电话
            }else{
                name = resolveInfo.loadLabel(mPackageManager).toString();
            } 
            appList.add(new AppInfo(name,icon, packageName,className));              
        }

        return appList;
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
    
    private OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
                
			PackageManager pm = getPackageManager();
            String packageName = appList.get(position).getPackage_name();
            String activityName = appList.get(position).getActivity_name();
            
            Log.e("dengying","onItemClick="+"packageName="+packageName+",activityName="+activityName);
            
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
}
