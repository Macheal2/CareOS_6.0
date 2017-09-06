package cappu.sos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

import cappu.sos.R;

public class CopyContactsList extends Activity {

	private final int UPDATE_LIST = 1;
	List<Map<String, Object>> data;
	ArrayList<String> contactsList;
	ArrayList<String> getcontactsList;
	private ImageButton okbtn;
	private ImageButton cancelbtn;
	private ProgressDialog proDialog;
	private ListView listView;
	private String copyContactsMode;
	
	Thread getcontacts;
	Handler updateListHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case UPDATE_LIST:
				if (proDialog != null) {
					proDialog.dismiss();
				}
				updateList();
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.contacts_choose);

		Intent intent = getIntent();
		copyContactsMode = intent.getStringExtra("copyContactsMode");
		
		data = new ArrayList<Map<String, Object>>();

		contactsList = new ArrayList<String>();
		getcontactsList = new ArrayList<String>();

		okbtn = (ImageButton) findViewById(R.id.contacts_done_button);
		okbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (getcontactsList != null && getcontactsList.size() > 0) {
					Intent i = new Intent();
					Bundle b = new Bundle();
					b.putStringArrayList("GET_CONTACT", getcontactsList);
					i.putExtras(b);
					setResult(RESULT_OK, i);
				}

				CopyContactsList.this.finish();
			}
		});

		cancelbtn = (ImageButton) findViewById(R.id.contact_back_button);
		cancelbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CopyContactsList.this.finish();
			}
		});

		listView = (ListView) findViewById(R.id.listview);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (copyContactsMode.equals("Single")) {

					String number = (String) data.get(position).get("number");
					getcontactsList.add(number);

					if (getcontactsList != null && getcontactsList.size() > 0) {
						Intent i = new Intent();
						Bundle b = new Bundle();
						b.putStringArrayList("GET_CONTACT", getcontactsList);
						i.putExtras(b);
						setResult(RESULT_OK, i);
					}
					CopyContactsList.this.finish();
				}
			}
		});

		getcontacts = new Thread(new GetContacts());
		getcontacts.start();
		proDialog = ProgressDialog.show(CopyContactsList.this, "loading", "loading", true, true);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	void updateList() {
		if (contactsList != null) {
			MyAdapter adapter = new MyAdapter(this);
			listView.setAdapter(adapter);
		}
	}

	class GetContacts implements Runnable {
		@SuppressWarnings("deprecation")
		@Override
		public void run() {

			// TODO Auto-generated method stub
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
			String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID };
			String selection = "1=1";// ContactsContract.Contacts.IN_VISIBLE_GROUP
										// + " = '1'";
			String[] selectionArgs = null;
			String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
			Cursor cursor = managedQuery(uri, projection, selection, selectionArgs, sortOrder);
			Cursor phonecur = null;

			while (cursor.moveToNext()) {

				// 取得联系人名字
				int nameFieldColumnIndex = cursor.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME);
				String name = cursor.getString(nameFieldColumnIndex);
				// 取得联系人ID
				String contactId = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
				phonecur = managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				// 取得电话号码(可能存在多个号码)
				while (phonecur.moveToNext()) {
					String strPhoneNumber = phonecur.getString(phonecur.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER));

					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("name", name);
					map.put("number", strPhoneNumber);
					map.put("isChecked", "false");
					data.add(map);
				}
			}

			//我在代码中使用了Context.managedQuery()，Cursor.close()方法，但是在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭。
			if(android.os.Build.VERSION.SDK_INT < 14){
				if (phonecur != null){
					phonecur.close();
				}
			
				if (cursor != null){
					cursor.close();  
				}
			}
			
			Message msg1 = new Message();
			msg1.what = UPDATE_LIST;
			updateListHandler.sendMessage(msg1);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		contactsList.clear();
		getcontactsList.clear();
		super.onDestroy();
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView number;
		public CheckBox ck;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.contacts_listitem, null);
	
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.number = (TextView) convertView.findViewById(R.id.number);
				holder.ck = (CheckBox) convertView.findViewById(R.id.ck);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.icon.setImageResource(R.drawable.contact_default_photo);
			
			String name = (String) data.get(position).get("name");
			final String number = (String) data.get(position).get("number");

			holder.name.setText(name);
			holder.number.setText(number);

			final CheckBox ck = holder.ck;
			final int index = position;
			String isChecked = (String) data.get(position).get("isChecked");
		
			Log.e("dengying", "position="+position + " isChecked="+isChecked);
			
			ck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						
						/*if(getcontactsList.size()>5){
							ck.setChecked(false);
							showToast(getString(R.string.sos_sms_nubmer_toast));
							return;
						}*/
						
						if(!getcontactsList.contains(number)){
							getcontactsList.add(number);
						}
						ck.setChecked(true);
						data.get(index).put("isChecked", "true");
						
						Log.e("dengying", "index="+index + " isChecked="+(String) data.get(index).get("isChecked"));
					} else {
						getcontactsList.remove(number);
						ck.setChecked(false);
						data.get(index).put("isChecked", "false");
					}
				}
			});

			
			 if("true" == isChecked){
				 ck.setChecked(true);
			 }else if("false" == isChecked){
				 ck.setChecked(false);
			 }
			
			if (copyContactsMode.equals("Single")) {
				ck.setVisibility(android.view.View.GONE);
			}
			
			return convertView;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			Bundle b = new Bundle();
			b.putStringArrayList("GET_CONTACT", getcontactsList);
			i.putExtras(b); // }
			setResult(RESULT_OK, i);
		}
		return super.onKeyDown(keyCode, event);
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