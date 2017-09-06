
package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

public class KookProgress extends ProgressBar {
    String text;
    Paint mPaint;

    public KookProgress(Context context, AttributeSet attrs) { // 必须要写的构造方法
        super(context, attrs);
        initText();
    }

    @Override
    public synchronized void setProgress(int progress) {
        // TODO Auto-generated method stub
        setText(progress);
        invalidate();
        super.setProgress(progress);
    }
    
    /**显示的百分比*/
    public void setProgressTextPercentage(int progress){
        setText(progress);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        // this.setText();
        if (rect == null){
            rect = new Rect();
        }
        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
        int x = (getWidth() / 2) - rect.centerX();
        int y = (getHeight() / 2) - rect.centerY();
        canvas.drawText(this.text, x, y, this.mPaint);
    }

    private Rect rect = new Rect();
    // 初始化，画笔
    private void initText() {
        this.mPaint = new Paint();
        this.mPaint.setColor(Color.parseColor("#FFFFFF"));
        this.mPaint.setTextSize(30);

    }

    private void setText() {
        setText(this.getProgress());
    }

    // 设置文字内容
    private void setText(int progress) {
        int i = 0;
        if(getMax() != 0){
            i = (progress * 100) / this.getMax();
        }
        this.text = String.valueOf(i) + "%";
    }

}
