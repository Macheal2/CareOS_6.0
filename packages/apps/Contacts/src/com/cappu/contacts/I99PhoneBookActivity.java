package com.cappu.contacts;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.ContentUris;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ImageButton;
import android.util.Log;

import com.android.contacts.R;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.GroupDetailActivity;


public class I99PhoneBookActivity extends Activity implements OnClickListener{
    private static final String TAG = "I99PhoneBookActivity";
    TextView mFamilyGroup , mRelativeGroup , mFriendGroup, mCommunityGroup;
    TextView mRecentContact , mAllContact;

    Intent mFamilyIntent , mRelativeIntent , mFriendIntent, mCommunityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_phone_book_layout);

        mFamilyGroup = (TextView)findViewById(R.id.i99_family_group);
        mRelativeGroup = (TextView)findViewById(R.id.i99_relatives_group);
        mFriendGroup = (TextView)findViewById(R.id.i99_friend_group);
        mCommunityGroup = (TextView)findViewById(R.id.i99_community_group);
        mRecentContact = (TextView)findViewById(R.id.i99_recent);
        mAllContact = (TextView)findViewById(R.id.i99_all);

        mFamilyGroup.setOnClickListener(this);
        mRelativeGroup.setOnClickListener(this);
        mFriendGroup.setOnClickListener(this);
        mCommunityGroup.setOnClickListener(this);
        mRecentContact.setOnClickListener(this);
        mAllContact.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        getLoaderManager().initLoader(0, null, mLoader);
        super.onStart();
    }

    @Override
    public void onClick(View v){
        Intent intent = null;
        switch(v.getId()){
            case R.id.i99_family_group :
                if(mFamilyIntent != null){
                    startActivity(mFamilyIntent);
                }else{
                    Log.i(TAG,"mFamilyIntent = null");
                }
            break;
            case R.id.i99_relatives_group :
                if(mRelativeIntent != null){
                    startActivity(mRelativeIntent);
                }else{
                    Log.i(TAG,"mRelativeIntent = null");
                }
            break;
            case R.id.i99_friend_group :
                if(mFriendIntent != null){
                    startActivity(mFriendIntent);
                }else{
                    Log.i(TAG,"mFriendIntent != null = null");
                }
            break;
            case R.id.i99_community_group :
                if(mCommunityIntent != null){
                    startActivity(mCommunityIntent);
                }else{
                    Log.i(TAG,"mCommunityIntent = null");
                }
            break;
            case R.id.i99_recent :
                intent = new Intent(I99PhoneBookActivity.this,I99RecentContactsActivity.class);
                startActivity(intent);
            break;
            case R.id.i99_all :
                intent = new Intent(I99PhoneBookActivity.this , PeopleActivity.class);
                startActivity(intent);
            break;


        }
    }

    LoaderCallbacks<Cursor> mLoader = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new I99GroupListLoader(I99PhoneBookActivity.this);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            // TODO Auto-generated method stub

        }
        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                int count = cursor.getInt(2);
                String sysid = cursor.getString(3);
                String accountName = cursor.getString(4);
                String accountType = cursor.getString(5);
                String dataset = cursor.getString(6);
                int systemid = -1;
                if(!TextUtils.isEmpty(sysid)){
                    systemid = Integer.parseInt(sysid);
                }
                switch(systemid){
                    case I99Configure.FAMILY_ID:
                        mFamilyIntent = makeIntent(id,count,title,accountName,accountType,dataset);
                    break;
                    case I99Configure.RELATIVE_ID:
                        mRelativeIntent = makeIntent(id,count,title,accountName,accountType,dataset);
                    break;
                    case I99Configure.FRIEND_ID:
                        mFriendIntent = makeIntent(id,count,title,accountName,accountType,dataset);
                    break;
                    case I99Configure.COMMUNITY_ID:
                        mCommunityIntent = makeIntent(id,count,title,accountName,accountType,dataset);
                    break;

                }

            }
        }

        public int getSystemId(int resId){
            return getResources().getInteger(resId);
        }

        public Intent makeIntent(int id ,int count , String title, String accountName, String accountType, String dataset){
            Intent intent = new Intent(I99PhoneBookActivity.this , I99GroupActivity.class);
            intent.putExtra(I99GroupActivity.KEY_GROUP_ID, id);
            intent.putExtra(I99GroupActivity.KEY_GROUP_TITLE, title);
            intent.putExtra(I99GroupActivity.KEY_GROUP_COUNT, count);
            intent.putExtra(I99GroupActivity.KEY_GROUP_ACCOUNT_NAME, accountName);
            intent.putExtra(I99GroupActivity.KEY_GROUP_ACCOUNT_TYPE, accountType);
            intent.putExtra(I99GroupActivity.KEY_GROUP_DATA_SET, dataset);
            return intent;
        }

    };
}
