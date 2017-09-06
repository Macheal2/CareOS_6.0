
package com.cappu.launcherwin.netinfo;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.LauncherProvider;
import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.netinfo.widget.NetDateDao;
import com.cappu.launcherwin.netinfo.widget.TabTopBar;
import com.cappu.launcherwin.speech.SpeechTools;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.TopBar;
//modify by even
import com.cappu.launcherwin.speech.SpeechSetting;
import android.os.Parcel;

// add by y.haiyang for speech more (start)
import com.cappu.launcherwin.speech.LauncherSpeechTools;
// add by y.haiyang for speech more (end)
import com.cappu.launcherwin.tools.DensityUtil;

public class NetLookActivity extends BasicActivity implements View.OnClickListener,OnTabChangeListener {
    
    String TAG = "NetLookActivity";
    
    /**新闻*/
    private static final String NEWS = "News";
    /**健康*/
    private static final String HEALTH = "Health";
    /**旅游*/
    private static final String TRAVEL = "Travel";
    /**财经*/
    private static final String FINANCES = "finances";

    //modify by even
    private static final String BAIDUMSSP = "more";
    //end

    /**新闻*/
    public static final int NEWS_INDEX =  6;
    public static final String STR_NEWS_INDEX =  "6";
    /**健康*/
    public static final int HEALTH_INDEX = 7;
    public static final String STR_HEALTH_INDEX = "7";
    /**旅游*/
    public static final int TRAVEL_INDEX = 8;
    public static final String STR_TRAVEL_INDEX = "8";
    /**财经*/
    public static final int FINANCES_INDEX = 9;
    public static final String STR_FINANCES_INDEX = "9";

    ImageButton mCancel;

    TextView mTitle;
    
    TabTopBar mTabTopBar;
    
    ImageButton mOption;
    
    protected TabHost mTabHost;

    //TextView about_info;

    CardAdapter mCardAdapter;
    
    List<Map<String, List<BaseCard>>> mCardsList = new ArrayList<Map<String, List<BaseCard>>>();
    LoaderTask mLoaderTask;
    
    Handler sWorker = new Handler();
    Calendar mDummyDate;
    ListView cardlist;
    TextView  net_tips;
    String dateStr;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    /**判断是新闻还是健康信息*/
    String type;
    
    String position;
    ArrayList<NetDateDao> mListNetDateDao;
    NetDateDao mNetDateDao;

    private NetDateDao mNDbaidumssp = new NetDateDao();//modify by even20170214
    
    private PopupWindow mPopupWindowMenu;
    private LinearLayout mLinearLayout;
    private Button mButton;
    
    private static final int EFFECT_BATTER = 6;
    private int mClickCount;
    long mFirstTime = 0;
    
    // modify by y.haiyang for speech more (start)
    /**
     * private SpeechTools mSpeechTools = null;
     */
    private LauncherSpeechTools mSpeechTools;
    // modify by y.haiyang for speech more (end)
    
    class SortByType implements Comparator {
        public int compare(Object o1, Object o2) {
            if(o1 == null || o2 == null){
                return 0;
            }
            NetDateDao nd1 = (NetDateDao) o1;
            NetDateDao nd2 = (NetDateDao) o2;
            
            Log.i(TAG, "nd1.type < nd2.type:"+(nd1.type < nd2.type)+"    "+nd1.type+"    "+nd2.type );
            if (nd1.type < nd2.type) {
                return -1;
            }else{
                return 0;
            }
        }
    }
    
    private class LoaderTask implements Runnable {
        private boolean mStopped;

        @Override
        public void run() {
            mCardsList.clear();
            loadContent();
            mCardAdapter.setData(mCardsList);
            if(mCardsList != null && mCardsList.size()>0){
                net_tips.setVisibility(View.GONE);
            }else{
                net_tips.setVisibility(View.VISIBLE);
            }
            mCardAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_info_layout);

        Calendar nowDate = Calendar.getInstance();
        mDummyDate = Calendar.getInstance();
        mDummyDate.setTimeZone(nowDate.getTimeZone());
        Date date = mDummyDate.getTime();
        
       dateStr = dateFormat.format(date);
        
        
        // modify by y.haiyang for speech more (start)
        /**
         * mSpeechTools = new SpeechTools(this);
         */
        mSpeechTools = ThemeRes.getInstance().getSpeechTools();
        // modify by y.haiyang for speech more (start)
        
        init();
        checkServer();
    }

    private void checkServer(){
        boolean isRunning = false;
    }
    
    private void init() {
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        
        mTabTopBar = (TabTopBar) findViewById(R.id.topbar);
        
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);
        
        
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        //mOption.setImageResource(R.drawable.favorites_more);
        mCancel.setOnClickListener(this);
        //mOption.setOnClickListener(this);
        
        mTitle.setOnClickListener(this);
        
        mLoaderTask = new LoaderTask();
        //sWorker.post(mLoaderTask);
        
        type = getIntent().getStringExtra("type");
        mNetDateDao = getIntent().getParcelableExtra("TYPE");
        mListNetDateDao = getIntent().getParcelableArrayListExtra("TYPES");

        //modify by even
        mNDbaidumssp.date = "";
        mNDbaidumssp.title = "";
        mNDbaidumssp.introduce = "";
        mNDbaidumssp.address = "";
        mNDbaidumssp.banner = "";
        mNDbaidumssp.icon = "";
        mNDbaidumssp.type = 99;//type for baidumssp
        //Log.i(TAG, "type:"+type);
        //Log.i(TAG, "NetDateDao:"+nd.toString());
        
        mListNetDateDao.add(mNDbaidumssp);
        //end

        position = getIntent().getStringExtra("position");
        
        Log.i(TAG, "                       mNetDateDao = "+(mNetDateDao == null)+"            mListNetDateDao.size = "+mListNetDateDao.size());
        
        if(mNetDateDao == null || mListNetDateDao == null){
            mTabTopBar.setTabWidget(false);
            if("Left".equals(position)){
                mTitle.setText(R.string.news);
            }else{
                mTitle.setText(R.string.travel);
            }
        }else{
            
            mTabTopBar.setTabWidget(true);
        }
        if(ThemeManager.getInstance().getCurrentThemeType(getApplicationContext()) == ThemeManager.THEME_CHINESESTYLE && mListNetDateDao != null){ // add by wangyang
            int initType = 0;
            if("Left".equals(position)){
                initType = 6;
            }else{
                initType = 8;
            }
            NetDateDao initNd = null;
            for(int a = 0; a<mListNetDateDao.size(); a++){
                NetDateDao nd = mListNetDateDao.get(a);
                if(nd.type == initType){
//                    initTab(nd);
                    initNd = mListNetDateDao.get(a);
                    break;
                }
            }
            if(initNd != null){
                initTab(initNd);
            } else{
                initTab((NetDateDao) getIntent().getParcelableExtra("TYPE"));
            }
        } else{
            initTab((NetDateDao) getIntent().getParcelableExtra("TYPE"));
        }
        
        if(mNetDateDao != null){
            Log.i(TAG, "mNetDateDao:"+mNetDateDao.toString());
        }
        
        
        cardlist = (ListView) findViewById(R.id.cardlist);
        mCardAdapter = new CardAdapter(this, mCardsList);
        cardlist.setAdapter(mCardAdapter);
        
        mLinearLayout =(LinearLayout) View.inflate(this, R.layout.button, null);
        
        net_tips = (TextView) findViewById(R.id.net_tips);
        
        mButton =(Button) mLinearLayout.findViewById(R.id.button);
        //modify by even start
        //mButton.setText(getString(R.string.help_notes));
        mButton.setText(getString(R.string.dialect_settings));        
        //end
        mButton.setOnClickListener(this);
        mPopupWindowMenu = new PopupWindow(mLinearLayout, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        
        mPopupWindowMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.i99_popup_bg));
        mPopupWindowMenu.setFocusable(true);
        mPopupWindowMenu.setAnimationStyle(R.style.menushow);
        mPopupWindowMenu.update();
        
        mLinearLayout.setOnKeyListener(new android.view.View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowMenu.isShowing())) {
                    //Log.i("HHJ", "129 setOnKeyListener");
                    mPopupWindowMenu.dismiss();  
                    return true;
                }
                return false;
            }
        });
    }
    
    private void initTab(NetDateDao nd) {
        if(mListNetDateDao == null){
            Log.i(TAG, "mListNetDateDao is null");
            return;
        }
        
        /*for (NetDateDao netDateDao:mListNetDateDao) {
            mTabHost.addTab(mTabHost.newTabSpec(getStringId(netDateDao.type)).setIndicator(onCreatTabView(netDateDao.type)).setContent(android.R.id.tabcontent));
        }*/
        Collections.sort(mListNetDateDao, new SortByType());
        if (mListNetDateDao.size() == 1) {
            for (NetDateDao netDateDao : mListNetDateDao) {
                mTabHost.addTab(mTabHost.newTabSpec(getStringId(netDateDao.type)).setIndicator(onCreatTabView(mListNetDateDao,netDateDao.type)).setContent(android.R.id.tabcontent));
                mTabHost.addTab(mTabHost.newTabSpec(getStringId(netDateDao.type)).setIndicator(onCreatTabView(mListNetDateDao,netDateDao.type)).setContent(android.R.id.tabcontent));
                //modify by even 20170210
                //添加“baidumssp”Tab到TabHost控件中
                //mTabHost.addTab(mTabHost.newTabSpec("searcher")
                //    .setIndicator(onCreatTabView(mListNetDateDao,netDateDao.type))
                //    .setContent(android.R.id.tabcontent));
                //end
            }
        }else{
            int tadid = 0;

            // 设置mCurrentTab为非-1,addtab时候不会进入setCurrentTab()
            try {
                Field idcurrent = mTabHost.getClass().getDeclaredField("mCurrentTab");
                idcurrent.setAccessible(true);
                idcurrent.setInt(mTabHost, -2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int i = 0;
            for (NetDateDao netDateDao:mListNetDateDao) {
                if(nd != null && netDateDao.type == nd.type){
                    tadid = i;
                }
                mTabHost.addTab(mTabHost.newTabSpec(getStringId(netDateDao.type)).setIndicator(onCreatTabView(mListNetDateDao,netDateDao.type)).setContent(android.R.id.tabcontent));
                i ++;
            }

            // 设置mCurrentTab与tadid不同，并且不能数组越界(0-2)，保证第一次进入tab的setCurrentTab()方法正常运行
            try {
                Field idcurrent = mTabHost.getClass().getDeclaredField("mCurrentTab");
                idcurrent.setAccessible(true);
                if (tadid == 0) {
                    idcurrent.setInt(mTabHost, 1);
                } else {
                    idcurrent.setInt(mTabHost, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mTabHost.setCurrentTab(tadid);
        }

    }
    
    
    /**
     * get current tab id though index
     * 
     * @param index
     * @return
     */
    private String getStringId(int index) {
        String tabStr = "";
        switch (index) {
            case NEWS_INDEX:
                tabStr = NEWS;
                break;
            case HEALTH_INDEX:
                tabStr = HEALTH;
                break;
            case TRAVEL_INDEX:
                tabStr = TRAVEL;
                break;
            case FINANCES_INDEX:
                tabStr = FINANCES;
                break;
            //modify by even
            case 99:
                tabStr = BAIDUMSSP;
                break;
            //end
            default:
                break;
        }
        return tabStr;
    }
    
    private int getIdString(String tag){
        int index;
        if(tag.equals(NEWS)){
            index = NEWS_INDEX;
        }else if(tag.equals(HEALTH)){
            index = HEALTH_INDEX;
        }else if(tag.equals(TRAVEL)){
            index = TRAVEL_INDEX;
        }else if(tag.equals(FINANCES)){
            index = FINANCES_INDEX;
        }else{
            index = 0;
        }
        return index;
    }
    
    @SuppressLint("ResourceAsColor")
    public View onCreatTabView(ArrayList<NetDateDao> listNetDateDao,int type){
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.leftMargin = 0;
        lp.rightMargin = 0; 
        if(listNetDateDao.size() == 1){
            if(ThemeManager.getInstance().getCurrentThemeType(this) == ThemeManager.THEME_CHINESESTYLE){
//                tv.setBackgroundResource(R.drawable.shape_beige_normal);
                tv.setBackgroundResource(R.drawable.care_topbar_normal);
            }else{
                tv.setBackgroundResource(R.drawable.care_topbar_normal);
            }
            
        }else{
           if(ThemeManager.getInstance().getCurrentThemeType(this) == ThemeManager.THEME_CHINESESTYLE){
//                tv.setBackgroundResource(R.drawable.titlebar_beige);
                tv.setBackgroundResource(R.drawable.titlebar);
            }else{
                tv.setBackgroundResource(R.drawable.titlebar);
            }
            
        }
        
        //tv.setPadding(0, DensityUtil.dip2px(this, 5), 0, DensityUtil.dip2px(this, 4));
        tv.setSingleLine();
        tv.setGravity(Gravity.CENTER);
        tv.setEllipsize(TruncateAt.MARQUEE);
        tv.setFocusable(true);
        tv.setTextSize(DensityUtil.dip2px(this, 16));
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setTextAppearance(this, android.R.attr.textAppearanceSmall);
        if(type == 1 || type == 6){
            tv.setText(R.string.news);
        }else if(type == 2 || type == 7){
            tv.setText(R.string.health);
        }else if(type == 3 || type == 8){
            tv.setText(R.string.travel);
        }else if(type == 4 || type == 9){
            tv.setText(R.string.finances);
        }else if(type == 99){//modify by even
            tv.setText(R.string.more_baidumssp);
        }
        tv.setLayoutParams(lp);
        return tv;
    }

    private void loadContent() {
        if(mNetDateDao == null){
            Log.i(TAG, "mNetDateDao is null");
            return ;
        }

        final ContentResolver contentResolver = getContentResolver();
        final Cursor c = contentResolver.query(Uri.parse("content://com.cappu.download/downloadText"),null,BaseCard.TEXT_FLAG + " = '"+mNetDateDao.type+"'", null, "date desc, _id desc");//date 字段  desc降序 asc升序
        
        Log.i(TAG, "loadContent:"+c.getCount()+"     mNetDateDao.type:"+mNetDateDao.type);
        
        List<BaseCard> deleteDate = new ArrayList<BaseCard>();
        try {
            final int idIndex = c.getColumnIndex(BaseCard.ID);
            final int pushIDIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_ID);
            final int flagIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_FLAG);
            final int dateIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_DATE);
            final int titleIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_TITLE);
            final int introduceIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_INTRODUCE);
            final int addressIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_SITE);
            final int iconIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_ICON_PATH);
            final int bannerIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_BANNER_PATH);
            final int favoritesIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_FAVORITES);
            
            String CurDate = null;
            
            int index = 0;
            while (c.moveToNext()) {
                int id = c.getInt(idIndex);
                int pushID = (int) c.getLong(pushIDIndex);
                int type = (int) c.getInt(flagIndex);
                String date = c.getString(dateIndex);
                String title = c.getString(titleIndex);
                String introduce = c.getString(introduceIndex);
                String address = c.getString(addressIndex);
                String icon = c.getString(iconIndex);
                String banner = c.getString(bannerIndex);
                String favorites = c.getString(favoritesIndex);
                BaseCard baseCard = new BaseCard(id,pushID, type, date, title, introduce, address, icon, banner, favorites);
                
                long day = Math.abs(getQuot(dateStr, date));
                
                if(index >= 500){//day >= 30 && (!"1".equals(favorites))
                    /*Log.i("HHJ", "当前记录超过三十天 为"+day+"天即将删除");
                    DeleteDatabase(NetLookActivity.this, baseCard);*/
                    //deleteDate.add(baseCard);
                    
                    deleteDate.add(baseCard);//将 以前的三十天删除改为五百条后的都删除
                }else{
                    //Log.i("HHJ", "193 当前记录与现在相隔"+day+"天");
                    getResult(baseCard);
                }
                
                index++;
            }
            /*
            String key = null;
            for (int i = 0; i < mCardsList.size(); i++) {
                Map<String, List<BaseCard>> maps = mCardsList.get(i);//new HashMap<String, List<BaseCard>>();
                Iterator<String> iterator = maps.keySet().iterator();
                while (iterator.hasNext()) {
                    key = iterator.next();
                    List<BaseCard> dd = maps.get(key);
                    Log.i("HHJ", "key  "+dd.size());
                }
            }
            Log.i("HHJ", "155  "+mCardsList.size());*/
        }catch(Exception e){
            Log.i("HHJ", "Exception e  "+e);
        }finally {
            if (c != null) {
                c.close();
            }
            
        }
        
/*        try {
            for (int i = 0; i < deleteDate.size(); i++) {
                BaseCard baseCard = deleteDate.get(i);
                if(baseCard.getFavorites() == null || "".equals(baseCard.getFavorites())){
                    DeleteDatabase(NetLookActivity.this, baseCard);
                }
            }
        } catch (Exception e) {
            Log.i("HHJ", "263 删除push Exception e  "+e);
        }*/
        
    }
    
/*    public void DeleteDatabase(Context context, final BaseCard baseCard) {
        final Uri uri = Uri.parse("content://" + NetBroadcastReceiver.AUTHORITY + "/" + LauncherProvider.TABLE_NETINFO);
        final ContentResolver cr = context.getContentResolver();
        int c = cr.delete(uri, "_id =" + baseCard.getId(), null);// and favorites != '" + 1 + "'"
        deletedrawable(baseCard.getIcon());
        deletedrawable(baseCard.getBanner());

        Log.i("HHJ", "删除 推送内容  id:" + c + "   baseCard：" + baseCard.toString());
    }*/
    
    /**删除*/
    public void deletedrawable(String imgpath) {
        File f=new File(imgpath);
        if(f.exists()){
            boolean b = f.delete();
            Log.i("HHJ", "删除图片 b:" + b + "   imgpath：" +imgpath);
        }
    }
    
    public static long getQuot(String time1, String time2) {
        long quot = 0;
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date1 = ft.parse(time1);
            Date date2 = ft.parse(time2);
            quot = date1.getTime() - date2.getTime();
            quot = quot / 1000 / 60 / 60 / 24;
            //Log.i("HHJ", "248 date1:"+date1+"   date2:"+date2+"  quot:"+quot);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("HHJ", "251 ParseException:"+e.toString());
        }
        return quot;
    }
    
    public void  updateListData(){
        if(mLoaderTask == null){
            mLoaderTask = new LoaderTask();
        }
        sWorker.post(mLoaderTask);
        
    }
    @Override
    protected void onResume() {

        //modify by even
        if(99 == mNetDateDao.type)
        {
            mTabHost.setCurrentTab(0);
            Log.i(TAG, "mNetDateDao.type:"+mNetDateDao.type);
        }
        //end
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpeechTools.stopSpeaking();
    }
    
    @Override
    protected void onDestroy() {
        mCardsList.clear();
//        mSpeechTools.onDestroy();
        super.onDestroy();
    }

    public void getResult(BaseCard baseCard) {
        Map<String, List<BaseCard>> map = null;
        List<BaseCard> ll = null;
     // 判断是不是已经添加到分组((标识是不是该日期)
        boolean isPush = false;
        for (int i = 0; i < mCardsList.size(); i++) {
            map = mCardsList.get(i);
            if (map.containsKey(baseCard.date)) {
                ll = map.get(baseCard.date);
                ll.add(baseCard);
                isPush = true;
            }
        }
        
        if (!isPush) {
            ll = new ArrayList<BaseCard>();
            map = new HashMap<String, List<BaseCard>>();
            ll.add(baseCard);
            map.put(baseCard.date, ll);
            mCardsList.add(map);
        }
    }
    
    /** 打开浏览器 */
    public static void openBrows(Context context, String address) {
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.addCategory(Intent.CATEGORY_BROWSABLE);
        intent2.setData(Uri.parse(address));
        context.startActivity(intent2);
    }

    /* 读取图片 */
    public static Bitmap loadBitmap(String imgpath) {
        return BitmapFactory.decodeFile(imgpath);
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		LauncherLog.v(TAG, "onActivityResult,jeff requestCode="+requestCode+",resultCode="+resultCode);
		if (requestCode == EFFECT_BATTER && mCardsList.isEmpty()) {
			finish();
		}
	}
    @Override
    public void onClick(View v) {
         if(v == mCancel){
             finish();
         }else if(mOption == v){
        	 startActivityForResult(new Intent(this, BaiduMSSPContentActivity.class),EFFECT_BATTER);//modify by even
         }else if(mButton == v){
             //modify by even start
             //startActivity(new Intent(this, HelpNotesActivity.class));
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            ComponentName cn = new ComponentName("com.magcomm.speech", "com.magcomm.speech.Settings");
            intent.setComponent(cn);
            startActivity(intent);
             //end
             mPopupWindowMenu.dismiss();
         }else if(mTitle == v){
             long second = System.currentTimeMillis();
             if (second - mFirstTime < 400) {
                 ++mClickCount;
             } else {
                 mClickCount = 1;
             }
             mFirstTime = second;
         }

    }

    class CardAdapter extends BaseAdapter {

        private Context mContext;

        private List<Map<String, List<BaseCard>>> mCardsList;
        
        //LayoutInflater mLayoutInflater = null;
        
        private Card mCard;

        public CardAdapter(Context mContext, List<Map<String, List<BaseCard>>> mCards) {
            this.mContext = mContext;
            this.mCardsList = mCards;
        }
        
        public void setData( List<Map<String, List<BaseCard>>> mCards){
            this.mCardsList = mCards;
        }

        @Override
        public int getCount() {
            return mCardsList.size();
        }

        @Override
        public Object getItem(int Index) {
            return mCardsList.get(Index);
        }

        @Override
        public long getItemId(int Index) {
            return Index;
        }

        @Override
        public View getView(int position, View mView, ViewGroup mParent) {
            /*Log.i("HHJ", "Main list:"+position);
            if(mLayoutInflater == null){
                mLayoutInflater = LayoutInflater.from(mContext);
            }*/
            
            /*Log.i("HHJ", "Main list (mView == null):"+(mView == null));
            if(mView == null){
                mCard = new Card(mContext);
                mView = mCard;
                mView.setTag(mCard);
            }else{
                mCard = (Card) mView.getTag();
            }*/
            
            mCard = new Card(mContext);
            String key = null;
            Map<String, List<BaseCard>> maps = mCardsList.get(position);//new HashMap<String, List<BaseCard>>();
            Iterator<String> iterator = maps.keySet().iterator();
            
            while (iterator.hasNext()) {
                key = iterator.next();
                List<BaseCard> baseCardList = maps.get(key);
                /*mCard = new Card(mContext);*/
                mCard.setDate(baseCardList,key,type,mSpeechTools);
                //mView = mCard;
            }
            return mCard;
        }
        
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Log.i("HHJ", "onMenuOpened:"+(mPopupWindowMenu != null));
        if (mPopupWindowMenu != null) {
            mPopupWindowMenu.showAtLocation(mButton, Gravity.BOTTOM, 0, 0);
        }
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.i("HHJ", "onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onTabChanged(String tag) {
        Log.i(TAG, "onTabChanged    tag:"+tag);
        mTabHost.setCurrentTabByTag(tag);
        if(mLoaderTask == null){
            mLoaderTask = new LoaderTask();
        }
        
        if(mNetDateDao != null && getStringId(mNetDateDao.type).equals(tag) && mCardsList.size() != 0){
            return;
        }
        //Log.i(TAG, "onTabChanged    tag:"+tag+"    mNetDateDao.type:"+mNetDateDao.type+"    "+getStringId(mNetDateDao.type)+"   mCardsList size "+mCardsList.size());
        
        for (NetDateDao netDateDao:mListNetDateDao) {
            if(tag.equals(NEWS) && netDateDao.type == 6){
                mNetDateDao = netDateDao;
            }else if(tag.equals(HEALTH)  && netDateDao.type == 7){
                mNetDateDao = netDateDao;
            }else if(tag.equals(TRAVEL)  && netDateDao.type == 8){
                mNetDateDao = netDateDao;
            }else if(tag.equals(FINANCES) && netDateDao.type == 9){
                mNetDateDao = netDateDao;
            }else if(tag.equals(BAIDUMSSP) && netDateDao.type == 99){//modify by even
                mNetDateDao = netDateDao;
                startActivityForResult(new Intent(this, BaiduMSSPContentActivity.class),EFFECT_BATTER);//modify by even
                return;
            }
        }
        sWorker.post(mLoaderTask);
        Log.i(TAG, "onTabChanged tag:"+(tag));
    }
    
    
    
}
