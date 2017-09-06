package com.cappu.contacts;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorActivity;

import com.cappu.contacts.util.I99Utils;

public class I99QuickDoingFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "I99QuickDoingFragment";
    private static final String EMPTY = "";
    private static final int REQUEST_REPLACE_QUICK_CONTACT = 103;
    private static final int REQUEST_EDIT_QUICK_CONTACT = 104;

    public static final String[] COLUMS_DATA_ID = { ContactsContract.Contacts.NAME_RAW_CONTACT_ID };
    public static final String[] COLUMS_DATA_RAW_CONTACT_ID = { ContactsContract.Contacts.Data.RAW_CONTACT_ID};
    public static final String[] COLUMS_CONTACT_ID_RAW_CONTACT_ID = {"_id","name_raw_contact_id"};//dengying@20160602
    
    private ImageView mPhotoImg;
    private ImageButton mOptionBu;
    private View mReplaceBu, mEditorBu , mDeleteBu;
    private View mSendMessage , mCallPhone;
    private View mAcitonsView , mEditorsView;
    private TextView mNameTv , mNumberTv;
    private TextView mActionSms , mActionCall;
    private TextView mReplaceTv , mEditorTv , mDeleteTv;

    Bundle mBundle;
    Uri mPhotoUri;
    String mNameStr , mNumberStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBundle = getActivity().getIntent().getExtras();
        if(mBundle != null){
            mPhotoUri = getActivity().getIntent().getData();
            mNameStr = mBundle.getString(I99QuickContactActivity.EXTRA_NAME);
            mNumberStr = mBundle.getString(I99QuickContactActivity.EXTRA_NUMBER);
            Log.i(TAG,"name = " + mNameStr);
            Log.i(TAG,"mNumberStr = " + mNumberStr);
            Log.i(TAG,"mPhotoUri == " + mPhotoUri);
        }else{
            mBundle = new Bundle();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.care_quick_doing_layout, null);
        mPhotoImg = (ImageView)view.findViewById(R.id.photo);
        mNameTv = (TextView)view.findViewById(R.id.name);
        mNumberTv = (TextView)view.findViewById(R.id.number);
        mActionSms = (TextView)view.findViewById(R.id.action_message);
        mActionCall = (TextView)view.findViewById(R.id.action_call);
        mReplaceTv = (TextView)view.findViewById(R.id.i99_replace_text);
        mEditorTv = (TextView)view.findViewById(R.id.i99_edit_text);
        mDeleteTv = (TextView)view.findViewById(R.id.i99_restore_text);

        mOptionBu = (ImageButton)view.findViewById(R.id.option);
        mSendMessage = view.findViewById(R.id.message);
        mCallPhone = view.findViewById(R.id.call);
        mEditorBu = view.findViewById(R.id.edit);
        mReplaceBu = view.findViewById(R.id.replace);
        mDeleteBu = view.findViewById(R.id.restore);
        mAcitonsView = view.findViewById(R.id.action_view);
        mEditorsView = view.findViewById(R.id.editor_view);

        mOptionBu.setOnClickListener(this);
        mSendMessage.setOnClickListener(this);
        mCallPhone.setOnClickListener(this);
        mEditorBu.setOnClickListener(this);
        mReplaceBu.setOnClickListener(this);
        mDeleteBu.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        I99Configure.updateFont(getActivity());
        mNameTv.setTextSize(I99Font.TITLE_S);
        mNumberTv.setTextSize(I99Font.SUMMERY);
        mActionSms.setTextSize(I99Font.TITLE);
        mActionCall.setTextSize(I99Font.TITLE);
        mEditorTv.setTextSize(I99Font.TITLE);
        mReplaceTv.setTextSize(I99Font.TITLE);
        mDeleteTv.setTextSize(I99Font.TITLE);

        mNameTv.setText(mNameStr);
        mNumberTv.setText(mNumberStr);
        ContentResolver cr = getActivity().getContentResolver();
        if(mPhotoUri == null ){
            mPhotoImg.setImageResource(R.drawable.i99_default_photo);
        }else{
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, mPhotoUri);
            if(input == null){
                mPhotoImg.setImageResource(R.drawable.i99_default_photo);
            }else{
                Bitmap photo = BitmapFactory.decodeStream(input);
                photo = I99Utils.getRoundedCornerBitmap(photo);
                mPhotoImg.setImageBitmap(photo);
            }
        }
        super.onResume();
    }

    @Override
    public void onClick(View v){
        Activity activity = getActivity();
        Intent intent = null ;
        switch(v.getId()){
            case R.id.option:
                if(mAcitonsView.getVisibility() == View.VISIBLE){
                    mOptionBu.setImageResource(R.drawable.i99_icon_return);
                    mAcitonsView.setVisibility(View.GONE);
                    mEditorsView.setVisibility(View.VISIBLE);
                }else{
                    mOptionBu.setImageResource(R.drawable.i99_icon_option);
                    mAcitonsView.setVisibility(View.VISIBLE);
                    mEditorsView.setVisibility(View.GONE);
                }
            break;
            case R.id.call:
                if(!TextUtils.isEmpty(mNumberStr)){
                    I99Utils.doCall(getActivity(),mNumberStr);
                    activity.finish();
                }
            break;
            case R.id.message:
                if(!TextUtils.isEmpty(mNumberStr)){
                    I99Utils.sendMessage(getActivity(),mNumberStr);
                    activity.finish();
                }
            break;
            case R.id.edit:
            	Log.e("dengyingContact", "I99QuickDoingFragment.java onClick edit");//dengying@20160602
                if(!TextUtils.isEmpty(mNumberStr)){
                    Uri lookupUri = I99Utils.getLookupUri(activity,mNumberStr);
                    intent = new Intent(Intent.ACTION_EDIT, lookupUri);
                    intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
                    startActivityForResult(intent, REQUEST_EDIT_QUICK_CONTACT);
                }
            break;
            case R.id.replace:
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //intent.setType(Phone.CONTENT_TYPE);//dengyingContact@20160602
                Log.e("dengyingContact", "I99QuickDoingFragment.java onClick replace");//dengyingContact@20160602
                startActivityForResult(intent, REQUEST_REPLACE_QUICK_CONTACT);
            break;
            case R.id.restore:
                intent = new Intent();
                mBundle.putString(I99QuickContactActivity.EXTRA_NUMBER,EMPTY);
                mBundle.putString(I99QuickContactActivity.EXTRA_NAME,EMPTY);
                intent.putExtras(mBundle);
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();
            break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Activity activity = getActivity();
        ContentResolver cr =  activity.getContentResolver();
        if(data == null){
            activity.setResult(Activity.RESULT_CANCELED , activity.getIntent());
            activity.finish();
            return;
        }
        Uri uri = data.getData();
        Uri dataUri = null;
        Log.i(TAG,"uri = " + uri);
        String[] colums = null;
        if(requestCode == REQUEST_EDIT_QUICK_CONTACT){
            colums = COLUMS_DATA_ID;
            dataUri = Uri.parse("content://com.android.contacts/data");
        }else if(requestCode == REQUEST_REPLACE_QUICK_CONTACT){
            colums = COLUMS_CONTACT_ID_RAW_CONTACT_ID;//dengying@20160602
            dataUri = uri;
        }
        Cursor cursor = cr.query(uri,colums, null, null, null);
        boolean isFirst = cursor.moveToFirst(); //modify by Yar @20170729
        
        int raw_contact_id = -1;
        int contact_id = -1;
        android.util.Log.i("Yar", "1. I99QuickDoingFragment isFirst = " + isFirst + ", cursor = " + cursor);
        //START: modify by Yar @20170729
        if (isFirst) {
            if(requestCode == REQUEST_EDIT_QUICK_CONTACT){
                raw_contact_id = cursor.getInt(0);
                contact_id = getContactID(cr, raw_contact_id);
            }else if(requestCode == REQUEST_REPLACE_QUICK_CONTACT){//dengying@20160602 begin
                contact_id = cursor.getInt(0);   
                raw_contact_id = cursor.getInt(1); 
            }//dengying@20160602 end        
        } else {
        	cursor.close();
        	activity.finish();
        	return;
        }
        //END: modify by Yar @20170729
        
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
            mBundle.putString(I99QuickContactActivity.EXTRA_PHOTO_KEY, photoKey);
        }
        Log.i(TAG,"photoKey == " + photoKey);
        Log.i(TAG,"photoUri == " + photoUri);

        mBundle.putString(I99QuickContactActivity.EXTRA_NAME,name);
        mBundle.putString(I99QuickContactActivity.EXTRA_NUMBER,number);
        //START: added by Yar @20170824
        mBundle.putInt("raw_id", contact_id);
        //END: added by Yar @20170824

        cursor.close();
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.android.cappu", "com.android.cappu.Launcher");
        intent.setComponent(cn);
        intent.setData(photoUri);
        intent.putExtras(mBundle);
        activity.setResult(Activity.RESULT_OK,intent);
        activity.finish();
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
					
                 info.put(Phone.CONTENT_ITEM_TYPE, data1);
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
