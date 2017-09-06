package com.cappu.cleaner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageStats;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.cappu.cleaner.context.CleanSimpleAdapter;
import com.cappu.cleaner.context.fileCleanInfo;
import com.cappu.cleaner.ui.MonitorBall;
import com.cappu.cleaner.widget.TopBar;
import com.cappu.cleaner.FileClean.SearchListener;
import com.cappu.cleaner.FileClean.CleanListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CleanerManager extends Activity implements TopBar.onTopBarListener, OnClickListener, SearchListener, CleanListener {
    private TopBar mTopBar;
    private GridView mAppGrid;
    private MonitorBall mMonitorBall;
    private Button mMonitorButton;
    private FileClean mFileClen;
    ProgressDialog mDialog = null;
    private PopupWindow mPopupWindow;

    ArrayList<HashMap<String, Object>> mLstImageItem = new ArrayList<HashMap<String, Object>>();
    private String[] mItemsId;
    private TypedArray mItemsDraw;
    private String[] mItemsStr;
    private CleanSimpleAdapter mCleanSimpleAdapter;
    private boolean firshEntry = false;

    private static final int CACHE_PERCENT = 40;
    private static final int FILE_PERCENT = 30;
    private static final int WECHAT_PERCENT = 30;
    public static final String ARRAYS_CACHE_ID = "cache";
    public static final String ARRAYS_PIC_ID = "pic";
    public static final String ARRAYS_WECHAT_ID = "wechat";
    public static final String ARRAYS_SOFT_ID = "soft";
    private static int index_cache = 0;
    private static int index_pic = 1;
    private static int index_wechat = 2;
    private static int index_soft = 3;
    private static final int MSG_SCANNING = 0;
    private static final int MSG_CACHE = 1;
    private static final int MSG_FILE = 2;
    private static final int MSG_WECHAT = 3;
    private static final int MSG_CLEAR = 4;

    public static final String ITEM_ID="ItemId";
    public static final String ITEM_IMAGE="ItemImage";
    public static final String ITEM_TEXT="ItemText";
    public static final String ITEM_STATE="ItemState";
    public static final String ITEM_DATA="ItemData";
    private String[] FROM = {ITEM_IMAGE, ITEM_TEXT};
    private int[] TO = {R.id.icon, R.id.text};

    private long mTotalCacheSize;
    private long mTotalFileSize;
    private long mTotalWechatSize;

    private int mCachePercent;
    private int mFilePercent;
    private int mWechatPercent;
    private long oldTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner);
        initView();
        if (mFileClen == null) {
            mFileClen = FileClean.getInstance(this);
        }
        firshEntry = true;
        mCachePercent = CACHE_PERCENT;
        mFilePercent = FILE_PERCENT;
        mWechatPercent = WECHAT_PERCENT;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && firshEntry && mFileClen.getState() == FileClean.NONE){
            File path = mFileClen.initSDcard();
            if (path != null) {
                mFileClen.setOnSearchListener(this);
                mFileClen.setOnCleanListener(this);
            }
            mFileClen.searchListFiles(path.getAbsolutePath(), FileClean.SEARCH_ALL);
        }
        Log.e("hmq","onWindowFocusChanged "+hasFocus);
        firshEntry = false;
    }

    public void initView() {
        mTotalCacheSize = 0;
        mTotalFileSize = 0;
        mTotalWechatSize = 0;

        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setText(R.string.app_name);
        mTopBar.setOnTopBarListener(this);
        mAppGrid = (GridView) findViewById(R.id.gview);
        mMonitorBall = (MonitorBall) findViewById(R.id.monitor_ball);
        mMonitorButton = (Button) mMonitorBall.findViewById(R.id.monitor_content);
        mMonitorButton.setOnClickListener(this);
        Util.initWechatIcon(getBaseContext());

        TypedArray cateDefaultArray = getResources().obtainTypedArray(R.array.app_items);//读取二维数组
        mItemsId = getResources().getStringArray(cateDefaultArray.getResourceId(0, -1));//读取id
        mItemsDraw = getResources().obtainTypedArray(cateDefaultArray.getResourceId(1, -1));//读取drawable数组
        mItemsStr = getResources().getStringArray(cateDefaultArray.getResourceId(2, -1));
        cateDefaultArray.recycle();

        for (int i = 0; i < mItemsId.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(ITEM_ID, mItemsId[i]);//图像资源
            map.put(ITEM_IMAGE, mItemsDraw.getResourceId(i, -1));//图像资源
            map.put(ITEM_TEXT, mItemsStr[i]);//标题
            map.put(ITEM_STATE, FileClean.NONE);
            map.put(ITEM_DATA, 0L);
            Log.e("hmq", "i=" + i + "; " + mItemsStr[i]);
            mLstImageItem.add(map);
            switch(mItemsId[i]){
                case ARRAYS_CACHE_ID:
                    index_cache = i;
                    break;
                case ARRAYS_PIC_ID:
                    index_pic = i;
                    break;
                case ARRAYS_WECHAT_ID:
                    index_wechat = i;
                    break;
                case ARRAYS_SOFT_ID:
                    index_soft = i;
                    break;
                default:
                    break;
            }
        }

        mCleanSimpleAdapter = new CleanSimpleAdapter(this, mLstImageItem, R.layout.app_item, FROM, TO);
        //配置适配器
        mAppGrid.setAdapter(mCleanSimpleAdapter);
        mAppGrid.setOnItemClickListener(null);
//        mAppGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.e("hmq", "view=" + view + "; " + mItemsId[position]);
//                Intent intent = new Intent();
//                switch (mItemsId[position]) {
//                    case ARRAYS_CACHE_ID:
//                        intent.setClass(getBaseContext(), cacheCleanActivity.class);// 添加Action属性
//                        startActivity(intent);
//                        break;
//                    case ARRAYS_PIC_ID:
//                        break;
//                    case ARRAYS_WECHAT_ID:
//                        intent.setClass(getBaseContext(), fileWechatCleanActivity.class);// 添加Action属性
//                        startActivity(intent);
//                        break;
//                    case ARRAYS_SOFT_ID:
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFileClen.initFileList();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onRightClick(View v) {
//        if (mMonitorBall.getState() == MonitorBall.NOMAL) {
//            mMonitorBall.setState(MonitorBall.SCANNING);
//        } else if (mMonitorBall.getState() == MonitorBall.SCANNING) {
//            mMonitorBall.setState(MonitorBall.FINISH);
//        } else if (mMonitorBall.getState() == MonitorBall.FINISH) {
//            mMonitorBall.setState(MonitorBall.NOMAL);
//        }
        showPopupWindow(v);
    }

    private void showPopupWindow(View view) {
        if (mPopupWindow == null) {
            View convertView=LayoutInflater.from(this).inflate(R.layout.pop_window, null);
//            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//
//            view = layoutInflater.inflate(R.layout.pop_window, null);
            // 创建一个PopuWidow对象
            mPopupWindow = new PopupWindow(convertView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            // 使其聚集
            mPopupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
            // 设置允许在外点击消失
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
            mPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
            // 设置按钮的点击事件
            Button button = (Button) convertView.findViewById(R.id.about_us);
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CleanerManager.this, AboutUsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    if (mPopupWindow != null) {
                        mPopupWindow.dismiss();
                    }
                }
            });
        }
        // 设置好参数之后再show
        mPopupWindow.showAsDropDown(view);
    }

    @Override
    public void onTitleClick(View v) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.monitor_content:
                Button view = (Button) v;
                view = (Button) v;
                if (view.getVisibility() == View.VISIBLE) {
                    if (getResources().getString(R.string.str_optimization_cancel).equals(view.getText())) {
                        cancelScanning();
                    } else {
                        cleanAll();
                    }
                }
                break;

        }
    }

    private void cancelScanning() {
        mFileClen.cancelScanning();
        mCleanSimpleAdapter.setState(FileClean.FINISH);
        mMonitorBall.setState(MonitorBall.FINISH);
    }

    private void cleanAll() {
        mFileClen.clear(FileClean.CLEAR_ALL);
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCANNING://scanning
                    String result = (String) msg.obj;
                    mMonitorBall.setVelueText(getResources().getString(R.string.str_scanning) + result);
                    mMonitorBall.setState(MonitorBall.SCANNING);
                    break;
                case MSG_CACHE://cache
                    Log.e("hmq","MSG_CACHE size=" + mTotalCacheSize);
                    mCleanSimpleAdapter.setVelue(index_cache, mTotalCacheSize);
                    mMonitorBall.setProgress(mCachePercent + mFilePercent);
//                    mCleanSimpleAdapter.notifyDataSetChanged();
                    break;
                case MSG_FILE://file
                    mCleanSimpleAdapter.setVelue(index_soft, mTotalFileSize);
                    mMonitorBall.setProgress(mCachePercent + mFilePercent);
                    break;
                case MSG_WECHAT://wecat
                    mCleanSimpleAdapter.setVelue(index_wechat, mTotalWechatSize);
                    mMonitorBall.setProgress(mCachePercent + mFilePercent);
                    break;
                case MSG_CLEAR:
                    mMonitorBall.setVelueText(getResources().getString(R.string.str_clearing));
                    mMonitorBall.setState(MonitorBall.SCANNING);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - oldTime < 3000) {
            super.onBackPressed();
            return;
        }
        oldTime = System.currentTimeMillis();
        Toast.makeText(this, getResources().getString(R.string.double_pressed), Toast.LENGTH_SHORT);

    }

    /**
     *
     * @param result 结果
     */
    @Override
    public void onSearching(String result) {
        Message msg = new Message();
        msg.what = MSG_SCANNING;
        msg.obj = result;
        handler.sendMessage(msg);
    }

    @Override
    public void onCatchSearchCache(PackageStats result, float percent) {
        Log.e("hmq","onCatchSearchCache result="+result.packageName + "; " + mTotalCacheSize);
        mCachePercent = (int)(CACHE_PERCENT * percent);
        mTotalCacheSize += result.cacheSize;
        Message msg = new Message();
        msg.what = MSG_CACHE;
        handler.sendMessage(msg);
    }

    @Override
    public void onCatchSearchFile(String result, int type, int name) {
        Message msg = new Message();
        fileCleanInfo info;

        switch (type) {
            case FileClean.RESULT_APK:
                info = Util.getPackageInfo(this, result);
                mTotalFileSize += info.getFilesize();
                msg.what = MSG_FILE;
                break;
            case FileClean.RESULT_AD:
                info = Util.getFileInfo(this, type, name, result);
                mTotalFileSize += info.getFilesize();
                msg.what = MSG_FILE;
                break;
            case FileClean.RESULT_WECHAT:
                info = Util.getFileInfo(this, type, name, result);
                mTotalWechatSize += info.getFilesize();
                msg.what = MSG_WECHAT;
                break;
            default:
                return;
        }
        msg.arg1 = mCachePercent + mFilePercent;
        handler.sendMessage(msg);
        //mCleanSimpleAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onSearchState(int state) {
        switch(state){
            case FileClean.NONE:
                mCleanSimpleAdapter.setState(FileClean.NONE);
                mMonitorBall.setState(MonitorBall.NOMAL);
                break;
            case FileClean.WORKING_CACHE:
                mCleanSimpleAdapter.setState(FileClean.WORKING_CACHE);
                mMonitorBall.setState(MonitorBall.SCANNING);
                break;
            case FileClean.WORKING_FILE:
                mCleanSimpleAdapter.setState(FileClean.WORKING_FILE);
                mMonitorBall.setState(MonitorBall.SCANNING);
                break;
            case FileClean.FINISH:
                mCleanSimpleAdapter.setState(FileClean.FINISH);
                mMonitorBall.setState(MonitorBall.FINISH);
                break;
        }
    }

    @Override
    public void onClearState(int type, long size, long Total_size) {
        if (type == FileClean.CLEAR_CACHE) {
            mTotalCacheSize = 0;
            mCachePercent = CACHE_PERCENT;
            mCleanSimpleAdapter.setVelue(index_cache, mTotalCacheSize);
            mMonitorBall.setProgress(mCachePercent + mFilePercent);
        }else if(type == FileClean.CLEAR_FILE){
            mTotalFileSize = 0;
            mFilePercent = FILE_PERCENT;
            mCleanSimpleAdapter.setVelue(index_soft, mTotalFileSize);
            mMonitorBall.setProgress(mCachePercent + mFilePercent);
        }else if(type == FileClean.CLEAR_WECHAT){
            mTotalWechatSize = 0;
            mWechatPercent = WECHAT_PERCENT;
            mCleanSimpleAdapter.setVelue(index_wechat, mTotalWechatSize);
            mMonitorBall.setProgress(mCachePercent + mFilePercent);
        }else if(type == FileClean.CLEAR_FINISH){
            mCleanSimpleAdapter.setVelue(index_cache, 0);
            mCleanSimpleAdapter.setVelue(index_soft, 0);
            mCleanSimpleAdapter.setVelue(index_wechat, 0);
            mMonitorBall.setProgress(mCachePercent + mFilePercent);
        }

    }
}
