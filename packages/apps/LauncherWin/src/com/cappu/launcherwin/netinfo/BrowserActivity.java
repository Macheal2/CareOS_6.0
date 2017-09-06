package com.cappu.launcherwin.netinfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.applicationList.activity.AllAppActivity;
import com.cappu.launcherwin.applicationList.activity.AppUninstallActivity;
import com.cappu.launcherwin.applicationList.activity.GooduseActivity;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.downloadUI.DownloadCenter;
import com.cappu.launcherwin.downloadUI.celllayout.DownloadCellLayoutMainActivity;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.speech.SpeechTools;
import com.cappu.launcherwin.speech.SpeechTools.SpeakerListener;
import com.cappu.launcherwin.widget.I99ThemeToast;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.LoadingDialog;
import com.cappu.launcherwin.widget.CappuDialogUI;
import com.cappu.launcherwin.widget.CappuShareDialog;
import com.cappu.launcherwin.widget.TopBar;

import android.webkit.WebSettings.RenderPriority;

// add by y.haiyang for speech more (start)
import com.cappu.launcherwin.speech.LauncherSpeechTools;
import com.tencent.mm.sdk.openapi.IWXAPI;
// add by y.haiyang for speech more (end)
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class BrowserActivity extends BasicActivity implements SpeakerListener {
    
    String TAG = "BrowserActivity";
    
    BaseCard mBaseCard;
    
    WebView mWebView;
    private LoadingDialog mLoadingDialog;
    WebSettings mWebSettings;
    private WakeLock mWakeLock;
    String mUrl;
    
    private RelativeLayout mWebCollection;
    private RelativeLayout mWebShare;
    private ImageView mWebCollectionImage;
    private ImageView mWebShareImage;

    private static final int MENU_INDEX = 1;
    
    // add by y.haiyang for speech more (start)
    /**
     * private SpeechTools mSpeechTools = null;
     */
    private LauncherSpeechTools mSpeechTools;
    // modify by y.haiyang for speech more (end)
    
    private String mCurrentHtmlPath = null;
    
    List<BaseCard> mListBaseCard = new ArrayList<BaseCard>();
    Handler sWorker = new Handler();
    
    private int mFavicons = 0;
    private int mId;
    private Context mContext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_browser);
        mContext = getApplicationContext();
        // modify by y.haiyang for speech more (start)
        /**
         * mSpeechTools = new SpeechTools(getApplicationContext());
         */
        mSpeechTools = ThemeRes.getInstance().getSpeechTools();
        // modify by y.haiyang for speech more (start)
        
        mSpeechTools.setSpeakerListener(this);
        
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mOption.setVisibility(View.VISIBLE);
        mCancel.setOnClickListener(this);
        mOption.setOnClickListener(this);
        mTitle.setText(getIntent().getStringExtra("title"));
        mOption.setImageResource(R.drawable.automatic_reading_button_white);
        mBaseCard = (BaseCard) getIntent().getSerializableExtra("object");
        mLoadingDialog = new LoadingDialog(BrowserActivity.this);
        init();
        final Activity activity = this;
        mWebView = (WebView) findViewById(R.id.net_browser);
        mWebSettings =  mWebView.getSettings();
        mWebSettings.setSupportZoom(true);
        //WebView启用Javascript脚本执行
        mWebSettings.setJavaScriptEnabled(true);// 设置支持javascript的例子
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.setWebViewClient(new KookWebViewClient());
        // 映射Java对象到一个名为”local_obj“的Javascript对象上
        // JavaScript中可以通过"window.local_obj"来调用Java对象的方法
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");

        mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
        mWebView.getSettings().setBlockNetworkImage(true); // 先加载文字 后加载图片
                                                            // 下面还有一个setBlockNetworkImage（false）

        mUrl = getIntent().getData().toString();

        Log.i(TAG, "mUrl:" + mUrl);

        int index = mUrl.lastIndexOf("/");
        String mFileName = mUrl.substring(index, mUrl.length());
        File saveFile = new File(Environment.getExternalStorageDirectory()    .getPath() + "/.icon/html" + mFileName);
        
        Log.i(TAG, "读取html:" + "file://"    +saveFile.exists() +"    saveFile:"+saveFile.length());
        mCurrentHtmlPath = Environment.getExternalStorageDirectory().getPath() + "/.icon/html" + mFileName;
        if (saveFile.exists() && saveFile.length() > 0) {// saveFile.exists() &&// saveFile.length()    // > 0
            mWebView.loadUrl("file://" + mCurrentHtmlPath);
        } else {
            mWebView.loadUrl(mUrl);
        }
        
    }
    
  //add by wangyang 2016.9.21
    private void init(){
//        Log.i(TAG, "                 mBaseCard = "+mBaseCard.toString());
        mId = mBaseCard.id;
        mWebCollection = (RelativeLayout)findViewById(R.id.web_collection);
        mWebShare = (RelativeLayout)findViewById(R.id.web_share);
        mWebCollectionImage = (ImageView)findViewById(R.id.web_collection_image);
        mWebShareImage = (ImageView)findViewById(R.id.web_share_image);
        mWebCollection.setOnClickListener(this);
        mWebShare.setOnClickListener(this);
        
        LoaderTask mLoaderTask = new LoaderTask();
        sWorker.post(mLoaderTask);
    }
    
    private class LoaderTask implements Runnable {
        private boolean mStopped;

        @Override
        public void run() {
            loadContent();
        }
    }
    
    private void loadContent() {

        final ContentResolver contentResolver = getContentResolver();
        final Cursor c = contentResolver.query(Uri.parse( "content://com.cappu.download/downloadText"),null, " favorites = '1' ", null, "date desc");//date 字段  desc降序 asc升序

        try {
            final int addressIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_SITE);
            
            while (c.moveToNext()) {
                String address = c.getString(addressIndex);
                Log.i("hehehehe", "        address                   address = "+address +"                           mUrl == address  "+(mUrl.equals(address)));
                if(mUrl.equals(address)){
                    mWebCollectionImage.setImageResource(R.drawable.news_collection_pass);
                    mFavicons = 1;
                    break;
                }
            }
        }catch(Exception e){
            Log.i("wangyang", "Exception e  "+e);
        }finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    /**
     * @JavascriptInterface   在android 4.4以后如果不加这个将播放不出来
     * */
    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            //saveFile(html);
            Log.i(TAG, "+++++++++++++++:"+html);
            if (html != null) {
                HTML = html;
            }
        }
    }

    public void onClick(View v) {
        if (v == mCancel) {
            finish();
        } else if (v.getId() == TopBar.RIGHT_ID) {

            if (TextUtils.isEmpty(HTML)) {
                HTML = readTextFile(mCurrentHtmlPath, "utf-8");
            }

            Log.d(TAG, "1 onClick:" + " HTML:"+(TextUtils.isEmpty(HTML)));
            
            String str = null;
            if (!TextUtils.isEmpty(HTML)) {
                str = Resolve(HTML);
            } else {
                I99ThemeToast.toast(getApplicationContext(), "网页未加载完...请稍等",
                        "l", Color.parseColor("#FFFFFF"));
            }
            Log.d(TAG, "onClick:" + str);
            if (mSpeechTools != null && str != null) {
                if (mSpeechTools.isSpeaking()) {
                    mSpeechTools.stopSpeaking();
                } else {
                    mSpeechTools.startSpeech(str, true);
                    keepScreenOn(this, true);
                }

            }

        }
        if(v == mWebShare){//add by wangyang 2016.9.22
            showPopupWindow(v);
        }
        if(v == mWebCollection){//add by wangyang 2016.9.21
            Log.i(TAG, "        mWebCollectionImage           mWebCollectionImage     ");
            if(mFavicons == 0){
                mFavicons =1;
                ContentValues cv = new ContentValues();
                cv.put("favorites", "1");
                updateAddFavoritesDatabase(mContext, mId, cv,"1");
            } else{
                mFavicons = 0;
                ContentValues cv = new ContentValues();
                cv.put("favorites", "");
                updateAddFavoritesDatabase(mContext, mId, cv,"");
            }
        }
        if(v == contentView){
            Log.i(TAG, "    web_share_1                  web_share_1   ");
        }
        if(v == mWebShare1){
            if(mListDataSec.size()>1){
                ShareItem share = mListDataSec.get(0);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==1){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mWebShare2){
            
            if(mListDataSec.size()>2){
                ShareItem share = mListDataSec.get(1);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==2){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mWebShare3){
            if(mListDataSec.size()>3){
                ShareItem share = mListDataSec.get(2);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==3){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mWebShare4){
            if(mListDataSec.size()>4){
                ShareItem share = mListDataSec.get(3);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==4){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mWebShare5){
            if(mListDataSec.size()>5){
                ShareItem share = mListDataSec.get(4);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==5){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mWebShare6){
            if(mListDataSec.size()>6){
                ShareItem share = mListDataSec.get(5);
                if(share.className.equals("com.tencent.mm.ui.tools.ShareImgUI")){
                    wechatShare(mBaseCard,0);
                } else if(share.className.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
                    wechatShare(mBaseCard,1);
                }else{
                    shareMsg(mContext,this.mBaseCard, share);
                }
            }
            if(mListDataSec.size() ==6){
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);  
              clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mBaseCard.address));
            }
            mPopupWindow.dismiss();
        }
        if(v == mTextCancel){
            mPopupWindow.dismiss();
        }
    }
    
    @SuppressLint("NewApi")
    private void shareMsg(Context context, BaseCard baseCard, ShareItem share) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");

        // intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, baseCard.address);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(share.packageName, share.className));
        context.startActivity(intent);
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
        LauncherLog.v(TAG, "wechatShare,jeff webpageUrl="+webpage.webpageUrl);
        wxApi.sendReq(req);  
    } 

//     packageName:com.android.contacts    className:com.mediatek.contacts.ShareContactViaSDCardActivity    手机存储
//     packageName:com.android.contacts    className:com.mediatek.contacts.ShareContactViaSMSActivity    短信
//     packageName:com.android.mms    className:com.android.mms.ui.ComposeMessageActivity    信息
//     packageName:com.android.mms    className:com.android.mms.ui.ShareVCardViaMMSActivity    彩信
//     packageName:com.android.bluetooth    className:com.android.bluetooth.opp.BluetoothOppLauncherActivity    蓝牙
//     packageName:com.tencent.mm    className:com.tencent.mm.ui.tools.ShareImgUI    微信          
//     packageName:com.tencent.mm    className:com.tencent.mm.ui.tools.AddFavoriteUI    添加到微信收藏
//     packageName:com.tencent.mm    className:com.tencent.mm.ui.tools.ShareToTimeLineUI    微信朋友圈                  
//     packageName:com.tencent.mobileqq    className:com.tencent.mobileqq.activity.JumpActivity    手机QQ
//     packageName:com.tencent.mobileqq    className:com.tencent.mobileqq.activity.qfileJumpActivity    发送到我的电脑
//     packageName:com.tencent.mobileqq    className:cooperation.qlink.QlinkShareJumpActivity    面对面快传
//     packageName:com.tencent.mobileqq    className:cooperation.qqfav.widget.QfavJumpActivity    保存到QQ收藏
    //add by  wangyang 按照 微信 朋友圈 手机QQ 手机短信 复制链接  排列
    private ImageView share_image_1;
    private View contentView;
    private List<Linear> mRelativeLayoutList = new ArrayList<Linear>();
    /**过滤掉分享的类名*/
    private List<String> mFilterPacknameClassname = new ArrayList<String>();
    private List<String> mListData = new ArrayList<String>();
    private List<ShareItem> mListDataSec = new ArrayList<ShareItem>();
    
    private RelativeLayout mWebShare1;
    private RelativeLayout mWebShare2;
    private RelativeLayout mWebShare3;
    private RelativeLayout mWebShare4;
    private RelativeLayout mWebShare5;
    private RelativeLayout mWebShare6;
    
    String WX_APP_ID = "wx312a5d621aceeace";
    private IWXAPI wxApi;
    private Linear mLinearCopy;
    private RelativeLayout mTextCancel;
    private PopupWindow mPopupWindow;
    private void showPopupWindow(View v) {
      //实例化  
        wxApi = WXAPIFactory.createWXAPI(mContext, WX_APP_ID);
        wxApi.registerApp(WX_APP_ID);
        if(mRelativeLayoutList != null && mRelativeLayoutList.size()>0){
            mRelativeLayoutList.clear();
        }
        if(mFilterPacknameClassname != null && mFilterPacknameClassname.size()>0){
            mFilterPacknameClassname.clear();
        }
        if(mListDataSec != null && mListDataSec.size()>0){
            mListDataSec.clear();
        }
        
        mFilterPacknameClassname.add("com.tencent.mm.ui.tools.ShareToTimeLineUI");
        mFilterPacknameClassname.add("com.tencent.mm.ui.tools.ShareImgUI");
        mFilterPacknameClassname.add("com.android.mms.ui.ComposeMessageActivity");
        mFilterPacknameClassname.add("com.tencent.mobileqq.activity.qfileJumpActivity");
        mFilterPacknameClassname.add("com.tencent.mobileqq.activity.JumpActivity");
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        contentView = mLayoutInflater.inflate(R.layout.web_share_activity, null);
        
        
        mWebShare1 = (RelativeLayout)contentView.findViewById(R.id.web_share_1);
        mWebShare2 = (RelativeLayout)contentView.findViewById(R.id.web_share_2);
        mWebShare3 = (RelativeLayout)contentView.findViewById(R.id.web_share_3);
        mWebShare4 = (RelativeLayout)contentView.findViewById(R.id.web_share_4);
        mWebShare5 = (RelativeLayout)contentView.findViewById(R.id.web_share_5);
        mWebShare6 = (RelativeLayout)contentView.findViewById(R.id.web_share_6);
        ImageView share_image_1 =(ImageView)contentView.findViewById(R.id.share_image_1);
        ImageView share_image_2 =(ImageView)contentView.findViewById(R.id.share_image_2);
        ImageView share_image_3 =(ImageView)contentView.findViewById(R.id.share_image_3);
        ImageView share_image_4 =(ImageView)contentView.findViewById(R.id.share_image_4);
        ImageView share_image_5 =(ImageView)contentView.findViewById(R.id.share_image_5);
        ImageView share_image_6 =(ImageView)contentView.findViewById(R.id.share_image_6);
        TextView share_text_1=(TextView)contentView.findViewById(R.id.share_text_1);
        TextView share_text_2=(TextView)contentView.findViewById(R.id.share_text_2);
        TextView share_text_3=(TextView)contentView.findViewById(R.id.share_text_3);
        TextView share_text_4=(TextView)contentView.findViewById(R.id.share_text_4);
        TextView share_text_5=(TextView)contentView.findViewById(R.id.share_text_5);
        TextView share_text_6=(TextView)contentView.findViewById(R.id.share_text_6);
        mRelativeLayoutList.add(new Linear(mWebShare1, share_text_1, share_image_1));
        mRelativeLayoutList.add(new Linear(mWebShare2, share_text_2, share_image_2));
        mRelativeLayoutList.add(new Linear(mWebShare3, share_text_3, share_image_3));
        mRelativeLayoutList.add(new Linear(mWebShare4, share_text_4, share_image_4));
        mRelativeLayoutList.add(new Linear(mWebShare5, share_text_5, share_image_5));
        mRelativeLayoutList.add(new Linear(mWebShare6, share_text_6, share_image_6));
        
        Intent intent = new Intent(Intent.ACTION_SEND, null);  
        intent.addCategory(Intent.CATEGORY_DEFAULT);  
        intent.setType("*/*");
        List<ResolveInfo> addList = mContext.getPackageManager().queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        Log.i(TAG, "addList:"+addList.size());
        for (ResolveInfo ri:addList) {
            String packageName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            Log.i(TAG, "packageName:"+packageName+"    className:"+className);
            
            if(!packageName.isEmpty() && mFilterPacknameClassname.contains(className)){
                mListData.add(className);
            }
        }
        
        if(mListData.contains("com.tencent.mm.ui.tools.ShareImgUI")){
            mListDataSec.add(new ShareItem("com.tencent.mm.ui.tools.ShareImgUI", R.string.wechar, R.drawable.wechare_share, "com.tencent.mm"));
        }
        if(mListData.contains("com.tencent.mm.ui.tools.ShareToTimeLineUI")){
            mListDataSec.add(new ShareItem("com.tencent.mm.ui.tools.ShareToTimeLineUI", R.string.wechar_friend, R.drawable.web_share_wefriend, "com.tencent.mm"));
        }
        if(mListData.contains("com.tencent.mobileqq.activity.JumpActivity")){
            mListDataSec.add(new ShareItem("com.tencent.mobileqq.activity.JumpActivity", R.string.web_share_qq, R.drawable.web_share_qq, "com.tencent.mobileqq"));
        }
        if(mListData.contains("com.android.mms.ui.ComposeMessageActivity")){
            mListDataSec.add(new ShareItem("com.android.mms.ui.ComposeMessageActivity", R.string.web_share_message, R.drawable.web_share_message, "com.android.mms"));
        }
        mListDataSec.add(new ShareItem("", R.string.web_share_copy, R.drawable.web_share_copy, ""));
        
        if(mListDataSec != null && mListDataSec.size() > 0){
            for(int b = 0; b<mListDataSec.size(); b++){
                ShareItem item = mListDataSec.get(b);
                Log.i(TAG, "   item                 item = "+item.className);
                Linear linear = mRelativeLayoutList.get(b);
                Log.i(TAG, "    linear                     linear a "+linear.a +"      b"+linear.b+"            c"+linear.c);
                linear.b.setText(item.text);
                linear.c.setImageResource(item.icon);
                linear.a.setOnClickListener(this);
            }
        }
        
        if(mListDataSec.size()<=4){
            LinearLayout web_linear_2 =(LinearLayout)contentView.findViewById(R.id.web_linear_2);
            web_linear_2.setVisibility(View.GONE);
        }
        
        mPopupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        
        mTextCancel = (RelativeLayout) contentView.findViewById(R.id.text_cancel); 
        mTextCancel.setOnClickListener(this);
        
        mPopupWindow.setTouchable(true);

        mPopupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                Log.i("mengdd", "onTouch : ");
                return false;
            }

        });
        mPopupWindow.setAnimationStyle(R.style.webShareAnimation);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        ColorDrawable dw = new ColorDrawable(Color.parseColor("#F0F0F0"));  
        mPopupWindow.setBackgroundDrawable(dw); 
        mPopupWindow.showAtLocation(BrowserActivity.this.findViewById(R.id.main), Gravity.BOTTOM, 0, 0);
//        popupWindow.showAsDropDown(contentView);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener(){

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                backgroundAlpha(1f); 
            } 
            
        });
        backgroundAlpha(0.8f);
    }
    
    private void backgroundAlpha(float bgAlpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();  
        lp.alpha = bgAlpha; //0.0-1.0  
        getWindow().setAttributes(lp);  
    }
    
    private class ShareItem{
        String className;
        int text;
        int icon;
        String packageName;
        public ShareItem(String className, int text, int icon, String packageName){
            this.className = className;
            this.text = text;
            this.icon = icon;
            this.packageName = packageName;
        }
    }
    
    private class Linear{
        RelativeLayout a;
        TextView b;
        ImageView c;
        public Linear(RelativeLayout a, TextView b, ImageView c){
            this.a = a;
            this.b =b;
            this.c = c;
        }
    }

    //add by wangyang 2016.9.21
    public void updateAddFavoritesDatabase(final Context context,int id ,final ContentValues values, final String type) {
        
        final Uri uri = ContentUris.withAppendedId(Uri.parse("content://com.cappu.download/downloadText/"), id);
        final ContentResolver cr = context.getContentResolver();
        sWorker.post(new Runnable() {
            public void run() {
                int index = cr.update(uri, values, null, null);
                Log.i(TAG, "     index             index =    "+index);
                if(index == 1){
                    if(type.equals("1")){
                        mWebCollectionImage.setImageResource(R.drawable.news_collection_pass);
                        I99ThemeToast.toast(context, context.getString(R.string.favorites_success), "l", Color.parseColor("#FFFFFF"), 1);
                    } else{
                        mWebCollectionImage.setImageResource(R.drawable.news_collection_normal);
                        I99ThemeToast.toast(context, context.getString(R.string.favicons_cancle_success), "l", Color.parseColor("#FFFFFF"), 1);
                    }
                    
                }
                
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechTools.isSpeaking()) {
            mSpeechTools.stopSpeaking();
        }

        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            dialogHandler.sendEmptyMessage(0);
        }
        keepScreenOn(this, false);

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        if (mSpeechTools.isSpeaking()) {
            mSpeechTools.stopSpeaking();
        }

        keepScreenOn(this, false);
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            dialogHandler.sendEmptyMessage(0);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(mSpeechTools.isSpeaking()){
            mSpeechTools.stopSpeaking();
        }
    }
    
    public String HTML;
    StringBuffer mStringBuffer;

    public String Resolve(String html) {
        Log.d(TAG, "Resolve:" + html.length());
        if (mStringBuffer == null) {
            mStringBuffer = new StringBuffer();
        } else {
            mStringBuffer.delete(0, mStringBuffer.length());
        }
        try {
            Document doc = Jsoup.parse(html);// Jsoup.connect(url).get();
            Element ele = doc.select("div[id=rt-mainbody-surround]").first();
            Elements uls = ele.select("p");
            for (int j = 0; j < uls.size(); j++) {
                Element ul = uls.get(j);
                
                String Str = removeAllSpace(ul.select("p").text());
                if(Str != null){
                    mStringBuffer.append(Str);
                }
            }
            return mStringBuffer.toString();
        } catch (Exception e) {
            Log.d(TAG, "IOException e:" + e.toString());
            return null;
        }
    }

    public String removeAllSpace(String str) {
        if(str!=null){
            String tmpstr = str.replace(" ", "");
            return tmpstr;
        }else{
            return null;
        }
        
    }

    final class KookWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading " + url);
            view.loadUrl(url);
            return true;
        }

        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            // dialogHandler.sendEmptyMessage(1);
/*            new Thread() {
                public void run() {
                    try {
                        HTML = getHtmlString(url);
                        Log.i(TAG, "onPageStarted HTML:" + HTML);
                    } catch (Exception e) {
                        Log.i(TAG, "onPageStarted exception:" + e.toString());
                    }
                };
            }.start();*/
            
            HTMLSaveTask tst = new HTMLSaveTask(url);
            tst.execute();
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, final String url) {
            // dialogHandler.sendEmptyMessage(0);
            Log.d(TAG, "onPageFinished " + url);
            // view.loadUrl("javascript:window.local_obj.showSource('<head>'+" +
            // "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");

            mWebView.getSettings().setBlockNetworkImage(false);// modify by even
            super.onPageFinished(view, url);
        }

    }

    /**
     * 使用URLConnection根据url读取html源代码
     * 
     * @param urlString
     * @return
     */
    private String getHtmlString(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection ucon = url.openConnection();
            InputStream instr = ucon.getInputStream();
            byte[] data = readInputStream(instr);
            return new String(data, "utf-8");
        } catch (Exception e) {
            Log.i(TAG, "getHtmlString exception:" + e.toString());
            return "";
        }
    }
    
    /**
     * 读取输入流，得到html的二进制数据
     * 
     * @param inStream
     * @return
     * @throws Exception
     */
    public byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
    
    private Handler dialogHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                case 1:
                    //mLoadingDialog.show();
                    break;
                case 0:
                    //mLoadingDialog.dismiss();
                    break;
                }
            } catch (Exception e) {
                Log.d(TAG, "dialog option exception  " + e.toString());
            }
        }
    };

    /**
     * 将Strintg 保存文件
     * 
     * @param toSaveString
     * @param filePath
     */
    public static void saveFile(String toSaveString) {
        try {
            File saveFile = new File(Environment.getExternalStorageDirectory().getPath() + "/html.text");
            if (!saveFile.exists()) {
                File dir = new File(saveFile.getParent());
                dir.mkdirs();
                saveFile.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(saveFile);
            outStream.write(toSaveString.getBytes());
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保持屏幕唤醒状态（即背景灯不熄灭）
     *
     * @param on
     *            是否唤醒
     */
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

    /**
     * 读取文件内容
     * 
     * @param filePath
     * @return 文件内容
     */
    public static String readFile(String filePath) {
        String str = "";
        try {
            File readFile = new File(filePath);
            if (!readFile.exists()) {
                return null;
            }
            FileInputStream inStream = new FileInputStream(readFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            str = stream.toString();
            stream.close();
            inStream.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCompleted() {
        keepScreenOn(this, false);
    }

    @Override
    public void onSpeakPaused() {
        keepScreenOn(this, false);
    }

    /**
     * 读取一个文件到字符串里.
     * 
     * @param sFileName
     *            文件名
     * @param sEncode
     *            String
     * @return 文件内容
     */
    public String readTextFile(String sFileName, String sEncode) {
        StringBuffer sbStr = new StringBuffer();

        try {
            File ff = new File(sFileName);
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    ff), sEncode);
            BufferedReader ins = new BufferedReader(read);

            String dataLine = "";
            while (null != (dataLine = ins.readLine())) {
                sbStr.append(dataLine);
                sbStr.append("/r/n");
            }

            ins.close();
        } catch (Exception e) {
            Log.i(TAG, "读取html文件异常 e:" + e.toString());
        }

        return sbStr.toString();
    }

    // 初始化菜单
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单
        // getMenuInflater().inflate(R.menu.activity_main, menu);
//        menu.add(0, MENU_INDEX, 0, getString(R.string.i99_share));
//        return true;
//    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_INDEX:
            if (mBaseCard != null) {
                new CappuShareDialog(this, mBaseCard/* mBaseCard.getAddress() */).show();
            }

            break;
        }
        return true;
//            Intent intent=new Intent(Intent.ACTION_SEND);  
//            intent.setType("image/*");  
//            intent.putExtra(Intent.EXTRA_SUBJECT, "Share");  
//            intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app");  
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
//            startActivity(Intent.createChooser(intent, getTitle()));  
//           return true;
//       }
//       return false;

    }

    class HTMLSaveTask extends AsyncTask<String, Void, String> {

        String mUrl;
        long mExecTime;
        String mFileName;

        public HTMLSaveTask(String url) {
            this.mUrl = url;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, " doInBackground  ");
            
            String html = null;
            try {
                java.util.Random random = new java.util.Random();// 定义随机类
                int result = random.nextInt(10);
                Thread.sleep(result * 6);
            } catch (Exception e) {
                Log.i(TAG, " doInBackground  Exception:" + e.toString());
            }

            File saveFile = null;
            try {
                saveFile = new File(Environment.getExternalStorageDirectory().getPath() + "/.icon/html" + mFileName);
                if (!saveFile.exists()) {
                    File dir = new File(saveFile.getParent());
                    dir.mkdirs();
                    saveFile.createNewFile();
                }
                
                if(saveFile.exists() && saveFile.length() > 0){
                    Log.i(TAG, " doInBackground 第二次不许要缓存 ");
                    return null;
                }
            } catch (IOException e) {
                Log.i(TAG, "---IOException e:" + e.toString());
            }

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
                String strLine = "";
                URL urlObj = new URL(mUrl);
                InputStream inputStream = urlObj.openStream();
                InputStreamReader readerObj = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(readerObj);
                while ((strLine = bufferedReader.readLine()) != null) {
                    fileOutputStream.write(strLine.getBytes());
                }
                
                bufferedReader.close();
                fileOutputStream.close();
                inputStream.close();
                readerObj.close();
                
                
            } catch (Exception e) {
                Log.i(TAG, "url   error:" + e.toString());
            }
            
            return html;
        }

        // **当后台操作结束时，此方法将会被调用，计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。*//*
        @Override
        protected void onPostExecute(String re) {
            super.onPostExecute(re);
            Log.i(TAG, "init  onPostExecute   re: " + re);

            Log.i(TAG, "抓取网页时间: " + (System.currentTimeMillis() - mExecTime));
        }

        // **在execute(Params... params)被调用后立即执行，一般用来在执行后台任务前对UI做一些标记。*//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mExecTime = System.currentTimeMillis();
            int index = mUrl.lastIndexOf("/");
            mFileName = mUrl.substring(index, mUrl.length());
            Log.i(TAG, "mFileName:" + mFileName);
        }
    }

}
