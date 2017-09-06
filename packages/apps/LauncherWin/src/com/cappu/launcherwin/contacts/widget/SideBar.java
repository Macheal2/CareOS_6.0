package com.cappu.launcherwin.contacts.widget;

import com.cappu.launcherwin.AllApps;
import com.cappu.launcherwin.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SideBar extends View {
    private AllApps mAllApps;
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] Charge = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
            "X", "Y", "Z", "#" };
    private int choose = -1;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / Charge.length;// 获取每一个字母的高度

        for (int i = 0; i < Charge.length; i++) {
            paint.setColor(Color.rgb(0, 0, 0));
            // paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(30);
            // 选中的状态
            if (i == choose) {
                paint.setColor(Color.parseColor("#3399ff"));
                paint.setFakeBoldText(true);
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(Charge[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(Charge[i], xPos, yPos, paint);
            paint.reset();// 重置画笔
        }

    }
    
    public void setContext(AllApps allapps){
        this.mAllApps = allapps;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(mAllApps!=null){
            mAllApps.Hide();
        }
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * Charge.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
        case MotionEvent.ACTION_UP:
            setBackgroundDrawable(new ColorDrawable(0x00000000));
            choose = -1;//
            invalidate();
            if (mTextDialog != null) {
                mTextDialog.setVisibility(View.INVISIBLE);
            }
            break;

        default:
            setBackgroundResource(R.drawable.sidebar_background);
            if (oldChoose != c) {
                if (c >= 0 && c < Charge.length) {
                    if (listener != null) {
                        listener.onTouchingLetterChanged(Charge[c]);
                    }
                    if (mTextDialog != null) {
                        mTextDialog.setText(Charge[c]);
                        mTextDialog.setVisibility(View.VISIBLE);
                    }

                    choose = c;
                    invalidate();
                }
            }

            break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     * 
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     * 
     * @author coder
     * 
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

}