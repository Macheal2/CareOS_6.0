package com.cappu.contacts;

import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.text.TextUtils;
import android.content.Intent;
import android.content.Loader;
import com.android.contacts.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.ContentUris;
import android.graphics.BitmapFactory;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.provider.ContactsContract;
import android.app.LoaderManager.LoaderCallbacks;
import android.provider.ContactsContract.RawContacts;
import com.cappu.contacts.util.I99Utils;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.provider.ContactsContract.Contacts;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.Gravity;
import android.content.ClipboardManager;
import com.android.contacts.interactions.ContactDeletionInteraction;
import android.content.res.Resources;
import android.view.Menu;
import android.widget.LinearLayout;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import android.os.Trace;

@SuppressLint("NewApi")
public class CareContactDetailActivity extends Activity implements OnClickListener{
    private String TAG="CareContactDetailActivity";
	private ImageView mImageView;
	private TextView mNameTextView,mPhoneNumber, mGroupTextView;
	private String raw_id;
    public Context mContext = null;
    private Button mEditBu, mShareBu,mCallBu,mSendMessage;
    private TopBar mTopBar;
    private Uri mLookupUri;
    private long contactId; 
    private Dialog mOptionDialog;
    private LinearLayout mCallAndMsmLinearLayout;
	//wangcunxi add begin Care_os(share contactData)
    private Contact mContactData;
    private ContactLoader mContactLoader;
	//wangcunxi add end
    @SuppressWarnings("deprecation")
    private static final String LEGACY_AUTHORITY = android.provider.Contacts.AUTHORITY;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.care_contact_detail_activity);
		initView();
        mContext = CareContactDetailActivity.this;
	}

	@Override
	protected void onResume() {
        processIntent(getIntent());
		super.onResume();
	}
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        showOptionDialog();
        return super.onMenuOpened(featureId, menu);
    }
    private void initView(){
        mImageView     = (ImageView) findViewById(R.id.imageview);
        mNameTextView  = (TextView)  findViewById(R.id.name);
        mGroupTextView = (TextView)  findViewById(R.id.group);
        mPhoneNumber   = (TextView)  findViewById(R.id.phone);
        mCallBu        = (Button)    findViewById(R.id.i99_action_call);
        mSendMessage   = (Button)    findViewById(R.id.i99_action_message);
        mShareBu       = (Button)    findViewById(R.id.i99_share);
        mEditBu        = (Button)    findViewById(R.id.i99_edit);
        mTopBar        = (TopBar)    findViewById(R.id.top_bar);
        mCallAndMsmLinearLayout =(LinearLayout)findViewById(R.id.call_msm_content);
        mCallBu.setOnClickListener(this);
        mSendMessage.setOnClickListener(this);
        mShareBu.setOnClickListener(this);
        mEditBu.setOnClickListener(this);
        mTopBar.setOnTopBarListener(mTopBarListener);
    }

    private void setDefaultValue(){
        mPhoneNumber.setText("");
        mNameTextView.setText("");
        mImageView.setImageResource(R.drawable.i99_default_photo);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            finish();
        }

        Uri lookupUri = intent.getData();
        Log.d(TAG, "The original uri from intent: " + lookupUri);
        ContentResolver contentResolver = mContext.getContentResolver();
        if (lookupUri != null && LEGACY_AUTHORITY.equals(lookupUri.getAuthority())) {
            final long rawContactId = ContentUris.parseId(lookupUri);
            lookupUri = RawContacts.getContactLookupUri(contentResolver,
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
            Log.d(TAG, "The uri from old version: " + lookupUri);
        }
        //wangcunxi modify begin Care_os(share contactData)
        final Uri oldLookupUri = mLookupUri;
        if (lookupUri == null) {
            finish();
            return;
        }
        mLookupUri = lookupUri;
        Log.d(TAG,"mLookupUri---"+mLookupUri+"   "+oldLookupUri);
        if (oldLookupUri == null) {
            mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                    2, null, mLoaderContactCallbacks);
        } else if (oldLookupUri != mLookupUri) {
            // After copying a directory contact, the contact URI changes. Therefore,
            // we need to restart the loader and reload the new contact.
            mContactLoader = (ContactLoader) getLoaderManager().restartLoader(
                    2, null, mLoaderContactCallbacks);
        }

        setDefaultValue();
		Cursor cursor = contentResolver.query(mLookupUri, null, null, null, null);
        //wangcunxi modify end
		while (cursor != null && cursor.moveToNext()) {
			contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "+ contactId, null, null);

            //dengyingContact@20160628 begin
			String phoneNumberType2= "";
			String phoneNumberType7= "";
			String phoneNumberOther= "";
			//dengyingContact@20160628 end
			
			while (phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
				String data2 = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.Data.DATA2));//dengyingContact@20160603
				String mimetype =phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.Data.MIMETYPE));//dengyingContact@20160603
					
				raw_id = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.Data.RAW_CONTACT_ID));
              getLoaderManager().destroyLoader(0);
              getLoaderManager().initLoader(0, null, mLoader);   
              
              Log.i("dengyingContact", "number1111 == " + phoneNumber +",data2=="+data2 +",mimetype="+mimetype);
              
              //dengyingContact@20160603 begin //data2=2 第一个号码 data2=7 第二个号码
              if(data2.equals("2") && Phone.CONTENT_ITEM_TYPE.equals(mimetype)){
            	  phoneNumberType2 = phoneNumber;
              }else if(data2.equals("7") && Phone.CONTENT_ITEM_TYPE.equals(mimetype)){
            	  phoneNumberType7 = phoneNumber;
              }else{
            	  phoneNumberOther = phoneNumber;
              }//dengyingContact@20160603 end
			}
			
			//dengyingContact@20160628 begin
			if(!"".equals(phoneNumberType2)){
				mPhoneNumber.setText(phoneNumberType2);
			}else if(!"".equals(phoneNumberType7)){
				mPhoneNumber.setText(phoneNumberType7);
			}else{
				mPhoneNumber.setText(phoneNumberOther);
			}
			//dengyingContact@20160628 end
			
			phoneCursor.close();
            //wangcunxi modify begin Care_os(difference header photo or picture)
            Cursor dataCursor = contentResolver.query(Data.CONTENT_URI,null,"raw_contact_id=?", new String[] { raw_id + "" }, null);
            while (dataCursor.moveToNext()) {
                String mime = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
                if(Photo.CONTENT_ITEM_TYPE.equals(mime)){
                    final int colum = dataCursor.getColumnIndex("photo_key");
                    if(colum > 0){
                        String photoKey = dataCursor.getString(colum);
                        if(TextUtils.isEmpty(photoKey)){
                            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, uri);
                            if(null != input){
                                Bitmap contactPhoto = BitmapFactory.decodeStream(input);
                                mImageView.setImageBitmap(contactPhoto);
                                try{
                                    input.close();
                                }catch(IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            String photoName = "header/"+photoKey+".png";
                            Bitmap contactPhoto = I99Utils.getImageFromAssetsFile(mContext,photoName);
                            if(contactPhoto!=null)
                                mImageView.setImageBitmap(contactPhoto);
                        }
                    }
                }

            }
            dataCursor.close();
            //wangcunxi modify end
            mNameTextView.setText(name);
        }
		
		if (cursor != null) {
			cursor.close();
		}
		
        if("".equals(mPhoneNumber.getText().toString())){
            mCallAndMsmLinearLayout.setVisibility(View.GONE);
        }else{
            mCallAndMsmLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    //wangcunxi add begin Care_os(share contactData)
      private final LoaderCallbacks<Contact> mLoaderContactCallbacks =
          new LoaderCallbacks<Contact>() {
      @Override
      public void onLoaderReset(Loader<Contact> loader) {
          Log.d(TAG, "[onLoaderReset], mContactData been set null");
          mContactData = null;
      }

      @Override
      public void onLoadFinished(Loader<Contact> loader, Contact data) {
          Trace.beginSection("onLoadFinished()");
          try {

              if (isFinishing()) {
                  return;
              }
              if (data.isError()) {
                  // This means either the contact is invalid or we had an
                  // internal error such as an acore crash.
                  Log.i(TAG, "Failed to load contact: " + ((ContactLoader)loader).getLookupUri());
                  Toast.makeText(CareContactDetailActivity.this, R.string.invalidContactMessage,
                          Toast.LENGTH_LONG).show();
                  finish();
                  return;
              }
              if (data.isNotFound()) {
                  Log.i(TAG, "No contact found: " + ((ContactLoader)loader).getLookupUri());
                  Toast.makeText(CareContactDetailActivity.this, R.string.invalidContactMessage,
                          Toast.LENGTH_LONG).show();
                  finish();
                  return;
              }
              Log.d(TAG, "onLoadFinished " + " | data.getContactId() : "
                      + data.getContactId() + " | data.getUri() : " + data.getUri());

              mContactData = data;

          } finally {
              Trace.endSection();
          }
          android.util.Log.e("wangcunxi","mContactData==="+mContactData);
      }
      //wangcunxi add end

      @Override
      public Loader<Contact> onCreateLoader(int id, Bundle args) {
          if (mLookupUri == null) {
              Log.wtf(TAG, "Lookup uri wasn't initialized. Loader was started too early");
          }
          // Load all contact data. We need loadGroupMetaData=true to determine whether the
          // contact is invisible. If it is, we need to display an "Add to Contacts" MenuItem.
          return new ContactLoader(getApplicationContext(), mLookupUri,
                  true /*loadGroupMetaData*/, false /*loadInvitableAccountTypes*/,
                  true /*postViewNotification*/, true /*computeFormattedPhoneNumber*/);
      }
    };

	LoaderCallbacks<Cursor> mLoader = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new I99GroupListLoader(CareContactDetailActivity.this);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {

		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			StringBuilder sb = new StringBuilder();
            while (cursor.moveToNext()) {
				int groupId = cursor.getInt(0);
				String title = cursor.getString(1);
				boolean hasgroup = getContactsByGroupId(groupId);
				if (hasgroup) {
					sb.append(title);
					if (sb.length() > 0) {
						sb.append("  ");
					}
				}
			}
			cursor.close();
            String groupString = sb.toString();
            if ("".equals(groupString)) {
                groupString = mContext.getResources().getString(R.string.i99_no_group);
            }
			mGroupTextView.setText(groupString);
		}
	};

	public boolean getContactsByGroupId(int groupId) {

		String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
				+ "=?"
				+ " and "
				+ ContactsContract.Data.MIMETYPE
				+ "="
				+ "'"
				+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
				+ "'" + " and " + ContactsContract.Data.RAW_CONTACT_ID + "=?";
        android.util.Log.e("wangcunxi","getContactsByGroupId"+raw_id);
		Cursor cursor = mContext.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, RAW_CONTACTS_WHERE,
				new String[] { groupId + "", raw_id },null);
		Log.w(TAG, "getCount  " + cursor.getCount());
        boolean ingroup = false;
        if(cursor != null){
            if (cursor.getCount() > 0) {
    			cursor.close();
    			ingroup = true;
    		}else{
		        cursor.close();
    		    ingroup = false;
            }
        }
		return ingroup;
	}

    @Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.i99_action_call:
			I99Utils.doCall(mContext,mPhoneNumber.getText().toString());
			break;
        case R.id.i99_action_message:
            I99Utils.sendMessage(mContext,mPhoneNumber.getText().toString());
            break;
        case R.id.i99_share:
            //wangcunxi modify begin (share Contacts)
            //I99Utils.shareContact(mContext,lookupUri,contactId);
			I99Utils.shareContact(mContext,mContactData);
            //wangcunxi modify end
            break;
        case R.id.i99_edit:
        case R.id.i99_dialog_edit:
            I99Utils.editContact(mContext,mLookupUri);
            dismissOptionDialog();
            break;

        case R.id.i99_dialog_copy:
            ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(getContactInfo());
            showCopyDialog();
            dismissOptionDialog();
            break;

        case R.id.i99_dialog_delete:
            ContactDeletionInteraction.start(CareContactDetailActivity.this, mLookupUri, true);
            dismissOptionDialog();
            break;

        case R.id.i99_ip_calls:
            I99Utils.doIPCall(CareContactDetailActivity.this,mPhoneNumber.getText().toString());
            //START: added by Yar @20170802
            dismissOptionDialog();
            //END: added by Yar @20170802
            break;
		default:
            Log.w(TAG,"NO this view!");
			break;
		}
	}

    private String getContactInfo(){
        String message = null ;
        Resources res = getResources();
        String name = res.getString(R.string.i99_name) + mNameTextView.getText();
        String number = res.getString(R.string.i99_phone) + mPhoneNumber.getText();
        message = name+"\n"+number;
        return message;
    }
    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            CareContactDetailActivity.this.finish();
        }
        public void onRightClick(View v){
            showOptionDialog();
        }
        public void onTitleClick(View v){

        }
    };
    private void showCopyDialog(){
        final Dialog dialog = new Dialog(this, R.style.I99DialogStyle);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.i99_contact_details_copy_dialog, null);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView content = (TextView)view.findViewById(R.id.content);
        Button ok = (Button)view.findViewById(R.id.i99_ok);
        ok.setOnClickListener(new View.OnClickListener (){
            @Override
            public void onClick(View v){
                dialog.dismiss();
            }
        });

        title.setTextSize(I99Font.TITLE);
        content.setTextSize(I99Font.TITLE);
        ok.setTextSize(I99Font.TITLE);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = I99Utils.getScreenSize(this)[0];
        dialog.addContentView(view, lp);
        dialog.show();
        Window window = dialog.getWindow();
        lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.I99DialogAnim);
    }
    private void dismissOptionDialog(){
        if(mOptionDialog !=null && mOptionDialog.isShowing()){
            mOptionDialog.dismiss();
            mOptionDialog = null;
        }
    }
    private void showOptionDialog(){
        mOptionDialog           = new Dialog(this, R.style.I99DialogStyle);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view               = inflater.inflate(R.layout.i99_contact_details_option_dialog, null);
        TextView title          = (TextView)view.findViewById(R.id.title);
        Button ipcalls          = (Button)view.findViewById(R.id.i99_ip_calls);
        Button copy             = (Button)view.findViewById(R.id.i99_dialog_copy);
        Button edit             = (Button)view.findViewById(R.id.i99_dialog_edit);
        Button delete           = (Button)view.findViewById(R.id.i99_dialog_delete);
        ipcalls.setOnClickListener(CareContactDetailActivity.this);
        copy.setOnClickListener(CareContactDetailActivity.this);
        edit.setOnClickListener(CareContactDetailActivity.this);
        delete.setOnClickListener(CareContactDetailActivity.this);
        if("".equals(mPhoneNumber.getText().toString())){
            ipcalls.setVisibility(View.GONE);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = I99Utils.getScreenSize(this)[0];
        mOptionDialog.addContentView(view, lp);
        mOptionDialog.show();
        Window window = mOptionDialog.getWindow();
        lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.I99DialogAnim);
    }
}
