package cappu.sos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import cappu.sos.widget.TopBar;
//import cappu.sos.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;

import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class SOSSmsNumberSetting extends Activity {

	EditText number_editor;

	private SharedPreferences sharedPreferences;
	View mRoot;

	// 实例化监听
	private onTopBarListener mTopBarListener = new onTopBarListener(){
	    public void onLeftClick(View v){
	    	finish();
	    }
	    
	    public void onRightClick(View v){
	    }   
	    
	    public void onTitleClick(View v){
	    }
	};	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_sms_number_setting);

		initView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
  
		//Log.e("dengying", "onActivityResult requestCode="+requestCode+",resultCode="+resultCode+",data="+data.getDataString());
		
		switch (requestCode) {

		case 100009:
			if (resultCode == Activity.RESULT_OK) {
				//Log.e("dengying", "onActivityResult data="+data.getDataString());  
				
//				Bundle b = data.getExtras();
//				ArrayList<String> getcontactsList = b.getStringArrayList("GET_CONTACT");
//	
//				String numberList = "";
//				
//				for(int i = 0;i < getcontactsList.size(); i ++){
//					numberList = numberList + getcontactsList.get(i) + ",";
//				}
//				
//				number_editor.setText(numberList);
				//yuan tong qin add 
				 if (data != null) {
	                 processPickResult(data);
	                }

			break;
		}
		}
	}
	//yuan tong qin add start 
	  private void processPickResult(final Intent data) {
	      
	        final long[] contactsId = data.getLongArrayExtra("com.mediatek.contacts.list.pickdataresult");
	        if ((contactsId == null || contactsId.length <= 0) ) {
	            return;
	        }
	      
	        new Thread(new Runnable() {
	            public void run() {
	               
	              List<Contactinfoa> p = processDuplicatePickResult(contactsId);
	              Message msg = handler.obtainMessage();
	              msg.obj=p;
	              handler.sendMessage(msg );
	            }
	        }, "ComoseMessageActivity.processPickResult").start();
	    }
	 
	  Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			StringBuffer sb=new StringBuffer();
			List<Contactinfoa> list=(List<Contactinfoa>) msg.obj;
			for(int i = 0;i < list.size(); i ++){
				sb.append(""+list.get(i).getNumber()).append(",");
			}
			number_editor.setText(sb.toString());
		};  
	  };
	    
	  private List<Contactinfoa> processDuplicatePickResult( long[] contactsId) {
	       		//其中取代他的是getContactInfoForPhoneUrisInternal方法
		  StringBuilder idSetBuilder = getContactsid(contactsId);
		  List<Contactinfoa> infolist = getContactInfoForPhoneUrisInternal(idSetBuilder);
	            
		  return infolist;
               
	    }
	  
	  
	  //把联系人id得到
	  public StringBuilder getContactsid(long[] ids){
		  
		  StringBuilder idSetBuilder = new StringBuilder();
          boolean first = true;
          for (long id : ids) {
              if (first) {
                  first = false;
                  idSetBuilder.append(id);
              } else {
                  idSetBuilder.append(',').append(id);
              }
          }
          return idSetBuilder;
	  }
	  
	  private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;
	  private static final String[] CALLER_ID_PROJECTION = new String[] {
          Phone._ID,                      // 0
          Phone.NUMBER,                   // 1
          Phone.LABEL,                    // 2
          Phone.DISPLAY_NAME,             // 3
          Phone.CONTACT_ID,               // 4
          Phone.CONTACT_PRESENCE,         // 5
          Phone.CONTACT_STATUS,           // 6
        
          
  };
	  
	  private static final int PHONE_ID_COLUMN = 0;
      private static final int PHONE_NUMBER_COLUMN = 1;
      private static final int PHONE_LABEL_COLUMN = 2;
      private static final int CONTACT_NAME_COLUMN = 3;
      private static final int CONTACT_ID_COLUMN = 4;
      private static final int CONTACT_PRESENCE_COLUMN = 5;
      private static final int CONTACT_STATUS_COLUMN = 6;
     
      
      
      //查询联系人
	  private List<Contactinfoa> getContactInfoForPhoneUrisInternal(StringBuilder idSetBuilder) {
		  List<Contactinfoa> infoa=new ArrayList<Contactinfoa>();
          Cursor cursor = null;
          if (idSetBuilder.length() > 0) {
              final String whereClause = Phone._ID + " IN (" + idSetBuilder.toString() + ")";
              cursor = getContentResolver().query(
                      PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, whereClause, null,  Phone.NUMBER+" ASC ");
          }
          if (cursor == null) {
              return null;
          }
          try {
              while (cursor.moveToNext()) {
            	  String number=cursor.getString(PHONE_NUMBER_COLUMN);
            	  String name=cursor.getString(CONTACT_NAME_COLUMN);
            	  Contactinfoa info=new Contactinfoa(name, number);
            	  if(infoa.size() != 0){
            		  Contactinfoa cf = infoa.get(infoa.size()-1);
            		  String num = cf.getNumber();
            		  if(!num.equals(number)){
                    	  infoa.add(info);
            		  }
            	  }else{
                	  infoa.add(info);
            	  }
              }
          } finally {
              cursor.close();
          }
         return infoa;
      }
	  
	  private class Contactinfoa {
		   String name;
		   String number;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		  
		public Contactinfoa(){
			
		}
		public Contactinfoa(String name,String number){
			this.name=name;
			this.number=number;
		}
		   
		   
	  }
	  
	  
	  //yuan tong qin add end 
	 
 

	private void initView() {

	    mRoot = (View)findViewById(R.id.whole_layout);
		RelativeLayout title_layout = (RelativeLayout) findViewById(R.id.title_layout);

		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);		
		
		/*ImageButton btnCancel = (ImageButton) title_layout.findViewById(R.id.cancel);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/

		/*TextView txtTitle = (TextView) title_layout.findViewById(R.id.title);
		txtTitle.setText(R.string.number_title_mms);*/

		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    
			  //added by jy
                mRoot.getRootView().setBackgroundColor(R.color.background);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
                imm.hideSoftInputFromWindow(mRoot.getWindowToken(), 0); //强制隐藏键盘  
				finish();
			}
		});

		Button doneBtn = (Button) findViewById(R.id.doneBtn);

		sharedPreferences = getSharedPreferences("sospref", Context.MODE_PRIVATE);
		String sos_sms_mobile_number = sharedPreferences.getString("sos_sms_mobile_number", "");

		number_editor = (EditText) findViewById(R.id.number_editor);
		number_editor.setText(sos_sms_mobile_number);
		//number_editor.addTextChangedListener(new TextFilter(number_editor));
		
		doneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText number_editor = (EditText) findViewById(R.id.number_editor);

				String sos_number = number_editor.getText().toString();
				
				sos_number = ToDBC(sos_number);
				
				String[] str = sos_number.split(",");
				
				if(str.length > 5){
					showToast(getString(R.string.sos_sms_nubmer_toast));
					return;
				}
				
				Editor editor = sharedPreferences.edit();
				editor.putString("sos_sms_mobile_number", sos_number);

				editor.commit();
				
				//added by jy
                mRoot.getRootView().setBackgroundColor(R.color.background);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
                imm.hideSoftInputFromWindow(mRoot.getWindowToken(), 0); //强制隐藏键盘  
				finish();
			}
		});

		Button btn_number_choose = (Button) findViewById(R.id.number_choose);
		  android.util.Log.i("test", "1111111111111111==zheli chuangzhi ===");
		btn_number_choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONEANDEMAILS");
				intent.setType(Phone.CONTENT_TYPE);
				intent.putExtra("NUMBER_BALANCE", 5);
				startActivityForResult(intent, 1);*/
				
				  String ACTION_CONTACT_SELECTION = "android.intent.action.contacts.list.PICKMULTIPHONEANDEMAILS";
				  String PICK_CONTACT_NUMBER_BALANCE = "NUMBER_BALANCE";
				  int REQUEST_CODE_PICK             = 100009;
				  
				    Intent intent = new Intent(ACTION_CONTACT_SELECTION);
		            intent.setType(Phone.CONTENT_TYPE);
		            intent.putExtra("soss_choose_contacts", "soss");
		            /// M: OP09 add For pick limit: set number balance for picking contacts; As a common function
		            intent.putExtra(PICK_CONTACT_NUMBER_BALANCE, 5);
		            startActivityForResult(intent, REQUEST_CODE_PICK);
				
				
//				Intent i = new Intent(SOSSmsNumberSetting.this, CopyContactsList.class);
//				i.putExtra("copyContactsMode", "multiple");//Single	
//				startActivityForResult(i, 1);
//				
				
				
				
			}
		});

	}

    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
	private String ToDBC(String input) {
	 char c[] = input.toCharArray();
	 
	 for (int i = 0; i < c.length; i++) {
	   if (c[i] == '\u3000') {
		   c[i] = ' ';
	   } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
	     c[i] = (char) (c[i] - 65248);
	   }
	 }
	 
	 String returnString = new String(c);
	
	 return returnString;
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
				if (!phones.isClosed()) {
					phones.close();
				}
			}
		}
		return phoneResult;
	}

	
    class TextFilter implements TextWatcher {  
        private EditText editText;  
        private String rt;  
  
        public TextFilter(EditText editText) {  
            this.editText = editText;  
        }  
  
        @Override  
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            Log.e("dengying ", "beforeTextChanged CharSequence: " + s + " -start: " + start + " -count: " + count);  
        }  
  
        @Override  
        public void onTextChanged(CharSequence s, int start, int before, int count) {  
            Log.e("dengying ", "onTextChanged CharSequence: " + s + " -start: " + start + " before: " + before + " -count: " + count);   
        }  
  
        @Override  
        public void afterTextChanged(Editable s) {  
            Log.e("dengying", "afterTextChanged "+s.toString());  
        }  
    }  

	public void showToast(String msg) {
		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.toast_normal_layout, (ViewGroup) findViewById(R.id.toast_layout));

		Toast toast = new Toast(getApplicationContext());
		((TextView) toastLayout.findViewById(R.id.prompt_text)).setText(msg);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastLayout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
