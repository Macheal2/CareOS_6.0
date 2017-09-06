package com.cappu.launcherwin.kookview;

import java.util.ArrayList;
import java.util.List;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.theme.ThemeRes;


public class VideoRelativeLayout extends RelativeLayout implements OnChildViewClick{

    public VideoRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(Context c) {
        Intent intent =  new Intent();
        intent.setComponent(new ComponentName("com.mediatek.videoplayer", "com.mediatek.videoplayer.MovieListActivity"));
        c.startActivity(intent);
    }
    
    protected void onFinishInflate() {
    };
    

}
