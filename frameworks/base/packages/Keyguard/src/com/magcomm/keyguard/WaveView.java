/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.magcomm.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelXorXfermode;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.keyguard.R;

public class WaveView extends View {
    private Context mContext;
    private Paint mAboveWavePaint = new Paint();
    private Paint mBelowWavePaint = new Paint();
    private Path mAboveWavePath = new Path();
    private Path mBelowWavePath = new Path();
    
    private final int DEFAULT_BACKGROUND_WAVE_COLOR;
    private final int DEFAULT_WARNING_WAVE_COLOR;
    private final int DEFAULT_ABOVE_WAVE_COLOR;
    private final int DEFAULT_BELOW_WAVE_COLOR;
    private final float DEFAULT_WAVE_HZ = 0.13f;//0.13f 0.09f 0.05f
    private final int DEFAULT_BACKGROUND_WAVE_ALPHA = 50;//0~255
    private final int DEFAULT_ABOVE_WAVE_ALPHA = 255;//0~255
    private final int DEFAULT_BELOW_WAVE_ALPHA = 200;//0~255
    
    private Bitmap mDstBgBmp;//遮罩图片
    private final float X_SPACE = 20;
    private final double PI2 = 2 * Math.PI;
    private final float mWaveMultiple = 0.5f;//WAVE_LENGTH_MULTIPLE_MIDDLE 1.5f 1.0f 0.5f
    private final int mWaveHeight = 16;//WAVE_HEIGHT_MIDDLE 16 8 5//波幅的高度
    private float mBlowOffset = mWaveHeight * 0.6f;//mBlowOffset = mWaveHeight * 0.4f;
    private float mAboveOffset = 0.1f;
    private float mWaveLength;//波幅的长度
    private float mWaveHz = DEFAULT_WAVE_HZ;//波幅的速度
    private float mMaxRight;
    private int right, bottom;
    private double omega;
    private double mPercent = 0.5;//百分比
    private boolean mShowEmpty = false;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private boolean mWarningShow;
    
    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        DEFAULT_WARNING_WAVE_COLOR = Color.RED;
        DEFAULT_BACKGROUND_WAVE_COLOR = Color.BLACK;
        DEFAULT_ABOVE_WAVE_COLOR = this.getResources().getColor(R.color.magcomm_above_wave_color);
        DEFAULT_BELOW_WAVE_COLOR = this.getResources().getColor(R.color.magcomm_below_wave_color);
        initializePainters();

        mDstBgBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.flow_circular_bg);
    }
    
    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (WaveView.this) {
                long start = System.currentTimeMillis();
                calculatePath();
                invalidate();
                long gap = 35 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }
    
    public void initializePainters() {
        mAboveWavePaint.setColor(mWarningShow ? DEFAULT_WARNING_WAVE_COLOR : DEFAULT_ABOVE_WAVE_COLOR);
        mAboveWavePaint.setAlpha(DEFAULT_ABOVE_WAVE_ALPHA);
        mAboveWavePaint.setStyle(Paint.Style.FILL);
        mAboveWavePaint.setAntiAlias(true);

        mBelowWavePaint.setColor(mWarningShow ? DEFAULT_WARNING_WAVE_COLOR : DEFAULT_BELOW_WAVE_COLOR);
        mBelowWavePaint.setAlpha(DEFAULT_BELOW_WAVE_ALPHA);
        mBelowWavePaint.setStyle(Paint.Style.FILL);
        mBelowWavePaint.setAntiAlias(true);
    }
    
    /**
     * calculate wave track
     */
    private void calculatePath() {
        mAboveWavePath.reset();
        mBelowWavePath.reset();

        getWaveOffset();

        float y;
        mAboveWavePath.moveTo(0, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x + mAboveOffset)+this.getHeight()*mPercent);
            mAboveWavePath.lineTo(x, y);
        }
        mAboveWavePath.lineTo(right, bottom);

        mBelowWavePath.moveTo(0, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x + mBlowOffset)+this.getHeight()*mPercent);
            mBelowWavePath.lineTo(x, y);
        }
        mBelowWavePath.lineTo(right, bottom);
    }
    
    private void getWaveOffset() {
        if (mBlowOffset > Float.MAX_VALUE - 100) {
            mBlowOffset = 0;
        } else {
            mBlowOffset += mWaveHz;
        }

        if (mAboveOffset > Float.MAX_VALUE - 100) {
            mAboveOffset = 0;
        } else {
            mAboveOffset += mWaveHz;
        }
    }
    
    private void startWave() {
        if (this.getWidth() != 0) {
            int width = this.getWidth();
            mWaveLength = width * mWaveMultiple;
            right = getRight();
            bottom = getBottom();
            mMaxRight = right + X_SPACE;
            omega = PI2 / mWaveLength;
        }
    }
    
    public void setShowWarningColor(boolean warningShow){
        mWarningShow = warningShow;
        initializePainters();
    }
    
    public void setPercent(double percent){
        this.mPercent = 1.0 - percent;
    }
    
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (mWaveLength == 0) {
                startWave();
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = mDstBgBmp.getHeight();
        setMeasuredDimension(height, height);//重新定义view大小
    }
    
    @Override
    public void setVisibility(int visibility){
        super.setVisibility(visibility);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        Paint mPaint = new Paint();
        
        int circle_w = this.getWidth() / 2;
        int circle_h = this.getHeight() / 2;
        int radius = circle_w - 5;
        mPaint.setAntiAlias(true);// 设置画笔为无锯齿
        mPaint.setColor(DEFAULT_BACKGROUND_WAVE_COLOR);// 设置画笔颜色
        mPaint.setStrokeWidth((float) 2.0);// 线宽
        mPaint.setStyle(Style.FILL);// 实心效果
        mPaint.setAlpha(DEFAULT_BACKGROUND_WAVE_ALPHA);
        canvas.drawCircle((float) circle_w, (float) circle_w, radius, mPaint);// 绘制圆形内背景色
        mPaint.setColor(mWarningShow ? DEFAULT_WARNING_WAVE_COLOR : DEFAULT_ABOVE_WAVE_COLOR);// 设置画笔颜色
        mPaint.setStyle(Style.STROKE);// 空心效果
        mPaint.setAlpha(255);
        canvas.drawCircle((float) circle_w, (float) circle_w, radius, mPaint);// 绘制圆形外圈
        
        mPaint.reset();
        int sc = canvas.saveLayer(0, getTop(), getRight(), getBottom(), null, Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);//保存画布
        canvas.drawPath(mBelowWavePath, mBelowWavePaint);
        canvas.drawPath(mAboveWavePath, mAboveWavePaint);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));//SRC_IN 
        canvas.drawBitmap(mDstBgBmp, 0f, 0f, mPaint);
        mPaint.setXfermode(null);//取消合并模式
        canvas.restoreToCount(sc);//恢复画布
        super.onDraw(canvas);
    }
}
