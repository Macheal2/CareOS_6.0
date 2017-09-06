
package com.cappu.launcherwin.netinfo;

import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.netinfo.Card.ViewHolder;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.CappuDialog;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends BasicActivity implements View.OnClickListener {
    int mTextSize = 34;
    CardAdapter mCardAdapter;
    List<BaseCard> mListBaseCard = new ArrayList<BaseCard>();
    
    LoaderTask mLoaderTask;
    
    Handler sWorker = new Handler();
    
    private AsyncLoadImage asyncLoadImage; // 异步加载图片的类
    
    ListView cardlist;
    
    TextView  net_tips;
    
    private class LoaderTask implements Runnable {
        private boolean mStopped;

        @Override
        public void run() {
            loadContent();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_info_layout);
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        init();
    }

    public void init() {
        asyncLoadImage = new AsyncLoadImage();
        mLoaderTask = new LoaderTask();
        sWorker.post(mLoaderTask);
        
        net_tips = (TextView) findViewById(R.id.net_tips);
        
        cardlist = (ListView) findViewById(R.id.cardlist);
        
        cardlist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                ViewHolder ViewHolder = (com.cappu.launcherwin.netinfo.Card.ViewHolder) arg1.getTag();
                openBrows(FavoritesActivity.this, ViewHolder.baseCard,ViewHolder.baseCard.title);
            }
        });
        
        cardlist.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ViewHolder ViewHolder = (com.cappu.launcherwin.netinfo.Card.ViewHolder) arg1.getTag();
                CappuDialog md = new CappuDialog(FavoritesActivity.this,R.style.CappuDialog,ViewHolder.baseCard,CappuDialog.CAPPU_DIALOG_TYPE_FAVORITES);
                md.show();
                return true;
            }
        });
        
        mCardAdapter = new CardAdapter(this, mListBaseCard);
        cardlist.setAdapter(mCardAdapter);
    }
    
    
    private void loadContent() {

        final ContentResolver contentResolver = getContentResolver();
        final Cursor c = contentResolver.query(Uri.parse( "content://com.cappu.download/downloadText"),null, " favorites = '1' ", null, "date desc");//date 字段  desc降序 asc升序

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
                mListBaseCard.add(baseCard);
            }
            mCardAdapter.notifyDataSetChanged();
            net_tips.setVisibility(View.GONE);
            Log.i("HHJ", "155  "+c.getCount());
        }catch(Exception e){
            Log.i("HHJ", "Exception e  "+e);
        }finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    public void  updateListData(){
        if(mListBaseCard != null){
            mListBaseCard.clear();
        }
        if(mLoaderTask == null){
            mLoaderTask = new LoaderTask();
        }
        sWorker.post(mLoaderTask);
        
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    

    /** 打开浏览器 */
    public static void openBrows(Context context, BaseCard baseCard,String title) {
        try {
            Intent it = new Intent(context,BrowserActivity.class);
            it.setData(Uri.parse(baseCard.address));
            it.putExtra("title", title);
            it.putExtra("object", baseCard);
            context.startActivity(it);
            
            
            /*Intent it = new Intent();
            it.setData(Uri.parse(address));
            it.setAction(Intent.ACTION_VIEW);
            context.startActivity(it);*/
        } catch (Exception e) {
            Log.i("HHJ", "open browers exception address:"+baseCard.address);
            Toast.makeText(context, context.getString(R.string.uri_error), Toast.LENGTH_LONG).show();
        }
        
    }

    /* 读取图片 */
    public static Bitmap loadBitmap(String imgpath) {
        return BitmapFactory.decodeFile(imgpath);
    }

    class CardAdapter extends BaseAdapter {

        private Context mContext;

        private List<BaseCard> mListBaseCard;
        
        LayoutInflater vi = null;
        
        ViewHolder holder;

        public CardAdapter(Context mContext, List<BaseCard> mListBaseCard) {
            this.mContext = mContext;
            this.mListBaseCard = mListBaseCard;
        }
        
        public void setData(List<BaseCard> mListBaseCard){
            this.mListBaseCard = mListBaseCard;
        }

        @Override
        public int getCount() {
            return mListBaseCard.size();
        }

        @Override
        public Object getItem(int Index) {
            return mListBaseCard.get(Index);
        }

        @Override
        public long getItemId(int Index) {
            return Index;
        }

        @Override
        public View getView(int Index, View convertView, ViewGroup parent) {
            if (convertView == null) {
                vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.card_item_list, parent, false);
                holder = new ViewHolder();
                holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BaseCard baseCard = mListBaseCard.get(Index);

            Log.i("HHJ", "baseCard:"+(baseCard.icon));
            
            Bitmap image = loadBitmap(baseCard.icon);
            if (image == null) {
                /*if ("1".equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_health_mode_3);
                } else if ("2".equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_news_mode_3);
                }*/
                if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                    if(NetLookActivity.FINANCES_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_finances_icon_5);
                    }else if(NetLookActivity.TRAVEL_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_travel_icon_5);
                    }else if(NetLookActivity.HEALTH_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_health_icon_5);
                    }else if(NetLookActivity.NEWS_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_news_icon_5);
                    }
                }else{
                    if(NetLookActivity.FINANCES_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_finances_icon);
                    }else if(NetLookActivity.TRAVEL_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_travel_icon);
                    }else if(NetLookActivity.HEALTH_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_health_icon);
                    }else if(NetLookActivity.NEWS_INDEX == baseCard.flag){
                        holder.mIcon.setImageResource(R.drawable.application_news_icon);
                    }
                }
            } else {
                holder.mIcon.setImageBitmap(image);
            }

            holder.mTitle.setText(baseCard.title);
            holder.address = baseCard.address;
            holder.baseCard = baseCard;
            return convertView;
        }
        
        /* 读取图片 */
        public Bitmap loadBitmap(String imgpath) {
            File f=new File(imgpath);
            if(!f.exists()){
                return null;
            }
            return BitmapFactory.decodeFile(imgpath);
        }
        
    }
}
