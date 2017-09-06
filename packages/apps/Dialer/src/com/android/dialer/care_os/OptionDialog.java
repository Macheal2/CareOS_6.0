package com.android.dialer.care_os;

import com.android.dialer.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;


import android.content.DialogInterface.OnKeyListener;



public class OptionDialog extends Dialog implements View.OnClickListener,OnKeyListener{

    private TextView mTitle;
    private LinearLayout mContent;
    OnClickListener mListener;

    public OptionDialog(Context context) {
        super(context, R.style.OptionDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.i99_option_dialog, null);
        mTitle = (TextView) v.findViewById(R.id.title);
        mContent = (LinearLayout) v.findViewById(R.id.content);

        addContentView(v,getLayoutParams());
    }

    public void setTitle(int resid){
        mTitle.setText(resid);
    }

	//added by yzs for CareSms begin 20150629
    public Button getItemById(int id) {
		for (int i = 0; i < mContent.getChildCount(); i++) {
			if (id == mContent.getChildAt(i).getId()) {
				return (Button) mContent.getChildAt(i);
			}
		}
		return null;
	}
	//added by yzs for CareSms end 20150629

    public void addButton(int id){
        final int h = getContext().getResources().getDimensionPixelOffset(R.dimen.option_dialog_item_h) ;
        Button bu = new Button(getContext());
        bu.setText(id);
        bu.setTextSize(28);
        bu.setId(id);
        bu.setBackgroundResource(R.drawable.i99_option_dialog_item_bg);
        bu.setMinHeight(h);
        bu.setOnClickListener(this);
        mContent.addView(bu);
    }


    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.OptionDialogAnim);
    }

    private LayoutParams getLayoutParams(){
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
         DisplayMetrics dm = new DisplayMetrics();
        dm = getContext().getResources().getDisplayMetrics();
       lp.width =  dm.widthPixels;
        
        return lp;
    }


    public void setOnClickListener(OnClickListener listener){
        mListener = listener;
    }

    public interface OnClickListener{
        public void onClick(View v);
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
        if(mListener != null){
            mListener.onClick(v);
        }
    }

	@Override
	public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
		Log.i("test", "sdfsd");
		return false;
	}


}
