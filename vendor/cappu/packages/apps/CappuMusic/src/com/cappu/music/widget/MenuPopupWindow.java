package com.cappu.music.widget;

import com.cappu.music.BatchAcitvity;
import com.cappu.music.BatchAddAcitvity;
import com.cappu.music.MusicInventory;
import com.cappu.music.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class MenuPopupWindow extends PopupWindow {

    private View mMenuView;
    LinearLayout mAdd, mBatch;

    public MenuPopupWindow(final Activity context,final MusicInventory mMusicInventory,final long InventoryId) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.menu_dialog, null);
        // 个人资料
        mAdd = (LinearLayout) mMenuView.findViewById(R.id.add);
        mAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, BatchAddAcitvity.class).putExtra("InventoryId", InventoryId).putExtra("InventoryName", mMusicInventory.getInventoryName()));
                dismiss();

            }
        });

        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();

        // 意见反馈
        mBatch = (LinearLayout) mMenuView.findViewById(R.id.batch);
        mBatch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, BatchAcitvity.class).putExtra("InventoryId",InventoryId).putExtra("InventoryName", mMusicInventory.getInventoryName()));
                dismiss();

            }
        });

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w / 2);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.dialog_style);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}
