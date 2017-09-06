package com.cappu.launcherwin.applicationList.activity;

import java.util.LinkedList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.cappu.widget.CareMenu;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;
import com.cappu.launcherwin.applicationList.DBHelper;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.TopBar;

public class AppAddActivity extends BasicActivity {
	
    private static final String dy_Tag = "dengyingApp";		
	
    private ListView add_lv;
    private List<AppInfo> add_AppList;
    private List<AppInfo> all;
    private AddAppAdapter add_adapter;
    private int mType;
    
    private List<ResolveInfo> mApps; 
    private PackageManager mPackageManager;
    
    private List<ResolveInfo> mCappuApps; 
    private static final String CAPPU_ACTION = "android.intent.cappu.LAUNCHER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_add);

        mType = (int)(this.getIntent().getExtras().getInt("type",-1));
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);

        Intent i = new Intent(CAPPU_ACTION);
        mCappuApps = mPackageManager.queryIntentActivities(i,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        
        initList();
        add_adapter = new AddAppAdapter(this);
        add_adapter.setList(all, null);
        add_lv = (ListView) findViewById(R.id.addlist);
        add_lv.setAdapter(add_adapter);
        add_lv.setOnItemClickListener(mOnItemClickListener);//hejianfeng add 
        setListViewHeightBasedOnChildren(this, add_lv);
        if (add_adapter.isEmpty()) {
        	add_lv.setVisibility(View.GONE);
        } else {
        	add_lv.setVisibility(View.VISIBLE);
        }
        //hejianfeng add start
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mOption.setVisibility(View.VISIBLE);
        mOption.setOnClickListener(myOnClickListener);
        initMenuDialog();
        //hejianfeng add end
    }
    
    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }
    //hejianfeng add start
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {

			PackageManager pm = getPackageManager();
			String packageName = ((AppInfo)add_adapter.getItem(position)).getPackage_name();
			String activityName = ((AppInfo)add_adapter.getItem(position)).getActivity_name();

			Log.e(dy_Tag, "onItemClick=" + "packageName=" + packageName
					+ ",activityName=" + activityName);

			if (activityName != null && !activityName.equals("")) {
				Intent intent = new Intent(Intent.ACTION_MAIN, null);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName(packageName, activityName);
				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

				if ("com.cappu.launcherwin".equals(packageName)) {
					intent.removeCategory(Intent.CATEGORY_LAUNCHER);
					intent.setAction("");
					intent.setFlags(0);
				}
				try {
					startActivity(intent);
				} catch (Exception e) {
					Intent i = pm.getLaunchIntentForPackage(packageName);
					if (i != null) {
						startActivity(i);
					}
				}
			} else {
				try {
					Intent i = pm.getLaunchIntentForPackage(packageName);
					if (i != null) {
						startActivity(i);
					}
				} catch (Exception e) {
					LauncherLog.e(dy_Tag, "packageName="+packageName+"is not exist");
				}
			}
		}
	};
	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case TopBar.RIGHT_ID:
				if (mOptionDialog.isShowing()) {
					mOptionDialog.dismiss();// 这里写明模拟menu的PopupWindow退出就行
				} else {
			    	mOptionDialog.show();
				}
				break;
			default:
				break;
			}
		}
	};
	private CareMenu mOptionDialog;
	private Button btnCancel;

	private void initMenuDialog() {
		mOptionDialog = new CareMenu(this);
		mOptionDialog.setTitle(R.string.action_settings);
		mOptionDialog.addButton(R.string.uninstall_app);
		btnCancel = new Button(this);
		mOptionDialog.addCancelButton(btnCancel);
		mOptionDialog.setOnClickListener(new CareMenu.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId()==R.string.uninstall_app) {
					Intent in = new Intent(AppAddActivity.this, AppUninstallActivity.class);
					AppAddActivity.this.startActivity(in);
				} else if (v == btnCancel) {
					mOptionDialog.dismiss();
				}
			}
		});
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
    	mOptionDialog.show();
		return false;// 返回为true 则显示系统menu
	}
    //hejianfeng add end
    private void initList() {
        add_AppList = new LinkedList<AppInfo>();
        all = new LinkedList<AppInfo>();
        int type = ThemeManager.getInstance().getCurrentThemeType(this);
        String name = "";
        
        for(int i = 0; i < mCappuApps.size(); i++){
            ResolveInfo resolveInfo = mCappuApps.get(i);
            name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;

            //屏蔽应用
			if (className
					.equals("com.cappu.launcherwin.applicationList.activity.GooduseActivity")
					|| className
							.equals("com.cappu.launcherwin.applicationList.activity.ToolActivity")) {
				continue;
			}
            Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager));
            all.add(new AppInfo(name,icon, packageName,className));
        }
        
        for(int i = 0; i < mApps.size(); i++){
            ResolveInfo resolveInfo = mApps.get(i);
            name = resolveInfo.loadLabel(mPackageManager).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(packageName+"/"+className+"/"+type,resolveInfo.loadIcon(mPackageManager));
            
            //屏蔽应用
			if (packageName.equals("com.android.development")
					|| packageName.equals("com.android.contacts")
					|| packageName.equals("com.cappu.launcherwin")
					|| packageName.equals("com.cappu.remote.parent")
					|| packageName.equals("com.cappu.download")
					|| packageName.equals("com.iflytek.speechsuite")
					|| packageName.equals("com.cappu.halllockscreen")
					|| packageName.equals("com.cappu.collection")
					|| packageName.equals("com.magcomm.speech")) {
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
            
            all.add(new AppInfo(name,icon, packageName,className));
        }
        DBHelper helper = new DBHelper(this);
        Cursor cursor = helper.queryByTypeDesc(mType);
        while (cursor.moveToNext()) {
            String packname = cursor.getString(1);// 获取第二列的值
            String activityName = cursor.getString(2);
            
            for (AppInfo mi : all) {             
            	
            	if(packname.equals(mi.getPackage_name())){
	            	if(activityName != null && !activityName.equals("")){
	            		if(activityName.equals(mi.getActivity_name())){
	            			add_AppList.add(mi);
	        				break; 
	            		}
	            	}else{
	            		add_AppList.add(mi);
	    				break; 
	            	}
            	}
            }
        
        }
        cursor.close();
        helper.close();
    }

    public static void setListViewHeightBasedOnChildren(Context c, ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        WindowManager wm = (WindowManager) c.getApplicationContext().getSystemService("window");
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        
        int scr_h = dm.heightPixels;
        if (listAdapter == null) {
            return;
        }
        
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        int totalHeight = 0;
        View view = null;
        
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, scr_h / 8);
            totalHeight += scr_h / 8;// view.getMeasuredHeight();
        }
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

 
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mApps != null){
            mApps.clear();
            mApps = null;
        }
        if(mCappuApps != null){
            mCappuApps.clear();
            mCappuApps = null;
        }
        if(all!=null){
            all.clear();
            all = null;
        }
        if(add_AppList != null){
            add_AppList.clear();
            add_AppList = null;
        }
    }
    
    /****************************************************************************************************************************/
    public class AddAppAdapter extends BaseAdapter {

        private class GridHolder {
            ImageView appImage;
            TextView appName;
            ImageButton add;
        }
        private Context context;

        private List<AppInfo> list=new LinkedList<AppInfo>();
        private LayoutInflater mInflater;

        public AddAppAdapter(Context c) {
            super();
            this.context = c;

        }

        public void setList(List<AppInfo> list, Handler handler) {
            this.list.addAll(list);
            for (AppInfo mi : add_AppList) {
            	if(this.list.contains(mi)){
            		this.list.remove(mi);
            		this.list.add(0,mi);
            	}
            }
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int index) {
            return list.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }
        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            GridHolder holder;
            WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService("window");
            Display display = wm.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            
            int scr_w = dm.widthPixels;
            int scr_h = dm.heightPixels;
            
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_add_item, null);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, scr_h / 8);
                convertView.setLayoutParams(param);
                holder = new GridHolder();
                holder.appImage = (ImageView) convertView.findViewById(R.id.icon);
                holder.appName = (TextView) convertView.findViewById(R.id.appName);
                holder.add = (ImageButton) convertView.findViewById(R.id.button1);
                convertView.setTag(holder);
            } else {
                holder = (GridHolder) convertView.getTag();
            }
            
            final AppInfo info = list.get(index);
              
            if (info != null) {
                holder.appName.setText(info.getApp_name());
                holder.appName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
                holder.appName.setSingleLine(true);
                //holder.appImage.setImageDrawable(info.getApp_icon());
                holder.appImage.setImageBitmap(info.getApp_icon());
				holder.add.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						boolean isAdded = false;
						for (AppInfo mi : add_AppList) {
							String pkgName = info.getPackage_name();
							String activityName = info.getActivity_name();

							if (pkgName.equals(mi.getPackage_name())) {
								if (activityName != null
										&& !activityName.equals("")) {
									if (activityName.equals(mi
											.getActivity_name())) {
										isAdded = true;
										break;
									}
								} else {
									isAdded = true;
									break;
								}
							}
						}

						if (isAdded) {
							add_AppList.remove(info);
							//hejianfeng add start
							int position=all.indexOf(info);
							LauncherLog.v(dy_Tag, "all="+all.size());
							if(position<(add_AppList.size())){
								LauncherLog.v(dy_Tag, "position="+position);
								position=add_AppList.size();
							}
							LauncherLog.v(dy_Tag, "position end="+position);
							list.remove(info);
							list.add(position, info);
							//hejianfeng add end
							String pkgName = info.getPackage_name();
							String activityName = info.getActivity_name();

							Log.e(dy_Tag, "app remove,pkg=" + pkgName
									+ ",activityName=" + activityName);
							DBHelper helper = new DBHelper(
									getApplicationContext());
							helper.delByActivityByType(info.getPackage_name(),
									info.getActivity_name(), mType);
							helper.close();
						} else {
							add_AppList.add(0, info);

							String pkgName = info.getPackage_name();
							String activityName = info.getActivity_name();

							LauncherLog.e(dy_Tag, "app add,pkg=" + pkgName
									+ ",activityName=" + activityName);
							list.remove(info);
							list.add(0, info);
							DBHelper helper = new DBHelper(
									getApplicationContext());
							ContentValues values = new ContentValues();
							values.put("app_pkg", pkgName);
							values.put("app_activity", activityName);
							values.put("type", mType);
							helper.insert(values);
							helper.close();
						}
						AddAppAdapter.this.notifyDataSetChanged();
					}

				});
            }
              
        	boolean isAdded = false;
            for (AppInfo mi : add_AppList) {
            	
            	String pkgName = info.getPackage_name();
            	String activityName = info.getActivity_name();
            	
            	if(pkgName.equals(mi.getPackage_name())){
                    if(activityName != null && !activityName.equals("")){
                    	if(activityName.equals(mi.getActivity_name())){
                			isAdded = true;
                            break;
                    	}
                	}else{
            			isAdded = true;
                        break;    
                	} 
            	}
            }
            
            if(isAdded){
                holder.add.setImageResource(R.drawable.butt_del);
            }else{
            	holder.add.setImageResource(R.drawable.butt_add);
            }
            
            return convertView;
        }

    }
    /****************************************************************************************************************************/
}
