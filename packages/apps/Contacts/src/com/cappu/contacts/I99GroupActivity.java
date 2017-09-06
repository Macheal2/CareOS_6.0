package com.cappu.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cappu.widget.CareMenu;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;


import com.android.contacts.R;

import com.mediatek.contacts.list.ContactListMultiChoiceActivity;

import com.cappu.contacts.ContactEntry;
import com.cappu.contacts.I99Configure;
import com.cappu.contacts.I99Font;
import com.cappu.contacts.util.I99Utils;
import com.cappu.contacts.widget.I99Dialog;


public class I99GroupActivity  extends Activity implements OnItemClickListener{

    public static final int NO_GROUP = -1 ;
    private static final int MULTIPLE_ADD_GROUP = 100 ;

    public static final String TAG = "I99GroupActivity";
    public static final String KEY_GROUP_ID = "extra_group_id";
    public static final String KEY_GROUP_TITLE = "extra_group_title";
    public static final String KEY_GROUP_COUNT = "extra_group_count";
    public static final String KEY_GROUP_ACCOUNT_NAME="extra_group_account_name";
    public static final String KEY_GROUP_ACCOUNT_TYPE="extra_group_account_type";
    public static final String KEY_GROUP_DATA_SET = "extra_group_data_set";

    private static final String RESULTINTENTEXTRANAME = "com.mediatek.contacts.list.pickcontactsresult";

    private int mGroupId;
    private int mCount;
    private String mGroupTitle;
    private String mAccountName ,mAccountType;
    private String mDataSet;
    AddTask mTask;
    ListView mContactList;

    Button mAddContactBu, mRemoveContactBu;
    CareMenu mOptionDialog;
    I99Dialog mProgressDialog;

    TopBar mTopBar;

    I99OnClickListener mClickListener;
    I99GroupAdapter mI99GroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_group_activity);
        Intent intent = getIntent();
        mGroupId = intent.getIntExtra(KEY_GROUP_ID, -1);
        mCount = intent.getIntExtra(KEY_GROUP_COUNT, -1);
        mGroupTitle = intent.getStringExtra(KEY_GROUP_TITLE);
        mAccountName = intent.getStringExtra(KEY_GROUP_ACCOUNT_NAME);
        mAccountType = intent.getStringExtra(KEY_GROUP_ACCOUNT_TYPE);
        mDataSet = intent.getStringExtra(KEY_GROUP_DATA_SET);
        if( mGroupId == -1){
            finish();
        }
        mClickListener = new I99OnClickListener();

        mContactList = (ListView) findViewById(android.R.id.list);
        mContactList.setOnItemClickListener(this);
        configureEmptyView();

        mAddContactBu = (Button)findViewById(R.id.add_contact);
        mAddContactBu.setOnClickListener(mClickListener);

        mRemoveContactBu = (Button)findViewById(R.id.remove_contact);
        mRemoveContactBu.setOnClickListener(mClickListener);

        mTopBar = (TopBar)findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(mTopBarListener);
        mTopBar.setText(mGroupTitle);

    }

    @Override
    protected void onResume() {
        ArrayList<ContactEntry> data = getContactsByGroupId(mGroupId);
        mI99GroupAdapter = new I99GroupAdapter(data);
        mContactList.setAdapter(mI99GroupAdapter);
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        ContactEntry entry = (ContactEntry)mI99GroupAdapter.getItem(position);
        Uri lookupUri = I99Utils.getLookupUri(I99GroupActivity.this, entry.number);
        Intent intent = new Intent(Intent.ACTION_VIEW ,lookupUri);
        startActivity(intent);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(mOptionDialog != null && mOptionDialog.isShowing()){
            mOptionDialog.dismiss();
            mOptionDialog = null ;
        }else{
            showOptionDialog();
        }
        return super.onMenuOpened(featureId, menu);
    }

    private class I99OnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.select:
                case R.id.add_contact:
                    addMembers();
                    if(mOptionDialog != null && mOptionDialog.isShowing()){
                        mOptionDialog.dismiss();
                        mOptionDialog = null ;
                    }
                    break;

                case R.id.remove_contact:
                    Intent intent = new Intent(I99GroupActivity.this,I99GroupDeleteMember.class);
                    intent.putParcelableArrayListExtra("GROUP_MEMBER",mI99GroupAdapter.getData());
                    intent.putExtra(KEY_GROUP_ID,mGroupId);
                    startActivity(intent);
                    if(mOptionDialog != null && mOptionDialog.isShowing()){
                        mOptionDialog.dismiss();
                        mOptionDialog = null ;
                    }
                    break;
            }
        }
    }

    private void addMembers() {
        try {
            Intent intent = new Intent(I99GroupActivity.this, ContactListMultiChoiceActivity.class);
            intent.setAction(com.mediatek.contacts.util.ContactsIntent.LIST.ACTION_GROUP_ADD_MULTI_CONTACTS);//dengying del
            intent.setAction(com.mediatek.contacts.util.ContactsIntent.LIST.ACTION_GROUP_MOVE_MULTI_CONTACTS);

            intent.setType(Contacts.CONTENT_TYPE);
            intent.putExtra("account_type", mAccountType);
            intent.putExtra("account_name", mAccountName);
            final ArrayList<ContactEntry> data = mI99GroupAdapter.getData();
            int size = data.size();
            long[] mContactIds = new long[size];
            int i = 0;
            for (ContactEntry entry : data) {
                mContactIds[i++] = entry.id;
            }
            intent.putExtra("member_ids", mContactIds);
            startActivityForResult(intent, MULTIPLE_ADD_GROUP);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "ActivityNotFoundException for addMembers Intent");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MULTIPLE_ADD_GROUP && data != null) {
            long[] contactIds = data.getLongArrayExtra(RESULTINTENTEXTRANAME);
            // just add 5 Contacts to show ProgressDialog();
            if(contactIds.length  >= 5){
                mProgressDialog = getProgressDialog();
                mProgressDialog.show();
            }
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(contactIds);
            mTask = getTask();
            mTask.execute(params);
        }
    }

    private I99Dialog getProgressDialog(){
        if(mProgressDialog == null){
            mProgressDialog = new I99Dialog(I99GroupActivity.this);
            mProgressDialog.setTitle(R.string.i99_add);
            mProgressDialog.setMessage(R.string.i99_adding);
            mProgressDialog.setControlVisibility(View.GONE);
            mProgressDialog.setProgressVisibility(View.VISIBLE);
        }
        return mProgressDialog;
    }

    private void showOptionDialog(){
        mOptionDialog = new CareMenu(I99GroupActivity.this);
        mOptionDialog.setTitle(R.string.i99_option);
        mOptionDialog.addButton(R.string.i99_add);
        mOptionDialog.addButton(R.string.i99_remove);
        mOptionDialog.setOnClickListener(new CareMenu.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = null;
                switch(v.getId()){
                    case R.string.i99_add:
                        addMembers();
                        break;
                    case R.string.i99_remove:
                        
                        break;
                }
            }
        });
        mOptionDialog.show();
    }


    public ArrayList<ContactEntry> getContactsByGroupId(int groupId) {

        String[] RAW_PROJECTION = new String[] {
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.PHOTO_URI};

        String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                + "=?"
                + " and "
                + ContactsContract.Data.MIMETYPE
                + "="
                + "'"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                + "'";

        Cursor cursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[] { groupId + "" }, "data1 asc");

        ArrayList<ContactEntry> contactList = new ArrayList<ContactEntry>();

        while (cursor.moveToNext()) {
            // RAW_CONTACT_ID
            int col = cursor.getColumnIndex("raw_contact_id");
            int raw_contact_id = cursor.getInt(col);
            String uri = cursor.getString(1);
            ContactEntry entry = new ContactEntry();

            entry.setId(raw_contact_id);
            // add for delete
            entry.setState(false);

            Uri dataUri = Uri.parse("content://com.android.contacts/data");
            Cursor dataCursor = getContentResolver().query(dataUri, null,
                    "raw_contact_id=?", new String[] { raw_contact_id + "" },
                    null);

            while (dataCursor.moveToNext()) {
                String data1 = dataCursor.getString(dataCursor
                        .getColumnIndex("data1"));
                String mime = dataCursor.getString(dataCursor
                        .getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/phone_v2".equals(mime)) {
                    entry.setNumber(data1);
                } else if ("vnd.android.cursor.item/name".equals(mime)) {
                    entry.setName(data1);
                }
            }

            dataCursor.close();
            contactList.add(entry);
            entry = null;
        }

        cursor.close();

        return contactList;
    }

    public class I99GroupAdapter extends BaseAdapter {
        ArrayList<ContactEntry> data;

        public I99GroupAdapter(ArrayList<ContactEntry> data) {
            I99GroupAdapter.this.data = data;
        }

        public ArrayList<ContactEntry> getData(){
            return data;
        }

        public void update(ArrayList<ContactEntry> data){
            I99GroupAdapter.this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
            ContactCache cache = null;
            ContactEntry entry = data.get(position);
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                view = inflater.inflate(R.layout.care_group_item_layout, null);
                cache = new ContactCache(view);
                view.setTag(cache);
            } else {
                cache = (ContactCache) view.getTag();
            }
            cache.name.setText(entry.name);
            cache.name.setTextSize(I99Font.TITLE);
            cache.setAction(entry.number);
            return view;
        }

    }

    private class ContactCache {
        ImageView action;
        TextView name;

        public ContactCache(View v) {
            action = (ImageView) v.findViewById(R.id.action);
            name = (TextView) v.findViewById(R.id.name);
            action.setOnClickListener(mAction);
        }

        public void setAction(String number) {
            if(!TextUtils.isEmpty(number)){
                Uri uri = Uri.fromParts("tel", number, null);
                Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, uri);
                action.setTag(intent);
            }
        }

        public View.OnClickListener mAction = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (view.getTag() instanceof Intent) {
                    Intent intent = (Intent)view.getTag();
                    startActivity(intent);
                }
            }

        };
    }

    private AddTask getTask(){
        if(mTask != null){
            mTask.cancel(true);
            mTask = null ;
        }
        return new AddTask();
    }

    class AddTask extends AsyncTask<ArrayList, Integer, ArrayList> {


        public AddTask() {
            super();
        }

        @Override
        protected ArrayList doInBackground(ArrayList... params) {
            if (isCancelled()) {
                return null;
            }
            ArrayList<Object> list = params[0];
            long[] contactIds = (long[]) list.get(0);

            for (long id :contactIds){
                final String[] projection = new String[] {RawContacts._ID};
                final String selection = RawContacts.CONTACT_ID + "=" + id;
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(RawContactsEntity.CONTENT_URI,projection, selection, null, null);
                cursor.moveToFirst();
                final long rawId = cursor.getLong(0);
                ContentValues values = new ContentValues();
                values.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID,rawId);
                values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,mGroupId);
                values.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE,ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }
            ArrayList<ContactEntry> data = getContactsByGroupId(mGroupId);
            return data;
        }
        @Override
        protected void onPostExecute(ArrayList result) {
            mI99GroupAdapter.update(result);
            if(mProgressDialog != null && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            I99GroupActivity.this.mTask = null ;
            super.onPostExecute(result);
        }
    }

    
    // add by y.haiyang for care os (start)
    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            I99GroupActivity.this.finish();
        }
        public void onRightClick(View v){
            showOptionDialog();
        }
        public void onTitleClick(View v){

        }
    };

    private void configureEmptyView() {
        View empty = findViewById(android.R.id.empty);

        TextView text = (TextView) empty.findViewById(R.id.label);
        text.setText(R.string.noContacts);

        ImageView icon = (ImageView) empty.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.care_icon_empty_contacts);
        mContactList.setEmptyView(empty);
    }
    // add by y.haiyang for care os (end)
}
