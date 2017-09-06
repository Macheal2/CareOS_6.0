package com.cappu.launcherwin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.AlarmClock;
//import android.provider.Telephony.MmsSms;
import android.provider.CallLog;
//added by yzs
import android.provider.CallLog.Calls;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.LauncherModel.Callbacks;
import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.downloadUI.celllayout.CellLyouatUtil;
import com.cappu.launcherwin.downloadUI.celllayout.DownloadCellLayoutMainActivity;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.kookview.AlbumsKookView;
import com.cappu.launcherwin.kookview.AlbumsRelativeLayout;
import com.cappu.launcherwin.kookview.GPRSKookView;
import com.cappu.launcherwin.kookview.CappuKookView;
import com.cappu.launcherwin.kookview.assembly.AssemblyJeffView;
import com.cappu.launcherwin.kookview.assembly.AssemblyRelativeLayout;
import com.cappu.launcherwin.kookview.MusicKookView;
import com.cappu.launcherwin.kookview.VideoKookView;
import com.cappu.launcherwin.netinfo.NetLookActivity;
// add by y.haiyang for speech more (start)
import com.cappu.launcherwin.speech.LauncherSpeechTools;
import com.cappu.launcherwin.tools.AppComponentNameReplace;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.CareDialog;
import com.cappu.launcherwin.widget.I99ThemeToast;
import com.cappu.launcherwin.widget.Indicator;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.CappuDialogUI;
//added by yzs begin

import android.os.RemoteException;
import com.iflytek.business.speech.SynthesizerListener;
// add by y.haiyang for speech more (end)

//added by yzs for unreadview begin
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.provider.Telephony.MmsSms;
import android.provider.CallLog;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
//added by yzs for unreadview end

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.cappu.widget.CareMenu;

public class Launcher extends BasicActivity implements Callbacks,View.OnClickListener,View.OnLongClickListener /*BubbleView.OnChildViewLongClick*/{
    
    public static final String TAG = "Launcher";
    public static final int REPLACE_REQUESTCODE = 20;
    public static final int CONTACT_REQUESTCODE = 21;
    
    
    public enum LauncherType{onCreate,onStart,onResume,onPause,onStop,onDestroy};
    private LauncherType mLauncherType;
    
    private WorkspaceUpdateReceiver mWorkspaceUpdateReceiver;
    
    private LauncherModel mModel;
    private Workspace mWorkspace;
    private LayoutInflater mInflater;
    private DragLayer mDragLayer;
    
    // add by y.haiyang for speech more (start)
    /**
     * private SpeechTools mSpeechTools = null;
     */
    private LauncherSpeechTools mSpeechTools;
    // modify by y.haiyang for speech more (end)
    
    private InputMethodManager mInputMethodManager;
    
    private Button mUnreadMms;
    private Button mUnreadCall;
    
    private Indicator mIndicator;
    
    private PackageManager mPackageManager;
    
    private FrameLayout phone;
    private FrameLayout mms;
    private FrameLayout camera;
    
    public Handler mHandler = new Handler();	//modified by Yar @20170824
    
    private ConnectivityManager mCM;
    
    private LauncherApplication mLauncherApplication;
    
    private Dialog mSimDialog = null;
    
    private Dialog mProgressDialog = null;
    
    private DragController mDragController;
    
    /**文字*/
    private int mTextSize = -1;
    /**launcher 模式选择  1表示默认模式  2 便是简单模式   3 表示极简模式*/
    private int mModeSelect = 1;
    /**是否循环滑动*/
    private int isSupportCycleSlidingScreen = 0;
    
    private LayoutInflater mLayoutInflater;
    
    private View mEditCellView;
    
    private Gallery mGalleryEdit;
    private ImageAdapter mGallerymImageAdapter;
    
	private int[] mPolePics = { R.drawable.bg_cell_001, R.drawable.bg_cell_002,
			R.drawable.bg_cell_004, R.drawable.bg_cell_005,
			R.drawable.bg_cell_006, R.drawable.bg_cell_007,
			R.drawable.bg_cell_008, R.drawable.bg_cell_009 };
	private String[] mPolePicsName = { "bg_cell_001", "bg_cell_002",
			"bg_cell_004", "bg_cell_005", "bg_cell_006", "bg_cell_007",
			"bg_cell_008", "bg_cell_009" };
	private int mPolePos = mPolePics.length / 2;
    
	private CareDialog.Builder mEditCellBuilder;
	private CareDialog mAlertDialog;
    
	private boolean SpeechStatus = true;
    
    private DisplayMetrics mDisplayMetrics;
    
    /*估算每个块的大小*/
    public static int  ITEM_WIDTH = 0;
    public static int ITEM_HEIGHT = 0;
    
    private int[] mLocation = new int[2];
    
  //add by wangyang 2016.10.19 start 
    private boolean ThemeUpdateTextSize = false;
  //add by wangyang 2016.10.19 ebd
    
    private WallpaperManager mWallpaperManager;//hejianfeng add
    private class ImageAdapter extends BaseAdapter{

        @Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPolePics.length;

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
			ImageView img = new ImageView(Launcher.this);
			img.setBackgroundResource(mPolePics[position]);
            img.setAdjustViewBounds(true);//保持宽高比，不设置则gallery显示一张图片
            img.setMaxHeight(160);
            img.setMaxWidth(240);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setLayoutParams(new Gallery.LayoutParams(128,96));
            return img;
        }        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LauncherLog.v(TAG, "onCreate,jeff mLauncherType.onCreate");
        super.onCreate(savedInstanceState);
      //hejianfeng add start
        if(mProgressDialog == null){
            showHomeLoadingDialog();
        }else{
            mProgressDialog.show();
        }
        //hejianfeng add end
        mLauncherType = mLauncherType.onCreate;
        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        // modify by y.haiyang for speech more (start)
        /**
         * mSpeechTools = new SpeechTools(this);
         */
        mSpeechTools = ThemeRes.getInstance().getSpeechTools();
        // modify by y.haiyang for speech more (end)

        mInflater = getLayoutInflater();
        mPackageManager = getPackageManager();
        
        mLauncherApplication = ((LauncherApplication)getApplication());
        mModel = mLauncherApplication.setLauncher(this);
        setContentView(R.layout.launcher);
        
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        mModeSelect = ThemeManager.getInstance().getCurrentThemeType(this);
        
        mWorkspaceUpdateReceiver = new WorkspaceUpdateReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK); //每分钟变化的action
        mFilter.addAction(Intent.ACTION_TIME_CHANGED); //设置了系统时间的action    
        //hejianfeng add start
        mWorkspaceUpdateReceiver.setLancher(this);
        mFilter.addAction("android.intent.action.WALLPAPER_CHANGED");
        
        mFilter.addAction("com.cappu.weather.action.datarefresh");
        mFilter.addAction("com.cappu.healthcenter.action.datarefresh");// 云健康数据刷新
        mFilter.addAction("com.cappu.healthcenter.action.step_count_refresh");
        //hejianfeng add end
        
        /**下面这三个是为GPRS 添加*/
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
        mFilter.addAction("android.net.conn.CONNECTIVITY_CANCEL");//dengying@20150414
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGEING"); 
        
        registerReceiver(mWorkspaceUpdateReceiver, mFilter);
        mWorkspaceUpdateReceiver.registerContentObserver(this);
        
        //注册爱分享的内容观察者   added by wangyang 2016.8.8
        mWorkspaceUpdateReceiver.registerIShareContentObserver(this);
        
        mModel.setCallbacks(this);
        mModel.setLauncher(this);
        mModel.startLoader(this,true);
        mDragController = new DragController(this);
        init();
        mWorkspace = mDragLayer.getWorkspace();
        mWorkspace.setLauncher(this);
        mWorkspace.setDragController(mDragController);
        ThemeManager.getInstance().setWorkspace(mWorkspace);
        mIndicator = (Indicator) findViewById(R.id.view_indicator);
        mWorkspace.setIndicator(mIndicator);
        
        mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        
        //added by yzs for unreadview begin
        mDragLayer.refreshPhoneState(getUnreceivedCallCount());
        mDragLayer.refrshMmsState(getNewMmsCount() + getNewSmsCount());
        registerMessageObserver();
        //added by yzs for unreadview end
        mDragLayer.setBackgroundInMode();
        
        Bitmap mBitmap = ThemeRes
				.getInstance()
				.getThemeResBitmap(
						"com.cappu.launcherwin/com.cappu.launcherwin.applicationList.activity.GooduseActivity"
								+ "/" + ThemeManager.getInstance().getCurrentThemeType());
        if(mBitmap!=null){
	        ThemeManager.getInstance().mBubbleViewWidth = mBitmap.getWidth();
	        ThemeManager.getInstance().mBubbleViewHeight = mBitmap.getHeight();
        }
        
        mWallpaperManager = WallpaperManager.getInstance(this);//hejianfeng add 
    }
    
    private boolean ServiceIsStart(List<ActivityManager.RunningServiceInfo> mServiceList,String className){    
        
        for(int i = 0; i < mServiceList.size(); i ++){    
            if(className.equals(mServiceList.get(i).service.getClassName())){    
                return true;    
            }    
        }    
        return false;    
    } 
    private void init() {
        
        mLayoutInflater = LayoutInflater.from(this);
        mEditCellBuilder = new CareDialog.Builder(this);
        mEditCellView = mLayoutInflater.inflate(R.layout.edit_cell, null);
        
        mEditCellBuilder.setTitle(getString(R.string.setting_cell_background));
        mEditCellBuilder.setView(mEditCellView);
        mGalleryEdit = (Gallery) mEditCellView.findViewById(R.id.gallery);
        mGallerymImageAdapter = new ImageAdapter();
        mGalleryEdit.setAdapter(mGallerymImageAdapter);
        mGalleryEdit.setSelection(mPolePics.length/2);
        
        mGalleryEdit.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 mPolePos = arg2;
            }
        });
        mGalleryEdit.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                try {
                    ImageView imageview = (ImageView)arg1;
                    Animation animation = AnimationUtils.loadAnimation(Launcher.this, R.anim.pic_in); //实现动画效果
                    arg1.setLayoutParams(new Gallery.LayoutParams(160, 120));
                    arg1.startAnimation(animation);
                    for(int i=0; i<arg0.getChildCount();i++){
                            ImageView local_imageview = (ImageView)arg0.getChildAt(i);
                            if(local_imageview!=imageview){
                                    local_imageview.setLayoutParams(new Gallery.LayoutParams(128, 96));
                            }
                    }
                    mPolePos = arg2;
                } catch (Exception e) {
                    Log.i(TAG, "mGalleryEdit 异常:"+(e.toString()));
                }
                
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        
        mEditCellBuilder.setNegativeButton(getString(R.string.dialog_cancel), new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
            }
        });
        mEditCellBuilder.setPositiveButton(getString(R.string.dialog_confirm), new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mAlertDialog.dismiss();
                //后续还需要设置目标的背景
				ContentValues cv = new ContentValues();
				cv.put(Favorites.BACKGROUND, mPolePicsName[mPolePos]);
                restorationDatabase(Launcher.this, (int)mWorkspace.getTargetId(), cv);
                mModel.updateOneItem(mWorkspace.getTargetId());
            }
        });
        
        mAlertDialog = mEditCellBuilder.create();
        
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        
        if(mDragLayer!=null){
            mDragLayer.setup(this,mDragController);
        }
        initMenuDialog();
    }
    //hejianfeng add start
    private CareMenu mOptionDialog;
    private Button btnCancel;
    private void initMenuDialog(){
    	 mOptionDialog = new CareMenu(this);
         mOptionDialog.addButton(R.string.run);
         mOptionDialog.addButton(R.string.replace);
         mOptionDialog.addButton(R.string.edit_cell);
         mOptionDialog.addButton(R.string.system_setting);
         btnCancel=new Button(this);
         mOptionDialog.addCancelButton(btnCancel);
         mOptionDialog.setOnClickListener(new CareMenu.OnClickListener(){
             @Override
             public void onClick(View v){
                 switch(v.getId()){
                     case R.string.run:
                    	 int Id = (int)mWorkspace.getTargetId();
                         ItemInfo itemInfo = mModel.queryDate(Id);
                         
                         Log.i(TAG, "run :"+(itemInfo == null));
                         if(itemInfo instanceof ItemShortcut){
                             ItemShortcut is = (ItemShortcut) itemInfo;
                             if(is.intent!=null){                   
                                 if(TextUtils.equals(getString(R.string.app_name_radio), is.getTitle())){
                                     
                                     if(!SpeechStatus){
                                         SecurityStartActivity(is.intent);
                                     }else{
                                         
                                         if(mSpeechTools.isAudioActive()){
                                             mSpeechTools.setSpeechListener(new SynthesizerListener.Stub() {
                                            	    @Override
                                            		public void onProgressCallBack(int arg0) throws RemoteException {
                                            			Log.d(TAG, "onProgressCallBack");
                                            		}
                                            		
                                            		@Override
                                            		public void onPlayCompletedCallBack(int arg0) throws RemoteException {
//                                            			mService.stopSpeak();
                                            			Log.d(TAG, "onPlayCompletedCallBack");
                                            			 Intent intent = new Intent(Intent.ACTION_MAIN);  
                                                         intent.addCategory(Intent.CATEGORY_LAUNCHER);              
                                                         ComponentName cn = new ComponentName("com.mediatek.fmradio", "com.mediatek.fmradio.FmRadioActivity");              
                                                         intent.setComponent(cn);
                                                         SecurityStartActivity(intent);
                                            		}
                                            		
                                            		@Override
                                            		public void onPlayBeginCallBack() throws RemoteException {
                                            			Log.d(TAG, "onPlayBeginCallBack");
                                            		}
                                            		
                                            		@Override
                                            		public void onInterruptedCallback() throws RemoteException {
                                            			Log.d(TAG, "onInterruptedCallback");
                                            		}
                                            		
                                            		@Override
                                            		public void onInit(int arg0) throws RemoteException {
                                            			Log.d(TAG, "onInit");
                                            		}



                                            		@Override
                                            		public void onSpeakPaused() throws RemoteException {
                                            			// TODO Auto-generated method stub
                                            			Log.d(TAG, "onSpeakPaused");
                                            		}

                                            		@Override
                                            		public void onSpeakResumed() throws RemoteException {
                                            			// TODO Auto-generated method stub
                                            			Log.d(TAG, "onSpeakResumed");
                                            		}

                                            		@Override
                                            		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
                                            				throws RemoteException {
                                            			Log.d(TAG, "onEvent");
                                            			
                                            		}
                                             });
                                         } else{ 
                                             SecurityStartActivity(is.intent);
                                         }
                                     }
                                     mSpeechTools.startSpeech(is.getTitle());
                                 }else{
                                     mSpeechTools.startSpeech(is.getTitle(),SpeechStatus);
                                     SecurityStartActivity(is.intent);
                                 }
                                 mSpeechTools.setSpeechListener(null); 
                             }else{
                                 Log.i(TAG, "intent:"+(is.intent == null)+"   v.getTag(): ");
                             }
                         }else if(itemInfo instanceof ItemContacts){
                             ItemContacts ic = (ItemContacts) itemInfo;
                             if(ic.intent!=null){
                                 mSpeechTools.startSpeech(ic.getTitle(),SpeechStatus);
                                 SecurityStartActivity(ic.intent);
                             }else{
                                 Log.i(TAG, "intent:"+(ic.intent == null)+"   v.getTag(): ");
                             }
                         }else if(itemInfo instanceof ItemWidget){
                             Log.i(TAG, "itemInfo 点击");
                             BubbleView bv = findBubbleView(itemInfo);
                             if(bv != null){
                                 bv.bubbleViewClick();
                             }
                         }
                         break;
                     case R.string.replace:
                         Intent intent = new Intent(Launcher.this, AllApps.class);
                         intent.putExtra("id",mWorkspace.getTargetId());
                         startActivityForResult(intent, REPLACE_REQUESTCODE);
                         break;
                     case R.string.edit_cell:
                    	 mAlertDialog.show();
                         break;
                     case R.string.system_setting:
                    	 try {
                             startActivity(new Intent(Launcher.this, LauncherSettingActivity.class));
                         } catch (Exception e) {
                             Log.i(TAG, "startActivity(new Intent(this, LauncherSettingActivity.class)) Exception:"+e.toString());
                         }
                         break;
                 }
                 if(v==btnCancel){
                	 if(btnCancel.getText().equals(Launcher.this.getResources().getString(R.string.app_default_title))){
                		 int Id = (int)mWorkspace.getTargetId();
                         restoration(Id);
                	 }else{
                		 mOptionDialog.dismiss(); 
                	 }
                 }
             }
         });
    }
    //hejianfeng add end
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
    	mOptionDialog.setTitle(R.string.popu_title);
    	displayOnlySetting();
    	mOptionDialog.show();
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.i(TAG, "onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }
    
    /**
     * 默认显示
     */
    private void defaultDisplay(){
		mOptionDialog.getItemById(R.string.run).setVisibility(View.VISIBLE);
        mOptionDialog.getItemById(R.string.replace).setVisibility(View.VISIBLE);
        if(ThemeManager.getInstance().isSwitchIconBg()){
        	mOptionDialog.getItemById(R.string.edit_cell).setVisibility(View.VISIBLE);
		}else{
			mOptionDialog.getItemById(R.string.edit_cell).setVisibility(View.GONE);
		}
        mOptionDialog.getItemById(R.string.system_setting).setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setText(R.string.cancel);
		
    }
    /**
     * itemContacts隐藏运行和替换
     */
    private void itemContactsHide(){
		mOptionDialog.getItemById(R.string.run).setVisibility(View.GONE);
        mOptionDialog.getItemById(R.string.replace).setVisibility(View.GONE);
    }
    /**
     * 只显示设置
     */
    private void displayOnlySetting(){
        mOptionDialog.getItemById(R.string.run).setVisibility(View.GONE);
        mOptionDialog.getItemById(R.string.replace).setVisibility(View.GONE);
        mOptionDialog.getItemById(R.string.edit_cell).setVisibility(View.GONE);
        mOptionDialog.getItemById(R.string.system_setting).setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.GONE);
    }
    /**
     * 恢复默认显示
     */
    private void displayRestoration(){
    	btnCancel.setText(R.string.app_default_title);
    }
    /**
     * 是否是默认的应用和磁块
     * @param itemInfo
     * @param tagName
     * @return true:不是默认的应用和磁块
     */
    private boolean isReplaceIcon(ItemInfo itemInfo,String tagName){
    	LauncherLog.v(TAG, "isReplaceIcon, jeff ic itemInfo="+itemInfo);
    	if(ThemeManager.getInstance().getBackupList().size()==0){
    		mModel.loadBackupData();
    	}
    	ContentValues values = ThemeManager.getInstance().getBackupList().get(itemInfo.screen + "/" + itemInfo.cellX + "/"
				+ itemInfo.cellY + "/"
				+ ThemeManager.getInstance().getCurrentThemeType());
    	LauncherLog.v(TAG, "isReplaceIcon, jeff mBackupList="+ThemeManager.getInstance().getBackupList().size());
		if(values==null){
			LauncherLog.v(TAG, "isReplaceIcon, jeff values==null");
			//hejianfeng add start
			if(tagName.equals(LauncherProvider.TAG_FAVORITE)){
				ItemShortcut is= (ItemShortcut) itemInfo;
				if(is.intent.getComponent()
						.getClassName().equals("com.cappu.launcherwin.AllApps")){
					return false;
				}
			}
			//hejianfeng add end
			return true;
		}
		String packageclassName=values.getAsString("packageclassName");
		String background=values.getAsString(LauncherSettings.Favorites.BACKGROUND);
		String backup=packageclassName+"/"+background;
		LauncherLog.v(TAG, "isReplaceIcon, jeff  backup="+backup);
    	if(tagName.equals(LauncherProvider.TAG_CONTACTS)){
    		ItemContacts ic = (ItemContacts) itemInfo;
    		LauncherLog.v(TAG, "isReplaceIcon, jeff ic intent="+ic.intent);
    		String contactName=values.getAsString(LauncherSettings.Favorites.CONTACTNAME);
    		LauncherLog.v(TAG, "isReplaceIcon, jeff contactName="+contactName);
    		if(contactName!=null&& !contactName.isEmpty()){
    			backup=backup+"/"+values.getAsString(LauncherSettings.Favorites.CELL_DEF_IMAGE)
        				+"/"+contactName;
    		}else{
    			backup=backup+"/"+values.getAsString(LauncherSettings.Favorites.CELL_DEF_IMAGE)
        				+"/"+ic.getAliasTitle(values.getAsString(LauncherSettings.Favorites.ALIAS_TITLE));
    		}
    		LauncherLog.v(TAG, "isReplaceIcon, jeff  backup="+backup +",Title="+ic.getTitle());
    		if(backup.equals(ic.intent.getComponent().getPackageName()+"/"+ic.intent.getComponent()
					.getClassName()+"/"+ic
					.getBackgroundName()+"/"+ic.cellDefImage+"/"+ic.getTitle())){
    			return false;
    		}
    		
    	}else if(tagName.equals(LauncherProvider.TAG_APP)){
    		ItemWidget iw = (ItemWidget) itemInfo;
    		LauncherLog.v(TAG, "isReplaceIcon, jeff iw intent="+iw.intent);
    		if(backup.equals(iw.intent.getDataString()+"/"+iw
					.getBackgroundName())){
    			return false;
    		}
    	}else if(tagName.equals(LauncherProvider.TAG_FAVORITE)){
    		ItemShortcut is= (ItemShortcut) itemInfo;
    		LauncherLog.v(TAG, "isReplaceIcon, jeff is intent="+is.intent);
    		//hejianfeng add start
    		if(is.intent.getComponent().getClassName().equals("com.cappu.launcherwin.AllApps")){
    			return false;
    		}
    		//hejianfeng add end
    		if(backup.equals(is.intent.getComponent().getPackageName()+"/"+is.intent.getComponent()
					.getClassName()+"/"+is
					.getBackgroundName())){
    			return false;
    		}
    	}
    	return true;
    }
    //hejianfeng add end
    @Override
	public boolean onLongClick(View v) {
		if (v instanceof BubbleView) {
			if (mOptionDialog != null) {
				BubbleView bubbleView = (BubbleView) v;
				ItemInfo itemInfo = (ItemInfo) v.getTag();

				if (itemInfo instanceof ItemShortcut
						|| itemInfo instanceof ItemWidget) {
					if (Settings.Global
							.getInt(getContentResolver(),
									"workspace_lock",
									getResources().getInteger(
											R.integer.workspace_lock)) == 1) {
						return true;
					}
				}
				defaultDisplay();// hejianfeng add start
				LauncherLog.v(TAG, "onLongClick, jeff title="+((BubbleView) v).mTextView.getText());
				mOptionDialog.setTitle((String)((BubbleView) v).mTextView.getText());
				mOptionDialog.show();
				if (itemInfo.id < 1) {
					return true;
				}
				mWorkspace.setTargetId(itemInfo.id);
				if (itemInfo instanceof ItemShortcut) {
					if (isReplaceIcon(itemInfo, LauncherProvider.TAG_FAVORITE)) {
						displayRestoration();
					}
				} else if (itemInfo instanceof ItemContacts) {
					itemContactsHide();
					if (isReplaceIcon(itemInfo, LauncherProvider.TAG_CONTACTS)) {
						displayRestoration();
					}
				} else if (itemInfo instanceof ItemWidget) {
					if (isReplaceIcon(itemInfo, LauncherProvider.TAG_APP)) {
						displayRestoration();
					}
				}
			}
		}
		return true;
	}

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick-----------");
        SpeechStatus = getLauncherStatus();
        if(v instanceof BubbleView){
            ItemInfo itemInfo = (ItemInfo) v.getTag();
            mWorkspace.setTargetId(itemInfo.id);
            mBubbleView=(BubbleView) v;
			if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS 
					&& itemInfo.itemType!=LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
				mBubbleView.mRelativeLayoutInner
						.setBackgroundResource(R.drawable.cell_nine_grids_middle_pressed_click);
				mBubbleList.add(mBubbleView);
			}
            if(itemInfo instanceof ItemShortcut){
                ItemShortcut is = (ItemShortcut) itemInfo;
                if(is.intent!=null){
                    //added by yzs begin
                    if(is.intent.getComponent().getPackageName().equals("com.mediatek.fmradio")
                        && "com.mediatek.fmradio.FmRadioActivity".equals(is.intent.getComponent().getClassName())){

                        if(!SpeechStatus){
                            Intent intent = new Intent(Intent.ACTION_MAIN);  
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);              
                            ComponentName cn = new ComponentName("com.mediatek.fmradio", "com.mediatek.fmradio.FmRadioActivity");              
                            intent.setComponent(cn); 
                            SecurityStartActivity(intent);
                        }else{
                            if(mSpeechTools.isAudioActive()){
                                mSpeechTools.setSpeechListener(new SynthesizerListener.Stub() {
                            	    @Override
                            		public void onProgressCallBack(int arg0) throws RemoteException {
                            			Log.d(TAG, "onProgressCallBack");
                            		}
                            		
                            		@Override
                            		public void onPlayCompletedCallBack(int arg0) throws RemoteException {
//                            			mService.stopSpeak();
                            			Log.d(TAG, "onPlayCompletedCallBack");
                            			 Intent intent = new Intent(Intent.ACTION_MAIN);  
                                         intent.addCategory(Intent.CATEGORY_LAUNCHER);              
                                         ComponentName cn = new ComponentName("com.mediatek.fmradio", "com.mediatek.fmradio.FmRadioActivity");              
                                         intent.setComponent(cn);
                                         SecurityStartActivity(intent);
                            		}
                            		
                            		@Override
                            		public void onPlayBeginCallBack() throws RemoteException {
                            			Log.d(TAG, "onPlayBeginCallBack");
                            		}
                            		
                            		@Override
                            		public void onInterruptedCallback() throws RemoteException {
                            			Log.d(TAG, "onInterruptedCallback");
                            		}
                            		
                            		@Override
                            		public void onInit(int arg0) throws RemoteException {
                            			Log.d(TAG, "onInit");
                            		}



                            		@Override
                            		public void onSpeakPaused() throws RemoteException {
                            			// TODO Auto-generated method stub
                            			Log.d(TAG, "onSpeakPaused");
                            		}

                            		@Override
                            		public void onSpeakResumed() throws RemoteException {
                            			// TODO Auto-generated method stub
                            			Log.d(TAG, "onSpeakResumed");
                            		}

                            		@Override
                            		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
                            				throws RemoteException {
                            			Log.d(TAG, "onEvent");
                            			
                            		}
                                });
                            }else{ 
                                Intent intent = new Intent(Intent.ACTION_MAIN);  
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);              
                                ComponentName cn = new ComponentName("com.mediatek.fmradio", "com.mediatek.fmradio.FmRadioActivity");              
                                intent.setComponent(cn); 
                                SecurityStartActivity(intent);
                            }
                        }                       
                    }//hejianfeng add start
                    else if(is.intent.getComponent().getPackageName().equals("com.cappu.launcherwin")
                            && "com.cappu.launcherwin.AllApps".equals(is.intent.getComponent().getClassName())){
                    	Intent intent = new Intent(Launcher.this, AllApps.class);
                        intent.putExtra("id",mWorkspace.getTargetId());
                        startActivityForResult(intent, REPLACE_REQUESTCODE);
                    }
                    //hejianfeng add end
                    else{   
                        if("com.cappu.launcherwin".equals(is.intent.getComponent().getPackageName())){
                            is.intent.removeCategory(Intent.CATEGORY_LAUNCHER);
                            is.intent.setAction("");
                            is.intent.setFlags(0);
                        }
                        if("com.cappu.launcherwin".equals(is.intent.getComponent().getPackageName()) && "com.cappu.launcherwin.contacts.ContactListMultiChoiceActivity".equals(is.intent.getComponent().getClassName())){
                            is.intent.removeCategory(Intent.CATEGORY_LAUNCHER);
                            is.intent.setAction("android.kook.action.LOOK");
                            is.intent.setFlags(0);
                        }                     
                        SecurityStartActivity(is.intent);
                    }

                    mSpeechTools.startSpeech((String) mBubbleView.mTextView.getText().toString(),SpeechStatus);

                    mSpeechTools.setSpeechListener(null); 
                    //modified by yzs end for FMRedio Speech
                }else{
                    Log.i(TAG, "intent:"+(is.intent == null)+"   v.getTag(): ");
                }
            }else if(itemInfo instanceof ItemContacts){
				ItemContacts ic = (ItemContacts) itemInfo;
				mSpeechTools.startSpeech((String) mBubbleView.mTextView
						.getText().toString(), SpeechStatus);
				Intent ci = new Intent();
				ci.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ComponentName cn = new ComponentName("com.android.contacts",
						"com.cappu.contacts.I99QuickContactActivity");
				ci.setComponent(cn);
				startActivityForResult(
						ci.putExtra("id", ic.id)
								.putExtra("phoneNumber", ic.getPhoneNumber())
								.putExtra("contactName", ic.getContactName()),
						CONTACT_REQUESTCODE);
				//hejianfeng add start
				Settings.Global.putInt(getContentResolver(), "desktop_contacts", 1);
				//hejianfeng add end
				launcherSendBroadcastReceiver(true, null);
            }
            
        }else if(v == phone || v == mUnreadCall){
			try {
				Intent intent = new Intent();
				ComponentName cn = new ComponentName("com.android.dialer",
						"com.android.dialer.calllog.CallLogActivity");
				intent.setComponent(cn);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_NO_ANIMATION);

				startActivity(intent);
				mSpeechTools.startSpeech(getString(R.string.launcher_phone),
						SpeechStatus);
			} catch (Exception e) {
				Log.i(TAG, "onClick Exception:" + e.toString());
				Toast.makeText(this, getString(R.string.activity_not_found),
						Toast.LENGTH_LONG).show();
			}
            
        } else if (v == mms || v == mUnreadMms) {
            if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){
                try {
                    if(APKInstallTools.checkApkInstall(this, "com.android.mms", "com.android.mms.ui.BootActivity")){
                        Intent intent = new Intent();
                        ComponentName cn = new ComponentName("com.android.mms", "com.android.mms.ui.BootActivity");
                        intent.setComponent(cn);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mSpeechTools.startSpeech(getString(R.string.launcher_mms),SpeechStatus);
                        startActivity(intent);
                    }else{
                        Intent it = new Intent(Intent.ACTION_MAIN);
                        it.setType("vnd.android-dir/mms-sms");
                        mSpeechTools.startSpeech(getString(R.string.launcher_mms),SpeechStatus);
                        startActivity(it);
                    }
                } catch (Exception e) {
                    Log.i(TAG,"onClick Exception:"+e.toString());
                    Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
                }
            }            
        }else if(v == camera){
            try {
                Intent cameraIntent = new Intent();
                cameraIntent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(cameraIntent);
            } catch (Exception e) {
                Log.i(TAG, "cameraIntent Exception:"+e.toString());
            }
            //}
            mSpeechTools.startSpeech(getString(R.string.app_name_camera),SpeechStatus);
        } else if (v.getId() == R.id.alarm_layout) {
            try {
                if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){
                    Intent intent = new Intent();
                    //ComponentName cn = new ComponentName("com.smartisanos.clock", "com.smartisanos.clock.activity.ClockActivity");
                    ComponentName cn = new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock");
                    intent.setComponent(cn);
                    intent.putExtra("deskclock.select.tab", 0);//dengjianzhang@20150906
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//added by yzs
                    mSpeechTools.startSpeech(getString(R.string.launcher_ararm_clock),SpeechStatus);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                    mSpeechTools.startSpeech(getString(R.string.launcher_ararm_clock),SpeechStatus);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
            }
            
        } else if (v.getId() == R.id.time_widget) {
			Log.i(TAG, "v.getId() == R.id.time_widget");

			Intent intent = new Intent();
			ComponentName cn = new ComponentName("com.android.deskclock",
					"com.android.deskclock.DeskClock");
			intent.setComponent(cn);
			intent.putExtra("deskclock.select.tab", 1);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mSpeechTools.startSpeech(getString(R.string.launcher_time_setting),
					SpeechStatus);
			startActivity(intent);

        } else if (v.getId() == R.id.weather_notify || v.getId() == R.id.weather_layout_r) {
            try {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.cappu.launcherwin", "com.cappu.launcherwin.weather.WeatherActivity");
                intent.setComponent(cn);
                mSpeechTools.startSpeech(getString(R.string.launcher_weather),SpeechStatus);
                SecurityStartActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
            }
            
        }else if(v.getId() == R.id.news){
            mSpeechTools.startSpeech(getString(R.string.news),SpeechStatus);
            Intent news = new Intent(this, NetLookActivity.class);
            news.putExtra("type", "news");
            news.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(news);
            
        }else if(v.getId() == R.id.health){
            mSpeechTools.startSpeech(getString(R.string.health),SpeechStatus);
            Intent health = new Intent(this, NetLookActivity.class);
            health.putExtra("type", "health");
            health.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            SecurityStartActivity(health);
            
        }
    }
    
    private void restorationShowTip(int Id){
        restoration(Id);
        I99ThemeToast.toast(this, getString(R.string.restoration_app_tip), "l", Color.parseColor("#FFFFFF"));
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS&& mBubbleView!=null){
        	mBubbleView.mRelativeLayoutInner.setBackgroundResource(R.drawable.shape_transparent);
        }
    }
    //hejianfeng add start
    private void resetAddItem(ContentValues cv){
    	String packageName = "com.cappu.launcherwin";
		String className = "com.cappu.launcherwin.AllApps";
		ActivityInfo info = null;
		Intent intent = new Intent();
		ComponentName cn = null;
		try {
			cn = new ComponentName(packageName, className);
			info = mPackageManager.getActivityInfo(cn, 0);
		} catch (Exception nnfe) {
			Log.i(TAG,
					"919 PackageManager.NameNotFoundException:"
							+ nnfe.toString());
		}

		intent.setComponent(cn);
		intent.setAction(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		if (info != null) {
			cv.put(Favorites.INTENT, intent.toUri(0));
			cv.put(Favorites.TITLE, info.loadLabel(mPackageManager)
					.toString());
		} else {
			int it = 0;
			try {
				it = Integer.parseInt(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT+"");
			} catch (Exception e) {
				it = 0;
			}
			if (it == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
				cv.put(Favorites.INTENT, intent.toUri(0));
				cv.put(Favorites.TITLE, "");
			} else {
				cv.put(Favorites.INTENT, packageName + "/" + className);
				cv.put(Favorites.TITLE, "");
			}
		}

		cv.put(Favorites.BACKGROUND, "cell_twelve_grids_middle");
		cv.put(Favorites.PHONENUMBER, "");
		cv.put(Favorites.CONTACTNAME, "");
		cv.put(Favorites.ALIAS_TITLE, "add");
		cv.put(Favorites.CELL_DEF_IMAGE, "");
		cv.put(Favorites.PIC_URI, "");
		cv.put(Favorites.ALIAS_TITLE_BACKGROUND, "");
		cv.put(Favorites.ITEM_TYPE, LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT+"");
    }
    //hejianfeng add end
    /**恢复桌面一个磁块*/
    private void restoration(int Id) {
        ItemInfo itemInfo = mModel.queryDate(Id);
        if(ThemeManager.getInstance().getBackupList().size()==0){
    		mModel.loadBackupData();
    	}
        ContentValues contentValues = ThemeManager.getInstance().getBackupList().get(itemInfo.screen + "/" + itemInfo.cellX + "/"
				+ itemInfo.cellY + "/"
				+ ThemeManager.getInstance().getCurrentThemeType());
        //hejianfeng modif start
		ContentValues cv = new ContentValues();
		if (contentValues == null) {
			Log.i(TAG, "onclick restoration contentValues is null");
			resetAddItem(cv);
		} else {
			int itemType = contentValues.getAsInteger("itemType");
			Log.i(TAG, "restoration,jeff itemType="+itemType);
			String packageclassName = contentValues
					.getAsString("packageclassName");
			String background = contentValues.getAsString("background");
			String aliasTitle = contentValues
					.getAsString(LauncherSettings.Favorites.ALIAS_TITLE);
			String cellDefImage = contentValues
					.getAsString(LauncherSettings.Favorites.CELL_DEF_IMAGE);
			String aliasTitleBackground = contentValues
					.getAsString(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
			String[] split = packageclassName.split("/");
			String packageName = split[0];
			String className = split[1];
			ActivityInfo info = null;
			Intent intent = new Intent();
			ComponentName cn = null;
			try {
				cn = new ComponentName(packageName, className);
				info = mPackageManager.getActivityInfo(cn, 0);
			} catch (Exception nnfe) {
				Log.i(TAG,
						"919 PackageManager.NameNotFoundException:"
								+ nnfe.toString());
				// START: added by Yar @20170829
				android.util.Log.i("Yar", "nnfe = " + nnfe.toString());
				if (itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
					resetAddItem(cv);
					restorationDatabase(this, Id, cv);
					mModel.updateOneItem(Id);
					return;
				}
				// END: added by Yar @20170829
			}

			intent.setComponent(cn);
			intent.setAction(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			if (info != null) {
				cv.put(Favorites.INTENT, intent.toUri(0));
				cv.put(Favorites.TITLE, info.loadLabel(mPackageManager)
						.toString());
			} else {
				if (itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
					cv.put(Favorites.INTENT, intent.toUri(0));
					cv.put(Favorites.TITLE, aliasTitle);
				} else {
					cv.put(Favorites.INTENT, packageName + "/" + className);
					cv.put(Favorites.TITLE, aliasTitle);
				}
			}

			cv.put(Favorites.BACKGROUND, background);
			cv.put(Favorites.PHONENUMBER, contentValues
					.getAsString(LauncherSettings.Favorites.PHONENUMBER));
			cv.put(Favorites.CONTACTNAME, contentValues
					.getAsString(LauncherSettings.Favorites.CONTACTNAME));
			cv.put(Favorites.ALIAS_TITLE, aliasTitle);
			cv.put(Favorites.CELL_DEF_IMAGE, cellDefImage);
			cv.put(Favorites.PIC_URI, "");
			cv.put(Favorites.ALIAS_TITLE_BACKGROUND, aliasTitleBackground);
			cv.put(Favorites.ITEM_TYPE, itemType);
			cv.put("displayMode", -1);//added by Yar @20170825
		}
        restorationDatabase(this, Id, cv);
        mModel.updateOneItem(Id);
    }
    
    public String getAliasTitle(String aliasTitle) {
        int thumbRes = getResources().getIdentifier(aliasTitle, "string", "com.cappu.launcherwin");
        String aliastitle = getResources().getString(thumbRes);
        return aliastitle;
    }
    
	public void SecurityStartActivity(final Intent intent) {
		if (intent.getComponent() != null
				&& !APKInstallTools
						.checkApkInstall(this, intent.getComponent()
								.getPackageName(), intent.getComponent()
								.getClassName())) {
				restorationShowTip((int) mWorkspace.getTargetId());

		} else {
			try {
				if (intent.getComponent().getPackageName()
						.equals("com.cappu.launcherwin")) {
					intent.removeCategory(Intent.CATEGORY_LAUNCHER);
					intent.setAction(null);
					intent.setFlags(0);
					Log.i(TAG,
							"SecurityStartActivity  intent:"
									+ intent.toString());
				}
				startActivity(intent);
			} catch (Exception e) {
				LauncherLog
						.v(TAG, "jeff Exception intent:" + intent.toString());
				try {
					Intent intentPackage = mPackageManager.getLaunchIntentForPackage(intent.getComponent()
									.getPackageName());
					startActivity(intentPackage);
				} catch (Exception ex) {
					restorationShowTip((int) mWorkspace.getTargetId());
				}
			}
		}
	}
    
    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        SecurityStartActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLauncherType = mLauncherType.onDestroy;
        //modified by yzs for Unread Mms and call
        unregisterMessageObserver();
        //end
//        if(mSpeechTools!=null){
//            mSpeechTools.onDestroy();
//        }
        
        try {
            mWorkspaceUpdateReceiver.unRregisterContentObserver(this);
            mWorkspaceUpdateReceiver.unRregisterIShareContentObserver(this);//added by wangyang 2016.8.8
            unregisterReceiver(mWorkspaceUpdateReceiver);
            unregisterReceiver(mModel);
        } catch (Exception e) {
            Log.i(TAG, "Launcher onDestroy Exception:"+e.toString());
        }
        mBubbleList.clear();
        mBubbleList=null;
        
        Log.e(TAG, "onDestroy ");
    }
    //hejianfeng add start
    public void changeWallpaper(){
    	if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
    		LauncherLog.v(TAG, "changeWallpaper,jeffhejianfeng");
    		 if(mProgressDialog == null){
                 showHomeLoadingDialog();
             }else{
                 mProgressDialog.show();
             }
    		mWorkspace.removeAllWorkspaceScreens();
    		Drawable mDrawable = ThemeManager.getInstance().getCurrentWallPaper();
			ThemeManager.getInstance().getImagePiece()
					.initBitmapPiece(mDrawable);
			mModel.startLoader(this, true);
			mSystemBarTintManager.setTintDrawable(new BitmapDrawable(
					ThemeManager.getInstance().getImagePiece().statusBarBmp));
			mDragLayer.themeChange();
			mDragLayer.requestLayout();
    	}
    }
    private BubbleView mBubbleView=null;
    private ArrayList<BubbleView> mBubbleList=new ArrayList<BubbleView>();
    private boolean isfinishBindingItems=false;
    //hejianfeng add end
    @Override
    protected void onResume() {
        super.onResume();
        mLauncherType = mLauncherType.onResume;
        isSupportCycleSlidingScreen = Settings.Global.getInt(getContentResolver(), "workspace_tuoch",getResources().getInteger(R.integer.workspace_tuoch));        
        SpeechStatus = getLauncherStatus();        
        //hejianfeng add start
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	for(BubbleView mBubbleView:mBubbleList){
        		mBubbleView.mRelativeLayoutInner.setBackgroundResource(R.drawable.shape_transparent);
        	}
        	mBubbleList.clear();
        }
        //hejianfeng add end
        launcherSendBroadcastReceiver(false,mLauncherType.onResume);     

        //modify by even
        //start
        demo_show = (int) getIntent().getIntExtra("mobile_demo",0); 
        if(1 == demo_show)
        {  
            keepScreenOn(this,true);
            Mobile_Demo_CycleWorkSpace();
        }else if(2 == demo_show)
        {
            keepScreenOn(this,false);
            if(null != handler)
            {
                handler.removeCallbacks(runnable);// 关闭定时器处理
            }
        }
        LauncherLog.v(TAG, "onResume,jeff isfinishBindingItems="+isfinishBindingItems);
        //hejianfeng add start
        if(isfinishBindingItems){
        	mWorkspace.updateIndicatorPosition();
        	isfinishBindingItems=false;
        }
        //hejianfeng add end
        Settings.Global.putInt(getContentResolver(), "desktop_contacts", 0);//hejianfeng add 
        //end   
    }    
    
    //modify by even
    /**
     * 保持屏幕唤醒状态（即背景灯不熄灭）
     *
     * @param on
     * 是否唤醒
     */
    private WakeLock mWakeLock;
    public void keepScreenOn(Context context, boolean on) {
        if (on) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "==KeepScreenOn==");
            mWakeLock.acquire();
        } else {
            if (mWakeLock != null) {
                mWakeLock.release();
                mWakeLock = null;
            }
        }
    }    
    //end //modify by even
    public int getModeSelect(){
        return mModeSelect;
    }
    
    public boolean isSupportCycleSlidingScreen(){
        return isSupportCycleSlidingScreen == 1?true:false;
    }
    
    public DisplayMetrics getDisplayMetrics(){
        if(mDisplayMetrics == null){
            mDisplayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        }
        return mDisplayMetrics;
    }
    
    public LauncherSpeechTools getSpeechTools(){
        return mSpeechTools;
    }
    
    public boolean getSpeechStatus(){
        return SpeechStatus;
    }
    
    public boolean getLauncherStatus(){
        return Settings.Global.getInt(getContentResolver(), "launcher_speech_status", getResources().getInteger(R.integer.launcher_speech_status)) == 1?true:false;
    }
    
    @Override
    public void onBackPressed() {
        
        if(Settings.Global.getInt(getContentResolver(), "back_tuoch",getResources().getInteger(R.integer.back_tuoch)) == 1){
            int finalPage = mWorkspace.getCurScreen();
            Scroller scroller = mWorkspace.getScroller();
            if(finalPage == mWorkspace.getChildCount()-1){
                if(scroller.isFinished()){
                   long time1 =  System.currentTimeMillis();
                    mWorkspace.snapToPageWithVelocity(0, -3000);
                    long time2 =  System.currentTimeMillis();
                    Log.d(TAG,"最后一页到第一页** System.currentTimeMillis()="               + (time2 - time1 ));
                }
            }else{
                if(scroller.isFinished()){
                    long time1 =  System.currentTimeMillis();
                    mWorkspace.snapToPageWithVelocity(finalPage+1, -3000);
                    long time2 =  System.currentTimeMillis();
                    Log.d(TAG,"下一页** System.currentTimeMillis()="               + (time2 - time1 ));
                }
            }
            mWorkspace.updateIndicatorPosition();
            launcherSendBroadcastReceiver(false,null);
        }
        
    }

    //modify by even
    //start
    int demo_show = 0;
    final Handler handler = new Handler(); 
    Runnable runnable = new Runnable(){ 
            @Override 
            public void run() { 
            // TODO Auto-generated method stub 
            // 在此处添加执行的代码 
            CycleWorkSpace();
            handler.postDelayed(this, 4000);// 50是延时时长 
            } 
    }; 

    public void Mobile_Demo_CycleWorkSpace() {
        Log.i("SpecialCharSequenceMgr","Mobile_Demo_CycleWorkSpace");
		if(null != handler)
		{
			handler.removeCallbacks(runnable);// 关闭定时器处理
		}
        handler.postDelayed(runnable, 1000);// 打开定时器，执行操作 
        
    }

    public void CycleWorkSpace() {
        Log.i("SpecialCharSequenceMgr","CycleWorkSpace");
        int finalPage = mWorkspace.getCurScreen();
        Scroller scroller = mWorkspace.getScroller();

        if(finalPage == mWorkspace.getChildCount()-1){
            if(scroller.isFinished()){
                long time1 =  System.currentTimeMillis();
                mWorkspace.snapToPageWithVelocity(0, -3000);
                long time2 =  System.currentTimeMillis();
                Log.d(TAG,"最后一页到第一页** System.currentTimeMillis()="               + (time2 - time1 ));
            }
        }else{
            if(scroller.isFinished()){
                long time1 =  System.currentTimeMillis();
                mWorkspace.snapToPageWithVelocity(finalPage+1, -3000);
                long time2 =  System.currentTimeMillis();
                Log.d(TAG,"下一页** System.currentTimeMillis()="               + (time2 - time1 ));
            }
        }

        mWorkspace.updateIndicatorPosition();
        launcherSendBroadcastReceiver(false,null);
    }
    //end
    
    @Override
    protected void onStart() {
        super.onStart();
        mLauncherType = mLauncherType.onStart;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须存储新的intent,否则getIntent()将返回旧的intent  //moidfy by even
        LauncherLog.v(TAG, "onNewIntent,jeffhejianfeng intent.getAction()="+intent.getAction());
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            if(mOptionDialog.isShowing()){
            	mOptionDialog.dismiss();
            }
            mWorkspace.setDefaultScreen(mWorkspace.getDefaultPage());
            mWorkspace.moveToDefaultScreen(true);
        }else if(BasicKEY.THEME_CHANGE_ACTION.equals(intent.getAction())){
          //modify by wangyang 2016.10.19 start 
            ThemeUpdateTextSize = true;
          //modify by wangyang 2016.10.19 end
            setTheme(mThemeManager.getThemeId());
            if(mProgressDialog == null){
                showHomeLoadingDialog();
            }else{
                mProgressDialog.show();
            }
            LauncherLog.v(TAG, "onNewIntent,jeff mWorkspace.getChildCount()="+mWorkspace.getChildCount());
            mWorkspace.removeAllWorkspaceScreens();
            
            mDragLayer.themeChange();
            mWorkspace = mDragLayer.getWorkspace();
            mWorkspace.setLauncher(this);
            mWorkspace.setDragController(mDragController);
            LauncherLog.v(TAG, "onNewIntent,jeff BasicKEY.THEME_CHANGE_ACTION");
            mModel.startLoader(this, true);
            
//            mIndicator = (Indicator) findViewById(R.id.view_indicator);
//            mWorkspace.setIndicator(mIndicator);
            
            int themeType =  ThemeManager.getInstance().getCurrentThemeType(this);
            Log.i(TAG, "BasicActivity onThemeChanged themeType:"+themeType);
            if(themeType == ThemeManager.THEME_CHINESESTYLE){
                mSystemBarTintManager.setTintColor(Color.parseColor("#fff0d8ce"));
            }else{
                mSystemBarTintManager.setTintColor(Color.parseColor("#009dde"));
            }
            mDragLayer.requestLayout();
        }
        //hejianfeng add start
        else if(BasicKEY.THEME_WALLPAPER_ACTION.equals(intent.getAction())){
        	 if(mProgressDialog == null){
                 showHomeLoadingDialog();
             }else{
                 mProgressDialog.show();
             }
        	 int currentPosition=Settings.Global.getInt(getContentResolver(), "current_theme_bg", 0); 
        	 int currentres=Settings.Global.getInt(getContentResolver(), "setWallpaper", 0); 
			try {
				if (currentPosition == -1) {
					mWallpaperManager.setBitmap(ThemeManager.getInstance()
							.getCurWallpaperBmp());
				} else {
					mWallpaperManager.setResource(currentres);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
        	 changeWallpaper();
		}
        else if (BasicKEY.THEME_ADD_ACTION.equals(intent.getAction())) {
			if (mProgressDialog == null) {
				showHomeLoadingDialog();
			} else {
				mProgressDialog.show();
			}
			addItemScreen();
		} else if (BasicKEY.THEME_DELETE_ACTION.equals(intent.getAction())) {
			int delete_screen=intent.getIntExtra("delete_select_page", -1);
			if(delete_screen!=-1){
				removeItemScreen(delete_screen);
			}
		}
        //hejianfeng add end
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "requestCode:"+requestCode+" resultCode:"+resultCode+"  data:");
        
        if(REPLACE_REQUESTCODE == resultCode){
            final long id = data.getLongExtra("id", -1);
            //mModel.updateBindWorkspace();
            mModel.updateOneItem(id);
        }else if(21==requestCode && resultCode==Activity.RESULT_OK){
            Uri uri = data.getData();
            long id = data.getLongExtra("id", -1);
            String phoneNumber = data.getStringExtra("phoneNumber");
            String contactName = data.getStringExtra("contactName");
            String headName = data.getStringExtra("photoName");
            //START: added by Yar @20170824
            int raw_id = data.getIntExtra("raw_id", -1);
            
            String cellDefName = data.getStringExtra("cellDefImage");
            
            Log.i("dengyingContact", "Launcher.java onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode+" phoneNumber:"+phoneNumber+" contactName:"+contactName+" headName="+headName);
            //Log.i("dengyingContact", "Launcher.java onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode+" phoneNumber:"+phoneNumber+" contactName:"+contactName+" headName="+headName + ", uri = " + uri + ", id = " + id + ", raw_id = " + raw_id);
            //hejianfeng add start
            /*if(TextUtils.isEmpty(phoneNumber)&& TextUtils.isEmpty(contactName)){
            	LauncherLog.v("dengyingContact", "onActivityResult,isEmpty");
            	restoration((int)id);
            	return;
            }
            //hejianfeng add end
            ContentValues cv = new ContentValues();
            cv.put(Favorites.PHONENUMBER, phoneNumber);
            cv.put(Favorites.CONTACTNAME, contactName);
            //START: added by Yar @20170824
            cv.put("displayMode", raw_id);
            //END: added by Yar @20170824
            if(uri != null){
                cv.put(Favorites.PIC_URI, uri.toString());
            }else{
                if(!TextUtils.isEmpty(headName)){
                    cv.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,headName);
                }
                if(!TextUtils.isEmpty(cellDefName)){
                    cv.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefName);
                }
                
                cv.put(Favorites.PIC_URI, "");
            }          
            restorationDatabase(this, (int)id, cv);
            mModel.updateOneItem(id);*/
            updateContactsItem(uri, id, phoneNumber, contactName, headName, raw_id, cellDefName);
            //END: added by Yar @20170824
        }
    }
    
	//START: added by Yar @20170824
    public void updateContactsItem(Uri uri, long id, String phoneNumber, String contactName, String headName, int raw_id, String cellDefName) {
        //Log.i("dengyingContact", "Launcher.java onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode+" phoneNumber:"+phoneNumber+" contactName:"+contactName+" headName="+headName);
        Log.i("dengyingContact", "Yar Launcher.java updateContactsItem phoneNumber:"+phoneNumber+" contactName:"+contactName+" headName="+headName + ", uri = " + uri + ", id = " + id + ", raw_id = " + raw_id);
        //hejianfeng add start
        if(TextUtils.isEmpty(phoneNumber)&& TextUtils.isEmpty(contactName)){
        	LauncherLog.v("dengyingContact", "onActivityResult,isEmpty");
        	restoration((int)id);
        	return;
        }
        //hejianfeng add end
        ContentValues cv = new ContentValues();
        cv.put(Favorites.PHONENUMBER, phoneNumber);
        cv.put(Favorites.CONTACTNAME, contactName);
        //START: added by Yar @20170824
        cv.put("displayMode", raw_id);
        //END: added by Yar @20170824
        if(uri != null){
            cv.put(Favorites.PIC_URI, uri.toString());
        }else{
            if(!TextUtils.isEmpty(headName)){
                cv.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,headName);
            }
            if(!TextUtils.isEmpty(cellDefName)){
                cv.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefName);
            }
            
            cv.put(Favorites.PIC_URI, "");
        }          
        restorationDatabase(this, (int)id, cv);
        mModel.updateOneItem(id);
    }
	//END: added by Yar @20170824
    
    
    // 打开或关闭GPRS
    private boolean gprsEnabled(boolean bEnable) {
        Object[] argObjects = null;

        boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
        if (isOpen == !bEnable) {
            setGprsEnabled("setMobileDataEnabled", bEnable);
        }

        return isOpen;
    }

    /**检测GPRS是否打开*/
    private boolean gprsIsOpenMethod(String methodName) {
        Class cmClass = mCM.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;

        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod(methodName, argClasses);

            isOpen = (Boolean) method.invoke(mCM, argObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOpen;
    }
    
    public boolean getPwdOffOrOn(){
        SharedPreferences sp = getSharedPreferences("gprs_state", 0);
        boolean state = sp.getBoolean("state", true);
        return state;
    }
    
    public String getPwd(){
        SharedPreferences sp = getSharedPreferences("gprs_pwd", 0);
        String  pwd = sp.getString("pwd", "9999");
        return pwd;
    }

    // 开启/关闭GPRS
    private void setGprsEnabled(String methodName, boolean isEnable) {
        Class cmClass = mCM.getClass();
        Class[] argClasses = new Class[1];
        argClasses[0] = boolean.class;

        try {
            Method method = cmClass.getMethod(methodName, argClasses);
            method.invoke(mCM, isEnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }   
    
    private void showSimDialog() {
        if (mSimDialog == null) {
            mSimDialog = new Dialog(this);
            mSimDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSimDialog.setContentView(R.layout.sim_dialog);
            mSimDialog.setCancelable(false);            
        }
        mSimDialog.show();
        new Thread() {
            @Override
            public void run() {
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.i(TAG, " InterruptedException  e: "+e.toString());
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        mSimDialog.dismiss();
                    }
                });
            }
        }.start();
    }
   
    /**获取手机号跟IMELI*/
    public String getIMEI(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
        String imei = tm.getDeviceId(); 
        String tel = tm.getLine1Number(); 
        return imei;
    }
    
    /**获取手机号*/
    public String getPhoneNumber(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
        String imei = tm.getDeviceId(); 
        String tel = tm.getLine1Number(); 
        return tel;
    }
    
    private void restorationDatabase(Context context,int id ,ContentValues values) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        ContentResolver cr = context.getContentResolver();
        int index = cr.update(uri, values, null, null);
    }
    
    static Cursor getDatabaseCellCursor(Context context, int id) {
        final Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        final ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        return cursor;
    }
    //hejianfeng add start
    private boolean isAddScreen=false;
    private AppComponentNameReplace mAppComponentNameReplace;
    private void removeItemScreen(int delete_screen){
    	LauncherLog.v(TAG, "removeItemScreen,jeff delete_screen="+delete_screen);
    	getContentResolver().delete(
				LauncherSettings.Favorites.CONTENT_URI,
				" modeSelect = "
						+ ThemeManager.getInstance().getCurrentThemeType()
						+ " and screen = "
						+ (delete_screen), null);
    	mWorkspace.getWorkspaceScreens().remove(delete_screen);
    	mWorkspace.getScreenOrder().remove((Object)delete_screen);
        mWorkspace.updateWorkSpaceUI();
    }

	private void addItemScreen() {
		mAppComponentNameReplace = new AppComponentNameReplace(this);
		String curThemepath = LauncherApplication.CappuThemes
				+ ThemeRes.getInstance().getThemeFileName(
						ThemeManager.getInstance().getCurrentThemeType());
		File addFile = new File(curThemepath + "/add_workspace.xml");
		int addScreen=mWorkspace.getScreenOrder().get(mWorkspace.getScreenOrder().size()-1)+1;
		loadFavorites(addScreen, ThemeManager.getInstance()
				.getThemeWorkspace(addFile));
		mModel.updateBindScreenWorkspace(addScreen);
		isAddScreen=true;
	}
    /**
     * author hejianfeng
     * 主题中的布局风格
     * @param db
     * @return 
     */
    private void loadFavorites(int screen, Document doc){
		if (doc != null) {
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ContentValues values = new ContentValues();
			loadFavorites(screen, doc,values, intent, LauncherProvider.TAG_FAVORITE);
			loadFavorites(screen, doc, values,intent, LauncherProvider.TAG_CONTACTS);
			loadFavorites(screen, doc, values,intent, LauncherProvider.TAG_APP);
		}
    }
    /**
     * author hejianfeng
     * 主题中的布局风格
     * @param db
     * @return 
     */
    private void loadFavorites(int screen, Document doc,ContentValues values,Intent intent,String tagName){
         NodeList mNodeList = doc.getElementsByTagName(tagName);
         for(int i = 0; i < mNodeList.getLength(); i++){
        	 Element element=(Element) mNodeList.item(i);
        	 loadSingleNode(screen,element,values,intent,tagName);
         }
    }
    /**
     * author hejianfeng
     * 导入单个节点
     * @param db
     * @return 
     */
	private void loadSingleNode(int screen, Element element,
			ContentValues values, Intent intent, String tagName) {
		PackageManager packageManager = getPackageManager();
		try {
			long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
			LauncherLog.v(TAG, "loadSingleNode, jeff container= "+element
					.getAttribute(LauncherSettings.Favorites.CONTAINER));
			container = Long.valueOf(element
					.getAttribute(LauncherSettings.Favorites.CONTAINER));
			values.clear();
			values.put(LauncherSettings.Favorites.CONTAINER, container);
			values.put(LauncherSettings.Favorites.BACKGROUND, element
					.getAttribute(LauncherSettings.Favorites.BACKGROUND));
			values.put(LauncherSettings.Favorites.SCREEN,screen);
			values.put(LauncherSettings.Favorites.CELLX,
					element.getAttribute("x"));
			values.put(LauncherSettings.Favorites.CELLY,
					element.getAttribute("y"));
			values.put(LauncherSettings.Favorites.SPANX,
					element.getAttribute(LauncherSettings.Favorites.SPANX));
			values.put(LauncherSettings.Favorites.SPANY,
					element.getAttribute(LauncherSettings.Favorites.SPANY));
			values.put(LauncherSettings.Favorites.MODE,
					element.getAttribute(LauncherSettings.Favorites.MODE));
			if (LauncherProvider.TAG_FAVORITE.equals(tagName)) {
				LauncherLog.v(TAG, "loadSingleNode, jeff TAG_FAVORITE Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
						+",className="+element.getAttribute("className"));
				addThemeAppShortcut(values, element, packageManager,
						intent, container);
			} else if (LauncherProvider.TAG_CONTACTS.equals(tagName)) {
				LauncherLog.v(TAG, "loadSingleNode, jeff TAG_CONTACTS Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
						+",className="+element.getAttribute("className"));
				addThemeContact(values, element, packageManager,
						intent);
			} else if (LauncherProvider.TAG_APP.equals(tagName)) {
				String packageName = element.getAttribute("packageName");
				String className = element.getAttribute("className");
				LauncherLog.v(TAG, "loadSingleNode, jeff TAG_APP Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
						+",className="+className);
				values.put(LauncherSettings.Favorites.INTENT, packageName
						+ "/" + className);
				values.put(LauncherSettings.Favorites.ALIAS_TITLE,
						element.getAttribute("aliasTitle"));
				values.put(Favorites.ITEM_TYPE,
						Favorites.ITEM_TYPE_APPWIDGET);
				getContentResolver().insert(LauncherSettings.Favorites.CONTENT_URI, values);
			}
		} catch (Exception e) {
			Log.w(TAG, "Got exception parsing favorites.", e);
		}
    }
    private boolean addThemeContact(ContentValues values, Element element,PackageManager packageManager, Intent intent) {

        ActivityInfo info = null;
        String packageName = element.getAttribute("packageName");
        String className = element.getAttribute("className");
        ComponentName cn = null;
        try {
            cn = new ComponentName(packageName, className);
            ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
            if(componentNameR != null){
                cn = componentNameR;
            }
            info = packageManager.getActivityInfo(cn, 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
        }

        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        values.put(Favorites.INTENT, intent.toUri(0));
        String aliasTitle = element.getAttribute("aliasTitle");
        if(info != null){
            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
        }else{
            values.put(Favorites.TITLE, aliasTitle);
        }
        
        if(aliasTitle != null && !"".equals(aliasTitle)){
            values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
        }else{
            values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
        }
        
        String cellDefImage = element.getAttribute("cellDefImage");
        if(cellDefImage != null && !"".equals(cellDefImage)){
            values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefImage);
        }else{
            values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,"");
        }
        
        String aliasTitleBackground = element.getAttribute("aliasTitleBackground");
        if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
            values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
        }else{
            values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
        }
        
        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_CONTACTS);
        getContentResolver().insert(LauncherSettings.Favorites.CONTENT_URI, values);
        return true;
    }
    private boolean addThemeAppShortcut(ContentValues values, Element element,
            PackageManager packageManager, Intent intent,long container) {

        ActivityInfo info = null;
        String packageName = element.getAttribute("packageName");
        String className = element.getAttribute("className");
        ComponentName cn = null;
        try {
            cn = new ComponentName(packageName, className);

            ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
            if(componentNameR != null){
                cn = componentNameR;
            }
            
            info = packageManager.getActivityInfo(cn, 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
        }

        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        values.put(Favorites.INTENT, intent.toUri(0));
        String aliasTitle = element.getAttribute("aliasTitle");
        if(info != null){
            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
        }else{
            values.put(Favorites.TITLE, aliasTitle);//getAliasTitle(aliasTitle)
        }
        
        if(aliasTitle != null && !"".equals(aliasTitle)){
            values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
        }else{
            values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
        }
     
        String aliasTitleBackground = element.getAttribute("aliasTitleBackground");
        if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
            values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
        }else{
            values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
        }
        
        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
        getContentResolver().insert(LauncherSettings.Favorites.CONTENT_URI, values);
        return true;
    }
    //hejianfeng add end
    private static final Handler sWorker = new Handler();

    @Override
    public void finishBindingItems() {
        LauncherLog.v(TAG, "finishBindingItems..."+mWorkspace.getChildCount()+"   "+mWorkspace.getScrollX());
        mWorkspace.updateWorkSpaceUI();
        if(isAddScreen){
        	mWorkspace.setCurrentPage(mWorkspace.getChildCount()-1);
        	mWorkspace.updateIndicatorPosition();
        	isAddScreen=false;
        }
        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Exception when Dialog.dismiss()...");
            }
        }
        isfinishBindingItems=true;//hejianfeng add
    }
    
    private void showHomeLoadingDialog() {
    	LauncherLog.v(TAG, "showHomeLoadingDialog ");
        if (mProgressDialog == null) {
            try{
                mProgressDialog = new Dialog(Launcher.this);
                mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mProgressDialog.setContentView(R.layout.progressbar);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }catch(Exception e){
            	LauncherLog.v(TAG,"launcher showHomeLoadingDialog:"+e.toString());
            }
            
            LauncherLog.v(TAG, "showHomeLoadingDialog  show");
        }
    }
    //hejianfeng add start
    @Override
	public void bindAllItems(ArrayList<ItemShortcut> is,
			ArrayList<ItemContacts> ic, ArrayList<ItemWidget> iw) {
		synchronized (LauncherModel.mObject) {
			for (int i = 0; i < is.size(); i++) {
				ItemShortcut item = is.get(i);
				switch (item.itemType) {
				case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					View shortcut = createShortcut(item, item.screen);
					shortcut.setTag(item);
					mWorkspace.addInScreen(shortcut, item.screen, item.cellX,
							item.cellY, item.spanX, item.spanY, false);
					break;
				}
			}
			for (int i = 0; i < ic.size(); i++) {
				ItemContacts item = ic.get(i);
				switch (item.itemType) {
				case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
					View shortcut = createShortcut(item, item.screen);
					shortcut.setTag(item);
					mWorkspace.addInScreen(shortcut, item.screen, item.cellX,
							item.cellY, 1, 1, false);
					break;
				}
			}
			for (int i = 0; i < iw.size(); i++) {
				ItemWidget item = iw.get(i);
				switch (item.itemType) {
				case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
					View shortcut = createShortcut(item, item.screen);
					shortcut.setTag(item);
					mWorkspace.addInScreen(shortcut, item.screen, item.cellX,
							item.cellY, item.spanX, item.spanY, false);
					break;
				}
			}
		}

	}
    //hejianfeng add end
    @Override
    public void bindItemsShortcut(ArrayList<ItemShortcut> is) {
        synchronized (LauncherModel.mObject) {
            for (int i = 0; i < is.size(); i++) {
                final ItemShortcut item = is.get(i);
                switch (item.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    	//hejianfeng add start
						if (item.getIconDrawable() == null ||TextUtils.isEmpty(item.getTitle())) {
							LauncherLog.v(TAG, "bindItemsShortcut,jeff item.screen="+item.screen);
							ContentValues cv = new ContentValues();
							resetAddItem(cv);
							restorationDatabase(this, (int) item.id, cv);
							mModel.updateOneItem((int) item.id);
						}
                        //hejianfeng add end
                        BubbleView bv = findBubbleView(item);
                        if(bv != null){
                            modifyBubbleView(bv, item);
                            bv.setTag(item);
                        }else{
                            final View shortcut = createShortcut(item,item.screen);
                            shortcut.setTag(item);
                            
                            mWorkspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, item.spanX,item.spanY, false);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void bindItemsContacts(ArrayList<ItemContacts> ic) {

    	LauncherLog.v(TAG, "bindItemsContacts,jeff ic.size()="+ic.size());
        synchronized (LauncherModel.mObject) {
            for (int i = 0; i < ic.size(); i++) {
                final ItemContacts item = ic.get(i);
                switch (item.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
                        
                        BubbleView bv = findBubbleView(item);
                        if(bv != null){
                            modifyBubbleView(bv, item);
                            bv.setTag(item);
                        }else if(item.screen>=0){
                            final View shortcut = createShortcut(item,item.screen);
                            shortcut.setTag(item);
                            mWorkspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1, false);
                        }
                        break;
                }
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void bindItemsWidget(ItemWidget iw) {
    	LauncherLog.v(TAG, "bindItemsWidget,jeff iw="+iw);
        boolean CellNeedsAdd = true;
        try { 
            BubbleView bv = findBubbleView(iw);
            View view = getItemsWidget(mWorkspace,iw);
            if(view == null){
                return;
            }
            Bitmap bitmap=ThemeManager.getInstance().getQuickIcon(iw.intent.getDataString());
            LauncherLog.v(TAG, "bindItemsWidget,jeff bitmap="+bitmap);
            if(bitmap!=null){
            	BitmapDrawable drawable = new BitmapDrawable(bitmap);  
            	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS
            			&&ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.CappuKookView")){
            		view.findViewById(R.id.main).setBackgroundResource(R.drawable.weather_background);
            	}else{
            		view.setBackground(drawable);
            	}
            }
            if(bv == null){
                CellNeedsAdd = true;
                bv = (BubbleView) createShortcut(iw, iw.screen);
            }else{
                CellNeedsAdd = false;
                modifyBubbleView(bv, iw);
            }
            LauncherLog.v(TAG, "bindItemsWidget,jeff iw.intent="+iw.intent);
            bv.setOnClickIntent((OnChildViewClick) view);
            RelativeLayout ryBubbleView =((RelativeLayout)bv.findViewById(R.id.ry_bubbleView));
            LauncherLog.v(TAG, "bindItemsWidget,jeff ryBubbleView.getChildCount()="+ryBubbleView.getChildCount());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			if (ThemeManager.getInstance().isExistWidget(iw.intent,
					"com.cappu.launcherwin.kookview.GPRSKookView")) {
				layoutParams
						.addRule(RelativeLayout.ABOVE, R.id.bubbleView_text);
				if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS) {
					layoutParams.width = 150;
					layoutParams.topMargin = 30;
					layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				}
			}
            
            view.setLayoutParams(layoutParams);
            ryBubbleView.addView(view, 0, layoutParams);
            
            bv.setTag(iw);
            CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(iw.screen);
            if(cellLayout !=null && CellNeedsAdd){
                mWorkspace.addInScreen(bv, iw.screen, iw.cellX, iw.cellY, iw.spanX, iw.spanY, false);
            }
       } catch (NameNotFoundException e) { 
           Log.i(TAG, "添加异常 NameNotFoundException:"+e.toString());
       } catch (ClassNotFoundException e) {
           Log.i(TAG, "添加异常 ClassNotFoundException:"+e.toString());
       } catch (InstantiationException e) { 
           Log.i(TAG, "添加异常 InstantiationException:"+e.toString());
       } catch (IllegalAccessException e) { 
           Log.i(TAG, "添加异常 IllegalAccessException:"+e.toString());
       } catch (IllegalArgumentException e) { 
           Log.i(TAG, "添加异常 IllegalArgumentException:"+e.toString());
       } catch (InvocationTargetException e) { 
           Log.i(TAG, "添加异常 InvocationTargetException:"+e.toString());
       } catch (NoSuchMethodException e) { 
           Log.i(TAG, "添加异常 NoSuchMethodException:"+e.toString());
       } 
    }
    
    /**查找某一个位置是否有了磁块*/
    public BubbleView findBubbleView(ItemInfo ii){
    	CellLayout cellLayout =null;
		if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS) {
			int position = mWorkspace.getScreenOrder().indexOf(ii.screen);
			if (mWorkspace.isNegativeScreen()) {
				cellLayout = (CellLayout) mWorkspace.getChildAt(position);
			} else {
				cellLayout = (CellLayout) mWorkspace.getChildAt(position - 1);
			}
		}else{
			cellLayout = (CellLayout) mWorkspace.getChildAt(ii.screen);
		}
        BubbleView bv = null;
        if(cellLayout !=null){
            for (int i = 0; i < cellLayout.getChildCount(); i++) {
                if(cellLayout.getChildAt(i) instanceof BubbleView){
                    BubbleView bubbleView = (BubbleView) cellLayout.getChildAt(i);
                    ItemInfo ItemInfo= (ItemInfo) bubbleView.getTag();
                    bubbleView.getLocationOnScreen(mLocation);
                    if(ii.screen == ItemInfo.screen && ii.cellX == ItemInfo.cellX && ii.cellY == ItemInfo.cellY){
                        bv = bubbleView;
                    }
                }
            }
        }
        return bv;
    }
    
    private View getItemsWidget(Workspace workspace,ItemWidget iw) throws NameNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    	LauncherLog.v(TAG, "getItemsWidget,jeff iw.intent.getDataString()="+iw.intent.getDataString());
    	if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.AlbumsKookView")){
    		LauncherLog.v(TAG, "getItemsWidget,jeff AlbumsKookView");
            AlbumsKookView owner = new AlbumsKookView();
            return owner.getView(Launcher.this,mWorkspaceUpdateReceiver);
        }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.GPRSKookView")){
        	LauncherLog.v(TAG, "getItemsWidget,jeff GPRSKookView");
            GPRSKookView owner = new GPRSKookView();
            return owner.getView(Launcher.this,mWorkspaceUpdateReceiver);
        }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.CappuKookView")){
            CappuKookView owner = new CappuKookView();
            return owner.getView(Launcher.this,mWorkspaceUpdateReceiver);
        }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.MusicKookView")){
            MusicKookView owner = new MusicKookView();
            return owner.getView(Launcher.this);
        }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.VideoKookView")){
            VideoKookView owner = new VideoKookView();
            return owner.getView(Launcher.this);
        }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.assembly.AssemblyJeffView")){
        	AssemblyJeffView owner = new AssemblyJeffView();
            return owner.getView(Launcher.this,mWorkspaceUpdateReceiver);
        }else{
            return null;
        }
    }
    
    private View createShortcut(ItemInfo info, int screen) {
        return createShortcut(R.layout.application, (ViewGroup) mWorkspace.getChildAt(screen), info);
    }
    
    private View createShortcut(int layoutResId, ViewGroup parent, ItemInfo info) {
        BubbleView bubbleView = (BubbleView) mInflater.inflate(layoutResId, parent, false);
        return modifyBubbleView(bubbleView, info);
    }
    
    private View modifyBubbleView(BubbleView bubbleView,ItemInfo info){
        bubbleView.setItemInfo(this,info,mSpeechTools);
        if(!(info instanceof ItemWidget)){
            bubbleView.setOnClickListener(this);
        }
        boolean isTrue = true;
        if((info instanceof ItemWidget)){
            ItemWidget iw = (ItemWidget) info;
            if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.CappuKookView")){
                isTrue = false;
            }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.MusicKookView")){
                isTrue = false;
            }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.AlbumsKookView")){
                isTrue = false;
            }else if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.assembly.AssemblyJeffView")){
                isTrue = false;
            }
        }else if(info instanceof ItemShortcut){
            ItemShortcut is = (ItemShortcut) info;
            if(is.id < 1){
                isTrue = false;
            }
            bubbleView.setOnChildViewDrag(mDragLayer);
        }//hejianfeng add start
        else if((info instanceof ItemContacts) && !ThemeManager.getInstance().isSwitchIconBg()){
        	isTrue = false;
        }
        //hejianfeng add end
        
        if(isTrue){
            bubbleView.setOnLongClickListener(this);
        }      
        return bubbleView;
    }

    @Override
    public void bindItemShortcut(ItemShortcut is) {
        final Workspace workspace = mWorkspace;
        BubbleView bv = findBubbleView(is);
        if(bv != null){
            modifyBubbleView(bv, is);
            bv.setTag(is);
        }else{
            final View shortcut = createShortcut(is,is.screen);
            shortcut.setTag(is);
            workspace.addInScreen(shortcut, is.screen, is.cellX, is.cellY, 1, 1, false);
        }
    }

    @Override
    public void bindItemContacts(ItemContacts ic) {
        BubbleView bv = findBubbleView(ic);
        if(bv != null){
            modifyBubbleView(bv, ic);
            bv.setTag(ic);
        }        
    }
    
    public void launcherSendBroadcastReceiver(boolean hide, LauncherType lt) {

        Log.i(TAG, "Launcher launcherSendBroadcastReceiver hide:" + hide + "  mWorkspace.getNextPage() = " + mWorkspace.getNextPage() + "   mWorkspace.getCurrentPage()" + mWorkspace.getCurrentPage());
        Intent intent = new Intent("com.cappu.launcher.pagechange");
        if(hide){
            intent.putExtra("page", -1);
        }else if(mWorkspace.isNegativeScreen()&&ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	if(mWorkspace.getNextPage()==2){
        		intent.putExtra("page", 1);
        	}else if(mWorkspace.getNextPage()==1){
        		intent.putExtra("page", -1);
        	}else if(mWorkspace.getNextPage()==3){
        		intent.putExtra("page", 2);
        	}else{
        		intent.putExtra("page", mWorkspace.getNextPage());
        	}
        }else{
            intent.putExtra("page", mWorkspace.getNextPage());
        }
        //hejianfeng add start
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS && !hide){
	        mSystemBarTintManager.setTintDrawable(new BitmapDrawable(
					ThemeManager.getInstance().getImagePiece().statusBarBmp));
        }
        //hejianfeng add end
        sendBroadcast(intent);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop ");
        mLauncherType = mLauncherType.onStop;
        if((mWorkspace.getNextPage()==1&& !mWorkspace.isNegativeScreen())||(mWorkspace.getNextPage()==2&&mWorkspace.isNegativeScreen())){
        	launcherSendBroadcastReceiver(true,null);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mLauncherType = mLauncherType.onPause;
        Log.e(TAG, "onPause ");
//        launcherSendBroadcastReceiver(true,null);
    }  
    
    public LauncherType getLauncherType(){
        return mLauncherType;
    }

    //added by yzs for UnreadView begin 
    public int getNewSmsCount() { 
        int result = 0; 
        Cursor csr = getContentResolver().query(Uri.parse("content://sms"), null, 
            "type = 1 and read = 0", null, null); 
        if (csr != null) { 
            result = csr.getCount(); 
            csr.close(); 
        } 
        return result; 
    } 

    public int getNewMmsCount() { 
        int result = 0; 
        Cursor csr = getContentResolver().query(Uri.parse("content://mms/inbox"), 
        null, "read = 0", null, null); 
        if (csr != null) { 
            result = csr.getCount(); 
            csr.close(); 
        } 
        return result; 
    }

    public int getUnreceivedCallCount() {
		ContentResolver localContentResolver = getContentResolver();
		Uri localUri = CallLog.Calls.CONTENT_URI;
		String[] arrayOfString = new String[1];
		arrayOfString[0] = "_id";
		Cursor localCursor = localContentResolver.query(localUri,
				arrayOfString, "type=3 and new<>0", null, null);

		int number;
		if (localCursor == null) {
			return -1;
		} else {
			try {
				number = localCursor.getCount();
                if(localCursor != null){
				    localCursor.close();
                }
				//localCursor.close();
			} finally {
                if(localCursor != null){
    				localCursor.close();
                }
			}
		}
		return number;
	}
    //hejianfeng add start
    private ContentObserver changeNegativeScreen = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
        	LauncherLog.v(TAG, "changeNegativeScreen,jeff selfChange="+mWorkspace.isNegativeScreen());
        	mWorkspace.updateWorkSpaceUI();
        }                        
     };
    //hejianfeng add end
    private ContentObserver newMmsContentObserver = new ContentObserver(new Handler()) {
       public void onChange(boolean selfChange) {
          int messageUnread = getNewSmsCount() + getNewMmsCount();
          mDragLayer.refrshMmsState(messageUnread);
       }                        
    };
   
 
    private ContentObserver newPhoneContentObserver = new ContentObserver(
        new Handler()) {
           public void onChange(boolean selfChange) {
              int phoneUnread = getUnreceivedCallCount();
              mDragLayer.refreshPhoneState(phoneUnread);
           }            
        };

    @SuppressLint("NewApi")
    private void registerMessageObserver() {
         unregisterMessageObserver();
         getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, newMmsContentObserver);
         getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, newMmsContentObserver); 
         getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, newPhoneContentObserver);
         
         getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_contacts_screen"), true, changeNegativeScreen);//hejianfeng add
    }

    private synchronized void unregisterMessageObserver() {
        try {
            if (newMmsContentObserver != null) {
                getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }  
            if (newPhoneContentObserver != null) {
                getContentResolver().unregisterContentObserver(newPhoneContentObserver);
            }
            //hejianfeng add start
            if (changeNegativeScreen != null) {
                getContentResolver().unregisterContentObserver(changeNegativeScreen);
            }
            //hejianfeng add end 
        } catch (Exception e) {
            Log.e("yzs001", "unregisterObserver fail");
        }
    }
}
