package com.cappu.launcherwin.kookview;

import java.lang.reflect.Method;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.WorkspaceUpdateReceiver;
import com.cappu.launcherwin.kookview.AlbumsRelativeLayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class AlbumsKookView {
    private Context mContext;
    
    public View getView(final Context context, WorkspaceUpdateReceiver workspaceUpdateReceiver){
        this.mContext = context;
        LayoutInflater li = LayoutInflater.from(context);
            View v = li.inflate(R.layout.albums_appwidget, null);
            if(v instanceof AlbumsRelativeLayout){
                workspaceUpdateReceiver.initAlbumsRelativeLayout((AlbumsRelativeLayout)v);
            }
            return v;
    }
}
