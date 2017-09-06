package com.cappu.launcherwin.characterSequence.tools;

import java.util.List;

import com.cappu.launcherwin.R;
//import com.cappu.launcherwin.contacts.ContactListMultiChoiceActivity.ActionoPerate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class ContactAdapter extends BaseAdapter implements SectionIndexer,View.OnClickListener {
    private List<SortModel> list = null;
    private Context mContext;
    public enum ActionoPerate{
        look,select
    }
    private ActionoPerate mActionoPerate;

    public ContactAdapter(Context mContext, List<SortModel> list,ActionoPerate actionoPerate) {
        this.mContext = mContext;
        this.list = list;
        this.mActionoPerate = actionoPerate;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     * 
     * @param list
     */
    public void updateListView(List<SortModel> list,ActionoPerate actionoPerate) {
        this.list = list;
        this.mActionoPerate = actionoPerate;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final SortModel sortModel = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.kook_contact_item, null);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            viewHolder.imgCall= (ImageButton) view.findViewById(R.id.call_customer);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(sortModel.getSortLetters());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }
        
        if(mActionoPerate == ActionoPerate.select){
            viewHolder.imgCall.setVisibility(View.GONE);
        }else if(mActionoPerate == ActionoPerate.select){
            viewHolder.imgCall.setVisibility(View.VISIBLE);
        }
        
        
        viewHolder.imgCall.setTag(sortModel.getNumber());
        viewHolder.imgCall.setOnClickListener(this);

        viewHolder.tvTitle.setText(this.list.get(position).getName());
        viewHolder.sortModel = sortModel;

        return view;

    }

    public final static class ViewHolder {
        ImageButton imgCall;
        TextView tvLetter;
        TextView tvTitle;
        public SortModel sortModel;
    }
    

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 提取英文的首字母，非英文字母用#代替。
     * 
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public void onClick(View v) {
        Log.i("HHJ", "149  ###########################################:");
        long id = v.getId();
        if(R.id.call_customer == id){
            String phoneNumber = (String) v.getTag();
            Intent quickIntent = new Intent();
            quickIntent.setAction(Intent.ACTION_CALL);
            quickIntent.setData(Uri.parse("tel:"+phoneNumber));
            if(phoneNumber == null || "".equals(phoneNumber)){
                Toast.makeText(mContext, mContext.getString(R.string.contact_is_no_number), Toast.LENGTH_LONG).show();
                return;
            }
            mContext.startActivity(quickIntent);
        }
        
    }
}
