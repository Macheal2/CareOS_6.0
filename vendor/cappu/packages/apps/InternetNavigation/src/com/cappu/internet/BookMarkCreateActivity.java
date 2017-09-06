package com.cappu.internet;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import com.cappu.internet.widget.TopBar;
//import com.cappu.internet.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;

public class BookMarkCreateActivity extends Activity {

	private SQLiteHelper mSqliteHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSqliteHelper = new SQLiteHelper(this);
		mSqliteHelper.open();

		setContentView(R.layout.bookmark_create);
		init();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		mSqliteHelper.close();

		super.onDestroy();
	}

	private void init() {

		/*ImageButton mBtnCancel = (ImageButton) findViewById(R.id.cancel);

		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/

		final EditText mEditName = (EditText) findViewById(R.id.edit_name);
		final EditText mEditUrl = (EditText) findViewById(R.id.edit_url);

		// 实例化监听
		onTopBarListener mTopBarListener = new onTopBarListener(){
		    public void onLeftClick(View v){
		    	finish();
		    }
		    
		    public void onRightClick(View v){
		    	String name = mEditName.getText().toString().trim();

				String url = mEditUrl.getText().toString().trim();

				if ("".equals(name) || "".equals(url)) {
					LayoutInflater inflater = getLayoutInflater();
					View toastLayout = inflater.inflate(R.layout.toast_normal_layout, (ViewGroup) findViewById(R.id.toast_layout));
					
					//Toast toast = Toast.makeText(getApplicationContext(), R.string.name_and_url_cannot_be_empty, Toast.LENGTH_LONG);
					Toast toast=new Toast(getApplicationContext());
					((TextView)toastLayout.findViewById(R.id.prompt_text)).setText(R.string.name_and_url_cannot_be_empty);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(toastLayout);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {

					if (!url.startsWith("http://")) {
						url = "http://" + url;
					}

					mSqliteHelper.insertData(name, url);

					mEditName.setText("");
					mEditUrl.setText("");

					finish();
					
					//Intent i = new Intent(BookMarkCreateActivity.this, BookMarkActivity.class);
					//startActivity(i);
				}
		    }   
		    
		    public void onTitleClick(View v){
		    }
		};
		
		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		mTopBar.setText(R.string.bookmark_add);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);
		
		/*ImageButton mBtnSave = (ImageButton) findViewById(R.id.save);

		mBtnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String name = mEditName.getText().toString().trim();

				String url = mEditUrl.getText().toString().trim();

				if ("".equals(name) || "".equals(url)) {
					LayoutInflater inflater = getLayoutInflater();
					View toastLayout = inflater.inflate(R.layout.toast_normal_layout, (ViewGroup) findViewById(R.id.toast_layout));
					
					//Toast toast = Toast.makeText(getApplicationContext(), R.string.name_and_url_cannot_be_empty, Toast.LENGTH_LONG);
					Toast toast=new Toast(getApplicationContext());
					((TextView)toastLayout.findViewById(R.id.prompt_text)).setText(R.string.name_and_url_cannot_be_empty);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(toastLayout);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {

					if (!url.startsWith("http://")) {
						url = "http://" + url;
					}

					mSqliteHelper.insertData(name, url);

					mEditName.setText("");
					mEditUrl.setText("");

					finish();
					
					//Intent i = new Intent(BookMarkCreateActivity.this, BookMarkActivity.class);
					//startActivity(i);
				}
			}
		});*/

	}

}
