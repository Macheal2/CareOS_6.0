package com.cappu.launcherwin.phoneutils;

import java.util.List;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneUtilActivity extends BasicActivity implements View.OnClickListener,UIInterface{
    public static final String TAG = "PhoneUtilActivity";
    TextView inquiry;
    
    TextView telephone_inquiry;

    TextView showing;
    
    TextView phone_info;
    
    private PhoneUtil phoneUtils;
    private ContentObserver mObserver;
    private SMSHandler mHandler;
    
    private SMSReceiver mSMSReceiver;
    
    StringBuffer mStringBuffer;
    String onNewIntentString;
    
    /**运营商名字*/
    String mProvidersName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_util);
        
        init();
        RegisterReceiver();//短信拦截方式
        //addSMSObserver();//数据库拦截方式
        
        showContent(getIntent());
        
        Log.i(TAG, "  onCreate " );
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showContent(intent);
        Log.i(TAG, "  onNewIntent " );
    }
    
    public void showContent(Intent intent){
        String content = intent.getStringExtra("content");
        if(content!=null){
            Log.i(TAG, "  showContent  短信内容 " +"    onNewIntentString:"+onNewIntentString+"    content.equals(onNewIntentString):"+content.equals(onNewIntentString));
            if(!content.equals(onNewIntentString)){
                AnalysisSMSContent(content);
                onNewIntentString = content;
            }
            
        }
    }
    
    public void RegisterReceiver(){
        mSMSReceiver = new SMSReceiver();
        IntentFilter filter = new IntentFilter();  
        filter.addAction(SMSReceiver.SMS_ACTION); 
        registerReceiver(mSMSReceiver, filter);
    }
    public void addSMSObserver() {
        Log.i(TAG, "add a SMS observer. ");
        ContentResolver resolver = getContentResolver();
        mHandler = new SMSHandler(this,this,phoneUtils);
        mObserver = new SMSObserver(resolver, mHandler,phoneUtils);
        resolver.registerContentObserver(SMS.CONTENT_URI, true, mObserver);
    }
    
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy().");
        if(mObserver!=null){
            this.getContentResolver().unregisterContentObserver(mObserver);
        }
        super.onDestroy();
    }
    
    
    public void init() {
        
        inquiry = (TextView) findViewById(R.id.inquiry);
        showing = (TextView) findViewById(R.id.showing);
        telephone_inquiry = (TextView) findViewById(R.id.telephone_inquiry);
        
        showing.setVisibility(View.INVISIBLE);
        phone_info = (TextView) findViewById(R.id.phone_info);
        inquiry.setOnClickListener(this);
        
        PhoneUtil.init(this);
        phoneUtils = PhoneUtil.getInstance();
        
        mProvidersName = phoneUtils.getProvidersName();
        if(mProvidersName == null){
            //phone_info.setText("请检查SIM卡是否正常");
             phone_info.setText(R.string.checkSIM);
            telephone_inquiry.setText(R.string.telephone_inquiry_button);
        }
        else{
            String phoneNumber = phoneUtils.getNativePhoneNumber();
            if(TextUtils.isEmpty(phoneNumber)){
                //phone_info.setText("本机号：" +phoneNumber+ "    获取异常    " + "\n"+"运营商：" + mProvidersName + "\n");
                phone_info.setText("\n"+"运营商：" + mProvidersName + "\n");
            }else{
                phone_info.setText("本机号：" + phoneNumber + "   " + "\n"+"运营商：" + mProvidersName + "\n");
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        if(v == mCancel){
            finish();
        }else if(v == inquiry){
            if(mStringBuffer == null){
                mStringBuffer = new StringBuffer();
            }else{
                mStringBuffer.delete(0, mStringBuffer.length());
            }
            
            if(showing.getText().toString().equals(getString(R.string.inquiry_wait))){
                Toast.makeText(this, R.string.inquiry_wait, Toast.LENGTH_LONG).show();
                return;
            }
            String providerName = phoneUtils.getProvidersName();
            if(providerName != null){
                boolean isOK = phoneUtils.sendSms(providerName);
                if ( isOK ) {
                    phoneUtils.setInquiryType(true);
                    showing.setText(R.string.inquiry_wait);
                } else {
                    showing.setText(R.string.send_fail);
                }
                showing.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(this, R.string.sim_fail, Toast.LENGTH_LONG).show();
            }
            
        }
    }
    
    public void getInfo(){/*
      //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if(text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
        }
    */}
    
    @Override
    public void updateUi(MessageItem item) {
        if(!TextUtils.isEmpty(item.getBody())) {
            AnalysisSMSContent(item.getBody());
        }
    }
    
    public void AnalysisSMSContent(String content){
        phoneUtils.setInquiryType(false);
        showing.setVisibility(View.VISIBLE);
        if(mStringBuffer == null){
            mStringBuffer = new StringBuffer();
        }
        
        String[] info;
        if(content.indexOf("，") != -1){
            info = content.split("，");
        }else if(content.indexOf(",") != -1){
            info = content.split(",");
        }else{
            info = new String[1];
            info[0] = content;
        }
        
        //Log.i(TAG, "短信内容 sms content: " + content);
        for (int i=0;i<info.length;i++) {
            
            Log.i(TAG, " i:"+i+"  ----------------- in: " + info[i]+"                  "+(info[i].indexOf(getString(R.string.current_balance)) != -1));
            if(info[i].indexOf("元") != -1){
                mStringBuffer.append(info[i]);
            }
        }
        
        if(mStringBuffer.length()>0){
            showing.setText(mStringBuffer);
        }else{
            showing.setVisibility(View.INVISIBLE);
        }
        
        //showing.setText(content);
        
    }
}
