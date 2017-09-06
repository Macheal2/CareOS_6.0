package com.cappu.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.R;
import com.cappu.contacts.util.FilterNodes;
import com.cappu.contacts.widget.NoScrollGridView;

public class SurnamesListAdapter extends BaseAdapter implements OnClickListener {

    Context mContext;
    SurnamesCallBack mCallBack;
    LoaderManager mLoaderManager;
    List<FilterNodes> mData;
    List<List<FilterNodes>> mCacheDatas = new ArrayList();

    public SurnamesListAdapter(Context context, List<FilterNodes> data) {
        mContext = context;
        mData = data;
        mCacheDatas.clear();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        Cache cache = null;
        FilterNodes filter = mData.get(position);
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.i99_surnames_item_layout, null);
            cache = new Cache(view,filter);
            view.setTag(cache);
        } else {
            cache = (Cache) view.getTag();
        }

        cache.header.setOnClickListener(this);
        cache.header.setText(filter.getData());
        cache.header.setTag(filter);
        cache.content.setAdapter(new SubAdapter(filter.getChildren()));
        return view;
    }

    @Override
    public void onClick(View v) {
        FilterNodes filter = (FilterNodes)v.getTag();
        switch(v.getId()){
            case R.id.header:
                if(mCallBack != null){
                    mCallBack.showSurGrid(filter,false);
                }
            break;
            default:
                if(mCallBack != null){
                    mCallBack.showSurGrid(filter,true);
                }
            break;
        }


    }

    public void setCallBack(SurnamesCallBack callback){
        mCallBack = callback;
    }

    public interface CallBack{
        void showSurGrid(FilterNodes filter ,boolean add);
    }

    public class Cache {
        Button header;
        NoScrollGridView content;
        FilterNodes tree;
        public Cache(View view, FilterNodes tree) {
            Cache.this.tree = tree;
            header = (Button) view.findViewById(R.id.header);
            content = (NoScrollGridView) view.findViewById(R.id.content);
        }

    }

    private class SubAdapter extends BaseAdapter{
        List<FilterNodes> data;
        public SubAdapter(List<FilterNodes> data){
            SubAdapter.this.data = data;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
            SubViewHolder holder;
            if (view == null) {
                holder = new SubViewHolder();
                view = LayoutInflater.from(mContext).inflate(R.layout.i99_surnames_grid_item_layout, null);
                holder.name = (Button)view.findViewById(R.id.name);
                holder.name.setOnClickListener(SurnamesListAdapter.this);
                view.setTag(holder);
            }else{
                holder = (SubViewHolder)view.getTag();
            }
            holder.name.setText(data.get(position).getData());
            holder.name.setTag(data.get(position));
            return view;
        }
    }

    public class SubViewHolder{
        Button name;
    }
}
