package com.cappu.keyguard;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import com.android.keyguard.R;

/**
 * Created by lenovo on 15-10-14.
 */
public class BounceView extends ImageView {
    private static final String TAG = "BounceView";
    //画笔
    private Paint mPaint;
    private int radius = 50;
    private float density;
    private RectF rectF;
    private int mWidth, mHeight;
    //起点、终点、当前点
    private int startY, startX = 0, endY, currentY;
    private Bitmap mBitmap = null;
    private int mSharderHeight;

    public BounceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BounceView);
        mBitmap = drawableToBitamp(ta.getDrawable(R.styleable.BounceView_bounce_src));
        mSharderHeight = ta.getDimensionPixelOffset(R.styleable.BounceView_bounce_sharder_height,
                getResources().getDimensionPixelOffset(R.dimen.default_sharder_height));

        ta.recycle();

        density = getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentY == 0) {
            playAnimator();
        } else {
            canvas.drawBitmap(mBitmap, 0, currentY - mSharderHeight / 3 - mBitmap.getHeight(), mPaint);
            drawShader(canvas);
        }
    }

    //动画执行
    private void playAnimator() {
        //我们只需要取Y轴方向上的变化即可
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startY, endY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentY = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator(1.0f));
        valueAnimator.setRepeatCount(-1);
        valueAnimator.setRepeatMode(2);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    /**
     * 绘制阴影部分，由椭圆来支持，根据高度比来底部阴影的大小
     */
    private void drawShader(Canvas canvas) {
        //计算差值高度
        int dx = endY - startY;
        //计算当前点的高度差值
        int dx1 = currentY - startY;
        float ratio = (float) (dx1 * 1.0 / dx);
        if (ratio <= 0.3) {//当高度比例小于0.3，所在比较高的时候就不进行绘制影子
            return;
        }
        int ovalRadius = (int) (radius * ratio * density);
        //设置倒影的颜色
        mPaint.setColor(Color.parseColor("#BB3F3B2D"));
        //绘制椭圆
        rectF = new RectF(startX - ovalRadius / 2 + 55, endY - mSharderHeight, startX + ovalRadius / 2 + 55, endY);

        canvas.drawOval(rectF, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (MeasureSpec.EXACTLY == widthMode) {
            mWidth = widthMeasure;
        } else {
            mWidth = Math.max(mBitmap.getWidth(), 96);
        }

        if (MeasureSpec.EXACTLY == heightMode) {
            mHeight = heightMeasure;
        } else {
            mHeight = mBitmap.getHeight() + mSharderHeight + 40;
        }

        setMeasuredDimension(mWidth, mHeight);

        endY = mHeight;
        startY = mBitmap.getHeight();
    }
}
