package com.cappu.contacts;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.contacts.R;

import com.cappu.contacts.util.I99Utils;

public class I99QuickContactActivity extends Activity {
    public static final String EXTRA_NAME = "contactName";
    public static final String EXTRA_NUMBER = "phoneNumber";
    public static final String EXTRA_PHOTO_KEY = "cellDefImage";
    private static final String EMPTY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_quick_contact_activity);
        final Intent intent = getIntent();
        final String name = intent.getStringExtra(EXTRA_NAME);
        final String number = intent.getStringExtra(EXTRA_NUMBER);
        boolean hasName = !TextUtils.isEmpty(name);
        boolean hasNumber = !TextUtils.isEmpty(number);
        
        Log.e("dengyingContact", "I99QuickContactActivity.java onCreate name=" + name +" number="+number);
        
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (hasName || hasNumber) {
            if(I99Utils.isSavedNumber(I99QuickContactActivity.this,number)){
                I99QuickDoingFragment doingFragment = new I99QuickDoingFragment();
                transaction.add(R.id.container, doingFragment);
            }else{
                Toast.makeText(this, R.string.i99_contacts_already_delete, Toast.LENGTH_SHORT).show();
                intent.putExtra(EXTRA_NUMBER,EMPTY);
                intent.putExtra(EXTRA_NAME,EMPTY);
                setResult(Activity.RESULT_OK,intent);
                I99QuickContactActivity.this.finish();
            }
        }else{
            I99QuickSelectFragment selectFragment = new I99QuickSelectFragment();
            transaction.add(R.id.container,selectFragment);
        }
        transaction.commit();
    }

}
