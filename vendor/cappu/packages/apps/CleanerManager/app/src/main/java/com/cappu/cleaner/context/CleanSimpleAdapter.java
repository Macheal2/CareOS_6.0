package com.cappu.cleaner.context;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cappu.cleaner.CleanerManager;
import com.cappu.cleaner.FileClean;
import com.cappu.cleaner.R;
import com.cappu.cleaner.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hmq on 17-6-26.
 */

public class CleanSimpleAdapter extends SimpleAdapter {
    private Context mContext; /*运行环境*/
    private List<? extends Map> mData;/*数据源*/
    private LayoutInflater mListContainer; // 视图容器
    private int mResource;
    private int mState;

    class ListItemView { // 自定义控件集合
        public TextView state;
    }

    /*construction function*/
    public CleanSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                              @LayoutRes int resource, String[] from, @IdRes int[] to) {
        super(context, data, resource, from, to);
        this.mListContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
        this.mContext = context;
        this.mData = data;
        this.mResource = resource;
    }

    /**
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return mData.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {

        return mData.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    public int getItemId(String key) {
        int position = -1;
        for (int i = 0; i < getCount(); i++){
            if(mData.get(i).get(CleanerManager.ITEM_ID).equals(key)) {
                position = i;
            }
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int mPosition = position;
        ListItemView listItemView = null;
        if (convertView == null) {
            convertView = mListContainer.inflate(mResource, null);//加载布局
            listItemView = new ListItemView();
            listItemView.state = (TextView) convertView.findViewById(R.id.state);
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();//利用缓存的View
        }


        int state = ((Integer)mData.get(position).get(CleanerManager.ITEM_STATE)).intValue();
        long data = ((Long) mData.get(position).get(CleanerManager.ITEM_DATA)).longValue();

        if (state == FileClean.WORKING_CACHE || state == FileClean.WORKING_FILE) {
            convertView.setEnabled(false);
        } else {
            convertView.setEnabled(true);
        }
        if (data == 0) {
            listItemView.state.setText(R.string.str_clean);
            listItemView.state.setBackgroundResource(R.drawable.ic_item_health_green);
        } else {
            listItemView.state.setText(Util.FormatFileSize(mContext, data));
            listItemView.state.setBackgroundResource(R.drawable.ic_item_health_red);
        }
        return super.getView(position, convertView, parent);
        //return convertView;
    }

    public void setVelue(String key, long velue){
        int index = getItemId(key);
        setVelue(index, velue);
    }

    public void setVelue(int index, long velue) {
        if (index == -1) return;
        Long ivelue = new Long(velue);
        mData.get(index).put(CleanerManager.ITEM_DATA, ivelue);
        notifyDataSetChanged();
    }

    public void setState(int state) {
        mState = state;
    }

    public void initData() {
        if (!mData.isEmpty()) {
            mData.clear();
        }
    }
}