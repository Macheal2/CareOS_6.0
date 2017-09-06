
package com.cappu.launcherwin;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.downloadUI.celllayout.CellLyouatUtil;
import com.cappu.launcherwin.widget.LauncherLog;

public class LauncherModel extends BroadcastReceiver {
    
	private static final String TAG="LauncherModel";//hejianfeng add 
    public static final String CARE_OS_UPDATE = "android.cappu.careos.VERSION_CHANGE";
    String existsApkVersion = null;
    
    private final Object mLock = new Object();

    public static Object mObject = new Object();

    LauncherApplication mLauncherApplication;
    private Context mContext;
    private ThemeTools mThemeTools;
    
    private Handler mHandler = new Handler();
    private WeakReference<Callbacks> mCallbacks;
    
    final ArrayList<ItemContacts> mItemsContacts = new ArrayList<ItemContacts>();
    final ArrayList<ItemShortcut> mItemsShortcut = new ArrayList<ItemShortcut>();
    final ArrayList<ItemWidget> mItemsWidget = new ArrayList<ItemWidget>();
    
    private ItemContacts mItemContacts;
    private ItemShortcut mItemShortcut;
    private ItemWidget mItemWidget;

    LauncherModel(Context context,ThemeTools themeTools) {
        if(context instanceof LauncherApplication){
            mLauncherApplication = (LauncherApplication) context;
        }
        mContext = context;
        mThemeTools = themeTools;
        startHeartbeatBroacast(context, 0, CARE_OS_UPDATE);
    }

    public void startLoader(Context context, boolean isLaunching) {
        Log.i(TAG, "startLoader,jeff isLaunching="+isLaunching);
        LoaderScreenTask mLoaderScreenTask=new LoaderScreenTask();
        mLoaderScreenTask.execute();
    }
    //hejianfeng add start
    class LoaderScreenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
        	loadAndBindWorkspace();
        	Callbacks oldCallbacks = mCallbacks.get();
        	Callbacks callbacks = tryGetCallbacks(oldCallbacks);
        	callbacks.bindAllItems(mItemsShortcut,mItemsContacts,mItemsWidget);
        	return null;
        }
        @Override
        protected void onPostExecute(String re) {
            super.onPostExecute(re);
            bindWorkspace();
            callbacks.finishBindingItems();
        }
	}
    class AddUpdateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
        	loadScreenWorkspace(screen);
            LauncherLog.v(TAG, "UpdateTask,jeff start");
            callbacks.bindAllItems(mItemsShortcut,mItemsContacts,mItemsWidget);
        	return null;
        }
        @Override
        protected void onPostExecute(String re) {
            super.onPostExecute(re);
            bindWorkspace();
            callbacks.finishBindingItems();
        }
	}
    //hejianfeng add end
    
    public void updateBindScreenWorkspace(int screen){
    	this.screen=screen;
    	AddUpdateTask mAddUpdateTask=new AddUpdateTask();
    	mAddUpdateTask.execute();
        
    }
    //hejianfeng add start
    private int screen;
    private void loadScreenWorkspace(int screen) {
        
        final Context context = mContext;
        final ContentResolver contentResolver = context.getContentResolver();
        mItemsContacts.clear();
        mItemsWidget.clear();
        mItemsShortcut.clear();

		Cursor c = null;
		c = contentResolver.query(
				LauncherSettings.Favorites.CONTENT_URI,
				null,
				" modeSelect = "
						+ ThemeManager.getInstance().getCurrentThemeType(
								mContext)+" and screen="+screen, null, null);
		if(c==null){
			return;
		}
        try {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int backgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.BACKGROUND);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int phoneNumberIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PHONENUMBER);
            final int contactsNameIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTACTNAME);
            final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
            final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);

            final int aliasTitleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE);
            final int cellDefImageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELL_DEF_IMAGE);
            final int aliasTitleBackgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
            final int picUriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PIC_URI);
            
            final int modeSelectIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.MODE);

            //START: added by Yar @20170824
            final int rawIDIndex = c.getColumnIndexOrThrow("displayMode");
            //END: added by Yar @20170824
            
            String intentDescription;
            int container;
            long id;
            Intent intent = null;

            while (c.moveToNext()) {
                try {
                    int itemType = c.getInt(itemTypeIndex);

                    switch (itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
                        ItemContacts ic = new ItemContacts(mContext, mHandler);//modified by Yar @20170824
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (URISyntaxException e) {
                            continue;
                        }
                        ic.intent = intent;
                        ic.itemType = itemType;
                        ic.intent = intent;
                        ic.id = c.getLong(idIndex);
                        ic.background = c.getString(backgroundIndex);
                        ic.phoneNumber = c.getString(phoneNumberIndex);
                        ic.contactName = c.getString(contactsNameIndex);
                        ic.modeSelect = c.getString(modeSelectIndex);
                        
                        //START: added by Yar @20170824
                        ic.mRawId = c.getInt(rawIDIndex);
                        //END: added by Yar @20170824

                        ic.mAliasTitle = c.getString(aliasTitleIndex);
                        ic.cellDefImage = c.getString(cellDefImageIndex);
                        ic.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);

                        if (c.getString(picUriIndex) != null && !"".equals(c.getString(picUriIndex))) {
                            ic.CPUri = Uri.parse(c.getString(picUriIndex));
                        }
                        ic.container = c.getInt(containerIndex);
                        ic.screen = c.getInt(screenIndex);
                        ic.cellX = c.getInt(cellXIndex);
                        ic.cellY = c.getInt(cellYIndex);
                        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                        	ic.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[ic.cellX][ic.cellY];
                        	ic.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[ic.cellX][ic.cellY];
                        }
                        ic.init();
                        mItemsContacts.add(ic);

                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        ItemWidget iw = new ItemWidget(mContext);
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (Exception e) {
                            intent = new Intent();
                            String[] s = intentDescription.split("/");
                            intent.setComponent(new ComponentName(s[0], s[1]));
                        }
                        id = c.getLong(idIndex);
                        iw.intent = intent;
                        iw.id = id;
                        iw.modeSelect = c.getString(modeSelectIndex);
                        iw.screen = c.getInt(screenIndex);
                        iw.cellX = c.getInt(cellXIndex);
                        iw.cellY = c.getInt(cellYIndex);
                        iw.spanX = c.getInt(spanXIndex);
                        iw.spanY = c.getInt(spanYIndex);
                        iw.background = c.getString(backgroundIndex);
                        iw.aliasTitle = c.getString(aliasTitleIndex);
                        iw.init();
						if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS) {
							if (iw.screen == 1 ||iw.screen == 2||iw.screen == 3) {
								iw.pieceBg = ThemeManager.getInstance()
										.getImagePiece().widgetBmp;
								iw.pieceHalfBg=ThemeManager.getInstance()
										.getImagePiece().widgetBmpBg;
							} else {
								iw.pieceBg = ThemeManager.getInstance()
										.getImagePiece().cellBitmaps[iw.cellX][iw.cellY];
								iw.pieceHalfBg = ThemeManager.getInstance()
										.getImagePiece().cellBitmapsBg[iw.cellX][iw.cellY];
							}
						}
                        mItemsWidget.add(iw);

                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        ItemShortcut is = new ItemShortcut(mContext);// mContext
                        intentDescription = c.getString(intentIndex);
                        container = c.getInt(containerIndex);
                        if (container != -101) {
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                            } catch (URISyntaxException e) {
                                continue;
                            }
                        } else {
                            intent = null;
                        }
                        is.intent = intent;
                        is.itemType = itemType;
                        is.intent = intent;
                        is.id = c.getLong(idIndex);
                        is.background = c.getString(backgroundIndex);
                        
                        is.modeSelect = c.getString(modeSelectIndex);

                        is.aliasTitle = c.getString(aliasTitleIndex);
                        is.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);

                        is.container = c.getInt(containerIndex);
                        is.screen = c.getInt(screenIndex);
                        is.cellX = c.getInt(cellXIndex);
                        is.cellY = c.getInt(cellYIndex);
                        
                        is.spanX = c.getInt(spanXIndex);
                        is.spanY = c.getInt(spanYIndex);

                        is.setThemeTools(mThemeTools);
                        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                        	is.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[is.cellX][is.cellY];
                        	is.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[is.cellX][is.cellY];
                        }
                        is.init();
                        mItemsShortcut.add(is);
                        break;
                    }
                } catch (Exception e) {
                    Log.w("HHJ", "Desktop items loading interrupted:", e);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    //hejianfeng add end
    public void updateOneItem(long id){
        mHandlerOneItem.sendEmptyMessage((int)id);
    }
    
    private Handler mHandlerOneItem = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int type = loadOneItem(msg.what);
            
            if(type == LauncherSettings.Favorites.ITEM_TYPE_CONTACTS){
                if(mItemContacts != null){
//START: modified by Yar @20170824
//                    mHandler.post(new Runnable() {
//                        public void run() {
                            callbacks.bindItemContacts(mItemContacts);
//                        }
//                    });
//END: modified by Yar @20170824
                }else{
                }
            }else if(type == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
            	LauncherLog.v(TAG, "mHandlerOneItem,jeff ITEM_TYPE_APPWIDGET");
                if(mItemWidget != null){
                    mHandler.post(new Runnable() {
                        public void run() {
                            callbacks.bindItemsWidget(mItemWidget);
                        }
                    });
                }else{
                }
            }else if(type == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
                if(mItemShortcut != null){
                	//hejianfeng delete
//                    mHandler.post(new Runnable() {
//                        public void run() {
                            callbacks.bindItemShortcut(mItemShortcut);
//                        }
//                    });
                }
            }
            
        }
    };
    
    public ItemInfo queryDate(int tId){
        int type = loadOneItem(tId);
        
        if(type == LauncherSettings.Favorites.ITEM_TYPE_CONTACTS){
            if (mItemContacts != null) {
                return mItemContacts;
            }else{
                return null;
            }
        }else if(type == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
            if (mItemWidget != null) {
                return mItemWidget;
            }else{
                return null;
            }
        }else if(type == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
            if (mItemShortcut != null) {
                return mItemShortcut;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }
    
    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }
    
    private void loadAndBindWorkspace() {
        Log.v(TAG, "loadAndBindWorkspace,jeff loadWorkspace");
        loadWorkspace();
        loadBackupData();
    }

    private int loadOneItem(int tId){
        
        int type = -1;
        final Context context = mContext;
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,null, " _id = "+tId, null, null);

        mItemContacts = null;
        mItemShortcut = null;
        mItemWidget = null;
        
        if (c != null) {
            Log.i("HHJ", "104   " + c.getCount());
        } else {
            Log.i("HHJ", "106   " + c.getCount());
            return type;
        }
        
        try {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int backgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.BACKGROUND);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int phoneNumberIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PHONENUMBER);
            final int contactsNameIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTACTNAME);
            final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
            final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
            
            final int aliasTitleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE);
            final int cellDefImageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELL_DEF_IMAGE);
            final int aliasTitleBackgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
            final int picUriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PIC_URI);
            
            final int modeSelectIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.MODE);
            
            //START: added by Yar @20170824
            final int rawIDIndex = c.getColumnIndexOrThrow("displayMode");
            //END: added by Yar @20170824

            String intentDescription;
            int container;
            long id;
            Intent intent = null;

            while (c.moveToNext()) {
                try {
                    int itemType = c.getInt(itemTypeIndex);
                    type = itemType;

                    switch (itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
                            mItemContacts = new ItemContacts(mContext, mHandler);//modified by Yar @20170824
                            intentDescription = c.getString(intentIndex);
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                            } catch (URISyntaxException e) {
                                continue;
                            }
                            mItemContacts.intent = intent;
                            mItemContacts.itemType = itemType;
                            mItemContacts.intent = intent;
                            mItemContacts.id = c.getLong(idIndex);
                            mItemContacts.background = c.getString(backgroundIndex);
                            mItemContacts.phoneNumber = c.getString(phoneNumberIndex);
                            mItemContacts.contactName = c.getString(contactsNameIndex);
                            
                            //START: added by Yar @20170824
                            mItemContacts.mRawId = c.getInt(rawIDIndex);
                            //END: added by Yar @20170824
                            android.util.Log.i("Yar", "1. mItemContacts.mRawId = " + mItemContacts.mRawId + ", mItemContacts.phoneNumber = " + mItemContacts.phoneNumber + ", mItemContacts.contactName = " + mItemContacts.contactName);
                            mItemContacts.mAliasTitle = c.getString(aliasTitleIndex);
                            mItemContacts.cellDefImage = c.getString(cellDefImageIndex);
                            mItemContacts.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);
                            
                            if(c.getString(picUriIndex) != null && !"".equals(c.getString(picUriIndex))){
                                mItemContacts.CPUri = Uri.parse(c.getString(picUriIndex));
                            }
                            
                            mItemContacts.container = c.getInt(containerIndex);
                            mItemContacts.screen = c.getInt(screenIndex);
                            mItemContacts.cellX = c.getInt(cellXIndex);
                            mItemContacts.cellY = c.getInt(cellYIndex);
                            
                            mItemContacts.modeSelect = c.getString(modeSelectIndex);
                            if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                            	mItemContacts.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[mItemContacts.cellX][mItemContacts.cellY];
                            	mItemContacts.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[mItemContacts.cellX][mItemContacts.cellY];
                            }
                            mItemContacts.init();
                            
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            mItemWidget = new ItemWidget(mContext);
                            intentDescription = c.getString(intentIndex);
                            Log.i("HHJ", "121 ++++ intent intentDescription :"+intentDescription);
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                                if(intent.getComponent() == null){
                                    String[] s = intentDescription.split("/");
                                    intent.setComponent(new ComponentName(s[0], s[1]));
                                }
                                
                            } catch (Exception e) {
                                Log.i("HHJ", "这里传入的包名类名出了问题 从先解析:");
                                intent = new Intent();
                                String[] s = intentDescription.split("/");
                                intent.setComponent(new ComponentName(s[0], s[1]));
                            }
                            id = c.getLong(idIndex);
                            mItemWidget.intent = intent;
                            mItemWidget.id = id;
                            mItemWidget.screen = c.getInt(screenIndex);
                            mItemWidget.cellX = c.getInt(cellXIndex);
                            mItemWidget.cellY = c.getInt(cellYIndex);
                            mItemWidget.spanX = c.getInt(spanXIndex);
                            mItemWidget.spanY = c.getInt(spanYIndex);
                            mItemWidget.modeSelect = c.getString(modeSelectIndex);
                            mItemWidget.background = c.getString(backgroundIndex);
                            mItemWidget.aliasTitle = c.getString(aliasTitleIndex);
                            if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                            	if((mItemWidget.screen==1 ||mItemWidget.screen==2||mItemWidget.screen==3 )&& (mItemWidget.spanX >1 || mItemWidget.spanY>1)){
                            		mItemWidget.pieceBg=ThemeManager.getInstance().getImagePiece().widgetBmp;
                            		mItemWidget.pieceHalfBg=ThemeManager.getInstance().getImagePiece().widgetBmpBg;
                            	}else{
                            		mItemWidget.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[mItemWidget.cellX][mItemWidget.cellY];
                            		mItemWidget.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[mItemWidget.cellX][mItemWidget.cellY];
                            	}
                            }
                            mItemWidget.init();
                            
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            mItemShortcut = new ItemShortcut(mContext);//mContext
                            intentDescription = c.getString(intentIndex);
                            container = c.getInt(containerIndex);
                            if(container != -101){
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                    Log.i("HHJ", "288 intent:"+intent.toString());
                                } catch (URISyntaxException e) {
                                    continue;
                                }
                            }else{
                                intent = null;
                            }
                            mItemShortcut.intent = intent;
                            mItemShortcut.itemType = itemType;
                            mItemShortcut.intent = intent;
                            mItemShortcut.id = c.getLong(idIndex);
                            mItemShortcut.background = c.getString(backgroundIndex);
                            
                            mItemShortcut.modeSelect = c.getString(modeSelectIndex);
                            
                            
                            
                            mItemShortcut.aliasTitle = c.getString(aliasTitleIndex);
                            mItemShortcut.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);
                            
                            mItemShortcut.container = c.getInt(containerIndex);
                            mItemShortcut.screen = c.getInt(screenIndex);
                            mItemShortcut.cellX = c.getInt(cellXIndex);
                            mItemShortcut.cellY = c.getInt(cellYIndex);
                            
                            mItemShortcut.spanX = c.getInt(spanXIndex);
                            mItemShortcut.spanY = c.getInt(spanYIndex);

                            mItemShortcut.setThemeTools(mThemeTools);
                            if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                            	mItemShortcut.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[mItemShortcut.cellX][mItemShortcut.cellY];
                            	mItemShortcut.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[mItemShortcut.cellX][mItemShortcut.cellY];
                            }
                            mItemShortcut.init();
                            break;
                    }
                
                } catch (Exception e) {
                    Log.w("HHJ", "Desktop items loading interrupted:", e);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return type;
    }
    
    //hejianfeng add start
    private Map<String,ContentValues> backupList=new HashMap<String,ContentValues>();
    public void loadBackupData(){
    	final Context context = mContext;
        final ContentResolver contentResolver = context.getContentResolver();
		Cursor c = null;
		c = contentResolver.query(
				LauncherSettings.Favorites.BACKUP_URI,
				null,
				" modeSelect = "
						+ ThemeManager.getInstance().getCurrentThemeType(
								mContext), null, null);
		if(c==null){
			return;
		}
        try {
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int backgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.BACKGROUND);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
            final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
            final int aliasTitleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE);
            final int cellDefImageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELL_DEF_IMAGE);
            final int aliasTitleBackgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
            final int modeSelectIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.MODE);
            String intentDescription;
            int container;
            Intent intent = null;
            int screen;
            int cellX;
            String modeSelect;
            int cellY;
            while (c.moveToNext()) {
                	ContentValues values = new ContentValues();
                    int itemType = c.getInt(itemTypeIndex);
                    screen=c.getInt(screenIndex);
                    cellX=c.getInt(cellXIndex);
                    cellY=c.getInt(cellYIndex);
                    modeSelect=c.getString(modeSelectIndex);
                    values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex)); 
                    values.put(LauncherSettings.Favorites.BACKGROUND, c.getString(backgroundIndex)); 
                    values.put(LauncherSettings.Favorites.SCREEN,screen);
                    values.put(LauncherSettings.Favorites.CELLX, cellX);
                    values.put(LauncherSettings.Favorites.CELLY, cellY);
                    values.put(LauncherSettings.Favorites.SPANX, c.getInt(spanXIndex));
                    values.put(LauncherSettings.Favorites.SPANY, c.getInt(spanYIndex));
                    values.put(LauncherSettings.Favorites.MODE, modeSelect);
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE, c.getString(aliasTitleIndex));
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,c.getString(aliasTitleBackgroundIndex));
                    values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,c.getString(cellDefImageIndex));
                    values.put(Favorites.ITEM_TYPE, itemType);
                    switch (itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (URISyntaxException e) {
                            continue;
                        }
                        LauncherLog.v(TAG, "loadBackupData,jeff ITEM_TYPE_CONTACTS intent="+intent);
					values.put(
							LauncherSettings.Favorites.CONTACTNAME,
							c.getString(c
									.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTACTNAME)));// hejianfeng add
					values.put(
							LauncherSettings.Favorites.PHONENUMBER,
							c.getString(c
									.getColumnIndexOrThrow(LauncherSettings.Favorites.PHONENUMBER)));// hejianfeng add
					values.put("packageclassName", intent.getComponent()
							.getPackageName()
							+ "/"
							+ intent.getComponent().getClassName());
                        backupList.put(screen+"/"+cellX+"/"+cellY+"/"+modeSelect, values);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (Exception e) {
                            intent = new Intent();
                            String[] s = intentDescription.split("/");
                            intent.setComponent(new ComponentName(s[0], s[1]));
                        }
                        LauncherLog.v(TAG, "loadBackupData,jeff ITEM_TYPE_APPWIDGET intent="+intent);
                        values.put("packageclassName", intent.getDataString());
                        backupList.put(screen+"/"+cellX+"/"+cellY+"/"+modeSelect, values);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        intentDescription = c.getString(intentIndex);
                        container = c.getInt(containerIndex);
                        if (container != -101) {
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                            } catch (URISyntaxException e) {
                                continue;
                            }
                        } else {
                            intent = null;
                        }
                        LauncherLog.v(TAG, "loadBackupData,jeff ITEM_TYPE_SHORTCUT intent="+intent);
                        values.put("packageclassName", intent.getComponent().getPackageName()+"/"+intent.getComponent().getClassName());
                        backupList.put(screen+"/"+cellX+"/"+cellY+"/"+modeSelect, values);
                        break;
                    }
            }
            ThemeManager.getInstance().setBackupList(backupList);
            backupList.clear();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    //hejianfeng add end
    private void loadWorkspace() {
        
        final Context context = mContext;
        final ContentResolver contentResolver = context.getContentResolver();
        mItemsContacts.clear();
        mItemsWidget.clear();
        mItemsShortcut.clear();

		Cursor c = null;
		c = contentResolver.query(
				LauncherSettings.Favorites.CONTENT_URI,
				null,
				" modeSelect = "
						+ ThemeManager.getInstance().getCurrentThemeType(
								mContext), null, null);
		if(c==null){
			return;
		}
        try {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int backgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.BACKGROUND);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int phoneNumberIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PHONENUMBER);
            final int contactsNameIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTACTNAME);
            final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
            final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);

            final int aliasTitleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE);
            final int cellDefImageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELL_DEF_IMAGE);
            final int aliasTitleBackgroundIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
            final int picUriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PIC_URI);
            
            final int modeSelectIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.MODE);

            //START: added by Yar @20170824
            final int rawIDIndex = c.getColumnIndexOrThrow("displayMode");
            //END: added by Yar @20170824
            String intentDescription;
            int container;
            long id;
            Intent intent = null;

            while (c.moveToNext()) {
                try {
                    int itemType = c.getInt(itemTypeIndex);

                    switch (itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_CONTACTS:
                        ItemContacts ic = new ItemContacts(mContext, mHandler);//modified by Yar @20170824
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (URISyntaxException e) {
                            continue;
                        }
                        ic.intent = intent;
                        ic.itemType = itemType;
                        ic.intent = intent;
                        ic.id = c.getLong(idIndex);
                        ic.background = c.getString(backgroundIndex);
                        ic.phoneNumber = c.getString(phoneNumberIndex);
                        ic.contactName = c.getString(contactsNameIndex);
                        ic.modeSelect = c.getString(modeSelectIndex);
                        //START: added by Yar @20170824
                        ic.mRawId = c.getInt(rawIDIndex);
                        //END: added by Yar @20170824

                        ic.mAliasTitle = c.getString(aliasTitleIndex);
                        ic.cellDefImage = c.getString(cellDefImageIndex);
                        ic.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);

                        if (c.getString(picUriIndex) != null && !"".equals(c.getString(picUriIndex))) {
                            ic.CPUri = Uri.parse(c.getString(picUriIndex));
                        }
                        ic.container = c.getInt(containerIndex);
                        ic.screen = c.getInt(screenIndex);
                        ic.cellX = c.getInt(cellXIndex);
                        ic.cellY = c.getInt(cellYIndex);
                        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                        	ic.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[ic.cellX][ic.cellY];
                        	ic.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[ic.cellX][ic.cellY];
                        }
                        ic.init();
                        mItemsContacts.add(ic);

                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        ItemWidget iw = new ItemWidget(mContext);
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (Exception e) {
                            intent = new Intent();
                            String[] s = intentDescription.split("/");
                            intent.setComponent(new ComponentName(s[0], s[1]));
                        }
                        id = c.getLong(idIndex);
                        iw.intent = intent;
                        iw.id = id;
                        iw.modeSelect = c.getString(modeSelectIndex);
                        iw.screen = c.getInt(screenIndex);
                        iw.cellX = c.getInt(cellXIndex);
                        iw.cellY = c.getInt(cellYIndex);
                        iw.spanX = c.getInt(spanXIndex);
                        iw.spanY = c.getInt(spanYIndex);
                        iw.background = c.getString(backgroundIndex);
                        iw.aliasTitle = c.getString(aliasTitleIndex);
                        iw.init();
						if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS) {
							if ((iw.screen == 1 &&iw.spanX>1) ||(iw.screen == 2 &&iw.spanX>1)||(iw.screen == 3 &&iw.spanX>1)) {
								iw.pieceBg = ThemeManager.getInstance()
										.getImagePiece().widgetBmp;
								iw.pieceHalfBg=ThemeManager.getInstance()
										.getImagePiece().widgetBmpBg;
							} else {
								iw.pieceBg = ThemeManager.getInstance()
										.getImagePiece().cellBitmaps[iw.cellX][iw.cellY];
								iw.pieceHalfBg = ThemeManager.getInstance()
										.getImagePiece().cellBitmapsBg[iw.cellX][iw.cellY];
							}
						}
                        mItemsWidget.add(iw);

                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        ItemShortcut is = new ItemShortcut(mContext);// mContext
                        intentDescription = c.getString(intentIndex);
                        container = c.getInt(containerIndex);
                        if (container != -101) {
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                            } catch (URISyntaxException e) {
                                continue;
                            }
                        } else {
                            intent = null;
                        }
                        is.intent = intent;
                        is.itemType = itemType;
                        is.intent = intent;
                        is.id = c.getLong(idIndex);
                        is.background = c.getString(backgroundIndex);
                        
                        is.modeSelect = c.getString(modeSelectIndex);

                        is.aliasTitle = c.getString(aliasTitleIndex);
                        is.aliasTitleBackground = c.getString(aliasTitleBackgroundIndex);

                        is.container = c.getInt(containerIndex);
                        is.screen = c.getInt(screenIndex);
                        is.cellX = c.getInt(cellXIndex);
                        is.cellY = c.getInt(cellYIndex);
                        
                        is.spanX = c.getInt(spanXIndex);
                        is.spanY = c.getInt(spanYIndex);

                        is.setThemeTools(mThemeTools);
                        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                        	is.pieceBg=ThemeManager.getInstance().getImagePiece().cellBitmaps[is.cellX][is.cellY];
                        	is.pieceHalfBg=ThemeManager.getInstance().getImagePiece().cellBitmapsBg[is.cellX][is.cellY];
                        }
                        is.init();
                        mItemsShortcut.add(is);
                        break;
                    }
                } catch (Exception e) {
                    Log.w("HHJ", "Desktop items loading interrupted:", e);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    
    Callbacks callbacks;
    
    public void setCallbacks(Callbacks callbacks){
        this.callbacks = callbacks;
    }
    
    Launcher mLauncher;
    public void setLauncher(Launcher launcheer){
        mLauncher = launcheer;
    }
    
	private void bindWorkspace() {
		final Callbacks oldCallbacks = mCallbacks.get();
		mHandler.post(new Runnable() {
			public void run() {
				LauncherLog.v(TAG,
						"第一次加载 mItemsShortcut:" + mItemsShortcut.size());
				Callbacks callbacks = tryGetCallbacks(oldCallbacks);
				if (callbacks != null) {
					callbacks.bindItemsShortcut(mItemsShortcut);
				}
			}
		});
		mHandler.post(new Runnable() {
			public void run() {
				LauncherLog.v(TAG, "mItemsContacts:" + mItemsContacts.size());
				Callbacks callbacks = tryGetCallbacks(oldCallbacks);
				if (callbacks != null) {
					callbacks.bindItemsContacts(mItemsContacts);
				}
			}
		});

		// once for the current screen
		mHandler.post(new Runnable() {
			public void run() {
				int N = mItemsWidget.size();
				LauncherLog.v(TAG, "bindWorkspace,jeff mItemsWidget.size()="
						+ N);
				try {
					for (int i = 0; i < N; i++) {
						ItemWidget widget = mItemsWidget.get(i);
						Callbacks callbacks = tryGetCallbacks(oldCallbacks);
						if (callbacks != null) {
							LauncherLog.v(TAG, "bindWorkspace,jeff widget="
									+ widget);
							callbacks.bindItemsWidget(widget);
						}
					}
				} catch (Exception e) {
					LauncherLog.v(TAG, "bindWorkspace,jeff Exception");
				}
				LauncherLog.v(TAG, "bindWorkspace,jeff finishBindingItems");
			}
		});
	}
    
    Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
        synchronized (mLock) {

            if (mCallbacks == null) {
                return null;
            }

            final Callbacks callbacks = mCallbacks.get();
            if (callbacks != oldCallbacks) {
                return null;
            }
            if (callbacks == null) {
                return null;
            }

            return callbacks;
        }
    }
    
    public interface Callbacks {
        /*下面三个是开始绑定的 是多个*/
    	/**开始加载所有控件*/
        public void bindAllItems(ArrayList<ItemShortcut> is,ArrayList<ItemContacts> ic,ArrayList<ItemWidget> iw);//hejianfeng add 
        /**开始加载绑定快捷方式*/
        public void bindItemsShortcut(ArrayList<ItemShortcut> is);
        /**开始加载绑定联系人*/
        public void bindItemsContacts(ArrayList<ItemContacts> ic);
        /**加载绑定自定义view*/
        public void bindItemsWidget(ItemWidget iw);
        /*下面三个是用于后续更新用的 是单一的*/
        public void bindItemShortcut(ItemShortcut is);
        public void bindItemContacts(ItemContacts ic);
        public void finishBindingItems();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.d("HHJ", "LauncherModel onReceive intent=" + intent);
        Log.d("DownloadService", "LauncherModel onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action) || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
        }else if("com.cappu.launcherwin.downloadapk.services.KookLocalService".equals(action)){
            if("downloadSucceed".equals(intent.getStringExtra("method"))){
                Log.d("HHJ", "++++++++++++++++++++++++文件下载完成+++++++++++++++++++++++++++");
                //ThemeRes.getInstance().addZipThemes(LauncherApplication.CappuDate+"themes.zip");
            }
        }else if (action.equals(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS)) {
            int pro = intent.getExtras().getInt("progress");
        }else if (action.equals(CellLyouatUtil.ACTION_DOWNLOAD_FAIL)) {
            Log.i("DownloadService", "下载失败");
        }else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni.getState() == State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i("DownloadService", "wifi 链接");
                
            } else if (ni.getState() == State.DISCONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i("DownloadService", "wifi 链接关闭");
            }
        }
    }
    
    public ContentValues readConfig() {
        CellLyouatUtil mCellLyouatUtil = mLauncherApplication.getCellLyouatUtil();
        Map<String,ContentValues> map = mCellLyouatUtil.getExpandConfig();
        return map.get("com.cappu.launcherwin/com.cappu.launcherwin.Launcher");
    }
    
    public static void startHeartbeatBroacast(Context context, long triggerAtTime, String action) {
        if(triggerAtTime == 0){
            triggerAtTime = System.currentTimeMillis()+25000;//开始启动后二十五秒检测一次
            Log.e("DownloadService", "开始启动后二十五秒检测一次 action:"+action);
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
    }
}
