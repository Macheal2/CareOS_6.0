package com.cappu.contacts;

import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.widget.ListView;

import com.android.contacts.R;
import com.cappu.contacts.util.I99Utils;
import com.cappu.contacts.widget.I99Dialog;

public class I99GroupDeleteMember extends Activity implements View.OnClickListener , OnItemClickListener{
    private static final String TAG = "I99GroupDeleteMember";
    private int mGroupId;

    ListView mListView;

    ArrayList<ContactEntry> mData;
    ListAdapter mListAdapter;
    Dialog mOptionDialog;
    I99Dialog mProgressDialog;
    I99Dialog mInitDataDialog;

    RemoveTask mRemoveTask;
    InitDataTask mInitTask;

    TopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_delete_member_activty_layout);

        mData = getIntent().getParcelableArrayListExtra("GROUP_MEMBER");
        mGroupId = getIntent().getIntExtra(I99GroupActivity.KEY_GROUP_ID, -1);

        mListView = (ListView)findViewById(R.id.list);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);
        configureEmptyView();

        mTopBar = (TopBar)findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(mTopBarListener);
    }

    @Override
    protected void onResume() {
        // if mData size is large more 8 show dialog
        if(mData.size() > 8){
            mInitDataDialog = getInitDataDialog();
            mInitDataDialog.show();
        }

        mInitTask = getInitTask();
        mInitTask.execute(mData);
        super.onResume();
    }

    @Override
    public void onClick(View v){
        //TODO
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        ListCache cache = (ListCache)view.getTag();
        final boolean state = cache.isChecked();
        ContactEntry entry = (ContactEntry)mData.get(position);
        entry.setState(!state);
        mListView.setItemChecked(position,!state);
        mListAdapter.notifyDataSetChanged();
    }

    private void removeContact(int groupId , int contactId){
        if(groupId == -1){
            Log.i(TAG,"remove error");
            return;
        }

        String select = ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID
                + "=? and "
                + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                + "=? and "
                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
                + "=?";
        String args[] ={
            String.valueOf(contactId),
            String.valueOf(groupId),
            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
        };
        getContentResolver().delete(ContactsContract.Data.CONTENT_URI, select, args);

    }
    public class ListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
            ListCache cache = null ;
            ContactEntry entry = (ContactEntry)mData.get(position);
            if(view == null){
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                view = inflater.inflate(R.layout.care_remove_member_item_layout, null);
                cache = new ListCache(view);
                view.setTag(cache);
            }else{
                cache = (ListCache)view.getTag();
            }


            if(entry.photo == null){
                cache.photo.setImageResource(R.drawable.i99_default_photo);
            }else{
                cache.photo.setImageBitmap(entry.photo);
            }
            cache.name.setText(entry.name);
            cache.state.setChecked(entry.state);
            return view;
        }

    }

    private class ListCache{
        ImageView photo;
        TextView name;
        CheckBox state;
        public ListCache(View v){
            photo = (ImageView)v.findViewById(R.id.photo);
            name = (TextView)v.findViewById(R.id.name);
            state = (CheckBox)v.findViewById(R.id.state);
        }
        public void setChecked(boolean check){
            state.setChecked(check);
        }
        public boolean isChecked(){
            return state.isChecked();
        }
    }

    private void showOptionDialog(){
        mOptionDialog = new Dialog(this, R.style.I99DialogStyle);
        final boolean isSelectedAll = isSelectedAll();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.i99_multi_choice_option_dialog, null);
        TextView title = (TextView)view.findViewById(R.id.title);
        Button select = (Button)view.findViewById(R.id.select);
        select.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isSelectedAll){
                    setSelectedNone();
                }else{
                    setSelectedAll();
                }
                if(mOptionDialog != null && mOptionDialog.isShowing()){
                    mOptionDialog.dismiss();
                    mOptionDialog = null ;
                }
            }
        });
        boolean mIsSelectedAll = false;
        if(isSelectedAll){
           select.setText(R.string.menu_select_none);
        }else{
           select.setText(R.string.menu_select_all);
        }

        title.setTextSize(I99Font.TITLE);
        select.setTextSize(I99Font.TITLE);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = I99Utils.getScreenSize(this)[0];
        mOptionDialog.addContentView(view, lp);
        mOptionDialog.show();
        Window window = mOptionDialog.getWindow();
        lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.CarePopupAnimation);
    }

    private boolean isSelectedAll(){
        for(int i=0 ; i< mData.size() ; i++){
            if(!mData.get(i).state){
                return false;
            }
        }
        return true;
    }

    private boolean isSelectedNone(){
        for(int i=0 ; i< mData.size() ; i++){
            if(mData.get(i).state){
                return false;
            }
        }
        return true;
    }

    private void setSelectedAll(){
        for(int i=0 ; i< mData.size() ; i++){
            if(!mData.get(i).state){
                mData.get(i).setState(true);
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    private void setSelectedNone(){
        for(int i=0 ; i< mData.size() ; i++){
            if(mData.get(i).state){
                mData.get(i).setState(false);
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(mListAdapter.getCount() > 0 ){
            showOptionDialog();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(mOptionDialog != null && mOptionDialog.isShowing()){
            mOptionDialog.dismiss();
            mOptionDialog = null ;
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOptionDialog != null && mOptionDialog.isShowing()){
            mOptionDialog.dismiss();
            mOptionDialog = null ;
        }
        if(mInitDataDialog != null && mInitDataDialog.isShowing()){
            mInitDataDialog.dismiss();
            mInitDataDialog = null ;
        }

    }

    private RemoveTask getRemoveTask(){
        if(mRemoveTask != null){
            mRemoveTask.cancel(true);
            mRemoveTask = null ;
        }
        return new RemoveTask();
    }

    class RemoveTask extends AsyncTask<ArrayList, Integer, ArrayList> {

        public RemoveTask() {
            super();
        }
        @Override
        protected ArrayList doInBackground(ArrayList... params) {
            if (isCancelled()) {
                return null;
            }
            for(ContactEntry entry : mData){
                if(entry.state){
                    removeContact(mGroupId,entry.id);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList result) {
            if(mProgressDialog != null && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            I99GroupDeleteMember.this.finish();
            super.onPostExecute(result);
        }
    }

    private I99Dialog getProgressDialog(){
        if(mProgressDialog == null){
            mProgressDialog = new I99Dialog(I99GroupDeleteMember.this);
            mProgressDialog.setTitle(R.string.i99_delete);
            mProgressDialog.setMessage(R.string.i99_deleting);
            mProgressDialog.setControlVisibility(View.GONE);
            mProgressDialog.setProgressVisibility(View.VISIBLE);
        }
        return mProgressDialog;
    }

    private InitDataTask getInitTask(){
        if(mInitTask != null){
            mInitTask.cancel(true);
            mInitTask = null ;
        }
        return new InitDataTask();
    }
    class InitDataTask extends AsyncTask<ArrayList, Integer, ArrayList> {

        public InitDataTask() {
            super();
        }
        @Override
        protected ArrayList doInBackground(ArrayList... params) {
            if (isCancelled()) {
                return null;
            }
            ContentResolver cr = getContentResolver();
            for(int i=0;i<mData.size() ; i++ ){
                ContactEntry entry = (ContactEntry)mData.get(i);
                InputStream input = parseDisplayPhoto(cr,entry.id);
                if(input == null){
                    input = parseThumbnailPhoto(cr,entry.id);
                }
                if(input != null){
                    Bitmap photo = BitmapFactory.decodeStream(input);
                    photo = I99Utils.getRoundedCornerBitmap(photo);
                    entry.setPhoto(photo);
                }else{
                    entry.setPhoto(null);
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(ArrayList result) {
            mListAdapter.notifyDataSetChanged();
            if(mInitDataDialog != null && mInitDataDialog.isShowing()){
                mInitDataDialog.dismiss();
                mInitDataDialog = null;
            }
            super.onPostExecute(result);
        }
    }

    private I99Dialog getInitDataDialog(){
        if(mInitDataDialog == null){
            mInitDataDialog = new I99Dialog(I99GroupDeleteMember.this);
            mInitDataDialog.setTitle(R.string.i99_load);
            mInitDataDialog.setMessage(R.string.i99_loading);
            mInitDataDialog.setControlVisibility(View.GONE);
            mInitDataDialog.setProgressVisibility(View.VISIBLE);
        }
        return mInitDataDialog;
    }

    private InputStream parseDisplayPhoto(ContentResolver cr ,int id){
        Cursor cursor = cr.query(RawContacts.CONTENT_URI,new String[]{RawContacts.CONTACT_ID}, RawContacts._ID + "=?", new String[] { id + ""},null);
        cursor.moveToFirst();
        int contac_id = cursor.getInt(0);
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contac_id);
        Uri displayPhotoUri = Uri.withAppendedPath(photoUri, Contacts.Photo.DISPLAY_PHOTO);
         try {
             AssetFileDescriptor fd = cr.openAssetFileDescriptor(displayPhotoUri, "r");
             cursor.close();
             return fd.createInputStream();
         } catch (IOException e) {
             return null;
         }
    }

    private InputStream parseThumbnailPhoto(ContentResolver cr ,int id){
        Cursor cursor = cr.query(RawContacts.CONTENT_URI,new String[]{RawContacts.CONTACT_ID}, RawContacts._ID + "=?", new String[] { id + ""},null);
        cursor.moveToFirst();
        int contac_id = cursor.getInt(0);
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contac_id);
        cursor.close();
        return  ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
    }

    private void configureEmptyView() {
        View empty = findViewById(android.R.id.empty);

        TextView text = (TextView) empty.findViewById(R.id.label);
        text.setText(R.string.noContacts);

        ImageView icon = (ImageView) empty.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.care_icon_empty_contacts);
        mListView.setEmptyView(empty);
    }

    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            I99GroupDeleteMember.this.finish();
        }
        public void onRightClick(View v){
            if(isSelectedNone()){
                Toast.makeText(v.getContext(), R.string.multichoice_no_select_alert,
                                 Toast.LENGTH_SHORT).show();
            }else{
                mProgressDialog = getProgressDialog();
                mProgressDialog.show();

                mRemoveTask = getRemoveTask();
                mRemoveTask.execute(mData);
            }
        }
        public void onTitleClick(View v){

        }
    };
}
