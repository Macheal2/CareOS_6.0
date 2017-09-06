package com.cappu.drugsteward.util;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cappu.drugsteward.entity.Member;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;

public class MemberAdapter extends BaseAdapter{
    
    private Context mContext;
    private List<Member> mFamily;
    private LayoutInflater inflater;
    private SqlOperation operation;

    
    public MemberAdapter(Context context,List<Member> m){
        this.mContext=context;
        this.mFamily=m;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.i("test","==适配器=="+mFamily.size());
        operation = new SqlOperation(mContext);
    }

    @Override
    public int getCount() {
        if(mFamily == null){
            return 0;
        }
        return mFamily.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        GameTag tag=null;
        if(convertView==null){
            convertView=inflater.inflate(R.layout.activity_exist_member_item,null);
            tag=new GameTag();
            tag.mname=(TextView) convertView.findViewById(R.id.member_name);
            //tag.mdelete=(ImageView) convertView.findViewById(R.id.member_delete);
            convertView.setTag(tag);
        }
        tag=(GameTag) convertView.getTag();
        tag.mname.setText(""+mFamily.get(position).getName());
//        tag.mdelete.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View arg0) {
//                operation.deletemember( mFamily.get(position));
//                mFamily=operation.getmember();
//                notifyDataSetChanged();
//            }
//        });
        return convertView;
    }
    
    private class GameTag{
        TextView mname;
        ImageView mdelete;
        
    }
    
    

}
