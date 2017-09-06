/** 
 * Copyright (C) 2014 The Cappu Android Source Project
 *
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.cappu.cn
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2015年7月2日 上午10:44:31 
 * @author: y.haiyang@qq.com
 * @company: Shang Hai Cappu Co.,Ltd. 
 */
package com.cappu.widget;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.internal.R;
import com.cappu.util.CareUtils;
/**
 * @hide
 */
public class CareMenu extends Dialog implements View.OnClickListener{

    private final int TEXT_SIZE = 24;
    private static final int ITEM_MARGIN = 1;
    private final TextView mTitle;
    private final LinearLayout mContent;
    OnClickListener mListener;

    public CareMenu(Context context) {
        super(context, R.style.CareMenuStyle);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.care_menu, null);
        mTitle = (TextView) v.findViewById(R.id.title);
        mContent = (LinearLayout) v.findViewById(R.id.content);

        addContentView(v, getLayoutParams());
    }

    @Override
    public void setTitle(int resid){
        mTitle.setText(resid);
    }

    public void setTitle(String title){
        mTitle.setText(title);
    }

    public void addButton(int id){
        addButton(id, R.drawable.care_menu_item_bg);
    }

    public void addButton(int id, int backgroud){
        addButton(id, backgroud, mContent);
    }
    public void addCancelButton(Button btn){
    	 final int h =130;
    	 btn.setText(R.string.no);
    	 btn.setTextSize(TEXT_SIZE);
    	 btn.setTextColor(Color.BLACK);
    	 btn.setBackgroundResource(R.drawable.care_menu_item_bg);
    	 btn.setMinHeight(h);
    	 btn.setOnClickListener(this);
    	 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
         lp.topMargin = ITEM_MARGIN +16;
         lp.bottomMargin  = ITEM_MARGIN;
 		 lp.gravity=Gravity.CENTER;
         btn.setLayoutParams(lp);
         mContent.addView(btn, mContent.getChildCount());
    }
    public void addButton(int id, int backgroud, LinearLayout root){
        final int h =120;
        Button bu = new Button(getContext());
        bu.setText(id);
        bu.setTextSize(TEXT_SIZE);
        bu.setTextColor(Color.BLACK);
        bu.setId(id);
        bu.setBackgroundResource(backgroud);
        bu.setMinHeight(h);
        bu.setOnClickListener(this);
        bu.setLayoutParams(getChildLayoutParams());
        root.addView(bu);
    }

    public Button getItemById(int id){
        for (int i=0; i< mContent.getChildCount(); i++){
            if(id == mContent.getChildAt(i).getId()){
                return (Button)mContent.getChildAt(i);
            }
        }
        return null;
    }

    public void addButton(String name){
        addButton(name, R.drawable.care_menu_item_bg);
    }

    public void addButton(String name, int backgroud){
        addButton(name, backgroud, mContent);
    }

    public void addButton(String name, int backgroud, LinearLayout root){
        final int h = 120 ;
        Button bu = new Button(getContext());
        bu.setText(name);
        bu.setTextSize(TEXT_SIZE);
        bu.setTextColor(Color.BLACK);
        bu.setTag(name);
        bu.setBackgroundResource(backgroud);
        bu.setMinHeight(h);
        bu.setOnClickListener(this);
        bu.setLayoutParams(getChildLayoutParams());
        root.addView(bu);
    }

    public Button getButtonById(String tag){
        for (int i=0; i< mContent.getChildCount(); i++){
            Button child = (Button)mContent.getChildAt(i);
            String _tag = (String)child.getTag();
            if(TextUtils.equals(tag,_tag)){
                return child;
            }
        }
        return null;
    }

    public void addButton(int id, String text, int backgroud, LinearLayout root){
        final int h = 120 ;
        Button bu = new Button(getContext());
        bu.setId(id);
        bu.setText(text);
        bu.setTextSize(TEXT_SIZE);
        bu.setTextColor(Color.BLACK);
        bu.setTag(text);
        bu.setBackgroundResource(backgroud);
        bu.setMinHeight(h);
        bu.setOnClickListener(this);
        bu.setLayoutParams(getChildLayoutParams());
        root.addView(bu);
    }

    public void addGroupButtons(int[] ids){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.care_menu_group_root, null);
        for(int i=0;i<ids.length; i++){
            if(i==0){
                addButton(ids[i],R.drawable.care_menu_item_top_bg, root);
            }else if(i == ids.length -1){
                addButton(ids[i],R.drawable.care_menu_item_bottom_bg, root);
            }else{
                addButton(ids[i],R.drawable.care_menu_item_middle_bg, root);
            }
        }
        mContent.addView(root);
    }

    public void addGroupButtons(String[] ids){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.care_menu_group_root, null);
        for(int i=0;i<ids.length; i++){
            if(i==0){
                addButton(ids[i],R.drawable.care_menu_item_top_bg, root);
            }else if(i == ids.length -1){
                addButton(ids[i],R.drawable.care_menu_item_bottom_bg, root);
            }else{
                addButton(ids[i],R.drawable.care_menu_item_middle_bg, root);
            }
        }
        mContent.addView(root);
    }


    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.CareMenuAnim);
    }

    private LayoutParams getLayoutParams(){
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = CareUtils.getScreenSize(getContext())[0];
        return lp;
    }


    private LinearLayout.LayoutParams getChildLayoutParams(){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.topMargin = ITEM_MARGIN ;
        lp.bottomMargin  = ITEM_MARGIN;
		lp.gravity=Gravity.CENTER;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU && isShowing()){
            dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }

}
