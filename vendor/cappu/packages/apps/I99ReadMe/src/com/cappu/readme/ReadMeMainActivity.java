package com.cappu.readme;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.readme.util.DensityUtil;
import com.cappu.readme.view.LancherLayout;

public class ReadMeMainActivity extends Activity implements OnClickListener{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_me_activity);
    }

    @Override
    public void onClick(View v){
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Process.killProcess(Process.myPid());
    }

}
