package com.cappu.drugsteward.example.xing;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.drugsteward.example.xing.activity.CaptureActivity;
import com.cappu.drugsteward.example.xing.encoding.EncodingHandler;
import com.cappu.drugsteward.google.zxing.WriterException;
import com.cappu.healthmanage.R;

public class QrcodeScanner extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    private String TAG = "QrcodeScanner";
    private TextView resultTextView = null;
    private EditText qrStrEditText = null;
    private ImageView qrImgImageView = null;
    private LinearLayout mLayouttext = null;
    private boolean isurijump = false;
    private static final String WEIXIN_URL_HEAD = "http://weixin.qq.com/r/";
    private static final String ALPHANUM_PART = "[a-zA-Z0-9\\-]";
    private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern.compile('(' + ALPHANUM_PART + "+\\.)+" + ALPHANUM_PART + "{2,}" + // host
                                                                                                                                          // name
                                                                                                                                          // elements
            "(:\\d{1,5})?" + // maybe port
            "(/|\\?|$)"); // query, path or nothing
    private ClipboardManager cmb = null;
    private Button copybutton = null;
    private Button sharebutton = null;
    private Button openbutton = null;
    private boolean resultisweburl = false;
    private boolean resultisphonenumber = false;
    private boolean resultisemail = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainc);
        cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mLayouttext = (LinearLayout) this.findViewById(R.id.textlinear);
        resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
        qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
        qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
        copybutton = (Button) this.findViewById(R.id.copystring);
        sharebutton = (Button) this.findViewById(R.id.sharestring);
        openbutton = (Button) this.findViewById(R.id.openstring);
        copybutton.setOnClickListener(this);
        sharebutton.setOnClickListener(this);
        openbutton.setOnClickListener(this);
        /*
         * Button scanBarCodeButton = (Button) this
         * .findViewById(R.id.btn_scan_barcode); Button generateQRCodeButton =
         * (Button) this .findViewById(R.id.btn_add_qrcode); File f = new
         * File(Environment.getExternalStorageDirectory() + "/page/"); if
         * (!f.exists()) { f.mkdir(); }
         * scanBarCodeButton.setOnClickListener(this);
         * generateQRCodeButton.setOnClickListener(this);
         */
        Intent openCameraIntent = new Intent(QrcodeScanner.this, CaptureActivity.class);
        startActivityForResult(openCameraIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mLayouttext.setVisibility(View.VISIBLE);
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            formatResult(scanResult);
        } else {
            this.finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mLayouttext.getVisibility() == View.VISIBLE) {
                mLayouttext.setVisibility(View.GONE);
                Intent openCameraIntent = new Intent(QrcodeScanner.this, CaptureActivity.class);
                this.startActivityForResult(openCameraIntent, RESULT_CANCELED);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View) button
     * Onclick listener
     */

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_scan_barcode:
            Intent openCameraIntent = new Intent(QrcodeScanner.this, CaptureActivity.class);
            startActivityForResult(openCameraIntent, 0);
            break;
        case R.id.btn_add_qrcode:
            CreateCodeBitmap();
            break;
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

    @SuppressLint("NewApi")
    private void CopyString() {
        cmb.setText(resultTextView.getText());
        Toast.makeText(getApplicationContext(), R.string.replicated, Toast.LENGTH_SHORT).show();
    }

    private void ShareString() {
        if (!("".equals(resultTextView.getText()))) {
            Intent intent = new Intent(Intent.ACTION_SEND);
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
        /*
         * if(uri.startsWith(WEIXIN_URL_HEAD)) {
         * resultTextView.setText(getResources
         * ().getString(R.string.weixinerror));
         * mLayouttext.setVisibility(View.VISIBLE); return; }
         */
        Matcher matcher = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
        if (matcher.find() && matcher.start() == 0) {
            uri = "http://" + uri;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        try {
            this.startActivity(intent);
        } catch (Exception e) {
            resultTextView.setText(uri);
            mLayouttext.setVisibility(View.VISIBLE);
        }
    }

    private void formatResult(String scanResult) {
        resultTextView.setText(scanResult);
        Spannable spannable = new SpannableString(scanResult);
        if (Linkify.addLinks(spannable, Linkify.WEB_URLS)) {
            openbutton.setEnabled(true);
            openbutton.setText(R.string.openuri);
        } else {
            openbutton.setEnabled(false);
            openbutton.setText(R.string.openstring);

        }
    }

    /*
     * save bitmap ==> Temporarily not used
     */
    private void CreateCodeBitmap() {

        try {
            String contentString = qrStrEditText.getText().toString();
            if (!contentString.equals("")) {
                Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString, 350);
                qrImgImageView.setImageBitmap(qrCodeBitmap);
                File dir = new File(Environment.getExternalStorageDirectory() + "/page/");
                android.util.Log.e(TAG, "SAVE DIR:" + dir);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File f = new File(Environment.getExternalStorageDirectory() + "/page/" + "wangziqi" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.savebitmap), Toast.LENGTH_SHORT).show();
                    fos.flush();
                    fos.close();
                } catch (Exception e) {

                    e.printStackTrace();
                }

            } else {
                Toast.makeText(QrcodeScanner.this, getResources().getString(R.string.input_text), Toast.LENGTH_SHORT).show();
            }

        } catch (WriterException e) {

            e.printStackTrace();
        }

    }

}
