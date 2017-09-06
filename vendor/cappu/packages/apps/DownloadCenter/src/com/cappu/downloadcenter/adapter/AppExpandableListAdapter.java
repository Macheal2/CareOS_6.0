package com.cappu.downloadcenter.adapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.cache.BitmapCaches;
import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.common.view.AlertDialog;
import com.cappu.downloadcenter.common.view.ListGridView;
import com.cappu.downloadcenter.context.FolderApplication;
import com.cappu.downloadcenter.context.MainActivity;
import com.cappu.downloadcenter.context.RecommendButtonInfo;
import com.cappu.downloadcenter.download.DownloadManager;
import com.cappu.downloadcenter.common.entity.DownloadCenterTitle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.util.Log;

import com.cappu.downloadcenter.R;

public class AppExpandableListAdapter extends BaseExpandableListAdapter {
    public static final int ItemHeight = 75;// 每项的高度
    public static final int PaddingLeft = 36;// 每项的高度
    private int myPaddingLeft = 0;
    private List<ImgNode> mImgAdapters  = new ArrayList<ImgNode>();
//    private ListGridView toolbarGrid;
    private static final String TAG = "ExpandableListAdapter";
    private List<TreeNode> treeNodes = new ArrayList<TreeNode>();//父节点
    private Context parentContext;
    private LayoutInflater layoutInflater;

    static public class TreeNode {
        public DownloadCenterTitle parent;
        public List<RecommendButtonInfo> childs = new ArrayList<RecommendButtonInfo>();//子节点
    }
    
    static public class ImgNode {
        public int item;
        public MyImgAdapter adapter;
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
     * 获得标题数组内容
     * @param groupPosition 第几项分类数组
     * @return 返回数组
     * @author hmq
     */
    public DownloadCenterTitle getTitleGroup(int groupPosition) {
        return treeNodes.get(groupPosition).parent;
    }
    
    /**
     * 更新标题数组内容
     * @param groupPosition 第几项分类数组
     * @param list 分类数组内容
     * @author hmq
     */
    public void updateTitleGroup(int groupPosition, DownloadCenterTitle title) {
        treeNodes.get(groupPosition).parent = title;
        notifyDataSetChanged();
    }
    
    public DownloadCenterTitle getGroup(int groupPosition) {
        return treeNodes.get(groupPosition).parent;
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    
    public int getGroupCount() {
        return treeNodes.size();
    }
    
    public class ViewHolderTitle
    {  
        public TextView textView;
        public ImageView arrow;
        
    }  
    /**
     * 可自定义list
     */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderTitle holderTitle;
        
        if (convertView == null) {
            holderTitle = new ViewHolderTitle();
            convertView = LayoutInflater.from(parentContext).inflate(R.layout.expandable_title, null, false);
            holderTitle.textView = (TextView)convertView.findViewById(R.id.text_view);
            holderTitle.arrow = (ImageView)convertView.findViewById(R.id.arrow_img);
        }else{
            holderTitle = (ViewHolderTitle)convertView.getTag();
        }
        
        holderTitle.textView.setText(getGroup(groupPosition).name);
        
        if(isExpanded){
            holderTitle.arrow.setImageResource(R.drawable.im_arrow_up);
        }else{
            holderTitle.arrow.setImageResource(R.drawable.im_arrow_down);
        } 
        
        convertView.setTag(holderTitle);
        return convertView;
    }
    
    /************************CHILD*************************************/
    /**
     * 获得分类数组内容
     * @param groupPosition 第几项分类数组
     * @return 返回数组
     * @author hmq
     */
    public List<RecommendButtonInfo> getChildGroup(int groupPosition) {
        return treeNodes.get(groupPosition).childs;
    }
    
    /**
     * 更新分类数组内容
     * @param groupPosition 第几项分类数组
     * @param list 分类数组内容
     * @author hmq
     */
    public void updateChildGroup(int id, List<RecommendButtonInfo> list) {
        for (int i = 0; i < treeNodes.size(); i++){
            if (treeNodes.get(i).parent.id == id){
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
        int size = 0;
        if ((treeNodes.get(groupPosition).childs != null) && (treeNodes.get(groupPosition).childs.size() != 0))
            size = 1;
        return size;
    }
    
    public class MainViewHolder
    {
        ListGridView toolbarGrid;
    }
    
    /**
     * 可自定义ExpandableListView
     * 该方法决定每个子选项的外观
     */
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        MainViewHolder mainViewHolder;
        boolean hasAdapter = false;
        
        if (convertView == null) {
            mainViewHolder = new MainViewHolder();
            layoutInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.title_grid_view, null);
            mainViewHolder.toolbarGrid = (ListGridView) convertView.findViewById(R.id.gridview_toolbar);
            mainViewHolder.toolbarGrid.setNumColumns(4);// 设置每行列数
            mainViewHolder.toolbarGrid.setGravity(Gravity.CENTER);// 位置居中
            mainViewHolder.toolbarGrid.setHorizontalSpacing(2);// 水平间隔
            mainViewHolder.toolbarGrid.setVerticalSpacing(6);
//            toolbarGrid.setAdapter(getMenuAdapter(menu_toolbar_name_array, menu_toolbar_image_array));// 设置菜单Adapter
//            toolbarGrid.addView(getChildGroup(groupPosition).get(childPosition));
            
//            toolbarGrid.setOnItemClickListener(this);
            convertView.setTag(mainViewHolder);
        }else {
            mainViewHolder = (MainViewHolder)convertView.getTag();
//            mainViewHolder.toolbarGrid.setAdapter(mainViewHolder.adapter);// 设置菜单Adapter
        }
        
        boolean findAdapter = false;
        for (int i = 0; i < mImgAdapters.size(); i++) {
            if (mImgAdapters.get(i).item == treeNodes.get(groupPosition).parent.id){
                findAdapter = true;
                
//                if (mainViewHolder.toolbarGrid.getAdapter() != null) {
//                    mainViewHolder.toolbarGrid.setAdapter(mImgAdapters.get(i).adapter);
//                } else {
                    mainViewHolder.toolbarGrid.setAdapter(mImgAdapters.get(i).adapter);
//                }
            }
        }
        
        if (!findAdapter){
            MyImgAdapter adapter;
            adapter = new MyImgAdapter(parentContext, getChildGroup(groupPosition));

            ImgNode node = new ImgNode();
            node.item = treeNodes.get(groupPosition).parent.id;
            node.adapter = adapter;
            mImgAdapters.add(node);
            mainViewHolder.toolbarGrid.setAdapter(mImgAdapters.get(mImgAdapters.size() - 1).adapter);// 设置菜单Adapter

        }
        
        return convertView;
    }
    
    /***************************item**************************************/
    public class ViewHolder  
    {  
        public LinearLayout item;
        public ImageView icon;
        public TextView title;
    }  
    
    public class MyImgAdapter extends BaseAdapter {
        private Context context;// 用于接收传递过来的Context对象
        private List<RecommendButtonInfo> imgs;

        public MyImgAdapter(Context context,  List<RecommendButtonInfo> img) {
//            super();
            this.context = context;
            this.imgs = img;
        }
        
        @Override
        public int getCount() {
            if (imgs.size() < 1)
                return 0;
            return imgs.size();
        }
        
        @Override
        public Object getItem(int position) {
            return position;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        public void updateDate(List<RecommendButtonInfo> img){
            this.imgs = img;
            notifyDataSetChanged();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder myholder;
            RecommendButtonInfo child;

            if (convertView == null) {
                myholder = new ViewHolder();
                convertView = LayoutInflater.from(parentContext).inflate(R.layout.expandable_list, null, false);
                myholder.item = (LinearLayout) convertView.findViewById(R.id.item);
                myholder.icon = (ImageView) convertView.findViewById(R.id.recommend_icon);
                myholder.title = (TextView) convertView.findViewById(R.id.recommend_title);
                child = imgs.get(position);
            } else {
                // if(0 == position)
                // {
                // return convertView;
                // }
                myholder = (ViewHolder) convertView.getTag();
                child = imgs.get(position);
            }
            // BitmapCaches.getInstance().getBitmap(context,
            // child.getShorcutInfo().icon, myholder.icon, null);
            if (parent.getChildCount() == position) {
                child.setView(convertView);//(myholder.title, myholder.item);
//                child.getBitmap(myholder.icon);// myholder.icon.setImageBitmap           
//                myholder.title.setText(child.getShorcutInfo().name);
                myholder.item.setTag(child);
            }
            convertView.setTag(myholder);

            return convertView;
        }
    }
}
