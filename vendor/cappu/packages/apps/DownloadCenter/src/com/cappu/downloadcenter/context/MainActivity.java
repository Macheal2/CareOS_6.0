package com.cappu.downloadcenter.context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.ex.DbException;

import android.util.DisplayMetrics;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.downloadcenter.common.entity.DownloadCenterTitle;
import com.joy.network.RecommendShortcutInfo;

import com.joy.network.handler.RecommendDataHandler;
import com.joy.network.impl.Service;

import com.cappu.downloadcenter.update.UpdateCenter;
import com.cappu.downloadcenter.update.UpdateManager;

import com.cappu.downloadcenter.R;
import com.cappu.downloadcenter.adapter.AppExpandableListAdapter;
import com.cappu.downloadcenter.adapter.AppExpandableListAdapter.TreeNode;
import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.cache.BitmapCaches;
import com.cappu.downloadcenter.common.entity.AppInfo;
import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.common.view.AlertDialog;
import com.cappu.downloadcenter.download.DownloadInfo;
import com.cappu.downloadcenter.download.DownloadManager;
import com.cappu.downloadcenter.download.DownloadState;
import com.cappu.downloadcenter.install.SecretlyInstallReceiver;

import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener {
    private ExpandableListView mExpandableListView;
    private TopBar mTopBar;
    private Animation mRotateAnim;
    ImageView emptyImage;
    
    public static Context mContext;
    private FolderApplication mApp;
    private SharedPreferences sp;
    private AppExpandableListAdapter mDownloadExListAdapter;// mExpandableListView的adapter
    private ArrayList<String> folderPackageNameList = new ArrayList<String>();
    private ArrayList<HashMap<String, Object>> folderList = new ArrayList<HashMap<String, Object>>();
    private boolean mTitleLoading; // 是否正在加载列表目录
    private boolean mListLoading;// 是否显示loagding true:启动下载进程 false:未下载或者已经获取json列表
    private List<DownloadCenterTitle> mDownLoadTitleArray = new ArrayList<DownloadCenterTitle>();// 下载列表保存数组
    private List<TreeNode> mTreeNode;

    private final static String FOLDER_APP_LIST = "folder_app_list";
    private final long MIN_UPDATE_TIME = Constants.TIMEOUT;// 2000L;
    private static long mUpdateTitleTime = 0;
    private final long MIN_UPDATE_TITLE_TIME = 10000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onlinefolder_layout);

        mContext = this;
        x.view().inject(this);
        mApp = (FolderApplication) getApplication();
//        ViewUtils.inject(this);
        UpdateCenter.onResume(this);
        initRegisterReceiver();//注册获取网络的广播
        initView();
        
        if (!mApp.getConnectedType().equals(mApp.NETWORK_TYPE_NULL)) {
            asyncTaskLoadingListTitle();// 异步获取列表头
        } else {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.uu_download_no_networking_text), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initRegisterReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 获取网络切换广播
        registerReceiver(myReceiver, filter);
        
        IntentFilter mInstallSystemfilter = new IntentFilter();
        mInstallSystemfilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mInstallSystemfilter.addDataScheme("package");
        registerReceiver(installReceiver, mInstallSystemfilter);
    }
    
    private void initView() {
        DownloadManager.getInstance();
        mTreeNode = new ArrayList<TreeNode>();
        mDownLoadTitleArray = readTitleSPInfo();

        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setText(R.string.app_name);
        mTopBar.setOnTopBarListener(new TopBarListener());
        mTopBar.setRightVisibilty(View.INVISIBLE);

        mExpandableListView = (ExpandableListView) this.findViewById(R.id.expandableListView);
        mExpandableListView.setGroupIndicator(null);// 设置默认的箭头
        LinearLayout myEmpty=(LinearLayout)findViewById(R.id.list_empty);
        emptyImage = (ImageView)findViewById(R.id.empty_image);
        TextView emptyText = (TextView)findViewById(R.id.empty_text);
        TextView emptyTextSec = (TextView)findViewById(R.id.empty_text_sec);
        Button buttonFir = (Button)findViewById(R.id.button_first);
        Button buttonSec = (Button)findViewById(R.id.button_second);
        buttonFir.setOnClickListener(this);
        buttonSec.setOnClickListener(this);
        mDownloadExListAdapter = new AppExpandableListAdapter(this);
        
        mExpandableListView.setEmptyView(myEmpty);
        if (!mApp.getConnectedType().equals(mApp.NETWORK_TYPE_NULL)) {//有网络的情况
            emptyImage.setImageDrawable(getResources().getDrawable(R.drawable.im_line_download));
            emptyText.setText(getResources().getText(R.string.uu_download_loading_on_progress));
            emptyTextSec.setVisibility(View.GONE);
            buttonFir.setVisibility(View.GONE);
            buttonSec.setVisibility(View.GONE);
            //运行动画
            mRotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_tip);
            LinearInterpolator lin = new LinearInterpolator();  
            mRotateAnim.setInterpolator(lin);
            if (mRotateAnim != null) {
                emptyImage.startAnimation(mRotateAnim);
            }

            
            if (mDownLoadTitleArray != null && mDownLoadTitleArray.size() != 0) {
                for (int i = 0; i < mDownLoadTitleArray.size(); i++) {
                    TreeNode node = new TreeNode();
                    // ArrayList<RecommendShortcutInfo> list =
                    // readShortcutInfo(mDownLoadTitleArray.get(i).id);
                    node.parent = mDownLoadTitleArray.get(i);
                    ArrayList<RecommendShortcutInfo> list = readChildSPInfo(node.parent.id);
                    List<RecommendButtonInfo> buttonGroup = new ArrayList<RecommendButtonInfo>();
                    for (int j = 0; j < list.size(); j++) {
                        RecommendButtonInfo button = new RecommendButtonInfo(mContext, list.get(j));
                        buttonGroup.add(button);
                    }
                    node.childs = buttonGroup;
                    mTreeNode.add(node);
                }
                if (mExpandableListView.getAdapter() == null) {
                    mExpandableListView.setAdapter(mDownloadExListAdapter);
                }
                mDownloadExListAdapter.UpdateTreeNode(mTreeNode);
                
                for(int i = 0; i < mDownloadExListAdapter.getGroupCount(); i++){
                    if (mDownloadExListAdapter.getChildGroup(i) != null && mDownloadExListAdapter.getChildGroup(i).size() > 0)
                        mExpandableListView.expandGroup(i);
                }
            }
        } else {//没有网络的情况
            if (mDownLoadTitleArray != null && mDownLoadTitleArray.size() != 0) {
                for (int i = 0; i < mDownLoadTitleArray.size(); i++) {
                    TreeNode node = new TreeNode();
                    // ArrayList<RecommendShortcutInfo> list =
                    // readShortcutInfo(mDownLoadTitleArray.get(i).id);
                    node.parent = mDownLoadTitleArray.get(i);
                    ArrayList<RecommendShortcutInfo> list = readChildSPInfo(node.parent.id);
                    List<RecommendButtonInfo> buttonGroup = new ArrayList<RecommendButtonInfo>();
                    for (int j = 0; j < list.size(); j++) {
                        RecommendButtonInfo button = new RecommendButtonInfo(mContext, list.get(j));
                        buttonGroup.add(button);
                    }
                    node.childs = buttonGroup;
                    mTreeNode.add(node);
                }
                if (mExpandableListView.getAdapter() == null) {
                    mExpandableListView.setAdapter(mDownloadExListAdapter);
                }
                mDownloadExListAdapter.UpdateTreeNode(mTreeNode);
                
                for(int i = 0; i < mDownloadExListAdapter.getGroupCount(); i++){
                    if (mDownloadExListAdapter.getChildGroup(i) != null && mDownloadExListAdapter.getChildGroup(i).size() > 0)
                        mExpandableListView.expandGroup(i);
                }
            }
            
            emptyImage.setImageDrawable(getResources().getDrawable(R.drawable.im_line_network_error));
            emptyTextSec.setVisibility(View.VISIBLE);
            emptyText.setText(getResources().getText(R.string.uu_download_no_networking_text));
            emptyTextSec.setText(getResources().getText(R.string.uu_download_no_networking_sec_text));
            buttonFir.setVisibility(View.VISIBLE);
            buttonFir.setText(getResources().getText(R.string.update));
            buttonFir.setTag("update");
            buttonSec.setVisibility(View.VISIBLE);
            buttonSec.setText(getResources().getText(R.string.setting));
            buttonSec.setTag("setting");
            
        }
    }
    
    /**
     * 异步加载列表标题
     * 
     * @author hmq
     */
    private void asyncTaskLoadingListTitle() {
        final long StartTime = System.currentTimeMillis();
        if ((StartTime - mUpdateTitleTime) < MIN_UPDATE_TITLE_TIME){
//            Toast.makeText(getApplicationContext(), getResources().getText(R.string.uu_download_repeat_busying), Toast.LENGTH_SHORT).show();
//            return;
        }
        mUpdateTitleTime = StartTime;
        TitleAsyncTask task = new TitleAsyncTask(StartTime);
        task.execute(0);
    }

    class TitleAsyncTask extends AsyncTask<Integer, Integer, List<DownloadCenterTitle>> {// 继承AsyncTask
        List<DownloadCenterTitle> list = null;
        final long StartTime;

        public TitleAsyncTask(long StartTime) {
            this.StartTime = StartTime;
        }

        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected List<DownloadCenterTitle> doInBackground(Integer... params) {
            list = Service.getInstance().getRecommendTitles();
//            long updateTime = System.currentTimeMillis() - StartTime;
//            if (updateTime < MIN_UPDATE_TIME) {
//                try {
//                    Thread.sleep(MIN_UPDATE_TIME - updateTime);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            return list;
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
            // pb.setProgress(value);
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(List<DownloadCenterTitle> list) {
            if (list != null) {
                if (mRotateAnim != null && emptyImage != null && mRotateAnim.hasStarted()) {
                    emptyImage.clearAnimation();
                }
                if (mExpandableListView.getAdapter() == null){
                    mExpandableListView.setAdapter(mDownloadExListAdapter);
                }
                boolean hasUpdate = hasUpdateTitle(list);
                if (hasUpdate) {
                    loadResouseTitle(list);
                    saveTitleSPInfo(list);
                }
                threadLodingListChild(hasUpdate);// 加载标题下的子项
            }
        }
    }

    /**
     * 加载列表子项
     * 
     * @author hmq
     */
    private void threadLodingListChild(boolean titleupdate) {
        threadLodingListChild(-1, titleupdate);
    }

    private void threadLodingListChild(int item, boolean titleupdate) {
        // 标题没有内容
        if (mDownLoadTitleArray.size() == 0)
            return;
        // 异步加载标题中
        // if (mTitleLoading == true)
        // return;
        if (item == -1) {
            for (int i = 0; i < mDownLoadTitleArray.size(); i++) {
                ChildAsyncTask task = new ChildAsyncTask(System.currentTimeMillis(), i, mDownLoadTitleArray.get(i).id, titleupdate);
                task.execute(0);
            }
        }
    }

    /**
     * 异步加载子项
     * 
     * @author magcomm
     * 
     */
    class ChildAsyncTask extends AsyncTask<Integer, Integer, List<RecommendShortcutInfo>> {// 继承AsyncTask
        List<RecommendShortcutInfo> list = null;
        final long StartTime;
        final int listItem;
        boolean titleUpdate;
        int index;

        public ChildAsyncTask(long time, int index, int id, boolean titleupdate) {
            this.listItem = id;
            this.index = index;
            this.StartTime = System.currentTimeMillis();
            this.titleUpdate = titleupdate;
        }

        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected List<RecommendShortcutInfo> doInBackground(Integer... params) {
            list = Service.getInstance().getRecommendChild(listItem);
            long updateTime = System.currentTimeMillis() - StartTime;
            // if (updateTime < MIN_UPDATE_TIME) {
            // try {
            // Thread.sleep(MIN_UPDATE_TIME - updateTime);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // }
            return list;
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
            // pb.setProgress(value);
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(List<RecommendShortcutInfo> list) {
            if (list != null && (hasUpdateChild(listItem, list) || titleUpdate)) {
                loadResouseChild(listItem, list);
                saveChildInfo(listItem, list);
                mExpandableListView.expandGroup(index);
            }
            mApp.onlyOneRefresh = true;//已经加载完成不要在加载了
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    
    /**
     * 是否需要更新标题列表数据和界面
     * @param list
     * @return
     */
    private boolean hasUpdateTitle(List<DownloadCenterTitle> list) {
        boolean hasUpdate = false;// 是否需要更新
        if (list == null) {
            return false;
        }
        ArrayList<DownloadCenterTitle> downList = new ArrayList<DownloadCenterTitle>(list);// 实例化list,生成下载数据arraylist
        ArrayList<DownloadCenterTitle> localList = new ArrayList<DownloadCenterTitle>(mDownLoadTitleArray);// 实例化list,生成本地保存arraylist
        for (int i = localList.size() - 1; i >= 0; i--) {
            boolean haslike = false;
            for (int j = downList.size() - 1; j >= 0; j--) {
                if ((localList.get(i).id == downList.get(j).id) && (localList.get(i).name.equals(downList.get(j).name))) {
                    haslike = true;
                    downList.remove(j);// 每次有相同的内容就删除arrayList表里的内容，如果剩下有多余的说明列表被更新了
                    break;
                }
            }
            if (!haslike) {// 如果走到下面说明没找到相同的内容
                hasUpdate = true;
                break;
            }
        }
        if (downList.size() != 0 || hasUpdate) {// 对比后有增加的菜单 或者 菜单本地保存的和下载的匹配不上
            return true;
        }
        return false;
    }
    
    /**
     * 更新标题列表上的数据
     * @param list
     */
    private void loadResouseTitle(List<DownloadCenterTitle> list) {
        if (list == null) {
            return;
        }

        mTreeNode = new ArrayList<TreeNode>();
        mDownLoadTitleArray = list;

        for (int i = 0; i < list.size(); i++) {
            TreeNode node = new TreeNode();
            node.parent = list.get(i);
            mTreeNode.add(node);
        }

        mDownloadExListAdapter.UpdateTreeNode(mTreeNode);
    }
    
    /**
     * 是否需要更新子类列表数据和界面
     * @param list
     * @return
     */
    private boolean hasUpdateChild(int listItem, List<RecommendShortcutInfo> list) {
        boolean hasUpdate = false;// 是否需要更新
        if (list == null) {
            return false;
        }
        ArrayList<RecommendShortcutInfo> downList = new ArrayList<RecommendShortcutInfo>(list);// 实例化list,生成下载数据arraylist
        ArrayList<RecommendShortcutInfo> localList = (ArrayList<RecommendShortcutInfo>) readChildSPInfo(listItem).clone();// 复制内容,生成本地保存arraylist
        if (localList == null) {
            return true;
        }

        for (int i = localList.size() - 1; i >= 0; i--) {
            boolean haslike = false;
            for (int j = downList.size() - 1; j >= 0; j--) {
                if ((localList.get(i).id == downList.get(j).id) && (localList.get(i).icon.equals(downList.get(j).icon)) && (localList.get(i).size == downList.get(j).size)) {
                    haslike = true;
                    downList.remove(j);// 每次有相同的内容就删除arrayList表里的内容，如果剩下有多余的说明列表被更新了
                    break;
                }
            }
            if (!haslike) {// 如果走到下面说明没找到相同的内容
                hasUpdate = true;
                break;
            }
        }
        if (downList.size() != 0 || hasUpdate) {// 对比后有增加的菜单 或者 菜单本地保存的和下载的匹配不上
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param listItem
     * @param list
     */
    private void loadResouseChild(int listItem, List<RecommendShortcutInfo> list) {
        if (list == null) {
            return;
        }

        for (int i = 0; i < mDownLoadTitleArray.size(); i++) {
            if (mDownLoadTitleArray.get(i).id == listItem) {// APP 内容和title 相吻合
                List<RecommendButtonInfo> buttonGroup = new ArrayList<RecommendButtonInfo>();
                for (int j = 0; j < list.size(); j++) {
                    RecommendButtonInfo button = new RecommendButtonInfo(mContext, list.get(j));
                    buttonGroup.add(button);
                }
                mDownloadExListAdapter.updateChildGroup(mDownLoadTitleArray.get(i).id, buttonGroup);
                break;
            }
        }
    }

    /**
     * 通知更新文件夹应用列表
     * 
     * @author maqj
     */
    private void notifyDataChange() {
        // adapter.notifyDataSetChanged();
        // View addItem = gridView.getChildAt(gridView.getChildCount() - 1);
        // RelativeLayout bg = (RelativeLayout)
        // addItem.findViewById(R.id.uu_folder_icon_bg_panel);
        // bg.setBackgroundColor(color.transparent);
    }

    
    @Override
    public void onClick(View v) {
        String tag = (String)v.getTag();
        if (tag.equals("update")){
            initView();
            if (!mApp.getConnectedType().equals(mApp.NETWORK_TYPE_NULL)) {
                asyncTaskLoadingListTitle();// 异步获取列表头
            } else {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.uu_download_no_networking_text), Toast.LENGTH_SHORT).show();
            }
        }else if (tag.equals("setting")){
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings");
            startActivity(intent);
        }
    }

    private Dialog remindDialog = null;

    /**
     * recommend item regist brocast list
     * 
     * @author maqj
     */
    // public ArrayList<BroadcastReceiver> receiverList = new
    // ArrayList<BroadcastReceiver>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
        unregisterReceiver(installReceiver);
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        int isDownLoading = 0;
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            for (int i = 0; i < DownloadManager.getInstance().getDownloadListCount(); i++) {
                DownloadInfo downloadinfo = DownloadManager.getInstance().getDownloadInfoIndex(i);
                if (downloadinfo.getState() == DownloadState.STARTED) {
                    isDownLoading++;
                }
                if (Util.DEBUG) Log.e(Util.TAG, "退出 第" + i + "条" + DownloadManager.getInstance().getDownloadInfoIndex(i).toStater());
                if (downloadinfo.getState() == DownloadState.FINISHED) {
                    try {
                        DownloadManager.getInstance().removeDownload(downloadinfo);
                    } catch (DbException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        if (Util.ERROR_DEBUG) Log.e(Util.TAG, "onKeyDown error=" + e.getMessage());
                    }
                }
            }
        }

        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            if (isDownLoading != 0) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
                alertBuilder.setMessage(R.string.have_download_exit).setPositiveButton(R.string.config, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager.getInstance().stopAllDownload();
                        finish();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.uu_folder_cancel_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }
    
    
    // private static final int ANIMATION_DURATION = 600;

    /**
     * 接收网络状态
     * 
     * @author maqj
     */
    BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                State wifiState = null;
                State mobileState = null;
                
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

                if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED == mobileState) {
                    mApp.changeNetwork(mApp.NETWORK_TYPE_MOBILE);
//                    Toast.makeText(context, "手机网络连接成功！", Toast.LENGTH_SHORT).show();
                    if(!mApp.onlyOneRefresh){
                        initView();
                        asyncTaskLoadingListTitle();// 异步获取列表头
                    }
                } else if (wifiState != null && mobileState != null && State.CONNECTED == wifiState && State.CONNECTED != mobileState) {
                    mApp.changeNetwork(mApp.NETWORK_TYPE_WIFI);
//                    Toast.makeText(context, "无线网络连接成功！", Toast.LENGTH_SHORT).show();
                    if(!mApp.onlyOneRefresh){
                        initView();
                        asyncTaskLoadingListTitle();// 异步获取列表头
                    }
                } else if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED != mobileState) {
                    mApp.changeNetwork(mApp.NETWORK_TYPE_NULL);
//                    Toast.makeText(context, "手机没有任何网络...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    
    /**
     * 接收应用到删除广播
     * 
     * @author maqj
     */
    BroadcastReceiver installReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                mDownloadExListAdapter.notifyDataSetChanged();
                if(Util.DEBUG)Log.e(Util.TAG, "卸载成功");
            } 
        }
    };

    /**
     * get app info by packageName
     * 
     * @param context
     * @param packageName
     * @return
     * @author maqj
     */
    private AppInfo getAppInfo(String packageName) throws NameNotFoundException {
        AppInfo info = new AppInfo();
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
        String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
        info.setAppName(appName == null ? "" : appName);
        info.setPackageName(packageInfo.packageName);
        info.setIcon(packageInfo.applicationInfo.loadIcon(pm));
        return info;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // add by y.haiyang (start)
    private boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    // add by y.haiyang (end)

    /**
     * 保存title信息到SharedPreferences
     * 
     * @param context
     * @param infoList
     * @author hmq
     */
    private void saveTitleSPInfo(List<DownloadCenterTitle> infoList) {
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        JSONArray arr = new JSONArray();
        for (DownloadCenterTitle info : infoList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", info.id);
                obj.put("name", info.name);
            } catch (JSONException e) {
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ERROR --saveTitleSPInfo --DownloadCenterTitle error=" + e);
            }
            arr.put(obj);
        }
        sp.edit().putString(Util.APP_TITLE_ARRAY, arr.toString()).commit();
    }

    /**
     * 保存list信息到SharedPreferences
     * 
     * @param context
     * @param infoList
     * @author hmq
     */
    public void saveChildInfo(int group, List<RecommendShortcutInfo> infoList) {
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        JSONArray arr = new JSONArray();
        for (RecommendShortcutInfo info : infoList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", info.id);
                obj.put("size", info.size);
                obj.put("icon", info.icon);
                obj.put("name", info.name);
                obj.put("packageName", info.packageName);
                obj.put("url", info.url);
            } catch (JSONException e) {
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.i(Util.TAG, "ERROR --saveChildInfo save error!");
            }
            arr.put(obj);
        }
        sp.edit().putString(Util.APP_LIST_ARRAY + group, arr.toString()).commit();
    }

    /**
     * 读取title信息到SharedPreferences
     * 
     * @param context
     * @param infoList
     * @author hmq
     */
    private ArrayList<DownloadCenterTitle> readTitleSPInfo() {
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String json = sp.getString(Util.APP_TITLE_ARRAY, "");
        ArrayList<DownloadCenterTitle> infoList = new ArrayList<DownloadCenterTitle>();
        if (json != "") {
            try {
                JSONArray arr = new JSONArray(json);
                int lenth = arr.length();
                for (int i = 0; i < lenth; i++) {
                    DownloadCenterTitle info = new DownloadCenterTitle();
                    JSONObject obj = arr.getJSONObject(i);
                    info.id = obj.optInt("id");
                    info.name = obj.optString("name");
                    infoList.add(info);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ERROR --readTitleSPInfo --DownloadCenterTitle error=" + e);
            }
        }
        return infoList;
    }

    /**
     * 读取child信息到SharedPreferences
     * 
     * @param context
     * @param infoList
     * @author hmq
     */
    private ArrayList<RecommendShortcutInfo> readChildSPInfo(int group) {
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String json = sp.getString(Util.APP_LIST_ARRAY+group, "");
        ArrayList<RecommendShortcutInfo> infoList = new ArrayList<RecommendShortcutInfo>();
        if (json != "") {
            try {
                JSONArray arr = new JSONArray(json);
                int lenth = arr.length();
                for (int i = 0; i < lenth; i++) {
                    RecommendShortcutInfo info = new RecommendShortcutInfo();
                    JSONObject obj = arr.getJSONObject(i);
                    info.id = obj.optInt("id");
                    info.size = obj.optInt("size");
                    info.icon = obj.optString("icon");
                    info.name = obj.optString("name");
                    info.packageName = obj.optString("packageName");
                    info.url = obj.optString("url");
                    infoList.add(info);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(Util.ERROR_DEBUG)Log.e(Util.TAG, "ERROR  --readChildSPInfo --DownloadCenterTitle error=" + e);
            }
        }
        return infoList;
    }

    private class TopBarListener implements onTopBarListener {

        @Override
        public void onLeftClick(View v) {
            finish();
        }

        @Override
        public void onRightClick(View v) {
        }

        @Override
        public void onTitleClick(View v) {
        }
    }
}
