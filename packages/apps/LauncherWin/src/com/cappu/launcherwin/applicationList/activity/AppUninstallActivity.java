package com.cappu.launcherwin.applicationList.activity;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;
import com.cappu.launcherwin.applicationList.adapter.UninstallAppAdapter;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.widget.TopBar;

public class AppUninstallActivity extends BasicActivity {
    private ListView lv;
    private List<AppInfo> appList;
    LauncherApplication mLauncherApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_app);
        mLauncherApplication =  (LauncherApplication) getApplication();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        UninstallAppAdapter adapter = new UninstallAppAdapter(this);
        adapter.setList(initList());
        
        lv = (ListView) findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(myOnItemClickListener);
    }

    private List<AppInfo> initList() {
        appList = new LinkedList<AppInfo>();
        PackageManager pm = this.getPackageManager();
        List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo ai : packageList) {
            Intent i = pm.getLaunchIntentForPackage(ai.packageName);
            if (i != null) {
                if (!(((ai.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) || ((ai.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))) {
                    String appName = ai.applicationInfo.loadLabel(pm).toString();
                    //Drawable icon = mThemeTools.getAppDrawableIcon(i.getComponent().getPackageName(), i.getComponent().getClassName(),ai.applicationInfo.loadIcon(pm));
                    
                    //Drawable icon = new BitmapDrawable(ThemeRes.getInstance().getThemeResBitmap(i.getComponent().getPackageName()+"/"+i.getComponent().getClassName()+"/"+ThemeManager.getInstance().getThemeType(),ai.applicationInfo.loadIcon(pm)));
                    
                    Bitmap icon = ThemeRes.getInstance().getThemeResBitmap(i.getComponent().getPackageName()+"/"+i.getComponent().getClassName()+"/"+ThemeManager.getInstance().getCurrentThemeType(this),ai.applicationInfo.loadIcon(pm));
                    if(!ai.packageName.equals("com.iflytek.speechsuite")){
                        appList.add(new AppInfo(appName, icon, ai.packageName));
                    }
                }
            }
        }
        packageList = null;
        return appList;
    }
    
    @Override
    protected void onDestroy() {
        if(appList != null){
            appList.clear();
            appList = null;
        }
        
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
    
    private OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String pkg = appList.get(position).getPackage_name();
            Uri packageURI = Uri.parse("package:" + pkg);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppUninstallActivity.this.startActivity(uninstallIntent);
        }

    };
}
