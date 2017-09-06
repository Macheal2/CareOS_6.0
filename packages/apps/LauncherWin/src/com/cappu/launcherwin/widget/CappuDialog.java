
package com.cappu.launcherwin.widget;

import java.io.File;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.launcherwin.LauncherProvider;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.netinfo.BaseCard;
import com.cappu.launcherwin.netinfo.FavoritesActivity;
import com.cappu.launcherwin.netinfo.NetLookActivity;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;



public class CappuDialog extends CareDialog implements View.OnClickListener{
    Context mContext;
    String AUTHORITY = "com.cappu.launcherwin";
    private Handler sWorker = new Handler();
    
   // private TextView title;
    private TextView favicons_tv;
    private LinearLayout favicons;
    private LinearLayout delete;
    private LinearLayout shar;
    private LinearLayout faviconslist;
    private BaseCard mBaseCard = null;
    
    /**收藏界面进入弹出的dialog*/
    public static final int CAPPU_DIALOG_TYPE_FAVORITES = 1;
    /**这个是推送列表界面进入弹出的dialog*/
    public static final int CAPPU_DIALOG_TYPE_MAIN = 2;
    
    private int type = -1;

    LayoutInflater mInflater;
    View mContentView;
    
    CareDialog.Builder mEBuilder;
    CareDialog mCareDialog;
    
    
  
    
    public CappuDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;
    }

    public CappuDialog(Context context, int theme) {
        super(context, theme);
    }
    
    public CappuDialog(Context context, int theme,BaseCard baseCard,int type) {
        super(context, theme);
        this.mContext = context;
        this.mBaseCard = baseCard;
        this.type = type;
        
        
        mEBuilder  = new CareDialog.Builder(context);
        mEBuilder.setView(onCreateContentView());
        mEBuilder.setTitle(mBaseCard.title);
        mCareDialog = mEBuilder.create();
    }

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setCanceledOnTouchOutside(false);
        mInflater = getLayoutInflater();
        mContentView = mInflater.inflate(R.layout.cappu_dialog, null);
        this.setContentView(mContentView);
        title = (TextView) mContentView.findViewById(R.id.title);
        favicons = (LinearLayout) mContentView.findViewById(R.id.favicons);
        delete = (LinearLayout) mContentView.findViewById(R.id.delete);
        shar = (LinearLayout) mContentView.findViewById(R.id.shar);
        favicons.setOnClickListener(this);
        delete.setOnClickListener(this);
        shar.setOnClickListener(this);
        title.setText(mBaseCard.getTitle());
        
        if(type == CAPPU_DIALOG_TYPE_FAVORITES){
            favicons.setVisibility(View.GONE);
        }else{
            favicons.setVisibility(View.VISIBLE);
        }
        
        favicons_tv  = (TextView) mContentView.findViewById(R.id.favicons_tv);
        
        if(mBaseCard.getFavorites() == null || "".equals(mBaseCard.getFavorites())){
            favicons_tv.setText(R.string.favicons);
        }else if(mBaseCard.getFavorites().equals("1")){
            favicons_tv.setText(R.string.favicons_cancle);
        }
    }*/
    
    public View onCreateContentView(){
        mInflater = getLayoutInflater();
        mContentView = mInflater.inflate(R.layout.cappu_dialog, null);
        //this.setContentView(mContentView);
        this.setView(mContentView);
        //title = (TextView) mContentView.findViewById(R.id.title);
        favicons = (LinearLayout) mContentView.findViewById(R.id.favicons);
        delete = (LinearLayout) mContentView.findViewById(R.id.delete);
        shar = (LinearLayout) mContentView.findViewById(R.id.shar);
        faviconslist  = (LinearLayout) mContentView.findViewById(R.id.faviconslist);
        favicons.setOnClickListener(this);
        delete.setOnClickListener(this);
        shar.setOnClickListener(this);
        faviconslist.setOnClickListener(this);
        //title.setText(mBaseCard.getTitle());
        
        if(type == CAPPU_DIALOG_TYPE_FAVORITES){
            favicons.setVisibility(View.GONE);
        }else{
            favicons.setVisibility(View.VISIBLE);
        }
        
        favicons_tv  = (TextView) mContentView.findViewById(R.id.favicons_tv);
        
        if(mBaseCard.favorites == null || "".equals(mBaseCard.favorites)){
            favicons_tv.setText(R.string.favicons);
        }else if(mBaseCard.favorites.equals("1")){
            favicons_tv.setText(R.string.favicons_cancle);
        }
        return mContentView;
    }
    
    @Override
    public void show() {
        mCareDialog.show();
    }

    public int[] getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = getContext().getResources().getDisplayMetrics();
        int[] size = { dm.widthPixels, dm.heightPixels };
        return size;
    }

    @Override
    public void onClick(View v) {
        if(v == delete){
            Log.i("HHJ", "DeleteDatabase mBaseCard.getFavorites():"+mBaseCard.favorites+"    "+("1".equals(mBaseCard.favorites))+"        "+(type != CAPPU_DIALOG_TYPE_FAVORITES));
            if("1".equals(mBaseCard.favorites) && type != CAPPU_DIALOG_TYPE_FAVORITES){
                String tips = "";
                if(mBaseCard.flag == NetLookActivity.NEWS_INDEX){
                    tips = String.format(mContext.getString(R.string.delete_net_tip), mContext.getString(R.string.news));
                }else if(mBaseCard.flag == NetLookActivity.HEALTH_INDEX){
                    tips = String.format(mContext.getString(R.string.delete_net_tip), mContext.getString(R.string.health));
                }else if(mBaseCard.flag == NetLookActivity.TRAVEL_INDEX){
                    tips = String.format(mContext.getString(R.string.delete_net_tip), mContext.getString(R.string.travel));
                }else if(mBaseCard.flag == NetLookActivity.FINANCES_INDEX){
                    tips = String.format(mContext.getString(R.string.delete_net_tip), mContext.getString(R.string.finances));
                }
                I99ThemeToast.toast(mContext,tips, "l", Color.parseColor("#FFFFFF"));
                
            }else{
                Log.i("HHJ", "DeleteDatabase 121:");
                DeleteDatabase(mContext, mBaseCard.id);
            }
        }else if(v == favicons){
            if(mBaseCard.favorites == null || "".equals(mBaseCard.favorites)){
                ContentValues cv = new ContentValues();
                cv.put("favorites", "1");
                updateAddFavoritesDatabase(mContext, mBaseCard.id, cv);
            }else if(mBaseCard.favorites.equals("1")){
                ContentValues cv = new ContentValues();
                cv.put("favorites", "");
                updateCancelFavoritesDatabase(mContext, mBaseCard.id, cv);
            }
        }else if(v == shar){
            new CappuShareDialog(mContext, mBaseCard/*mBaseCard.getAddress()*/).show();
        }else if(v == faviconslist){ //modify by even
            //new CappuShareDialog(mContext, mBaseCard/*mBaseCard.getAddress()*/).show();
            //startActivity(new Intent(this, FavoritesActivity.class));
            Intent intent = new Intent(); 
            intent.setClass(mContext, FavoritesActivity.class);
            mContext.startActivity(intent);

        }
        
        mCareDialog.dismiss();
    }
    
    
    public void DeleteDatabase(final Context context,final int id) {
        final Uri uri = Uri.parse("content://com.cappu.download/downloadText/");
        final ContentResolver cr = context.getContentResolver();
        sWorker.post(new Runnable() {
            public void run() {
                int c = cr.delete(uri, "_id = "+id, null);
                Log.i("HHJ", "DeleteDatabase  c:"+c+"   id:"+id);
                if(c == 1){
                    I99ThemeToast.toast(context, context.getString(R.string.delete_success), "l", Color.parseColor("#FFFFFF"));
                    deletedrawable(mBaseCard.icon);
                    deletedrawable(mBaseCard.banner);
                    dismiss();
                }
                
            }
        });
    }
    
    /**删除*/
    public void deletedrawable(String imgpath) {
        File f=new File(imgpath);
        if(f.exists()){
            boolean b = f.delete();
            Log.i("HHJ", "删除图片 b:" + b + "   imgpath：" +imgpath);
        }
    }
    
    public void updateAddFavoritesDatabase(final Context context,int id ,final ContentValues values) {
        /*
        static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + LauncherProvider.TABLE_FAVORITES + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        */

        
        final Uri uri = ContentUris.withAppendedId(Uri.parse("content://com.cappu.download/downloadText/"), id);
        final ContentResolver cr = context.getContentResolver();
        sWorker.post(new Runnable() {
            public void run() {
                int index = cr.update(uri, values, null, null);
                Log.i("HHJ", "updateAddFavoritesDatabase  index:"+index+"    uri:"+uri.toString());
                if(index == 1){
                    I99ThemeToast.toast(context, context.getString(R.string.favorites_success), "l", Color.parseColor("#FFFFFF"));
                    dismiss();
                }
                
            }
        });
    }
    
    public void updateCancelFavoritesDatabase(final Context context,int id ,final ContentValues values) {

        final Uri uri = Uri.parse("content://com.cappu.download/downloadText/"+id);
        final ContentResolver cr = context.getContentResolver();
        sWorker.post(new Runnable() {
            public void run() {
                int index = cr.update(uri, values, null, null);
                if(index == 1){
                    I99ThemeToast.toast(context, context.getString(R.string.favicons_cancle_success), "l", Color.parseColor("#FFFFFF"));
                    dismiss();
                }
                
            }
        });
    }
    
    @Override
    public void dismiss() {
        if(type == 1){
            ((FavoritesActivity) mContext).updateListData();
        }else{
            ((NetLookActivity) mContext).updateListData();
        }
        
        super.dismiss();
    }
}
