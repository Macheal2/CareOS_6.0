
package com.cappu.launcherwin.netinfo;

import com.cappu.launcherwin.LauncherSettingActivity;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.downloadUI.DownloadCenter;
import com.cappu.launcherwin.downloadUI.celllayout.DownloadCellLayoutMainActivity;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.speech.SpeechTools;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.CappuDialog;
import com.cappu.launcherwin.widget.CappuDialogUI;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// add by y.haiyang for speech more (start)
import com.cappu.launcherwin.speech.LauncherSpeechTools;
// add by y.haiyang for speech more (end)

public class Card extends LinearLayout {

    Context mContext;
    Context mTContext;

    LayoutInflater mLayoutInflater = null;

    LinearLayout mLinearLayout;

    RelativeLayout titile_layout;
    
    ImageView mReadingButton;

    TextView date;

    ImageView icon;

    TextView title;

    BobyListView boby_list;

    List<BaseCard> mList = new ArrayList<BaseCard>();

    BobyAdapter mBobyAdapter;

    String NetType;

    String mAddress;
    
    BaseCard mTitleBaseCard;

    AsyncLoadImage asyncLoadImage;// = new AsyncLoadImage();

    boolean SpeechStatus = true;
    /**
     * private SpeechTools mSpeechTools = null;
     */
    private LauncherSpeechTools mSpeechTools;
    // modify by y.haiyang for speech more (end)

    public Card(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (mContext == null) {
            mContext = getContext();
        }

        if (mLayoutInflater == null) {
            mLayoutInflater = ((Activity) mContext).getLayoutInflater();
        }
        mLinearLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.card_item, null);
        mReadingButton = (ImageView) mLinearLayout.findViewById(R.id.reading_button);

        mReadingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SpeechStatus = getNetInfoStatus();
                String readingStr = "";
                String Prefix = "";
                for (int i = 0; i < mList.size(); i++) {
                    BaseCard baseCard =mList.get(i);
                    String rs = baseCard.title;

                    String book=getResources().getString(R.string.item_count);
                    Prefix=String.format(book,i+1);
                    rs = Prefix + rs;
                    Log.i("HHJ", "rs:"+rs);
                    readingStr += rs+";";
                }

                if(mSpeechTools.isSpeaking()){
                    mSpeechTools.stopSpeaking();
                }else{
                    mSpeechTools.startSpeech(readingStr,SpeechStatus);
                }

                Log.i("HHJ", "readingStr:"+readingStr);
            }
        });

        titile_layout = (RelativeLayout) mLinearLayout.findViewById(R.id.titile_layout);
        if(getResources().getBoolean(R.bool.card_mode)){
            titile_layout.setVisibility(View.VISIBLE);
        }else{
            titile_layout.setVisibility(View.GONE);
        }
        
        
        titile_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                openBrows(mContext, mTitleBaseCard,title.getText().toString());
            }
        });
        titile_layout.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                CappuDialog md = new CappuDialog(mContext,R.style.CappuDialog,mList.get(0),CappuDialog.CAPPU_DIALOG_TYPE_MAIN);
                md.show();
                return false;
            }
        });

        date = (TextView) mLinearLayout.findViewById(R.id.date);
        icon = (ImageView) mLinearLayout.findViewById(R.id.icon);
        title = (TextView) mLinearLayout.findViewById(R.id.title);
        boby_list = (BobyListView) mLinearLayout.findViewById(R.id.boby_list);

        asyncLoadImage = new AsyncLoadImage();
        mBobyAdapter = new BobyAdapter(mContext, mList, asyncLoadImage);
        boby_list.setAdapter(mBobyAdapter);
        boby_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                ViewHolder ViewHolder = (com.cappu.launcherwin.netinfo.Card.ViewHolder) arg1.getTag();
                openBrows(mContext, ViewHolder.baseCard,ViewHolder.baseCard.title);
            }
        });
        boby_list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ViewHolder ViewHolder = (com.cappu.launcherwin.netinfo.Card.ViewHolder) arg1.getTag();
                CappuDialog md = new CappuDialog(mContext,R.style.CappuDialog,ViewHolder.baseCard,CappuDialog.CAPPU_DIALOG_TYPE_MAIN);
                md.show();
                return true;
            }
        });

        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mLinearLayout, params);
    }


    public boolean getNetInfoStatus(){
        try {
            if(mTContext == null){
                mTContext = mContext.createPackageContext("com.cappu.launcherwin", Context.CONTEXT_IGNORE_SECURITY);
            }
            //int launcher_speech_status = KookSharedPreferences.getInt(mTContext, "netinfo_speech_status");
            int launcher_speech_status = Settings.Global.getInt(mContext.getContentResolver(), "netinfo_speech_status",mContext.getResources().getInteger(R.integer.netinfo_speech_status));
            return launcher_speech_status == 1?true:false;
        } catch (Exception e) {
            Log.i("HHJ", "1254 e:"+e.toString());
            return false;
        }
    }

    // modify by y.haiyang for speech more (start)
    /**
     * public void setDate(List<BaseCard> list, String botycard, String type,SpeechTools speechTools) {
     */
    public void setDate(List<BaseCard> list, String botycard, String type, LauncherSpeechTools speechTools) {
    // modify by y.haiyang for speech more (end)

        this.mSpeechTools = speechTools;
        mList.clear();
        this.mList = list;
        NetType = type;
        //date.setText(botycard);i99_year
        date.setText(botycard.substring(0, 4)+mContext.getString(R.string.i99_year)+botycard.substring(4, 6)+mContext.getString(R.string.i99_month)+botycard.substring(6,botycard.length())+mContext.getString(R.string.i99_date));
        mBobyAdapter.setDate(list);
        mBobyAdapter.notifyDataSetChanged();

        if(list.size() == 0){
            return;
        }
        if(getResources().getBoolean(R.bool.card_mode)){
            BaseCard baseCard = list.get(0);//list.size()-1
            mAddress = baseCard.address;
            mTitleBaseCard = baseCard;
            title.setText(baseCard.title);
            icon.setTag(baseCard.banner);
            
            Log.i("Card", "baseCard.getType():"+baseCard.flag);
            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){ //add by wangyang
                if(NetLookActivity.FINANCES_INDEX== baseCard.flag){
                    icon.setImageResource(R.drawable.application_finances_mode_5_banner);
                }else if(NetLookActivity.TRAVEL_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_travel_mode_5_banner);
                }else if(NetLookActivity.HEALTH_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_health_mode_5_banner);
                }else if(NetLookActivity.NEWS_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_news_mode_5_banner);
                }
            } else{
                if(NetLookActivity.FINANCES_INDEX== baseCard.flag){
                    icon.setImageResource(R.drawable.application_finances_mode_3_banner);
                }else if(NetLookActivity.TRAVEL_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_travel_mode_3_banner);
                }else if(NetLookActivity.HEALTH_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_health_mode_3_banner);
                }else if(NetLookActivity.NEWS_INDEX == baseCard.flag){
                    icon.setImageResource(R.drawable.application_news_mode_3_banner);
                }
            }
            
            Bitmap bitmap = asyncLoadImage.loadDrawable(icon,baseCard.banner, new AsyncLoadImage.ImageCallback() {
                @Override
                public void imageLoad(Bitmap image, String imageUrl) {/*
                    if (imageUrl.equals(icon.getTag())) {
                        if(image == null){
                            if ("health".equals(NetType)) {
                                icon.setImageResource(R.drawable.application_health_mode_3);
                            } else if ("news".equals(NetType)) {
                                icon.setImageResource(R.drawable.application_news_mode_3);
                            }
                            return;
                        }
                        icon.setImageBitmap(image);
                    }
                */}
            });

        }

        postInvalidate();
        //invalidate();
    }

    class BobyAdapter extends BaseAdapter {

        private List<BaseCard> mList;

        private Context mContext;

        LayoutInflater vi = null;

        ViewHolder holder;

        private AsyncLoadImage asyncLoadImage; // 异步加载图片的类

        public BobyAdapter(Context mContext, List<BaseCard> mCards, AsyncLoadImage asyncLoadImage) {
            this.mContext = mContext;
            this.mList = mCards;
            this.asyncLoadImage = asyncLoadImage;
        }

        public void setDate(List<BaseCard> mCards) {
            mList.clear();
            this.mList = mCards;
        }

        @Override
        public int getCount() {
            if(getResources().getBoolean(R.bool.card_mode)){
                return mList.size() - 1;
            }else{
                return mList.size();
            }
        }

        @Override
        public Object getItem(int Index) {
            return mList.get(Index);
        }

        @Override
        public long getItemId(int Index) {
            return Index;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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

            final BaseCard baseCard;//
            if(getResources().getBoolean(R.bool.card_mode)){
                baseCard = mList.get(position+1);
            }else{
                baseCard = mList.get(position);
            }

            holder.mIcon.setTag(baseCard.icon);
            //Log.i("hehangjun", "l baseCard:"+baseCard.getTitle()+"    baseCard:"+baseCard.getType());
            if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
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
            
            Bitmap bitmap = asyncLoadImage.loadDrawable(holder.mIcon,baseCard.icon, new AsyncLoadImage.ImageCallback() {
                @Override
                public void imageLoad(Bitmap image, String imageUrl) {
                    Log.i("hehangjun", "image is null baseCard:"+baseCard.title+"        (image == null):"+(image == null)+"                  imageUrl:"+imageUrl+"    holder.mIcon.getTag():"+holder.mIcon.getTag());
                    if (imageUrl.equals(holder.mIcon.getTag())) {
                        if(image == null){
                            return;
                        }
                        //holder.mIcon.setImageBitmap(image);
                    }
                }
            });


            holder.mTitle.setText(baseCard.title);
            holder.address = baseCard.address;
            holder.baseCard = baseCard;
/*            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openBrows(mContext, baseCard.getAddress());

                }
            });*/
            return convertView;
        }

    }

    static class ViewHolder {
        public ImageView mIcon;

        public TextView mTitle;

        public String address;

        public BaseCard baseCard;
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
}
