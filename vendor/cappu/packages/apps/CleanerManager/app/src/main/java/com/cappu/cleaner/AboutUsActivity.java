package com.cappu.cleaner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cappu.cleaner.update.ApkUpdate;
import com.cappu.cleaner.update.KookSharedPreferences;
import com.cappu.cleaner.update.UpdateCenter;
import com.cappu.cleaner.widget.TopBar;

//import com.cappu.common.updatecenter.ApkUpdate;
//import com.cappu.common.updatecenter.KookSharedPreferences;
//import com.cappu.common.updatecenter.UpdateCenter;
//import com.cappu.naquba.BuildConfig;

/**
 * 关于我们
 */
public class AboutUsActivity extends Activity implements TopBar.onTopBarListener, View.OnClickListener{
    TextView mTvVersion;
    TextView mTvNewVersion;
    RelativeLayout mUpdateItem;
    private TopBar mTopBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(this);
        mUpdateItem = (RelativeLayout) findViewById(R.id.rl_version);
        mTvVersion = (TextView) findViewById(R.id.tv_version);
        mTvNewVersion = (TextView) findViewById(R.id.tv_new_version);
        mUpdateItem.setOnClickListener(this);
        initData();
    }

    private void initData() {
        /**
         * 读取当前版本号
         */
        PackageInfo pkg = null;
        try {
            pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pkg != null) {
            mTvVersion.setText(pkg.versionName);
        }
        String versionName = KookSharedPreferences.getString(this, ApkUpdate.UPGRADE_VERSION_KEY);

        int versionCodeNew = KookSharedPreferences.getInt(this, ApkUpdate.UPGRADE_VCODE_KEY);
        int versionCode    = pkg.versionCode;
        Log.e("hmq", "----当前versionCode:" + versionCode);
        if (versionCodeNew > versionCode) {
            mTvNewVersion.setVisibility(View.VISIBLE);
        } else {
            mTvNewVersion.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onRightClick(View v) {}

    @Override
    public void onTitleClick(View v) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.rl_version:
                UpdateCenter.updateDetect(this);
                break;
        }
    }
}
