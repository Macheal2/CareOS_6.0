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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;
import com.cappu.launcherwin.applicationList.DBHelper;
import com.cappu.launcherwin.applicationList.adapter.MainAdapter;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.widget.TopBar;

//added by yzs begin
import com.cappu.widget.CareButton;
//added by yzs end

public class PlayCenterActivity extends BasicActivity {
	
    private static final String dy_Tag = "dengyingApp";	
	
    private ListView mList;

    private List<AppInfo> appList;
    private MainAdapter adapter;

    private ImageButton mCancel;
    private TextView mTitle;
    private ImageButton mOption;

    //added by yzs begin
    private CareButton mAddApp;
    //private View mAddApp;
    //added by yzs end

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
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);    
        
        Intent i = new Intent(CAPPU_ACTION);
        mCappuApps = mPackageManager.queryIntentActivities(i,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
       // mType = ThemeManager.getInstance().getCurrentThemeType(this);
       mType = 3;
	    
        Log.e(dy_Tag,"mType = " + mType);
        
        adapter = new MainAdapter(this);
        adapter.setList(initList());

        mList = (ListView) findViewById(R.id.listView);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(mOnItemClickListener);
        mList.setOnItemLongClickListener(mOnItemLongClickListener);

        //addede by yzs begin
        mAddApp = (CareButton) findViewById(R.id.addapp_button);
        //mAddApp = (LinearLayout) findViewById(R.id.addapp_button);
        //added by yzs end

        
        mAddApp.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view){
                Intent in = new Intent(PlayCenterActivity.this, AppAddActivity.class);
                in.putExtra("type",mType);
                
                PlayCenterActivity.this.startActivity(in);
            }
        });

        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mOption.setVisibility(View.VISIBLE);
        mCancel.setOnClickListener(myOnClickListener);
        mTitle.setText(R.string.app_name_game);
        mCancel.setOnClickListener(myOnClickListener);
        mOption.setOnClickListener(myOnClickListener);
        
        initPopuWindow();

        initItemPopuWindow();
    }

    private List<AppInfo> initList() {
        appList = new LinkedList<AppInfo>();

        List<AppInfo> al = new LinkedList<AppInfo>();

       /*int type = ThemeManager.getInstance().getCurrentMode();
        if(type == 2){
            type = 3;
        }*/
		int type = ThemeManager.getInstance().getCurrentThemeType(this);
        
        for(int i = 0; i < mCappuApps.size(); i++){
            ResolveInfo resolveInfo = mCappuApps.get(i);
            String name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            //Drawable icon = new BitmapDrawable(ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager)));
            
            Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager));
            al.add(new AppInfo(name,icon, packageName,className));                       
        }          
        
        for(int i = 0; i < mApps.size(); i++){
            ResolveInfo resolveInfo = mApps.get(i);
            String name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            //Drawable icon = new BitmapDrawable(ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager)));
            //Drawable icon = new BitmapDrawable(ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,mResolveInfo.loadIcon(mPackageManager)));
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
            	/*if(packname.equals(mi.getPackage_name()) && activityName.equals(mi.getActivity_name())){
    				appList.add(mi);
    				break;            		
            	}*/
            	
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);

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
                
                if("com.android.cappu".equals(packageName)){
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

    private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener(){
        @Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
			// TODO Auto-generated method stub
 
            if (appList.get(position).getPackage_name() == null) {
                return true;
            }

            showPopuWindowItemMenu(position);
            
			return true;
		}
    };

    private OnClickListener myOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
            case TopBar.LEFT_ID:
                PlayCenterActivity.this.finish();
                break;
            case TopBar.RIGHT_ID:
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();// 这里写明模拟menu的PopupWindow退出就行
                } else {
                    showPopuWindowMenu();
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        showPopuWindowMenu();
        return false;// 返回为true 则显示系统menu
    }

    private void showPopuWindowMenu() {
        if (mPopupWindow != null) {
            if (!mPopupWindow.isShowing()) {
                /* 最重要的一步：弹出显示 在指定的位置(parent) 最后两个参数 是相对于 x / y 轴的坐标 */
                mPopupWindow.showAtLocation(sub_view.findViewById(R.id.menu), Gravity.BOTTOM, 0, 0);
            }
        }
    }

    private PopupWindow mPopupWindow = null;
    private View sub_view = null;

    private void hidePopuWindowMenu() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();// 这里写明模拟menu的PopupWindow退出就行
        }
    }

    private void initPopuWindow() {
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        /* 设置显示menu布局 view子VIEW */
        sub_view = mLayoutInflater.inflate(R.layout.cmenu_layout, null);
        /* 第一个参数弹出显示view 后两个是窗口大小 */
        mPopupWindow = new PopupWindow(sub_view, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        /* 设置背景显示 */
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_screen));
        /* 设置触摸外面时消失 */
        mPopupWindow.setOutsideTouchable(true);
        /* 设置系统动画 */
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.update();
        mPopupWindow.setTouchable(true);
        /* 设置点击menu以外其他地方以及返回键退出 */
        mPopupWindow.setFocusable(true);
        /**
         * 1.解决再次点击MENU键无反应问题 2.sub_view是PopupWindow的子View
         */
        sub_view.setFocusableInTouchMode(true);
        sub_view.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindow.isShowing())) {
                    hidePopuWindowMenu();// 这里写明模拟menu的PopupWindow退出就行
                    return true;
                }
                return false;
            }
        });

        /* 监听MENU事件 */
        sub_view.findViewById(R.id.action_remove).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // doSomething
                Intent in = new Intent(PlayCenterActivity.this, AppUninstallActivity.class);
                PlayCenterActivity.this.startActivity(in);
                hidePopuWindowMenu();
            }
        });
        sub_view.findViewById(R.id.action_show).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // doSomething
                Intent in = new Intent(PlayCenterActivity.this, AllAppActivity.class);
                PlayCenterActivity.this.startActivity(in);
                hidePopuWindowMenu();
            }
        });

    }

    private PopupWindow mPopupWindowItemMenu = null;
    private View sub_view_item_menu = null;

    private void showPopuWindowItemMenu(int pos) {
    	
        final int p = pos;
        final String pkg = (String) appList.get(pos).getPackage_name();
        final String activity = (String) appList.get(pos).getActivity_name();
        
        if (mPopupWindowItemMenu != null) {
            if (!mPopupWindowItemMenu.isShowing()) {
                /* 最重要的一步：弹出显示 在指定的位置(parent) 最后两个参数 是相对于 x / y 轴的坐标 */
                mPopupWindowItemMenu.showAtLocation(sub_view_item_menu.findViewById(R.id.menu), Gravity.BOTTOM, 0, 0);

                sub_view_item_menu.findViewById(R.id.run).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                                 
                        /*if(activity != null && !activity.equals("")){
                            Intent intent = new Intent();
                            ComponentName cn = new ComponentName(pkg, activity);
                            intent.setComponent(cn);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            
                            startActivity(intent); 
                    	}else{
                    		PackageManager pm = getPackageManager();
                            Intent i = pm.getLaunchIntentForPackage(pkg);

                            if (i != null){
                                startActivity(i);
                            }
                    	} */              
                             
                        
                        PackageManager pm = getPackageManager();
                        String packageName = appList.get(p).getPackage_name();
                        String activityName = appList.get(p).getActivity_name();
                                                           
                        if(activityName != null && !activityName.equals("")){
                            Intent intent = new Intent(Intent.ACTION_MAIN, null);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            ComponentName cn = new ComponentName(packageName, activityName);
                            intent.setComponent(cn);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
                            
                            if("com.android.cappu".equals(packageName)){
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
                                            
                        
                        hidePopuWindowItemMenu();
                    }
                });
                
                sub_view_item_menu.findViewById(R.id.del).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
 
                        DBHelper helper = new DBHelper(PlayCenterActivity.this);
             
                        helper.delByActivityByType(pkg,activity,mType);
                        helper.close();
                        
                        appList.remove(p);
                        adapter.notifyDataSetChanged();
                        
                        hidePopuWindowItemMenu();
                    }
                });
                
                sub_view_item_menu.findViewById(R.id.modify).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // doSomething
                    }
                });
                
                sub_view_item_menu.findViewById(R.id.modify).setVisibility(View.GONE);
                sub_view_item_menu.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // doSomething
                        hidePopuWindowItemMenu();
                    }
                });
            }
        }
    }

    private void hidePopuWindowItemMenu() {
        if (mPopupWindowItemMenu.isShowing()) {
            mPopupWindowItemMenu.dismiss();// 这里写明模拟menu的PopupWindow退出就行
        }
    }

    private void initItemPopuWindow() {
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        /* 设置显示menu布局 view子VIEW */
        sub_view_item_menu = mLayoutInflater.inflate(R.layout.item_menu_layout, null);
        /* 第一个参数弹出显示view 后两个是窗口大小 */
        mPopupWindowItemMenu = new PopupWindow(sub_view_item_menu, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        /* 设置背景显示 */
        mPopupWindowItemMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_screen));
        /* 设置触摸外面时消失 */
        mPopupWindowItemMenu.setOutsideTouchable(true);
        /* 设置系统动画 */
        mPopupWindowItemMenu.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindowItemMenu.update();
        mPopupWindowItemMenu.setTouchable(true);
        /* 设置点击menu以外其他地方以及返回键退出 */
        mPopupWindowItemMenu.setFocusable(true);
        /**
         * 1.解决再次点击MENU键无反应问题 2.sub_view是PopupWindow的子View
         */
        sub_view_item_menu.setFocusableInTouchMode(true);
        sub_view_item_menu.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowItemMenu.isShowing())) {
                    hidePopuWindowItemMenu();// 这里写明模拟menu的PopupWindow退出就行
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        hidePopuWindowMenu();
        hidePopuWindowItemMenu();
    }
    
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
        
        Log.e(dy_Tag,"PlayCenterActivity,update mType = " + mType);
        
        adapter = new MainAdapter(this);
        adapter.setList(initList());
        
        mList.setAdapter(adapter);
    }
    
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		
		 Log.e(dy_Tag,"PlayCenterActivity,registerBoradcastReceiver");
		
		//myIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
		//myIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
		//myIntentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
		myIntentFilter.addAction("cappu.app.action.UPDATE");
		
		this.registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	public void unregisterBoradcastReceiver() {
		
		 Log.e(dy_Tag,"PlayCenterActivity,unregisterBoradcastReceiver");
		
		this.unregisterReceiver(mBroadcastReceiver);
	}    

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
	        Log.e(dy_Tag,"GooduseActivity,action=" + action);
			if (action.equals("cappu.app.action.UPDATE")) {
				update();
			}
		}
	};	

}
