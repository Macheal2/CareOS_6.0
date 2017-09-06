package com.cappu.launcherwin.applicationList.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.AppInfo;

public class AllAppAdapter extends BaseAdapter {

    private class GridHolder {
        ImageView appImage;
        TextView appName;
    }

    private Context context;

    private List<AppInfo> list;
    private LayoutInflater mInflater;

    public AllAppAdapter(Context c) {
        super();
        this.context = c;
    }

    public void setList(List<AppInfo> list) {
        this.list = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int index) {

        return list.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        GridHolder holder;
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService("window");
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int scr_w = dm.widthPixels;
        int scr_h = dm.heightPixels;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.all_app_item, null);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, scr_h / 8);
            convertView.setLayoutParams(param);
            holder = new GridHolder();
            holder.appImage = (ImageView) convertView.findViewById(R.id.icon);
            holder.appName = (TextView) convertView.findViewById(R.id.appName);
            convertView.setTag(holder);

        } else {
            holder = (GridHolder) convertView.getTag();
        }
        AppInfo info = list.get(index);
        if (info != null) {
            holder.appName.setText(info.getApp_name());
            holder.appName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
            holder.appName.setSingleLine(true);
            //holder.appImage.setImageDrawable(info.getApp_icon());
            holder.appImage.setImageBitmap(info.getApp_icon());
        }
        return convertView;
    }

}