
package com.cappu.download.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cappu.download.R;
import com.cappu.download.ui.widget.TopBar;

public class HelpNotesActivity extends Activity{
    Context TextContext;
    ImageButton mCancel;

    TextView mTitle;

    ImageButton mOption;

    //TextView about_info;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_notes);
        init();
    }
    public void init() {
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mOption.setVisibility(View.GONE);
        mTitle.setText(getString(R.string.help_notes));
        
        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
