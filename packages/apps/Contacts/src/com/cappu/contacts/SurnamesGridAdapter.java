package com.cappu.contacts;

import java.util.ArrayList;

import java.util.List;

import android.app.LoaderManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.R;
import com.cappu.contacts.util.FilterNodes;
import com.cappu.contacts.widget.NoScrollGridView;

public class SurnamesGridAdapter extends BaseAdapter implements OnClickListener {

    Context mContext;
    SurnamesCallBack mCallBack;
    LoaderManager mLoaderManager;
    List<FilterNodes> mData;
    List<List<FilterNodes>> mCacheDatas = new ArrayList();

    public SurnamesGridAdapter(Context context, List<FilterNodes> data) {
        mContext = context;
        mData = data;
        mCacheDatas.clear();
    }

    @Override
    public int getCount() {
        if(mData != null ){
            return mData.size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0) {
        if(mData != null ){
            return mData.get(arg0);
        }else{
            return null ;
        }
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {

        if(mData == null ){
            return null;
        }
        ViewHolder holder ;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.i99_surnames_grid_item_layout, null);
            holder.name = (Button)view.findViewById(R.id.name);
            holder.name.setOnClickListener(this);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        holder.name.setText(mData.get(position).getData());
        holder.name.setTag(mData.get(position));

        return view;
    }

    @Override
    public void onClick(View view) {
        Button bu = (Button) view;
        FilterNodes filter = (FilterNodes) bu.getTag();
        List<FilterNodes> data = filter.getChildren();

        mCacheDatas.add(mData);
        mData = data;
        notifyDataSetChanged();

        if(mCallBack != null){
            mCallBack.updateFilter(filter);
        }

    }

    public void setCallBack(SurnamesCallBack callback){
        mCallBack = callback;
    }

    public interface CallBack{
        void showSurGrid(FilterNodes filter ,boolean add);
    }

    public boolean setBackData(){
        if(mCacheDatas.size() > 0 ){
            mData = mCacheDatas.get(mCacheDatas.size() -1);
            notifyDataSetChanged();
            mCacheDatas.remove(mCacheDatas.size() -1);
            return true;
        }else{
            return false;
        }
    }

    public List<FilterNodes> getBeforeData(){
        return mCacheDatas.get(mCacheDatas.size() -1);
    }

    public class ViewHolder {
        Button name;
    }
}
