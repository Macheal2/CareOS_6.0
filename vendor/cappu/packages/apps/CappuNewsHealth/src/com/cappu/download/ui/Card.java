
package com.cappu.download.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cappu.download.R;
import com.cappu.download.ui.imageLoader.ImageLoader;
import com.cappu.download.ui.imageLoader.ImageLoader.Type;

public class Card extends LinearLayout {

    private static String TAG = "Card";
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

    ImageLoader mImageLoader;
    boolean SpeechStatus = true;
    /**
     * private SpeechTools mSpeechTools = null;
     */
    // modify by y.haiyang for speech more (end)

    public Card(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImageLoader = ImageLoader.getInstance(3, Type.LIFO);
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
                /*if (!APKInstallTools.checkApkInstall(mContext, "com.iflytek.speechcloud")) {
                    CappuDialogUI dialog = new CappuDialogUI(mContext, CappuDialogUI.DIALOG_STYLE_TWO_BUTTONS);
                    dialog.setTitle(R.string.i99_dialog_confirm_title);
                    dialog.setMessage(R.string.i99_dialog_confirm_restore_tip);
                    dialog.setPositiveButton(R.string.i99_dialog_right, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent it = new Intent("com.android.cappu.Dowanload");
                            LauncherApplication mLauncherApplication = (LauncherApplication) mContext.getApplicationContext();
                            CellLyouatUtil cu = mLauncherApplication.getCellLyouatUtil();
                            ContentValues cv = cu.getApplicationInfo("com.iflytek.speechcloud", "com.iflytek.speechcloud.wxapi.WXEntryActivity",0);
                            if(cv != null){
                                String url = cv.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
                                String appName = cv.getAsString(LauncherSettings.Favorites.APP_NAME_CN);
                                it.putExtra("downloadUrl",url);
                                it.putExtra("appName",appName);
                                mContext.startActivity(it);
                                dialog.dismiss();
                            }else{
                                Log.i(TAG, "1173 contentvalues is null");
                            }
                        }
                    });
                    dialog.show();
                    return;
                }*/
                String readingStr = "";
                String Prefix = "";
                for (int i = 0; i < mList.size(); i++) {
                    BaseCard baseCard =mList.get(i);
                    String rs = baseCard.getTitle();

                    /*String book=getResources().getString(R.string.item_count);
                    Prefix=String.format(book,i+1);
                    rs = Prefix + rs;
                    Log.i("HHJ", "rs:"+rs);*/
                    readingStr += rs+";";
                }


                Log.i(TAG, "readingStr:"+readingStr);
            }
        });

        titile_layout = (RelativeLayout) mLinearLayout.findViewById(R.id.titile_layout);
        if(true){
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
            public boolean onLongClick(View v) {/*
                CappuDialog md = new CappuDialog(mContext,R.style.CappuDialog,mList.get(0),CappuDialog.MAGCOMM_DIALOG_TYPE_MAIN);
                md.show();
                return false;
            */
                return false;
                }
        });

        date = (TextView) mLinearLayout.findViewById(R.id.date);
        icon = (ImageView) mLinearLayout.findViewById(R.id.icon);
        title = (TextView) mLinearLayout.findViewById(R.id.title);
        boby_list = (BobyListView) mLinearLayout.findViewById(R.id.boby_list);

        mBobyAdapter = new BobyAdapter(mContext, mList,mImageLoader);
        boby_list.setAdapter(mBobyAdapter);
        boby_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {/*
                // TODO Auto-generated method stub
                ViewHolder ViewHolder = (com.cappu.careoslauncher.push.ui.Card.ViewHolder) arg1.getTag();
                openBrows(mContext, ViewHolder.baseCard,ViewHolder.baseCard.getTitle());
            */}
        });
        boby_list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                /*ViewHolder ViewHolder = (com.cappu.careoslauncher.push.ui.Card.ViewHolder) arg1.getTag();
                CappuDialog md = new CappuDialog(mContext,R.style.CappuDialog,ViewHolder.baseCard,CappuDialog.MAGCOMM_DIALOG_TYPE_MAIN);
                md.show();*/
                return true;
            }
        });

        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mLinearLayout, params);
    }


    public boolean getNetInfoStatus(){
        try {
            if(mTContext == null){
                mTContext = mContext.createPackageContext("com.cappu.careoslauncher", Context.CONTEXT_IGNORE_SECURITY);
            }
            //int launcher_speech_status = KookSharedPreferences.getInt(mTContext, "netinfo_speech_status");
            //int launcher_speech_status = Settings.System.getInt(mContext.getContentResolver(), "netinfo_speech_status",mContext.getResources().getInteger(R.integer.netinfo_speech_status));
            //return launcher_speech_status == 1?true:false;
            
            return true;
        } catch (Exception e) {
            Log.i(TAG, "1254 e:"+e.toString());
            return false;
        }
    }

    // modify by y.haiyang for speech more (start)
    /**
     * public void setDate(List<BaseCard> list, String botycard, String type,SpeechTools speechTools) {
     */
    public void setDate(List<BaseCard> list, String botycard, String type) {
    // modify by y.haiyang for speech more (end)

        mList.clear();
        this.mList = list;
        NetType = type;
        //date.setText(botycard);i99_year
        date.setText(botycard.substring(0, 4)+"年"+botycard.substring(4, 6)+"月"+botycard.substring(6,botycard.length())+"日");
        mBobyAdapter.setDate(list);
        mBobyAdapter.notifyDataSetChanged();

        if(list.size() == 0){
            return;
        }
        if(true){
            BaseCard baseCard = list.get(0);//list.size()-1
            mAddress = baseCard.getAddress();
            mTitleBaseCard = baseCard;
            title.setText(baseCard.getTitle());
            icon.setTag(baseCard.getBanner());
            
            Log.i("Card", "baseCard.getType():"+baseCard.getFlag());
/*            if(NetLookActivity.FINANCES_INDEX== baseCard.getType()){
                icon.setImageResource(R.drawable.application_finances_mode_3_banner);
            }else if(NetLookActivity.TRAVEL_INDEX == baseCard.getType()){
                icon.setImageResource(R.drawable.application_travel_mode_3_banner);
            }else if(NetLookActivity.HEALTH_INDEX == baseCard.getType()){
                icon.setImageResource(R.drawable.application_health_mode_3_banner);
            }else if(NetLookActivity.NEWS_INDEX == baseCard.getType()){
                icon.setImageResource(R.drawable.application_news_mode_3_banner);
            }*/
            
            //Bitmap bitmap = asyncLoadImage.loadDrawable(icon,baseCard.getBanner(),baseCard.getTitle(),0);
            
            mImageLoader.loadImage(baseCard.getBanner(), icon, false);

            /*if (bitmap == null) {//这里去掉是为了防止进去的时候闪
                if(NetLookActivity.STR_FINANCES_INDEX.equals(baseCard.getType())){
                    icon.setImageResource(R.drawable.application_finances_mode_3_banner);
                }else if(NetLookActivity.STR_TRAVEL_INDEX.equals(baseCard.getType())){
                    icon.setImageResource(R.drawable.application_travel_mode_3_banner);
                }else if(NetLookActivity.STR_HEALTH_INDEX.equals(baseCard.getType())){
                    icon.setImageResource(R.drawable.application_health_mode_3_banner);
                }else if(NetLookActivity.STR_NEWS_INDEX.equals(baseCard.getType())){
                    icon.setImageResource(R.drawable.application_news_mode_3_banner);
                }
            } else {
                icon.setImageBitmap(bitmap);
            }*/
        }

        postInvalidate();
    }

    class BobyAdapter extends BaseAdapter {

        private List<BaseCard> mList;

        private Context mContext;

        LayoutInflater vi = null;

        ViewHolder holder;


        ImageLoader mImageLoader;
        public BobyAdapter(Context mContext, List<BaseCard> mCards,ImageLoader imageLoader) {
            this.mContext = mContext;
            this.mList = mCards;
            this.mImageLoader = imageLoader;
        }

        public void setDate(List<BaseCard> mCards) {
            mList.clear();
            this.mList = mCards;
        }

        @Override
        public int getCount() {
            if(true){
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
            if(true){
                baseCard = mList.get(position+1);
            }else{
                baseCard = mList.get(position);
            }

            holder.mIcon.setTag(baseCard.getIcon());
            //Log.i("hehangjun", "l baseCard:"+baseCard.getTitle()+"    baseCard:"+baseCard.getType());
            
/*            if(NetLookActivity.FINANCES_INDEX == baseCard.getType()){
                holder.mIcon.setImageResource(R.drawable.application_finances_icon);
            }else if(NetLookActivity.TRAVEL_INDEX == baseCard.getType()){
                holder.mIcon.setImageResource(R.drawable.application_travel_icon);
            }else if(NetLookActivity.HEALTH_INDEX == baseCard.getType()){
                holder.mIcon.setImageResource(R.drawable.application_health_icon);
            }else if(NetLookActivity.NEWS_INDEX == baseCard.getType()){
                holder.mIcon.setImageResource(R.drawable.application_news_icon);
            }*/
            
            //Bitmap bitmap = asyncLoadImage.loadDrawable(holder.mIcon,baseCard.getIcon(),baseCard.getTitle(),position);
            
            mImageLoader.loadImage(baseCard.getIcon(), holder.mIcon, false);

/*            if (bitmap == null) {
                if (NetLookActivity.STR_FINANCES_INDEX.equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_finances_icon);
                } else if (NetLookActivity.STR_TRAVEL_INDEX.equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_travel_icon);
                } else if (NetLookActivity.STR_HEALTH_INDEX.equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_health_icon);
                } else if (NetLookActivity.STR_NEWS_INDEX.equals(baseCard.getType())) {
                    holder.mIcon.setImageResource(R.drawable.application_news_icon);
                }
            } else {
                holder.mIcon.setImageBitmap(bitmap);
            }*/

            holder.mTitle.setText(baseCard.getTitle());
            holder.address = baseCard.getAddress();
            holder.baseCard = baseCard;
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
    public static void openBrows(Context context, BaseCard baseCard,String title) {/*
        try {
            Intent it = new Intent(context,BrowserActivity.class);
            it.setData(Uri.parse(baseCard.getAddress()));
            it.putExtra("title", title);
            it.putExtra("object", baseCard);
            context.startActivity(it);
        } catch (Exception e) {
            Log.i(TAG, "open browers exception address:"+baseCard.getAddress());
            Toast.makeText(context, context.getString(R.string.uri_error), Toast.LENGTH_LONG).show();
        }

    */}
}
