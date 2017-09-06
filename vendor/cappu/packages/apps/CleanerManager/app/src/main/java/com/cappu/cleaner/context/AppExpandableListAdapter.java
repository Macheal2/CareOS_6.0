package com.cappu.cleaner.context;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.cappu.cleaner.R;
import com.cappu.cleaner.Util;

import java.util.ArrayList;
import java.util.List;

public class AppExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "ExpandableListAdapter";
    private List<TreeNode> treeNodes = new ArrayList<TreeNode>();//父节点
    private Context parentContext;
    private LayoutInflater layoutInflater;

    static public class TreeNode {
        public fileTitleInfo parent;
        public List<fileCleanInfo> childs = new ArrayList<fileCleanInfo>();//子节点
    }

    public AppExpandableListAdapter(Context context) {
        parentContext = context;
    }

    public List<TreeNode> GetTreeNode() {
        return treeNodes;
    }

    public void UpdateTreeNode(List<TreeNode> nodes) {
        if (nodes != null) {
            treeNodes = nodes;
            notifyDataSetChanged();
        }
    }

    public void RemoveAll() {
        treeNodes.clear();
    }

    public boolean hasStableIds() {
        return true;
    }
    /**************************GROUP***********************************/
    /**
     * 更新标题数组内容
     *
     * @param groupPosition 第几项分类数组
     * @param title         分类数组内容
     * @author hmq
     */
    public void updateTitleGroup(int groupPosition, fileTitleInfo title) {
        treeNodes.get(groupPosition).parent = title;
        notifyDataSetChanged();
    }

    public fileTitleInfo getGroup(int groupPosition) {
        return treeNodes.get(groupPosition).parent;
    }

    public fileTitleInfo getGroupById(int id) {
        for (int i = 0; i < treeNodes.size(); i++) {
            if (id == treeNodes.get(i).parent.id) {
                return treeNodes.get(i).parent;
            }
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return treeNodes.get(groupPosition).parent.id;
    }

    public int getGroupCount() {
        return treeNodes.size();
    }

    public class ViewHolderTitle {
        public TextView textView;
        public ImageView arrow;

    }

    /**
     * 可自定义list
     */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderTitle holderTitle;
        Log.e("hmq", "getGroupView=" + groupPosition);
        if (convertView == null) {
            holderTitle = new ViewHolderTitle();
            convertView = LayoutInflater.from(parentContext).inflate(R.layout.expandable_title, null, false);
            holderTitle.textView = (TextView) convertView.findViewById(R.id.text_view);
            holderTitle.arrow = (ImageView) convertView.findViewById(R.id.arrow_img);
        } else {
            holderTitle = (ViewHolderTitle) convertView.getTag();
        }

        holderTitle.textView.setText(getGroup(groupPosition).name);

        if (isExpanded) {
            holderTitle.arrow.setImageResource(R.drawable.im_arrow_up);
        } else {
            holderTitle.arrow.setImageResource(R.drawable.im_arrow_down);
        }

        convertView.setTag(holderTitle);
        return convertView;
    }

    /************************CHILD*************************************/
    /**
     * 获得分类数组内容
     *
     * @param groupPosition 第几项分类数组
     * @return 返回数组
     * @author hmq
     */
    public List<fileCleanInfo> getChildGroup(int groupPosition) {
        return treeNodes.get(groupPosition).childs;
    }

    /**
     * 获得分类数组内容
     *
     * @param id 根据主的ID来获取子的数据
     * @return 返回数组
     * @author hmq
     */
    public List<fileCleanInfo> getChildGroupByID(int id) {
        for (int i = 0; i < treeNodes.size(); i++) {
            if (id == treeNodes.get(i).parent.id) {
                return treeNodes.get(i).childs;
            }
        }
        return null;
    }

    /**
     * 更新分类数组内容
     *
     * @param list 分类数组内容
     * @author hmq
     */
    public void updateChildGroup(int id, List<fileCleanInfo> list) {
        for (int i = 0; i < treeNodes.size(); i++) {
            if (treeNodes.get(i).parent.id == id) {
                Log.e("hmq", "3 id= " + treeNodes.get(i).parent.name + "; list=" + list.size());
                treeNodes.get(i).childs = list;
                notifyDataSetChanged();
            }
        }
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return treeNodes.get(groupPosition).childs;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * 该组的子项数目
     */
    public int getChildrenCount(int groupPosition) {
        return treeNodes.get(groupPosition).childs.size();
    }

    public class MainViewHolder {
        RelativeLayout item;
        ImageView iconItem;
        TextView titleItem;
        TextView subTitleItem;
        TextView fileSizeItem;
        Switch switchItem;
    }

    /**
     * 可自定义ExpandableListView
     * 该方法决定每个子选项的外观
     */
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        MainViewHolder mainViewHolder;
        boolean hasAdapter = false;

        fileCleanInfo info = getChildGroup(groupPosition).get(childPosition);
        if (convertView == null) {
            mainViewHolder = new MainViewHolder();
            layoutInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_item, null);
            mainViewHolder.item = (RelativeLayout) convertView.findViewById(R.id.list_item);
            mainViewHolder.iconItem = (ImageView) convertView.findViewById(R.id.item_icon);
            mainViewHolder.titleItem = (TextView) convertView.findViewById(R.id.title_item);
            mainViewHolder.subTitleItem = (TextView) convertView.findViewById(R.id.sub_title_item);
            mainViewHolder.fileSizeItem = (TextView) convertView.findViewById(R.id.file_size_item);
            mainViewHolder.switchItem = (Switch) convertView.findViewById(R.id.switch_item);
            mainViewHolder.item.setOnClickListener(new MyClickListener(info));
        } else {
            mainViewHolder = (MainViewHolder) convertView.getTag();
//            mainViewHolder.toolbarGrid.setAdapter(mainViewHolder.adapter);// 设置菜单Adapter
        }

        mainViewHolder.iconItem.setImageDrawable(info.getIcon());
        mainViewHolder.titleItem.setText(info.getLabel());
        mainViewHolder.subTitleItem.setText(info.getVersion());
        mainViewHolder.fileSizeItem.setText(Util.FormatFileSize(info.getFilesize()));
        mainViewHolder.switchItem.setChecked(info.getState());
        convertView.setTag(mainViewHolder);
        return convertView;
    }

    private class MyClickListener implements View.OnClickListener {
        fileCleanInfo info;

        public MyClickListener(fileCleanInfo info) {
            this.info = info;

        }

        @Override
        public void onClick(View v) {
            Switch sh = (Switch) v.findViewById(R.id.switch_item);
            sh.setChecked(!sh.isChecked());
            info.setState(!sh.isChecked());
        }
    }

}
