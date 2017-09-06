package cappu.sos;


import java.util.ArrayList;

import cappu.sos.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;

import android.text.InputType;
public class SOSNumberSetting extends Activity {

	private SharedPreferences sharedPreferences;
	EditText number_editor;
	
	View mRoot ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_number_setting);
		
		initView();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		/*switch (resultCode) {
		case (RESULT_OK): {
				Bundle b = data.getExtras();
				ArrayList<String> getcontactsList = b.getStringArrayList("GET_CONTACT");
				String numberList = "";
				for(int i = 0;i < getcontactsList.size(); i ++){
					numberList = numberList + getcontactsList.get(i);
				}
				number_editor.setText(numberList);
				break;
		 	}
		}*/
		
		
		switch (requestCode) {
		case (1): {
			if (resultCode == Activity.RESULT_OK) {
			
				//Log.e("dengying","data="+data.getData());
				
				Uri contactData = data.getData();  
              Cursor cursor = managedQuery(contactData, null, null, null, null);  
              cursor.moveToFirst();  
              String number = this.getContactPhone(cursor); 
			  	number_editor.setText(number);
				          
	  			//我在代码中使用了Context.managedQuery()，Cursor.close()方法，但是在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭。
	  			if(android.os.Build.VERSION.SDK_INT < 14){
	  				if (cursor != null){
	  					cursor.close();  
	  				}
	  			}              
			}
			break;
		}
	  }
	}	
	
	
	// 获取联系人电话
	private String getContactPhone(Cursor cursor) {

		int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int phoneNum = cursor.getInt(phoneColumn);
		String phoneResult = "";
		// System.out.print(phoneNum);
		if (phoneNum > 0) {
			// 获得联系人的ID号
			int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			String contactId = cursor.getString(idColumn);
			// 获得联系人的电话号码的cursor;
			Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
			// int phoneCount = phones.getCount();
			// allPhoneNum = new ArrayList<String>(phoneCount);
			if (phones.moveToFirst()) {
				// 遍历所有的电话号码
				for (; !phones.isAfterLast(); phones.moveToNext()) {
					int index = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int typeindex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					int phone_type = phones.getInt(typeindex);
					String phoneNumber = phones.getString(index);

					// 获取联系人姓名代码改为： String
					// phoneName=phones.getString(phones.getColumnIndex(PhoneLookup.DISPLAY_NAME));

					switch (phone_type) {
					case 2:
						phoneResult = phoneNumber;
						break;
					}
					// allPhoneNum.add(phoneNumber);
				}
				
	  			//我在代码中使用了Context.managedQuery()，Cursor.close()方法，但是在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭。
	  			if(android.os.Build.VERSION.SDK_INT < 14){
					if (!phones.isClosed()) {
						phones.close();
					}
	  			}  
			}
		}
		return phoneResult;
	}	
	
	private void initView() {
				//added by jiangyan
	    mRoot = (View)findViewById(R.id.whole_layout);
	    
		/*ImageButton btnCancel = (ImageButton) findViewById(R.id.cancel);
				
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});	
		
		TextView txtTitle = (TextView) findViewById(R.id.title);
		txtTitle.setText(R.string.phone);*/
		
		
		onTopBarListener mTopBarListener = new onTopBarListener(){
		    public void onLeftClick(View v){
		    	finish();
		    }
		    
		    public void onRightClick(View v){
		    }   
		    
		    public void onTitleClick(View v){
		    }
		};		
		
		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);
		
		number_editor = (EditText) findViewById(R.id.number_editor);
		number_editor.setFocusable(true);
		
		
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			//added by jiangyan
			    mRoot.getRootView().setBackgroundColor(R.color.background);			
			    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		        imm.hideSoftInputFromWindow(mRoot.getWindowToken(), 0); //强制隐藏键盘  
	        
				finish();
			}
		});			

		Button doneBtn = (Button) findViewById(R.id.doneBtn);

		sharedPreferences = getSharedPreferences("sospref", Context.MODE_PRIVATE);
		String sos_mobile_number = sharedPreferences.getString("sos_mobile_number", "");

		number_editor.setText(sos_mobile_number);
		
		number_editor.setInputType(InputType.TYPE_CLASS_PHONE);
		
		doneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText number_editor = (EditText) findViewById(R.id.number_editor);

				Editor editor = sharedPreferences.edit();
				editor.putString("sos_mobile_number", number_editor.getText().toString());

				editor.commit();
				//added by jiangyan
				mRoot.getRootView().setBackgroundColor(R.color.background);
	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
	            imm.hideSoftInputFromWindow(mRoot.getWindowToken(), 0); //强制隐藏键盘  
	            
				finish();				
			}
		});		
		
		Button btn_number_choose = (Button) findViewById(R.id.number_choose);

		btn_number_choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, 1);
				
				/*Intent i = new Intent(SOSNumberSetting.this, CopyContactsList.class);
				i.putExtra("copyContactsMode", "Single");//Single
				startActivityForResult(i, 0);*/
			}
		});
		
	}
	
}
