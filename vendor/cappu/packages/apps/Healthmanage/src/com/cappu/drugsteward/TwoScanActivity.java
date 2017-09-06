package com.cappu.drugsteward;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.cappu.widget.TopBar;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.util.DensityUtil;
import com.cappu.drugsteward.util.JsonParse;
import com.cappu.drugsteward.util.Network;

public class TwoScanActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    private String TAG = "QrcodeResult";
    private TextView resultTextView = null;
    private static final String ALPHANUM_PART = "[a-zA-Z0-9\\-]";
    private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern.compile(
            '(' + ALPHANUM_PART + "+\\.)+" + ALPHANUM_PART + "{2,}" + // host name elements
            "(:\\d{1,5})?" + // maybe port
            "(/|\\?|$)"); // query, path or nothing
    private ClipboardManager cmb = null;
    private Button copybutton = null;
    private Button sharebutton = null;
    private Button openbutton = null;
    
    Map<String, Object> mdrug=new HashMap<String, Object>();
    private TextView barcode;
    private TextView drugname;
    private Mtask task;
    String dname="";
    private LinearLayout linear;
    private RelativeLayout relative;
    private TopBar mTopBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_bar);
        //TextView addbox=(TextView) findViewById(R.id.drug_bar_addbox);
        //addbox.setOnClickListener(this);
        initview(); 
       
        DisplayMetrics display = getResources().getDisplayMetrics();
        int width=display.widthPixels;
        int height=display.heightPixels;
        ImageView drugimage=(ImageView) findViewById(R.id.drug_bar_drugimage);
        drugimage.setLayoutParams(new LayoutParams(width/2, DensityUtil.dip2px(this, 150)));
        //----------------------
        Network net=new Network(this);
        Intent intent  = getIntent();
         if(null != intent){
            Bundle bundle = intent.getExtras();
            String scanResult = bundle.getString("scanresult");
            if(net.getconnect()){
                task = new Mtask();
                task.execute(scanResult);
            }else{
                Toast.makeText(this, "网络链接出现问题", 0).show();
                linear.setVisibility(View.GONE);
                relative.setVisibility(View.VISIBLE);
            }
        }else{
            Toast.makeText(this, "没有扫描到条码！！！", 0).show();
        }
        
    }
    
    
    public void initview(){
        barcode = (TextView) findViewById(R.id.bar_code);
        drugname = (TextView) findViewById(R.id.drug_name);
        //TextView back=(TextView) findViewById(R.id.back);
        //back.setOnClickListener(this);
       linear = (LinearLayout) findViewById(R.id.linear_date);
       relative = (RelativeLayout) findViewById(R.id.relative_date);
       mTopBar = (TopBar) findViewById(R.id.topbar);
       mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
           @Override
           public void onLeftClick(View v){
               TwoScanActivity.this.finish();
           }
   
           @Override
           public void onRightClick(View v){
//               Intent intent=new Intent(TwoScanActivity.this,AddDrugDetailActivity.class);
//               intent.putExtra("dname", dname);
//               intent.putExtra("type","");
//               startActivity(intent);
           }

           @Override
           public void onTitleClick(View v){

           }    
       });
    }
    
    public void getnetdrug(Map<String, Object> ma){
        if(ma != null && ma.size()>0){
           barcode.setText(ma.get("bardcode")+"");
           drugname.setText(ma.get("name")+"");
           dname=ma.get("name")+"";
        }
    }
    
    
    public class Mtask extends AsyncTask{

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(TwoScanActivity.this, "药品查询", "数据正在载入中请稍候。。。");
            
        }

        @Override
        protected Object doInBackground(Object... arg0) {
            
            Map<String,Object> maa=new HashMap<String, Object>();
           String code = (String) arg0[0];
           String path="http://api.juheapi.com/jhbar/bar?appkey=83f3e9b87f37304ed34d0e7efd27d2d6&pkg=com.cappu.yaopingguanli&barcode="+code+"&cityid=1";
           try {
            URL url=new URL(path);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            if(conn.getResponseCode()==200){
                InputStream is = conn.getInputStream();
                 maa=JsonParse.getdrug(is);
                 return maa;
            }
            
           } catch (Exception e) {
               e.printStackTrace();
           } 
            return null;
        } 
        
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            progress.dismiss();
            if(result != null){
               Map<String, Object>  ma=(Map<String, Object>) result;
                getnetdrug(ma);
                linear.setVisibility(View.VISIBLE);
                relative.setVisibility(View.GONE);
            }else{
//                Toast.makeText(TwoScanActivity.this, "加载数据失败，请重试", 0).show();
                linear.setVisibility(View.GONE);
                relative.setVisibility(View.VISIBLE);
            }
            
        }
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copystring:
                CopyString();
                break;
            case R.id.sharestring:
                ShareString();
                break;
            case R.id.openstring:
                openUrl();
                break;
//            case R.id.drug_bar_addbox:
//                Intent intent=new Intent(this,AddDrugDetailActivity.class);
//                intent.putExtra("dname", dname);
//                intent.putExtra("type","");
//                startActivity(intent);
//                break;
//            case R.id.back:
//                finish();
//                break;
            default:
                break;
        }
     
    }
    
    //暂时没有用
    public void withoutuse(){
        cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
        copybutton = (Button) this.findViewById(R.id.copystring);
        sharebutton = (Button) this.findViewById(R.id.sharestring);
        openbutton = (Button) this.findViewById(R.id.openstring);
        resultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        copybutton.setOnClickListener(this);
        sharebutton.setOnClickListener(this);
        openbutton.setOnClickListener(this);
    }

    
    /*
     * Copy Result
     */
    @SuppressLint("NewApi")
    private void CopyString(){
        cmb.setText(resultTextView.getText());
        Toast.makeText(getApplicationContext(), R.string.replicated, Toast.LENGTH_SHORT).show();
    }
    /*
     * Share Result
     */
    private void ShareString(){
        if(!("".equals(resultTextView.getText()))){
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, resultTextView.getText()); 
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getTitle())); 
        }
    }
    /*
     * scanner Result String default Open With URI
     */
    private void openUrl() {
        String uri = resultTextView.getText().toString();
        Matcher matcher = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
        if(matcher.find() && matcher.start() == 0) {
            uri = "http://" + uri;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        try{
            this.startActivity(intent);
        }catch (Exception e){
            resultTextView.setText(uri);
            Toast.makeText(getApplicationContext(), R.string.nofindactivity, Toast.LENGTH_SHORT).show();
        }
    }
    /*
     * format Result
     */
    private void formatResult(String scanResult){
        resultTextView.setText(scanResult);
        Spannable spannable = new SpannableString(scanResult);
        if(Linkify.addLinks(spannable, Linkify.WEB_URLS)){
            openbutton.setVisibility(View.VISIBLE);
        }else{
            openbutton.setVisibility(View.GONE);
            
        }
    }

}
