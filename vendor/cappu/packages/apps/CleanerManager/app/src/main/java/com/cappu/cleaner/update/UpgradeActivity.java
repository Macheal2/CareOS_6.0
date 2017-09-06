package com.cappu.cleaner.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Xml;
public class UpgradeActivity extends Activity {
    AlertDialog mAlertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        
    }
    
    private InputStream getFileStream(String fileName) {
        InputStream is = null;
        try {
            is = getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        //UpdateCenter.onResume(this);
    }

}
