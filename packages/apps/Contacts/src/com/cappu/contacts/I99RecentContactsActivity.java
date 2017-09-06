package com.cappu.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.Dialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import com.cappu.widget.CareMenu;
import android.widget.TextView;


import com.android.contacts.R;
import com.cappu.contacts.util.I99Utils;

public class I99RecentContactsActivity extends Activity implements OnItemClickListener,OnClickListener{

    List<ContactEntry> mData =new ArrayList<ContactEntry>();
    ListView mListView;


    CareMenu mOptionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_recent_activty_layout);
        mListView = (ListView)findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        configureEmptyView();
    }

    @Override
    protected void onStart() {
        getLoaderManager().initLoader(0, null, mCallBack);
        super.onStart();
    }

    @Override
    protected void onResume() {
        ListAdapter adapter = (ListAdapter)mListView.getAdapter();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }
    @Override
    public void onClick(View v){
        I99RecentContactsActivity.this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        final ContactEntry entry = (ContactEntry)mListView.getAdapter().getItem(position);
        showOptionDialog(entry.number);
    }


    private void showOptionDialog(final String number){
        mOptionDialog = new CareMenu(this);
        mOptionDialog = new CareMenu(I99RecentContactsActivity.this);
        mOptionDialog.setTitle(R.string.i99_option);
        mOptionDialog.addButton(R.string.i99_message);
        mOptionDialog.addButton(R.string.i99_call);
        mOptionDialog.setOnClickListener(new CareMenu.OnClickListener(){
            @Override
            public void onClick(View v){
                switch(v.getId()){
                    case R.string.i99_message:
                        I99Utils.sendMessage(I99RecentContactsActivity.this,number);
                        break;
                    case R.string.i99_call:
                        I99Utils.doCall(I99RecentContactsActivity.this,number);
                        break;
                }
            }
        });
        mOptionDialog.show();
    }

    LoaderCallbacks<Cursor> mCallBack = new LoaderCallbacks<Cursor>() {
        Map<String,String> mStaff = new HashMap<String, String>();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String PROJECT[] = {
                     CallLog.Calls._ID,
                     CallLog.Calls.DATE,
                     CallLog.Calls.CACHED_NAME,
                     CallLog.Calls.NUMBER};
            mData.clear();
            mStaff.clear();
            return new CursorLoader(I99RecentContactsActivity.this, CallLog.Calls.CONTENT_URI , PROJECT, null, null, CallLog.Calls.DATE + " DESC");

        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                long date = cursor.getLong(1);
                String name = cursor.getString(2);
                String number = cursor.getString(3);
                if(TextUtils.isEmpty(mStaff.get(number))){
                    ContactEntry entry = new ContactEntry();
                    entry.id = id;
                    entry.date = date;
                    entry.name = name;
                    entry.number = number;
                    mData.add(entry);
                    mStaff.put(number, number);
                }

            }
            mListView.setAdapter(new ListAdapter(mData));

        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            // TODO Auto-generated method stub

        }
    };

    public class ContactEntry {
        public int id;
        public long date;
        public String name;
        public String number;
    }

    private class ListAdapter extends BaseAdapter{

        List<ContactEntry> data = null;

        public ListAdapter(List<ContactEntry> data ){
            this.data = data;
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
            if(TextUtils.isEmpty(entry.name)){
                cache.name.setText(entry.number);
            }else{
                cache.name.setText(entry.name);
            }
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

    private void configureEmptyView() {
        View empty = findViewById(android.R.id.empty);

        TextView text = (TextView) empty.findViewById(R.id.label);
        text.setText(R.string.noContacts);

        ImageView icon = (ImageView) empty.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.care_icon_empty_contacts);
        mListView.setEmptyView(empty);
    }
}
