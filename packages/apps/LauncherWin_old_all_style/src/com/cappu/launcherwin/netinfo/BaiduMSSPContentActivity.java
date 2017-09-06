
package com.cappu.launcherwin.netinfo;

import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.netinfo.Card.ViewHolder;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.CappuDialog;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.cappu.launcherwin.netinfo.widget.TabTopBar;
import com.cappu.launcherwin.widget.TopBar;
import android.view.KeyEvent;
import android.webkit.WebViewClient;


public class BaiduMSSPContentActivity  extends Activity implements View.OnClickListener{
	private WebView mWebView;
    private ImageButton mCancel;
    private TextView mTitle;   
    private TabTopBar mTabTopBar;    
    private ImageButton mOption;
	
	///////////////chanel//////////////////////
	//http://cpu.baidu.com/1035/fa9cf5dd
	//http://cpu.baidu.com/1021/fa9cf5dd
	//http://cpu.baidu.com/1012/fa9cf5dd
	//http://cpu.baidu.com/1006/fa9cf5dd
	//http://cpu.baidu.com/1001/fa9cf5dd
    //http://cpu.baidu.com/1006/fa9cf5dd
    //http://adbd.cappu.com 公司跳转用

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baidumssp_main_layout);
		mWebView = (WebView) findViewById(R.id.webView);

		WebSettings settings = mWebView.getSettings();  
		settings.setJavaScriptEnabled(true);

        String url = "http://adbd.cappu.com";
        // 在当前页面打开链接，而不是启动用户手机上安装的浏览器打开  
        mWebView.setWebViewClient(new WebViewClient() {  
            @Override  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                //view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                view.loadUrl(url);  
                return true;  
            }  
        });  

		
		mWebView.loadUrl(url); 
		//new HttpThread(url, mWebView, handler).start();

        init();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    public void init() {
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        
        mTabTopBar = (TabTopBar) findViewById(R.id.topbar_baidu);
           
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mOption.setVisibility(View.INVISIBLE);
        mOption.setImageResource(R.drawable.favorites_more);
        mCancel.setOnClickListener(this);
        mOption.setOnClickListener(this);
        
        mTitle.setOnClickListener(this);
        
        mTitle.setText(R.string.channellife);
    }

    @Override
    public void onClick(View v) {
         if(v == mCancel){
             if (mWebView.canGoBack()) {
                mWebView.goBack();// 返回前一个页面
             }else{
                finish();
             }
         }else if(mOption == v){
             //startActivity(new Intent(this, BaiduMSSPContentActivity.class));//modify by even
         }else if(mTitle == v){
            
         }

    }
    
/* Cappu:lau on: Mon, 14 Aug 2017 20:55:34 +0800
 * TODO: 修改在webView界面看视频的时候按HOME和POWER视频不退出
 */
    @Override
    protected void onPause() {
        super.onPause();
        mWebView.reload();
    }
// End of Cappu:lau

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

