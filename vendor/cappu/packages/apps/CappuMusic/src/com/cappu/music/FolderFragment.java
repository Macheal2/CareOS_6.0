package com.cappu.music;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//import com.cappu.music.FolderFragment.GridViewAdapter.GridHolder;
import com.cappu.music.database.MusicProvider;
import com.cappu.music.entiy_gai.Songgengxin;
import com.cappu.music.widget.KookDialog;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FolderFragment extends Fragment implements OnItemClickListener,OnLongClickListener,OnClickListener,View.OnCreateContextMenuListener{

    GridView mGridView;
    GridViewAdapter mGridViewAdapter;
    private List<MusicInventory> mList;
    LayoutInflater mInflater;
    
    /**歌单重命名*/
    public static final int INVENTORY_RENAMING = 1;
    private long RENAMING_ID = -1;
    /**歌单删除*/
    public static final int INVENTORY_DELETE = 2;
    long TARCAT_ID;
    
    KookDialog mDeleteDialog;
    KookDialog.Builder mBuilder;
    
    private int[] mPics = {R.drawable.append_add_def,R.drawable.drama_music,R.drawable.famous_music,R.drawable.pop_music,R.drawable.classical_music,R.drawable.classic_music,R.drawable.pure_music,R.drawable.children_music,R.drawable.huangmei_opera,R.drawable.kunqu_music,R.drawable.qinqiang_music,R.drawable.bedtime_stories};
    int mPos = 0;

    private ColorDrawable mColorDrawable = new ColorDrawable(0xb0000000);
    private View mRootView;
    private PopupWindow mPopupWindowAdd;
    private View mAddViewDialogView;
    private View mFolderAddLayout;
    private TextView mDialogTitle;
    private TextView mAddViewDialogConfirm;//yuan tong qin add 把button改成textview
    private TextView mAddViewDialogCancel;
    private ImageView mFolderIcon;
    private LinearLayout mIconSelectLayout;
    private Gallery mIconGallery;
    private EditText mAddViewDialogEdit;
    private Activity mActivity;
    String headerString = "";
    
    ForegroundColorSpan mForegroundColorSpan = new ForegroundColorSpan(Color.GRAY);
    
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    
    Bundle mBundle;
    
    FolderFragment mFolderFragment;
    private Songgengxin songgengxin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        this.mActivity = getActivity();
        songgengxin = new Songgengxin(mActivity);
        mInflater = inflater;
        mBundle = savedInstanceState;
        mRootView = inflater.inflate(R.layout.folder_fragment, container, false);
        mGridView = (GridView) mRootView.findViewById(R.id.folder_gridview);
        //initData(mActivity);
        Log.i("hehangjun", "FolderFragment onCreateView ");
        //mGridViewAdapter = new GridViewAdapter(getActivity(),mList);
        //mGridView.setColumnWidth(mDisplayMetrics.widthPixels/3);
        //mGridView.setAdapter(mGridViewAdapter);
        //mGridView.setOnItemClickListener(this);
        //mGridView.setOnCreateContextMenuListener(this);
        //mGridView.setOnLongClickListener(this);
        
        mFolderFragment = this;
        return mRootView;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i("hehangjun", "FolderFragment onAttach:");
        if(activity instanceof MusicBrowserActivity){
            MusicBrowserActivity mba = (MusicBrowserActivity) activity;
            mba.setFolderFragment(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mInflater == null){
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        initPopupWindow();
    }
    
    private void initPopupWindow(){
        mAddViewDialogView = mInflater.inflate(R.layout.folder_add_layout, null);
        mDialogTitle = (TextView) mAddViewDialogView.findViewById(R.id.dialog_title);
        mFolderAddLayout = mAddViewDialogView.findViewById(R.id.folder_add_layout);
        mAddViewDialogConfirm = (TextView) mAddViewDialogView.findViewById(R.id.confirm_button);
        mAddViewDialogCancel = (TextView) mAddViewDialogView.findViewById(R.id.cancel_button);
        mFolderIcon  = (ImageView) mAddViewDialogView.findViewById(R.id.folder_icon);
        mAddViewDialogEdit = (EditText) mAddViewDialogView.findViewById(R.id.folder_edit);
        mIconSelectLayout = (LinearLayout) mAddViewDialogView.findViewById(R.id.icon_select_layout);
        mIconGallery = (Gallery) mAddViewDialogView.findViewById(R.id.icon_gallery);
        mIconGallery.setAdapter(new ImageAdapter());
        mIconGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

            @SuppressLint("NewApi")
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ImageView imageview = (ImageView)arg1;
                       
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.pic_in); //实现动画效果
                arg1.setLayoutParams(new Gallery.LayoutParams(160, 120));       
                arg1.startAnimation(animation);
                                       
                for(int i=0; i<arg0.getChildCount();i++){
                        ImageView local_imageview = (ImageView)arg0.getChildAt(i);
                        if(local_imageview!=imageview){
                                local_imageview.setLayoutParams(new Gallery.LayoutParams(128, 96));
                        }
                }
                    mPos = arg2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                
            }
        });
        mIconGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mPos = arg2;
                mFolderIcon.setImageResource(mPics[mPos]);
                mIconSelectLayout.setVisibility(View.GONE);
            }
        });
        
        mAddViewDialogConfirm.setOnClickListener(this);
        mAddViewDialogCancel.setOnClickListener(this);
        mFolderIcon.setOnClickListener(this);
        
        mPopupWindowAdd = new PopupWindow(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mPopupWindowAdd.setBackgroundDrawable(mColorDrawable);
        mPopupWindowAdd.setFocusable(true);
        mPopupWindowAdd.setAnimationStyle(R.style.popdialogshow);
        mPopupWindowAdd.update();

        
        mPopupWindowAdd.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        mAddViewDialogView.setFocusableInTouchMode(true);
        mAddViewDialogView.setOnKeyListener(new android.view.View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowAdd.isShowing())) {
                    mPopupWindowAdd.dismiss();
                    return true;
                }
                return false;
            }
        });
    }
    
    @SuppressLint("NewApi")
    public MusicInventory appendAddItem(Context context){
        MusicInventory musicInventory = new MusicInventory();
        musicInventory.setType(0);
        musicInventory.setIconBg(R.drawable.append_add_def);
        musicInventory.setIconRes(R.drawable.append_add_item);
        musicInventory.setInventoryName(context.getString(R.string.add));
        return musicInventory;
    }

    @SuppressLint("NewApi")
    private void initData(Context context) {
        if(context == null){
            Log.i("hehangjun", "initData Context is null:");
            return;
        }
        mList = ((MusicApplication)context.getApplicationContext()).getInventoryData(true);
        Log.i("hehangjun", "1 folder mList:"+mList.size());
        mList.add(0,appendAddItem(context));
        Log.i("hehangjun", "2 folder mList:"+mList.size());
    }
    
    public void Refresh(FolderFragment folderFragment){
        Log.i("hehangjun", "FolderFragment Refresh:"+ (folderFragment == mFolderFragment));
        initData(mActivity);
        if (mGridView != null) {
            if (mGridViewAdapter == null) {
                mGridViewAdapter = new GridViewAdapter(getActivity(), mList);
                mGridView.setColumnWidth(mDisplayMetrics.widthPixels / 3);
                mGridView.setAdapter(mGridViewAdapter);
                mGridView.setOnItemClickListener(this);
                //yuan tong qin add start 
//                mGridView.setOnCreateContextMenuListener(this);
                mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						
						MusicInventory	musicInventory = (MusicInventory) mGridView.getItemAtPosition(arg2);
			            if(musicInventory != null && musicInventory.type==0){
			            	return false;
			            }
			            
			            long id = -1;
			            int iconRes = -1;
			            if(musicInventory!=null){
			            	headerString = musicInventory.getInventoryName();
			                id = musicInventory.getId();
			                iconRes = musicInventory.getIconRes();
			            }
			            setTarcatId(id);
			            setPicPosition(iconRes);
			            AlertDialog.Builder builder=new AlertDialog.Builder(mActivity);
			            final AlertDialog create = builder.create();
			            View dialog=View.inflate(mActivity, R.layout.gridview_item_dialog, null);
			            TextView biaotidialog=(TextView)dialog.findViewById(R.id.biaoti);
			            TextView deletedialog=(TextView)dialog.findViewById(R.id.delete_dialog);
			            TextView renamedialog=(TextView)dialog.findViewById(R.id.rename_dialog);
			            biaotidialog.setText(headerString);
			            create.setView(dialog);
			            renamedialog.setOnClickListener(new OnClickListener() {
			            	
			            	@Override
			            	public void onClick(View v) {
			            		renameitem(headerString);
			            		create.cancel();
			            	}
			            });
			            deletedialog.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								deleteitem();
								create.cancel();
							}
						});
			            create.show();
			            
						return true;
					}
				});
                //yuan tong qin add end 
            }
            
            mGridView.invalidate();
            mGridViewAdapter.setList(mList);
            mGridView.setAdapter(mGridViewAdapter);
            mGridViewAdapter.notifyDataSetChanged();
            
            Log.i("hehangjun", "mGridView notifyDataSetChanged mGridView:" + mGridView.getCount() + "   mGridView.getAdapter():" + (mGridView.getAdapter() == null)+"   mRootView:"+(mRootView.getVisibility()==View.VISIBLE)+"     mGridView:"+(mGridView.getVisibility()==View.VISIBLE));
        }else{
            Log.i("hehangjun", "mGridView is null");
        }
        super.onViewStateRestored(mBundle);
    }
    
    private class ImageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mPics.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView img = new ImageView(getActivity());
            img.setBackgroundResource(mPics[position]);
            img.setAdjustViewBounds(true);//保持宽高比，不设置则gallery显示一张图片
            img.setMaxHeight(160);
            img.setMaxWidth(240);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //img.setLayoutParams(new Gallery.LayoutParams(android.widget.Gallery.LayoutParams.WRAP_CONTENT,Gallery.LayoutParams.WRAP_CONTENT));
            img.setLayoutParams(new Gallery.LayoutParams(128,96));
            return img;
        }
        
    }

    public class GridViewAdapter extends BaseAdapter {
        public class GridHolder {
            long id;
            int type;
            TextView name;
            ImageView imageView;
        }

        private Context context;
        private GridHolder mGridHolder;
        private List<MusicInventory> mList;
        private LayoutInflater mInflater;

        public GridViewAdapter(Context c,List<MusicInventory> list) {
            super();
            this.context = c;
            this.mList = list;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setList(List<MusicInventory> list) {
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int index) {
            return mList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            if (convertView == null) {
                mGridHolder = new GridHolder();
                convertView = mInflater.inflate(R.layout.folder_grid_item, null);
                mGridHolder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
                mGridHolder.name = (TextView) convertView.findViewById(R.id.itemName);
                convertView.setTag(mGridHolder);
            } else {
                mGridHolder = (GridHolder) convertView.getTag();
            }
            MusicInventory info = mList.get(index);
            if(info.iconBg != -1){
                mGridHolder.imageView.setBackgroundResource(info.iconBg);
                mGridHolder.imageView.setImageResource(info.getIconRes());
            }else{
                mGridHolder.imageView.setImageResource(info.getIconRes());
            }
            int count = 0;
            Cursor cursor = null;
            try {
                cursor = mActivity.getContentResolver().query(
                        MusicProvider.BaseMusicColumns.SONG_URI, null,
                        "songId != 0 and "+"songInventoryId = '" + info.getId() + "'", null, null);
                if (cursor != null) {
                    count = cursor.getCount();
                }
            } catch (Exception e) {
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            
            
            if(info.getType() == 0){
                mGridHolder.name.setText(info.getInventoryName());
            }else{
                SpannableString msp = new SpannableString(info.getInventoryName()+"("+count+"首)");
                msp.setSpan(new AbsoluteSizeSpan(13, true), info.getInventoryName().length(), msp.length(), 33);//add ytq gaibianzitidashao
                msp.setSpan(mForegroundColorSpan, info.getInventoryName().length(), msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mGridHolder.name.setText(msp);
            }
            
            mGridHolder.id = info.getId();
            mGridHolder.type = info.getType();
            
            Log.i("hehangjun", "GridViewAdapter info.getInventoryName():"+info.getInventoryName());
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
    //	long a=System.currentTimeMillis();
        com.cappu.music.FolderFragment.GridViewAdapter.GridHolder gridHolder = (com.cappu.music.FolderFragment.GridViewAdapter.GridHolder) view.getTag();
        Log.i("hehangjun", "onItemClick gridHolder:"+gridHolder.type);
        if(gridHolder.type == 0){
            if(mPopupWindowAdd == null){
                initPopupWindow();
            }
            mPopupWindowAdd.setContentView(mAddViewDialogView);
            mPopupWindowAdd.showAtLocation(getActivity().findViewById(android.R.id.tabhost), Gravity.BOTTOM, 0, 0);
            mDialogTitle.setText(R.string.new_inventory);
            mFolderIcon.setImageResource(mPics[0]);
            if(mIconSelectLayout.getVisibility() == View.VISIBLE){
                mIconSelectLayout.setVisibility(View.GONE);
            }
            mPos = 0;
            RENAMING_ID = -1;
        }else if(gridHolder.type == 1){
            startActivity(new Intent(getActivity(), InventoryFragmentAcitvity.class).putExtra("InventoryId", gridHolder.id).putExtra("songboolean", true));
        }
     //  long b= System.currentTimeMillis();
     //  Log.i("test", b-a+"=点击=时间间隙");
        //yuan tong qin add 
//      songgengxin.selectzongsong();
//      songgengxin.selectfenzongsong();
//      songgengxin.update();
//      songgengxin.deletefensong();
      //Toast.makeText(mActivity, "这里是歌曲更新有没有",0).show();
        
    }

    @Override
    public void onClick(View view) {
    	//yuan tong qin add 
        if(view == mAddViewDialogCancel){
            if(mPopupWindowAdd.isShowing()){
                mPopupWindowAdd.dismiss();
                mAddViewDialogEdit.setText("");
                mFolderIcon.setImageResource(R.drawable.append_add_def);
            }
        }else if(view == mAddViewDialogConfirm){
            String context = mAddViewDialogEdit.getText().toString();
            if(TextUtils.isEmpty(context)){
                KookToast.toast(getActivity(), getActivity().getString(R.string.tip_inventory_name), "s", Color.WHITE);
                return;
            }
            
            if(RENAMING_ID == -1){
                getActivity().getContentResolver().query(MusicProvider.BaseMusicColumns.INVENTORY_URI,null,null, null, null);
                ContentValues values = new ContentValues();
                values.put("songInventoryName", mAddViewDialogEdit.getText().toString());
                values.put("icon", mPics[mPos]);
                values.put("type", 1);
                getActivity().getContentResolver().insert(MusicProvider.BaseMusicColumns.INVENTORY_URI, values);
            }else{
                ContentValues values = new ContentValues();
                values.put("songInventoryName", mAddViewDialogEdit.getText().toString());
                values.put("icon", mPics[mPos]);
                values.put("type", 1);
                getActivity().getContentResolver().update(MusicProvider.BaseMusicUri.getInventoryUri(RENAMING_ID), values, null, null);
                RENAMING_ID = -1;
            }
            mAddViewDialogEdit.setText("");
            initData(getActivity());
            mGridViewAdapter.setList(mList);
            mGridViewAdapter.notifyDataSetChanged();
            if(mPopupWindowAdd.isShowing()){
                mPopupWindowAdd.dismiss();
            }
        }else if(view == mFolderIcon){
            if(mIconSelectLayout.getVisibility() == View.GONE){
                mIconSelectLayout.setVisibility(View.VISIBLE);
                mFolderAddLayout.postInvalidate();
                //mIconGallery.setSelection(mPos);
            }else{
                mIconSelectLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onLongClick(View arg0) {
        return false;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfoIn) {
//        GridView mGridView = null;
//        if(view instanceof GridView){
//            mGridView = (GridView)view;
//        }
//        MusicInventory musicInventory = null;
//        if(mGridView!=null){
//            AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
//            musicInventory = (MusicInventory) mGridView.getItemAtPosition(mi.position);
//            if(musicInventory != null && musicInventory.type==0){
//                return;
//            }
//        }
//        String headerStr = "";
//        long id = -1;
//        int iconRes = -1;
//        if(musicInventory!=null){
//            headerStr = musicInventory.getInventoryName();
//            menu.setHeaderTitle(headerStr);
//            id = musicInventory.getId();
//            iconRes = musicInventory.getIconRes();
//        }
//        
//        menu.add(0, INVENTORY_RENAMING, 0, R.string.rename_playlist_menu).setIntent(new Intent().putExtra("renaming", headerStr).putExtra("id", id).putExtra("icon", iconRes));
//        menu.add(0, INVENTORY_DELETE, 0, R.string.delete_playlist_menu).setIntent(new Intent().putExtra("renaming", headerStr).putExtra("id", id).putExtra("icon", iconRes));
//    
    }
    
    
    
    public void setTarcatId(long id){
        this.TARCAT_ID = id;
    }
    
    public long getTarcatId(){
        return this.TARCAT_ID;
    }
    
    public void setPicPosition(int res){
        for (int i = 0; i < mPics.length; i++) {
            if(mPics[i] == res){
                mPos = i;
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        setTarcatId(item.getIntent().getLongExtra("id",-1));
//        setPicPosition(item.getIntent().getIntExtra("icon", -1));
//        Log.i("hehangjun", "onContextItemSelected:"+item.getIntent().getLongExtra("id",-1)+"   "+item.getIntent().getStringExtra("renaming"));
//        final String headerStr = item.getIntent().getStringExtra("renaming");
//        
//        switch (item.getItemId()) {
//        case INVENTORY_RENAMING:
//            if(mPopupWindowAdd == null){
//                initPopupWindow();
//            }
//            
//            mPopupWindowAdd.setContentView(mAddViewDialogView);
//            mPopupWindowAdd.showAtLocation(getActivity().findViewById(android.R.id.tabhost), Gravity.BOTTOM, 0, 0);
//            mAddViewDialogEdit.setText(item.getIntent().getStringExtra("renaming"));
//            mDialogTitle.setText(R.string.inventory_edit);
//            mIconSelectLayout.setVisibility(View.GONE);
//            mFolderIcon.setImageResource(mPics[mPos]);
//            RENAMING_ID = getTarcatId();
//            Log.i("hehangjun", "修改文件加 RENAMING_ID："+RENAMING_ID);
//            return true;
//        case INVENTORY_DELETE:
//        	
//            if(mBuilder == null){
//                mBuilder = new KookDialog.Builder(getActivity());
//            }
//            
//            mBuilder.setNegativeButton(getString(R.string.cancel_button), new android.content.DialogInterface.OnClickListener() {
//                
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    mDeleteDialog.dismiss();
//                }
//            });
//            
//            mBuilder.setPositiveButton(R.string.confirm_button, new android.content.DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface arg0, int arg1) {
//                    if(TARCAT_ID != -1){
//                        getActivity().getContentResolver().delete(MusicProvider.BaseMusicColumns.INVENTORY_URI, "_id = "+TARCAT_ID, null);
//                    }
//                    
//                    initData(getActivity());
//                    mGridViewAdapter.setList(mList);
//                    mGridViewAdapter.notifyDataSetChanged();
//                }
//            });
//            
//            if(mDeleteDialog == null){
//                mDeleteDialog = mBuilder.create();
//            }
//            mDeleteDialog.setTitle(R.string.delete_inventory);
//            mDeleteDialog.show();
//            
//            return true;
//        }
        return super.onContextItemSelected(item);
    }
    //yuan tong qin add start 
    //重命名
    public void renameitem(String renameing){
    	
    	 if(mPopupWindowAdd == null){
             initPopupWindow();
         }
         
         mPopupWindowAdd.setContentView(mAddViewDialogView);
         mPopupWindowAdd.showAtLocation(getActivity().findViewById(android.R.id.tabhost), Gravity.BOTTOM, 0, 0);
         mAddViewDialogEdit.setText(renameing);
         mDialogTitle.setText(R.string.inventory_edit);
         mIconSelectLayout.setVisibility(View.GONE);
         mFolderIcon.setImageResource(mPics[mPos]);
         RENAMING_ID = getTarcatId();
    }
    //删除
    public void deleteitem(){
    	   if(mBuilder == null){
               mBuilder = new KookDialog.Builder(getActivity());
           }
           
           mBuilder.setNegativeButton(getString(R.string.cancel_button), new android.content.DialogInterface.OnClickListener() {
               
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   mDeleteDialog.dismiss();
               }
           });
           
           mBuilder.setPositiveButton(R.string.confirm_button, new android.content.DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface arg0, int arg1) {
                   if(TARCAT_ID != -1){
                       getActivity().getContentResolver().delete(MusicProvider.BaseMusicColumns.INVENTORY_URI, "_id = "+TARCAT_ID, null);
                   }
                   
                   initData(getActivity());
                   mGridViewAdapter.setList(mList);
                   mGridViewAdapter.notifyDataSetChanged();
               }
           });
           
           if(mDeleteDialog == null){
               mDeleteDialog = mBuilder.create();
           }
           mDeleteDialog.setTitle(R.string.delete_inventory);
           mDeleteDialog.show();
    }
    
    //yuan tong qin add end 
    
}
