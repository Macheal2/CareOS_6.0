package com.cappu.launcherwin.basic;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.lang.reflect.Field;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.theme.SystemBarTintManager;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeManager.OnThemeChangedListener;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.TopBar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class BasicActivity extends Activity implements OnThemeChangedListener,OnClickListener {

    private static final String TAG = "BasicActivity";
    public ThemeManager mThemeManager;
    
    public SystemBarTintManager mSystemBarTintManager;
    
    protected ImageButton mCancel;
    protected TextView mTitle;
    protected ImageButton mOption;
    
    
    private LayoutInflater mInflater;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherLog.v(TAG, "onCreate() jeff");
        mThemeManager = ThemeManager.getInstance();
        mInflater = getLayoutInflater();
        int themeId = mThemeManager.getThemeId();
        this.setTheme(themeId);
        
     // 创建状态栏的管理实例  
        if(mSystemBarTintManager == null){
            mSystemBarTintManager = new SystemBarTintManager(this);  
         // 激活状态栏设置  
            mSystemBarTintManager.setStatusBarTintEnabled(true);  
            // 激活导航栏设置  
            mSystemBarTintManager.setNavigationBarTintEnabled(true);
        }
    }
    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(19)
	private void setupTransparentSystemBarsForLmp( int color) {
		// TODO(sansid): use the APIs directly when compiling against L sdk.
		// Currently we use reflection to access the flags and the API to set
		// the transparency
		// on the System bars.
		try {
			getWindow().getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
			getWindow()
					.clearFlags(
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
									| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			Field drawsSysBackgroundsField = WindowManager.LayoutParams.class
					.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
			getWindow().addFlags(drawsSysBackgroundsField.getInt(null));

			Method setStatusBarColorMethod = Window.class.getDeclaredMethod(
					"setStatusBarColor", int.class);
			Method setNavigationBarColorMethod = Window.class
					.getDeclaredMethod("setNavigationBarColor", int.class);
			setStatusBarColorMethod.invoke(getWindow(), color);
			setNavigationBarColorMethod.invoke(getWindow(), color);
		} catch (NoSuchFieldException e) {
			Log.w(TAG, "NoSuchFieldException while setting up transparent bars");
		} catch (NoSuchMethodException ex) {
			Log.w(TAG,
					"NoSuchMethodException while setting up transparent bars");
		} catch (IllegalAccessException e) {
			Log.w(TAG,
					"IllegalAccessException while setting up transparent bars");
		} catch (IllegalArgumentException e) {
			Log.w(TAG,
					"IllegalArgumentException while setting up transparent bars");
		} catch (InvocationTargetException e) {
			Log.w(TAG,
					"InvocationTargetException while setting up transparent bars");
		} finally {
		}
	}
    @Override
    public void setContentView(int layoutResID) {
        View view = mInflater.inflate(layoutResID, null);
        view.setFitsSystemWindows(true);
        super.setContentView(view);
    }
    
    @Override
    public LayoutInflater getLayoutInflater() {
        return super.getLayoutInflater();
    }
    
    private void initTopBar(){
    	LauncherLog.v(TAG, "initTopBar,jeff getTitle="+getTitle());
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        if(mCancel!=null){
        	mCancel.setOnClickListener(this);
        }
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        if(mTitle!=null &&(mTitle.getText()==""||mTitle.getText()==null)){
        	mTitle.setText(getTitle());
        }
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        int themeType =  mThemeManager.getCurrentThemeType(this);
        Log.i(TAG, "BasicActivity onCreate jeff themeType:"+themeType);
        Log.i(TAG, "BasicActivity onCreate class:"+this.getClass());
        Log.i(TAG, "BasicActivity onCreate class:"+this);
        initTopBar();
        if(themeType == ThemeManager.THEME_CHINESESTYLE){
            mSystemBarTintManager.setTintResource(R.drawable.shape_grey_normal);
        }else if(themeType == ThemeManager.THEME_NINE_GRIDS){
        	Log.i(TAG, "BasicActivity onCreate ThemeManager.THEME_NINE_GRIDS");
        	mSystemBarTintManager.setTintDrawable(new BitmapDrawable(ThemeManager.getInstance().getImagePiece().statusBarBmp));
        }else{
            mSystemBarTintManager.setTintResource(R.drawable.care_topbar_normal);
        }
        if(!(this.getClass().getName().equals("com.cappu.launcherwin.Launcher"))){
        	Log.i(TAG, "BasicActivity onCreate success");
        	mSystemBarTintManager.setTintResource(R.drawable.care_topbar_normal);
        	setupTransparentSystemBarsForLmp(0x55000000);
        }else{
        	setupTransparentSystemBarsForLmp(Color.TRANSPARENT);
        }
        mThemeManager.setThemeChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onThemeChanged(int theme) {
        Log.i(TAG, "BasicActivity onThemeChanged jeff theme:"+theme);
//        recreate();
    }


    @Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case TopBar.LEFT_ID:
			LauncherLog.v(TAG, "onClick,jeff Title="+getTitle());
			finish();
		default:
            break;
		}
	}

}
