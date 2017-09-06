//package android.app;
package com.cappu.app;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.android.internal.R;
import com.cappu.internal.R;

/** @hide */
public class I99Dialog extends Dialog implements View.OnClickListener , DialogInterface{


    ListView mListView;

    View mBottomView;
    View mBottomLine;
    View mControls;
    View mContentView;
    View mCustomerView;
    View mDialogView;

    TextView mTitle;
    TextView mMessage;
    Button mOkBu, mCancelBu;
    ProgressBar mProgressBar;

    OnClickListener mDialogClick;

    public I99Dialog(Context context) {
        super(context, R.style.I99DialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.i99_dialog, null);
        setContentView(mDialogView);
        mBottomLine = mDialogView.findViewById(R.id.i99_line);
        mControls = mDialogView.findViewById(R.id.i99_dialog_controls);
        mTitle = (TextView) mDialogView.findViewById(R.id.i99_dialog_title);
        mMessage = (TextView) mDialogView.findViewById(R.id.i99_dialog_message);
        mOkBu = (Button) mDialogView.findViewById(R.id.i99_dialog_ok);
        mCancelBu = (Button)mDialogView.findViewById(R.id.i99_dialog_cancel);
        mProgressBar = (ProgressBar)mDialogView.findViewById(R.id.i99_dialog_progress);
        mListView = (ListView)mDialogView.findViewById(R.id.i99_list);
        mContentView = findViewById(R.id.i99_dialog_content);
        mBottomView = findViewById(R.id.i99_dialog_bottom_view);

        mOkBu.setOnClickListener(this);
        mCancelBu.setOnClickListener(this);
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    public void setTitle(int titleId) {
        mTitle.setText(titleId);
    }

    public ListView getListView(){
        return mListView;
    }
    public I99Dialog setSingleChoiceItems(ListAdapter adapter, int checkedItem, final DialogInterface.OnClickListener listener) {
        if(mListView != null){
            mListView.setAdapter(adapter);
        }
        if(listener != null){
            mDialogClick = listener;
        }
        mContentView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                mDialogClick.onClick(I99Dialog.this, position);
                I99Dialog.this.dismiss();
            }
        });
        return this;
    }

    public I99Dialog setTitleColor(ColorStateList color) {
        mTitle.setTextColor(color);
        return this;
    }

    public I99Dialog setTitleColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }

    public I99Dialog setTitleBackground(int resid) {
        mTitle.setBackgroundResource(resid);
        return this;
    }

    public I99Dialog setTitleBackground(Drawable drawable) {
        mTitle.setBackground(drawable);
        return this;
    }

    public I99Dialog setMessage(CharSequence message) {
        mMessage.setText(message);
        return this;
    }

    public I99Dialog setMessage(int resid) {
        mMessage.setText(resid);
        return this;
    }
    public I99Dialog setNegativeGone(){
        if(mCancelBu != null){
            mCancelBu.setVisibility(View.GONE);
            mBottomLine.setVisibility(View.GONE);
        }
        mOkBu.setBackgroundResource(R.drawable.i99_bottom_bg);
        return this;
    }

    public I99Dialog setNegativeButton(CharSequence text){
        if (!TextUtils.isEmpty(text)) {
            mCancelBu.setText(text);
        }
        mCancelBu.setOnClickListener(mDismissClick);
        return this;
    }

    public I99Dialog setNegativeButton(int textId){
        if(textId != -1){
            mCancelBu.setText(textId);
        }
        mCancelBu.setOnClickListener(mDismissClick);
        return this;
    }

    public I99Dialog setNegativeButton(CharSequence text,final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mCancelBu.setText(text);
        }
        if(listener != null){
            mDialogClick = listener;
        }
        return this;
    }

    public I99Dialog setNegativeButton(int textId,final DialogInterface.OnClickListener listener) {
        if(textId != -1){
            mCancelBu.setText(textId);
        }
        if(listener != null){
            mDialogClick = listener;
        }
        return this;
    }


    public I99Dialog setNegativeButton(CharSequence text,final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mCancelBu.setText(text);
        }
        if(listener != null){
            mCancelBu.setOnClickListener(listener);
        }
        return this;
    }

    public I99Dialog setNegativeButton(int textId,final View.OnClickListener listener) {
        if(textId != -1){
            mCancelBu.setText(textId);
        }
        if(listener != null){
            mCancelBu.setOnClickListener(listener);
        }
        return this;
    }
    public I99Dialog setPositiveButton(CharSequence text){
        if (!TextUtils.isEmpty(text)) {
            mOkBu.setText(text);
        }
        mOkBu.setOnClickListener(mDismissClick);
        return this;
    }

    public I99Dialog setPositiveButton(int textId){
        if(textId != 0 ){
            mOkBu.setText(textId);
        }
        mOkBu.setOnClickListener(mDismissClick);
        return this;
    }

    public I99Dialog setPositiveButton(CharSequence text,final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mOkBu.setText(text);
        }
        if(listener != null){
            mDialogClick = listener;
        }
        return this;
    }
    
    public I99Dialog setPositiveButton(int textId,final DialogInterface.OnClickListener listener) {
        if(textId != 0 ){
            mOkBu.setText(textId);
        }
        if(listener != null){
            mDialogClick = listener;
        }
        return this;
    }

    public I99Dialog setPositiveButton(CharSequence text,final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mOkBu.setText(text);
        }
        if(listener != null){
             mOkBu.setOnClickListener(listener);
        }
        return this;
    }

    public I99Dialog setPositiveButton(int textId,final View.OnClickListener listener) {
        mOkBu.setText(textId);
        if(listener != null){
            mOkBu.setOnClickListener(listener);
        }
        return this;
    }

    public I99Dialog setControlVisibility(int visible){
        mControls.setVisibility(visible);
        return this;
    }

    public I99Dialog setListViewVisibility(int visible){
        mListView.setVisibility(visible);
        return this;
    }

    public I99Dialog setMessageVisibility(int visible){
        mMessage.setVisibility(visible);
        return this;
    }

    public I99Dialog setProgressVisibility(int visible){
        mProgressBar.setVisibility(visible);
        return this;
    }

    public I99Dialog setContentVisibility(int visible){
        mContentView.setVisibility(visible);
        return this;
    }

    public View getContentView(){
        return mContentView;
    }

    public void addCustomerView(View view){
        mCustomerView = view;
        mProgressBar.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        LinearLayout parent = (LinearLayout)mContentView;
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        parent.addView(mCustomerView , lp);
    }

    public View getCustomerView(){
        return mCustomerView;
    }

    public I99Dialog setBottomViewVisibility(int visible){
        mBottomView.setVisibility(visible);
        return this;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mOkBu;
            case DialogInterface.BUTTON_NEGATIVE:
                return mCancelBu;
            default:
                return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.i99_dialog_ok:
                if(mDialogClick != null){
                    mDialogClick.onClick(I99Dialog.this ,DialogInterface.BUTTON_POSITIVE);
                }
                I99Dialog.this.dismiss();
                break;
            case R.id.i99_dialog_cancel:
                if(mDialogClick != null){
                    mDialogClick.onClick(I99Dialog.this ,DialogInterface.BUTTON_NEGATIVE);
                }
                I99Dialog.this.dismiss();
        default:
            break;
        }
    }


    @Override
    public void show() {
        super.show();
        final int width = getScreenSize(getContext())[0];
        final int height = getScreenSize(getContext())[1];
        LayoutParams lp = mDialogView.getLayoutParams();

        if( width > height){
            lp.width = getScreenSize(getContext())[0] / 2;
        }else{
            lp.width = getScreenSize(getContext())[0] - 10;
        }

        mDialogView.setLayoutParams(lp);
    }
    
    private View.OnClickListener mDismissClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            I99Dialog.this.dismiss();
        }
    };
}
