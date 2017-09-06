package com.cappu.launcherwin.kookview;

import java.lang.reflect.Method;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.widget.I99ThemeToast;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class VideoKookView {
    private Context mContext;
    
    public View getView(final Context context){
        this.mContext = context;
        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.video_appwidget, null);
        return v;
    }
}
