package com.cappu.music;

import java.util.List;

import com.cappu.music.database.MusicProvider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InventoryAdapter extends BaseAdapter {

    private Context mContext;
    //private List<Item> items;
    /**歌单数据*/
    List<MusicInventory> mInventoryListdate;

    /**当前选中歌曲的ID*/
    long mSelectedId = -1;
    InventoryViewHolder inventoryViewHolder;
    boolean isShow = false;

    public InventoryAdapter(Context context, List<MusicInventory> items) {
        super();
        this.mContext = context;
        this.mInventoryListdate = items;
    }
    
    public void setInventoryDate(List<MusicInventory> list){
        this.mInventoryListdate = list;
    }
    
    public void setSelectedID(Long id){
        this.mSelectedId = id;
    }

    @Override
    public int getCount() {
        return mInventoryListdate.size();
    }

    @Override
    public Object getItem(int position) {
        return mInventoryListdate.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.inventory_item, null);
            inventoryViewHolder = new InventoryViewHolder();
            inventoryViewHolder.nName = (TextView) convertView.findViewById(R.id.name);
            inventoryViewHolder.mAddTip = (CheckBox) convertView.findViewById(R.id.add_tip);
            convertView.setTag(inventoryViewHolder);
        } else {
            inventoryViewHolder = (InventoryViewHolder) convertView.getTag();
        }
        
        MusicInventory musicInventory = mInventoryListdate.get(position);
        
        inventoryViewHolder.nName.setText(musicInventory.getInventoryName());
        inventoryViewHolder.mInventoryId = musicInventory.getId();
        
        Cursor c = mContext.getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ inventoryViewHolder.mInventoryId +"' and songId ='"+mSelectedId+"'", null, null);
        if(c!= null && c.getCount() > 0){
            inventoryViewHolder.mAddTip.setChecked(true);
        }else{
            inventoryViewHolder.mAddTip.setChecked(false);
        }
        c.close();
        return convertView;
    }

    class InventoryViewHolder {
        long mInventoryId;
        TextView nName;
        CheckBox mAddTip;
    }

}
