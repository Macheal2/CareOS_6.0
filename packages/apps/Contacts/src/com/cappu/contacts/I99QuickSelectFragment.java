package com.cappu.contacts;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import com.android.contacts.R;

public class I99QuickSelectFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "I99QuickSelectFragment";
    private static final int REQUEST_ADD_QUICK_CONTACT = 100;
    private static final int REQUEST_SELECT_QUICK_CONTACT = 101;
    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED ="finishActivityOnSaveCompleted";
    public static final String[] COLUMS_DATA_ID = { ContactsContract.Contacts.NAME_RAW_CONTACT_ID };
    public static final String[] COLUMS_DATA_RAW_CONTACT_ID = { ContactsContract.Contacts.Data.RAW_CONTACT_ID};
    public static final String[] COLUMS_CONTACT_ID_RAW_CONTACT_ID = {"_id","name_raw_contact_id"};//dengying@20160602
    
    private View mAddContact;
    private View mSelectContact;
    private TextView mCreateTitle , mCreateSummery;
    private TextView mSelectTitle , mSelectSummery;
    private TextView mTipView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.care_quick_select_layout, null);
        mCreateTitle = (TextView)view.findViewById(R.id.create_contact_title);
        mCreateSummery = (TextView)view.findViewById(R.id.create_contact_summery);
        mSelectTitle = (TextView)view.findViewById(R.id.select_contact_title);
        mSelectSummery = (TextView)view.findViewById(R.id.select_contact_summery);
        mTipView = (TextView)view.findViewById(R.id.add_info);

        mAddContact = view.findViewById(R.id.create);
        mSelectContact = view.findViewById(R.id.select_contact);
        mAddContact.setOnClickListener(this);
        mSelectContact.setOnClickListener(this);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) return ;
        Activity activity = getActivity();
        Bundle bundle = activity.getIntent().getExtras();
        if(bundle == null){
            bundle = new Bundle();
            Log.w(TAG,"bundle == null ,and create" );
        }
        ContentResolver cr =  activity.getContentResolver();
        Uri uri = data.getData();

        Log.i("dengyingContact","uri = " + uri);

        String[] colums = null ;
        if(requestCode == REQUEST_ADD_QUICK_CONTACT){
            colums = COLUMS_DATA_ID;
            // dataUri = Uri.parse("content://com.android.contacts/data");
        }else if(requestCode == REQUEST_SELECT_QUICK_CONTACT){
            colums = COLUMS_CONTACT_ID_RAW_CONTACT_ID;//COLUMS_DATA_RAW_CONTACT_ID; //dengying@20160602
            // dataUri = uri;
        }
        Cursor cursor = cr.query(uri,colums, null, null, null);
        boolean isFirst = cursor.moveToFirst();	//modify by Yar @20170729

		/*boolean isPrintcolumn = false;
		if (cursor.moveToFirst()) {
			do {
				if(!isPrintcolumn){
					String columnNames[] = cursor.getColumnNames();
					for (int i = 0; i < columnNames.length; i++) {
						Log.e("dengyingContact", "columnNames[" + i + "]=" + columnNames[i] +",vaule="+cursor.getString(i));
					}
					
					isPrintcolumn = false;
				}
		
			} while (cursor.moveToNext());
		}*/
        
		 //cursor.moveToFirst();
        
        int raw_contact_id = -1;
        int contact_id = -1;
        
        android.util.Log.i("Yar", "1. I99QuickSelectFragment isFirst = " + isFirst + ", cursor = " + cursor);
        //START: modify by Yar @20170729
        if (isFirst) {
            if(requestCode == REQUEST_ADD_QUICK_CONTACT){
                raw_contact_id = cursor.getInt(0);
                contact_id = getContactID(cr, raw_contact_id);
            }else if(requestCode == REQUEST_SELECT_QUICK_CONTACT){//dengying@20160602
                contact_id = cursor.getInt(0);   
                raw_contact_id = cursor.getInt(1); 
            }     
        } else {
        	cursor.close();
        	activity.finish();
        	return;
        }
        //END: modify by Yar @20170729
        
        Log.i("dengyingContact","raw_contact_id = " + raw_contact_id +",contact_id="+contact_id);
        
        final String name = getDisplayName(cr, contact_id);

        final Map<String, String> info = getQuickInfo(cr, raw_contact_id);

        //final String number = getPhoneNumber(cr, dataUri, raw_contact_id);
        final String number = info.get(Phone.CONTENT_ITEM_TYPE);
        final String photoKey = info.get(Photo.CONTENT_ITEM_TYPE);

        final Uri photoUri;
        if(TextUtils.isEmpty(photoKey)){
            photoUri = parsePhotoUri(cr,contact_id);
        }else{
            photoUri = null;
            bundle.putString(I99QuickContactActivity.EXTRA_PHOTO_KEY, photoKey);
        }
        Log.i("dengyingContact","name=" + name + " number="+number+" photoKey == " + photoKey + ",photoUri == " + photoUri);

        bundle.putString(I99QuickContactActivity.EXTRA_NAME, name);
        bundle.putString(I99QuickContactActivity.EXTRA_NUMBER, number);
        //START: added by Yar @20170824
        bundle.putInt("raw_id", contact_id);
        //END: added by Yar @20170824


        cursor.close();

        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.android.cappu", "com.android.cappu.Launcher");
        intent.setComponent(cn);
        intent.setData(photoUri);
        intent.putExtras(bundle);
        activity.setResult(Activity.RESULT_OK,intent);
        activity.finish();
    }

    @Override
    public void onClick(View v){
        Intent intent = null ;
        switch(v.getId()){
            case R.id.create :
                intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED,true);
                startActivityForResult(intent, REQUEST_ADD_QUICK_CONTACT);
            break;
            case R.id.select_contact :
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //intent.setType(Phone.CONTENT_TYPE);  //dengying@20160602
                               
                Log.e("dengyingContact", "I99QuickSelectFragment.java onClick select_contact");
                startActivityForResult(intent, REQUEST_SELECT_QUICK_CONTACT);
            break;
        }
    }

    private int getContactID(ContentResolver cr ,int raw_id){
        Cursor cursor = cr.query(RawContacts.CONTENT_URI,new String[]{RawContacts.CONTACT_ID}, RawContacts._ID + "=?", new String[] { raw_id + ""},null);
        cursor.moveToFirst();
        int contact_id = cursor.getInt(0);
        cursor.close();
        return contact_id;
    }

    private Uri parsePhotoUri(ContentResolver cr ,int contact_id){
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contact_id);
        return Uri.withAppendedPath(photoUri, Contacts.Photo.DISPLAY_PHOTO);
    }

    private String getDisplayName(ContentResolver cr ,int contact_id){
        Cursor nameCur = cr.query(Contacts.CONTENT_URI,
                new String[]{Contacts.DISPLAY_NAME},
                Contacts._ID  + "= ?",
                new String []{contact_id + ""},
                null);
        nameCur.moveToFirst();
        String name = nameCur.getString(0);
        nameCur.close();
        return name;
    }

    private String getPhoneNumber(ContentResolver cr ,Uri dataUri , int raw_contact_id){
        Cursor dataCursor = cr.query(dataUri,null,"raw_contact_id=?", new String[] { raw_contact_id + "" }, null);
        while (dataCursor.moveToNext()) {
            String data1 = dataCursor.getString(dataCursor.getColumnIndex("data1"));
            String mime = dataCursor.getString(dataCursor.getColumnIndex("mimetype"));
            if (Phone.CONTENT_ITEM_TYPE.equals(mime)) {
                Log.i(TAG, "number == " + data1);
                dataCursor.close();
                return data1;
            }
        }
        return null ;
    }

    private Map<String, String> getQuickInfo(ContentResolver cr ,int raw_contact_id){
        Map<String, String> info = new HashMap<String, String>();

        Cursor dataCursor = cr.query(Data.CONTENT_URI,null,"raw_contact_id=?", new String[] { raw_contact_id + "" }, null);
        
        //dengyingContact@20160628 begin
        String data1data2_2= "";
        String data1data2_7= "";
        String data1data2_other= "";
        //dengyingContact@20160628 end 
        
        while (dataCursor.moveToNext()) {
            String data1 = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA1));
            String data2 = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA2));//data2=2 第一个号码 data2=7 第二个号码 //dengying@20160603
            String mime = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
            if (Phone.CONTENT_ITEM_TYPE.equals(mime)) {
                 Log.i("dengyingContact", "number == " + data1 +",data2=="+data2);
                 
					if (data2.equals("2")) {//dengying@20160603                       
						//info.put(Phone.CONTENT_ITEM_TYPE, data1);
						data1data2_2 = data1;
					}else if (data2.equals("7")) {                     
						data1data2_7 = data1;
					}else{                     
						data1data2_other = data1;
					}
            }else if(Photo.CONTENT_ITEM_TYPE.equals(mime)){
                final int colum = dataCursor.getColumnIndex("photo_key");
                if(colum > 0){
                    String key = dataCursor.getString(colum);
                    info.put(Photo.CONTENT_ITEM_TYPE, key);
                }
            }

        }
        
		//dengyingContact@20160628 begin
		if(!"".equals(data1data2_2)){
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_2);
		}else if(!"".equals(data1data2_7)){
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_7);
		}else{
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_other);
		}
		//dengyingContact@20160628 end
        
        dataCursor.close();
        return info ;
    }

}
