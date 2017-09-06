package com.cappu.launcherwin.kookview.assembly;

import java.util.List;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.WorkspaceUpdateReceiver;
import com.cappu.launcherwin.kookview.AlbumsRelativeLayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
public class AssemblyJeffView {
    private Context mContext;
    
    public View getView(final Context context,WorkspaceUpdateReceiver workspaceUpdateReceiver){
        this.mContext = context;
        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.assembly_appwidget, null);
        if(v instanceof AssemblyRelativeLayout){
        	List<View> lists=((AssemblyRelativeLayout)v).getLists();
        	for(View view : lists){
        		if(view instanceof AlbumsRelativeLayout){
        			workspaceUpdateReceiver.initAlbumsRelativeLayout((AlbumsRelativeLayout)view);
        		}else if(view instanceof HealthLinearLayout){
        			workspaceUpdateReceiver.initHealthLayout((HealthLinearLayout)view);
        		}
        	}
        }
        return v;
    }
}
