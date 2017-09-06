
package com.cappu.launcherwin;

import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.ThemeTools.IconSkin;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.characterSequence.tools.CharacterParser;
import com.cappu.launcherwin.characterSequence.tools.PinyinComparator;
import com.cappu.launcherwin.contacts.widget.ClearEditText;
import com.cappu.launcherwin.contacts.widget.SideBar;
import com.cappu.launcherwin.contacts.widget.SideBar.OnTouchingLetterChangedListener;
import com.cappu.launcherwin.tools.AppAliasesTools;
import com.cappu.launcherwin.widget.KookListView;
import com.cappu.launcherwin.widget.TopBar;
import com.cappu.launcherwin.widget.KookListView.onKybdsChangeListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllApps extends BasicActivity implements View.OnClickListener,OnItemClickListener{
    
    private static final String CAPPU_ACTION = "android.intent.cappu.LAUNCHER";
    private static final String TAG = "AllApps";
    
    private AppAdapter mAppAdapter;
    private KookListView mAllAppListView;
    
    private List<ResolveInfo> mApps;
    
    private PackageManager mPackageManager;
    
    private LoaderTask mLoaderTask;
    private long id;
    private int mTextSize = 34;;
    
    public class SortModel {

        private ResolveInfo resolveInfo;
        private String sortLetters; // 显示数据拼音的首字母
        private String name;
        
        public SortModel(ResolveInfo resolveInfo) {
            this.resolveInfo = resolveInfo;
            String name = (String) resolveInfo.loadLabel(mPackageManager);
            setName(name);
        }
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        
        public ResolveInfo getResolveInfo() {
            return resolveInfo;
        }

        public void setResolveInfo(ResolveInfo resolveInfo) {
            this.resolveInfo = resolveInfo;
        }

        public String getSortLetters() {
            return sortLetters;
        }
        public void setSortLetters(String sortLetters) {
            this.sortLetters = sortLetters;
        }
    }
    
    private class LoaderTask implements Runnable {
        @Override
        public void run() {
            
            if(mApps != null){
                /**去掉部分替换的启动界面*/
                List<ResolveInfo> sourceList = new ArrayList<ResolveInfo>();
                for (ResolveInfo ri:mApps) {
                    String packageName = ri.activityInfo.packageName;
                    String className = ri.activityInfo.name;
                    if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){//当前桌面是项目版本的时候将不显示launcher
						if ((className
								.equals("com.cappu.remote.parent.main.SettingActivity") && packageName
								.equals("com.cappu.remote.parent"))
								|| (className
										.equals("com.cappu.launcherwin.Launcher") && packageName
										.equals("com.cappu.launcherwin"))
								|| (packageName
										.equals("com.iflytek.speechsuite"))
								|| packageName
										.equals("com.cappu.halllockscreen")
								|| packageName.equals("com.cappu.collection")
								|| packageName.equals("com.magcomm.speech")) {
							continue;
						} else{
                            sourceList.add(ri);
                        }
                    }else{
                        sourceList.add(ri);
                    }
                }
                /**去掉部分替换的启动界面 end*/
                
                /**添加部分替换的启动界面*/
                Intent intent = new Intent(CAPPU_ACTION);
                List<ResolveInfo> addList = mPackageManager.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                //sourceList.addAll(addList);
                for (ResolveInfo ri:addList) {
                    String packageName = ri.activityInfo.packageName;
                    String className = ri.activityInfo.name;
                    if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){
                        Log.i(TAG, "BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER"+(!(className.equals("com.cappu.launcherwin.mms.ui.CareMmsNewEditActivity") && packageName.equals("com.cappu.launcherwin"))));
                        if(!(className.equals("com.cappu.launcherwin.mms.ui.ICareMmsMainActivity") && packageName.equals("com.cappu.launcherwin"))){
                            sourceList.add(ri);
                        }
                    }else{
                        Log.i(TAG, "BasicKEY.LAUNCHER_VERSION  is else ");
                        sourceList.add(ri);
                    }
                }
                /**添加部分替换的启动界面 end*/
                
                mSourceDateList.clear();
                for (int i = 0; i < sourceList.size(); i++) {
                    SortModel sortModel = new SortModel(sourceList.get(i));
                    String pinyin = characterParser.getSelling(sortModel.getName());
                    String sortString = pinyin.substring(0, 1).toUpperCase();
                    if (sortString.matches("[A-Z]")) {
                        sortModel.setSortLetters(sortString.toUpperCase());
                    } else {
                        sortModel.setSortLetters("#");
                    }
                    mSourceDateList.add(sortModel);
                }
            }
         // 根据a-z进行排序源数据
            if(mPinyinComparator == null){
                mPinyinComparator = new PinyinComparator();
            }
            
            Collections.sort(mSourceDateList, mPinyinComparator);
            mAppAdapter.updateListView(mSourceDateList);
        }
    }
    
    private class PinyinComparator implements Comparator<SortModel> {

        public int compare(SortModel o1, SortModel o2) {
            if ("@".equals(o1.getSortLetters()) || "#".equals(o2.getSortLetters())) {
                return -1;
            } else if (o1.getSortLetters().equals("#") || o2.getSortLetters().equals("@")) {
                return 1;
            } else {
                return o1.getSortLetters().compareTo(o2.getSortLetters());
            }
        }

    }
    
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> mSourceDateList = new ArrayList<SortModel>();
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator mPinyinComparator;
    private SideBar mSideBar;
    private ClearEditText mClearEditText;
    LauncherApplication mLauncherApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allapp);
        
        mLauncherApplication =  (LauncherApplication) getApplication();
        
        id = getIntent().getLongExtra("id", -1);
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPackageManager = getPackageManager();
        mApps = mPackageManager.queryIntentActivities(intent, 0);
        
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        
        init();
    }
    
    public void init() {
        mLoaderTask = new LoaderTask();
        mAllAppListView = (KookListView) findViewById(R.id.allapplist);
        mAllAppListView.setContext(this);
        mSideBar = (SideBar) findViewById(R.id.sidrbar);
        mSideBar.setContext(this);
        characterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
        mClearEditText = (ClearEditText) findViewById(R.id.kook_search);

        mAppAdapter = new AppAdapter(this, mSourceDateList, mPackageManager);

        // mListView.setDivider(null);
        mAllAppListView.setOnItemClickListener(this);

        // 设置右侧触摸监听
        mSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAppAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mAllAppListView.setSelection(position);
                }

            }
        });

        mAllAppListView.setAdapter(mAppAdapter);
        // 根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAllAppListView.setOnkbdStateListener(new onKybdsChangeListener() {

            public void onKeyBoardStateChange(int state) {
                switch (state) {
                case KookListView.KEYBOARD_STATE_HIDE:
                    mSideBar.setVisibility(View.VISIBLE);
                    break;
                case KookListView.KEYBOARD_STATE_SHOW:
                    mSideBar.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        });
    }
    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr){
        List<SortModel> filterDateList = new ArrayList<SortModel>();
        
        if(TextUtils.isEmpty(filterStr)){
            filterDateList = mSourceDateList;
        }else{
            filterDateList.clear();
            for(SortModel sortModel : mSourceDateList){
                String name = sortModel.getName();
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    filterDateList.add(sortModel);
                }
            }
        }
        
        // 根据a-z进行排序
        Collections.sort(filterDateList, mPinyinComparator);
        mAppAdapter.updateListView(filterDateList);
    }
    
    public static class AppComponentName {
        public AppComponentName(String packagename, String classname) {
            mPackageName = packagename;
            mClassName = classname;
        }
        String mPackageName;
        String mClassName;
    }
    
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mApps != null){
            mApps.clear();
            mApps = null;
        }
        if(mSourceDateList != null){
            mSourceDateList.clear();
            mSourceDateList = null;
        }
        if(mAppAdapter!=null){
            mAppAdapter.onDestroy();
        }
        mLoaderTask = null;
    }

    private class AppAdapter extends BaseAdapter {
        LayoutInflater vi = null;
        View mConvertView;
        ViewHolder holder;
        Bitmap mBitmap;
        Context context;
        List<SortModel> mApps;
        PackageManager mPackageManager;
        AppAdapter(Context context,List<SortModel> mApps,PackageManager mPackageManager){
            this.context = context;
            this.mApps =mApps;
            this.mPackageManager = mPackageManager;
        }
        
        @Override
        public int getCount() {
            return mApps.size();
        }

        @Override
        public View getItem(int position) {
            // TODO Auto-generated method stub
            return mConvertView;
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }
        
        public void onDestroy(){
            if(mApps != null){
                mApps.clear();
                mApps = null;
            }
        }
        
        /**
         * 当ListView数据发生变化时,调用此方法来更新ListView
         * 
         * @param list
         */
        public void updateListView(List<SortModel> list) {
            this.mApps = list;
            notifyDataSetChanged();
        }
        
        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mApps.get(i).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }

            return -1;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if (convertView == null) {
                vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.app_item, parent,false);
                holder = new ViewHolder();
                holder.mAppIcon = (ImageView) convertView.findViewById(R.id.appicon);
                holder.mAppNme = (TextView) convertView.findViewById(R.id.appname);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo mResolveInfo = mApps.get(position).getResolveInfo();
            String packageName = mResolveInfo.activityInfo.packageName;
            String className = mResolveInfo.activityInfo.name;
            //hejianfeng add start
            mBitmap=ThemeManager.getInstance().getAllAppsIcon(packageName+"/"+className,mResolveInfo.loadIcon(mPackageManager));
            if(mBitmap ==null){
            	mBitmap = ((BitmapDrawable) mResolveInfo.loadIcon(mPackageManager)).getBitmap();
            }
            //hejianfeng add end
            holder.mAppIcon.setImageBitmap(mBitmap);
            
            String as = AppAliasesTools.replaceAppAliases(context,packageName, className, (String) mResolveInfo.loadLabel(mPackageManager));
            holder.mAppNme.setText(as);
            holder.mAppNme.setTextSize(mTextSize);
            holder.resolveInfo = mResolveInfo;
            mConvertView = convertView;
            
            mBitmap = null;
            
            return convertView;
        }

    }
    static class ViewHolder{
        public ImageView mAppIcon;
        public TextView mAppNme;
        public ResolveInfo resolveInfo;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Intent data = new Intent();
        ResolveInfo resolveInfo = viewHolder.resolveInfo;
        ActivityInfo info = resolveInfo.activityInfo;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn;
        String packageName = info.packageName;//a.getString(R.styleable.Favorite_packageName);
        String className = info.name;//a.getString(R.styleable.Favorite_className);
        
        try {
            cn = new ComponentName(packageName, className);
            info = mPackageManager.getActivityInfo(cn, 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            String[] packages = mPackageManager.currentToCanonicalPackageNames(
                new String[] { packageName });
            cn = new ComponentName(packages[0], className);
            try {
                info = mPackageManager.getActivityInfo(cn, 0);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        
        ContentValues values = new ContentValues();
        
        values.put(LauncherSettings.Favorites.CONTAINER, 1);
        
        /**这样应该将替换应用的别名获取到列出来/由于时间原因后续来获取 继续做*/
        values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
        values.put(Favorites.INTENT, intent.toUri(0));
        values.put(Favorites.TITLE, resolveInfo.loadLabel(mPackageManager).toString());
        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
        
        restorationDatabase(this, id, values);
        data.putExtra("id", id);
        setResult(20, data);
        finish();
    }
    
    
    
    static void restorationDatabase(Context context,long id ,final ContentValues values) {

        final Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        final ContentResolver cr = context.getContentResolver();

        sWorker.post(new Runnable() {
                public void run() {
                    int index = cr.update(uri, values, null, null);
                    Log.i("hehangjun", "228  cr.update(uri, values, null, null) "+index);
                }
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        sWorker.post(mLoaderTask);
    }

    public void Hide() {
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            // 关闭输入法
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    private static final Handler sWorker = new Handler();
}
