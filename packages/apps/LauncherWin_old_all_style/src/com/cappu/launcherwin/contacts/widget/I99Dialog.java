package com.cappu.launcherwin.contacts.widget;

import com.cappu.launcherwin.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class I99Dialog extends Dialog implements View.OnClickListener {

    View mContentView;
    View mBottomLine;
    View mControls;
    TextView mTitle;
    TextView mMessage;
    Button mOkBu, mCancelBu;
    ProgressBar mProgressBar;

    public I99Dialog(Context context) {
        super(context, R.style.I99DialogStyle2);
        LayoutInflater inflater = getLayoutInflater();
        mContentView = inflater.inflate(R.layout.i99_dialog, null);
        setContentView(mContentView);
        mBottomLine = mContentView.findViewById(R.id.line);
        mControls = mContentView.findViewById(R.id.controls);
        mTitle = (TextView) mContentView.findViewById(R.id.title);
        mMessage = (TextView) mContentView.findViewById(R.id.message);
        mOkBu = (Button) mContentView.findViewById(R.id.ok);
        mCancelBu = (Button)mContentView.findViewById(R.id.cancel);
        mProgressBar = (ProgressBar)mContentView.findViewById(R.id.progress);

        mOkBu.setOnClickListener(this);
        mCancelBu.setOnClickListener(this);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTitle.setText(titleId);
    }

    public void setTitleColor(ColorStateList color) {
        mTitle.setTextColor(color);
    }

    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setTitleBackground(int resid) {
        mTitle.setBackgroundResource(resid);
    }

    @SuppressLint("NewApi")
    public void setTitleBackground(Drawable drawable) {
        mTitle.setBackground(drawable);
    }

    public void setMessage(CharSequence message) {
        mMessage.setText(message);
    }

    public void setMessage(int resid) {
        mMessage.setText(resid);
    }
    public void setNegativeGone(){
        if(mCancelBu != null){
            mCancelBu.setVisibility(View.GONE);
            mBottomLine.setVisibility(View.GONE);
        }
        mOkBu.setBackgroundResource(R.drawable.i99_bottom_bg);
    }
 
    public void setNegativeButton(CharSequence text,final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mCancelBu.setText(text);
        }
        if(listener != null){
            mCancelBu.setOnClickListener(listener);
        }

    }

    public void setNegativeButton(int textId,final View.OnClickListener listener) {
        mCancelBu.setText(textId);
        if(listener != null){
            mCancelBu.setOnClickListener(listener);
        }
    }

    public void setPositiveButton(CharSequence text,final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mOkBu.setText(text);
        }
        if(listener != null){
            mOkBu.setOnClickListener(listener);
        }
    }

    public void setPositiveButton(int textId,final View.OnClickListener listener) {
        mOkBu.setText(textId);
        if(listener != null){
            mOkBu.setOnClickListener(listener);
        }
    }

    public void setControlVisibility(int visible){
        mControls.setVisibility(visible);
    }

    public void setProgressVisibility(int visible){
        mProgressBar.setVisibility(visible);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ok:
            dismiss();
            break;
        case R.id.cancel:
            dismiss();
        default:
            break;
        }

    }

    
    @Override
    public void show() {
        super.show();
        final int width = getScreenSize(getContext())[0];
        final int height = getScreenSize(getContext())[1];
        LayoutParams lp = mContentView.getLayoutParams();

        if( width > height){
            lp.width = getScreenSize(getContext())[0] / 2;
        }else{
            lp.width = getScreenSize(getContext())[0] - 10;
        }
        
        mContentView.setLayoutParams(lp);
    }
}
