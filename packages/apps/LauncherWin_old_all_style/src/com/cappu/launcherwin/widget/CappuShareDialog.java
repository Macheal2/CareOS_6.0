package com.cappu.launcherwin.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.netinfo.BaseCard;
import com.cappu.launcherwin.tools.DensityUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;


public class CappuShareDialog extends Dialog implements AdapterView.OnItemClickListener {
    
    String TAG = "AndroidShare";
    
    private LinearLayout mLayout;
    private ListView mListView;
    private float mDensity;
    BaseCard mBaseCard;
    private int mScreenOrientation;
    private List<ShareItem> mListData;
    
    /**过滤掉分享的类名*/
    List<String> filterPacknameClassname = new ArrayList<String>();
    
    PackageManager mPackageManager;
    Context mContext;
    
    String WX_APP_ID = "wx312a5d621aceeace";
    private IWXAPI wxApi;

    public CappuShareDialog(Context context) {
        super(context, R.style.shareDialogTheme);
    }
    public CappuShareDialog(Context context, int theme, String msgText) {
        super(context, theme);
        mPackageManager = context.getPackageManager();
    }

    public CappuShareDialog(Context context, BaseCard baseCard) {
        super(context, R.style.shareDialogTheme);
        this.mBaseCard = baseCard;
        mPackageManager = context.getPackageManager();
        mContext = context;
      //实例化  
        wxApi = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        wxApi.registerApp(WX_APP_ID);
    }

    void init(Context context) {
        filterPacknameClassname.add("com.tencent.mm.ui.tools.ShareToTimeLineUI");
        filterPacknameClassname.add("com.tencent.mm.ui.tools.ShareImgUI");
        filterPacknameClassname.add("com.android.mms.ui.ComposeMessageActivity");
        filterPacknameClassname.add("com.tencent.mobileqq.activity.qfileJumpActivity");
        filterPacknameClassname.add("com.tencent.mobileqq.activity.JumpActivity");
        
        
        
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        this.mDensity = dm.density;
        this.mListData = new ArrayList<ShareItem>();
        
        Intent intent = new Intent(Intent.ACTION_SEND, null);  
        intent.addCategory(Intent.CATEGORY_DEFAULT);  
        intent.setType("*/*");
        List<ResolveInfo> addList = mPackageManager.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        Log.i(TAG, "addList:"+addList.size());
        for (ResolveInfo ri:addList) {
            String packageName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            Log.i(TAG, "packageName:"+packageName+"    className:"+className+"    "+ri.loadLabel(mPackageManager));
            
            /**包名不为空 而且过滤分享都不加入*/
            if(!packageName.isEmpty() && filterPacknameClassname.contains(className)){
                this.mListData.add(new ShareItem((String)ri.loadLabel(mPackageManager), ri.loadIcon(mPackageManager),className, packageName));
            }
            
        }

        this.mLayout = new LinearLayout(context);
        this.mLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        //params.leftMargin = ((int) (10.0F * this.mDensity));
        //params.rightMargin = ((int) (10.0F * this.mDensity));
        this.mLayout.setLayoutParams(params);
        this.mLayout.setBackgroundColor(Color.parseColor("#D9DEDF"));
        
        this.mListView = new ListView(context);
        this.mListView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        this.mLayout.addView(this.mListView);
    }


    public boolean isAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        init(context);
        setContentView(this.mLayout);

        getWindow().setGravity(80);
        
        this.mListView.setAdapter(new SharAdapter());
        this.mListView.setOnItemClickListener(this);
    }

    public void show() {
        super.show();
    }

    @SuppressLint("NewApi")
    public int getScreenOrientation() {
        int landscape = 0;
        int portrait = 1;
        Point pt = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(pt);
        int width = pt.x;
        int height = pt.y;
        return width > height ? portrait : landscape;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ShareItem share = (ShareItem) this.mListData.get(position);
        if(share.packageName.equals("com.tencent.mm") && share.activityName.equals("com.tencent.mm.ui.tools.ShareImgUI")){
            wechatShare(mBaseCard,0);
        }else if (share.packageName.equals("com.tencent.mm") && share.activityName.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
            wechatShare(mBaseCard,1);
        }else{
            shareMsg(getContext(),this.mBaseCard, share);
        }
        
        dismiss();
    }

    @SuppressLint("NewApi")
    private void shareMsg(Context context, BaseCard baseCard, ShareItem share) {

        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");

        // intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, baseCard.address);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(share.packageName, share.activityName));
        context.startActivity(intent);
    }

    private final class SharAdapter extends BaseAdapter {
        private static final int iv_id = 0x7f030061;
        private static final int tv_id = 0x7f030062;

        public int getCount() {
            return mListData.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0L;
        }

        private View getItemView() {
            RelativeLayout  relativeLayout = new RelativeLayout(getContext());
            int padding = (int) (10.0F * mDensity);
            relativeLayout.setPadding(padding, padding, padding, padding);
            relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            ImageView iv = new ImageView(getContext());
            relativeLayout.addView(iv);
            int wh = DensityUtil.dip2px(getContext(),60);
            RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(wh,wh);
            ivParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ivParams.rightMargin = padding;
            ivParams.addRule(RelativeLayout.CENTER_VERTICAL);
            iv.setLayoutParams(ivParams);
            iv.setId(iv_id);

            TextView tv = new TextView(getContext());
            RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            tvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tvParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvParams.topMargin = ((int) (5.0F * mDensity));
            tv.setLayoutParams(tvParams);
            tv.setTextColor(Color.parseColor("#212121"));
            tv.setTextSize(16.0F);
            tv.setId(tv_id);
            relativeLayout.addView(tv);

            return relativeLayout;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getItemView();
            }
            ImageView iv = (ImageView) convertView.findViewById(iv_id);
            TextView tv = (TextView) convertView.findViewById(tv_id);
            ShareItem item = (ShareItem) mListData.get(position);
            iv.setImageDrawable(item.icon);
            tv.setText(item.title);
            return convertView;
        }
    }

    private class ShareItem {
        String title;
        Drawable icon;
        String activityName;
        String packageName;

        public ShareItem(String title, Drawable icon, String activityName, String packageName) {
            this.title = title;
            this.icon = icon;
            this.activityName = activityName;
            this.packageName = packageName;
        }
    }
    
    private void wechatShare(BaseCard mBaseCard,int flag){  
        WXWebpageObject webpage = new WXWebpageObject();  
        webpage.webpageUrl = mBaseCard.address/*"这里填写链接url"*/;  
        WXMediaMessage msg = new WXMediaMessage(webpage);  
        msg.title = mBaseCard.title/*"这里填写标题"*/;  
        msg.description = mBaseCard.title/*"这里填写内容"*/;  
        //这里替换一张自己工程里的图片资源  
        Bitmap thumb = BitmapFactory.decodeFile(mBaseCard.icon);//BitmapFactory.decodeResource(mContext.getResources(), R.drawable.automatic_reading_pressed);
        msg.setThumbImage(thumb);  
          
        SendMessageToWX.Req req = new SendMessageToWX.Req();  
        req.transaction = String.valueOf(System.currentTimeMillis());  
        req.message = msg;  
        req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;  
        wxApi.sendReq(req);  
    } 
}
