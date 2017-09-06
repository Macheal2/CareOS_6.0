package com.android.dialer.care_os;


import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.EditText;

public class AutoScaleTextSizeWatcher implements TextWatcher {

	
	private EditText mTarget;
	private Paint mPaint;
	private Context mContext;
	private int minTextsize;
	private int maxTextsize;
	private int editWidth;
	private int deltaTextSize;
	private int currentTextSize;
	

	public AutoScaleTextSizeWatcher(EditText edit){
		mTarget = edit;
		mContext = edit.getContext();
		
		mPaint = new Paint();
		mPaint.set(edit.getPaint());
		mTarget.addTextChangedListener(this);
	}
	
	public void setAutoScaleParameters(int minTextsize,int maxTextsize,int deltaTextSize,int editWidth){
		this.minTextsize=minTextsize;
		this.maxTextsize=maxTextsize;
		this.editWidth=editWidth;
		this.deltaTextSize=deltaTextSize;
		
		this.currentTextSize=maxTextsize;
		mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
		
	}
	
	
	public void trigger(boolean delete){
		autoScaleTextSize(true);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		autoScaleTextSize(false);
	}
	
	
	public void autoScaleTextSize(boolean fage){
		
		final String editContext=mTarget.getText().toString();
		if(editContext.length()==0){
			currentTextSize=maxTextsize;
			mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
			return ;
		}
		int current=currentTextSize;
		int previously=current;
		final Paint paint=mPaint;
		paint.setTextSize(current);
		
		int inputWidth=(int) paint.measureText(editContext);
		//要分修改之后与修改之前
		if(!fage){//修改之后
			while(inputWidth > editWidth && current>minTextsize){
				current-=deltaTextSize;
				
				if(current<minTextsize){
					current=minTextsize;
					break;
				}
				paint.setTextSize(current);
				inputWidth=(int) paint.measureText(editContext);
				
			}
		}else {//修改之前
			
			while(inputWidth < editWidth && current < maxTextsize){
				previously=current;
				current+=deltaTextSize;
				
				if(current>maxTextsize){
					current=maxTextsize;
					break;
				}
				paint.setTextSize(current);
				inputWidth=(int) paint.measureText(editContext);
			}
			
			//break 之后执行这个
			 paint.setTextSize(current);
	         inputWidth = (int) paint.measureText(editContext);
	            
			if(inputWidth > editWidth){
				current=previously;
			}
			
		}
		//最后执行设置字体大少
		currentTextSize = current;
        mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, current);
	}
	
	
	

}
