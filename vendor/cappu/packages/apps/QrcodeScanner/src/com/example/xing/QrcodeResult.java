package com.example.xing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

public class QrcodeResult extends Activity implements OnClickListener{
	
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
		copybutton = (Button) this.findViewById(R.id.copystring);
		sharebutton = (Button) this.findViewById(R.id.sharestring);
		sharebutton.setVisibility(View.GONE);//yuan tong qin add 
		sharebutton.setClickable(false);//yuan tong qin add 
		openbutton = (Button) this.findViewById(R.id.openstring);
                resultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		copybutton.setOnClickListener(this);
		sharebutton.setOnClickListener(this);
		openbutton.setOnClickListener(this);
		Intent intent  = getIntent();
		Log.e(TAG,"intent:"+intent);
		if(null != intent){
			Bundle bundle = intent.getExtras();
			String scanResult = bundle.getString("scanresult");
			formatResult(scanResult);
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
			default:
				break;
		}
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
