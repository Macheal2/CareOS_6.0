package com.cappu.keyguard;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import com.android.keyguard.R;

import java.io.InputStream;
/**
 * @author Administrator
 */
public class WaterWaveProgress extends View {
    private static final String TAG = "WaterWaveProgress";

    // 水的画笔 // 画圆环的画笔// 进度百分比的画笔
    private Paint mPaintWater = null, mRingPaint = null, mTextPaint = null, mTextPercent = null, mChanging = null;
    //private Bitmap mBitmap;
    // 圆环颜色 // 圆环背景颜色 // 当前进度 //水波颜色 // 水波背景色 //进度条和水波之间的距离 //进度百分比字体大小
    // //进度百分比字体颜色
    private int mRingColor, mRingBgColor, mWaterColor, mWaterBgColor, mFontSize, mTextColor, mTextChanging;
    // 进度 //浪峰个数
    float crestCount = 1.5f;

    int mProgress = 10, mMaxProgress = 100;
    String mChangStatus;

    // 画布中心点
    private Point mCenterPoint;
    // 圆环宽度
    private float mRingWidth, mProgress2WaterWidth;
    // 是否显示进度条 //是否显示进度百分比
    private boolean mShowProgress = false, mShowNumerical = true;

    /**
     * 产生波浪效果的因子
     */
    private long mWaveFactor = 0L;
    /**
     * 正在执行波浪动画
     */
    private boolean isWaving = false;
    /**
     * 振幅
     */
    private float mAmplitude = 25.0F; // 20F
    /**
     * 波浪的速度
     */
    private float mWaveSpeed = 0.020F; // 0.020F
    /**
     * 水的透明度
     */
    private int mWaterAlpha = 170; // 255
    WaterWaveAttrInit attrInit;
    
    private Paint mPaint = null;//new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaint2 = null;//new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mRotate;
    private Matrix mMatrix = new Matrix();
    private Shader mShader;

    private MyHandler mHandler = null;

    private static class MyHandler extends Handler {
        private WeakReference<WaterWaveProgress> mWeakRef = null;

        private int refreshPeriod = 100;

        public MyHandler(WaterWaveProgress host) {
            mWeakRef = new WeakReference<WaterWaveProgress>(host);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mWeakRef.get() != null) {
                mWeakRef.get().invalidate();
                sendEmptyMessageDelayed(0, refreshPeriod);
            }
        }
    }

    public WaterWaveProgress(Context paramContext) {
        super(paramContext);
    }

    public WaterWaveProgress(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WaterWaveProgress(Context context, AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        attrInit = new WaterWaveAttrInit(context, attrs, defStyleAttr);
        init(context);
    }

    private Bitmap readBitMap(Context context, int resId){     
        BitmapFactory.Options opt = new BitmapFactory.Options();     
        opt.inPreferredConfig = Bitmap.Config.RGB_565;      
        opt.inPurgeable = true;     
        opt.inInputShareable = true;    
        InputStream is = context.getResources().openRawResource(resId);        
        return BitmapFactory.decodeStream(is,null,opt);        
    } 

    @SuppressLint("NewApi")
    private void init(Context context) {
        mCenterPoint = new Point();
        mRingColor = attrInit.getProgressColor();
        mRingBgColor = attrInit.getProgressBgColor();
        mWaterColor = attrInit.getWaterWaveColor();
        mWaterBgColor = attrInit.getWaterWaveBgColor();
        mRingWidth = attrInit.getProgressWidth();

        mChangStatus = attrInit.getChangStatus();
        mProgress2WaterWidth = attrInit.getProgress2WaterWidth();
        mShowProgress = attrInit.isShowProgress();
        mShowNumerical = attrInit.isShowNumerical();
        mFontSize = attrInit.getFontSize();
        mTextColor = attrInit.getTextColor();
        mTextChanging = attrInit.getTextChangingColor();

        mProgress = attrInit.getProgress();
        mMaxProgress = attrInit.getMaxProgress();
        //mBitmap = readBitMap(context, R.drawable.cappu_mirror);//BitmapFactory.decodeResource(context.getResources(), R.drawable.cappu_mirror);
        // 如果手机版本在4.0以上,则开启硬件加速
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            // setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(mRingWidth);

        mChanging = new Paint();
        mChanging.setAntiAlias(true);
        mChanging.setColor(mTextColor);
        mChanging.setTextSize(36f);
        mChanging.setShadowLayer(2.0f, 0f, 4.0f, mTextChanging);

        mPaintWater = new Paint();
        //mPaintWater.setAntiAlias(true);
		mPaintWater.setStrokeWidth(1.0F);
        mPaintWater.setColor(mWaterColor);
        // mPaintWater.setColor(getResources().getColor(mWaterColor));
        mPaintWater.setAlpha(mWaterAlpha);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mFontSize);

        mTextPercent = new Paint();
        mTextPercent.setAntiAlias(true);
        mTextPercent.setColor(mTextColor);
        mTextPercent.setStyle(Paint.Style.FILL);
        mTextPercent.setTextSize(62f);

        mHandler = new MyHandler(this);
	}

	public void animateWave() {
		if (!isWaving) {
			mWaveFactor = 0L;
			isWaving = true;
			mHandler.sendEmptyMessage(0);
		}
	}

    public void stopAnimate(){
        isWaving = false;
        invalidate();
    }

	@SuppressLint({ "DrawAllocation", "NewApi" })
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 获取整个View（容器）的宽、高
		int width = getWidth();
		int height = getHeight();
		width = height = (width < height) ? width : height;
		mAmplitude = width / 20f;

        mCenterPoint.x = width / 2;
        mCenterPoint.y = height / 2;
        
        if(isWaving){
            if(mShader == null){
                mShader = new SweepGradient(mCenterPoint.x, mCenterPoint.y,
                    new int[]{0xFF09F68C, 0xFFB0F44B, 0xFFE8DD30, 0xFFF1CA2E, 0xFFFF902F, 0xFFFF6433}, null);
            }
            mPaint.setShader(mShader); 

            mMatrix.setRotate(mRotate, mCenterPoint.x, mCenterPoint.y);
            mShader.setLocalMatrix(mMatrix);      
        }else{
            mPaint.setColor(mRingColor);
        }
        
        mPaint.setStyle(Paint.Style.STROKE);
        PathEffect effect = new DashPathEffect(new float[]{4, 2, 4, 2}, 1);
        mPaint.setPathEffect(effect);
        mPaint.setStrokeWidth(35);
        
		 // 重新设置进度条的宽度和水波与进度条的距离,,至于为什么写在这,我脑袋抽了可以不
		mRingWidth = mRingWidth == 0 ? width / 20 : mRingWidth;
		mProgress2WaterWidth = mProgress2WaterWidth == 0 ? mRingWidth * 0.6f
				: mProgress2WaterWidth;
		mRingPaint.setStrokeWidth(mRingWidth);
        mTextPaint.setTextSize(mFontSize == 0 ? width / 4 : mFontSize);
		if (VERSION.SDK_INT==VERSION_CODES.JELLY_BEAN) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}else {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}		

		RectF oval = new RectF();
		oval.left = mRingWidth / 2;
		oval.top = mRingWidth / 2;
		oval.right = width - mRingWidth / 2;
		oval.bottom = height - mRingWidth / 2;

		if (isInEditMode()) {
			mRingPaint.setColor(mRingBgColor);
			canvas.drawArc(oval, -90, 360, false, mRingPaint);
			mRingPaint.setColor(mRingColor);
			canvas.drawArc(oval, -90, 90, false, mRingPaint);
			canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mCenterPoint.x
					- mRingWidth - mProgress2WaterWidth, mPaintWater);
			return;
		}

		// 如果没有执行波浪动画，或者也没有指定容器宽高，就画个简单的矩形
		if ((width == 0) || (height == 0) || isInEditMode()) {
			canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, width / 2
					- mProgress2WaterWidth - mRingWidth, mPaintWater);
			return;
		}

        // 水与边框的距离
        //float waterPadding = mShowProgress ? mRingWidth + mProgress2WaterWidth
        //        : 0;

        float waterPadding = mRingWidth + 65;
		// 水最高处
		int waterHeightCount = mShowProgress ? (int) (height - waterPadding * 2)
				: height;

		// 重新生成波浪的形状
		mWaveFactor++;
		if (mWaveFactor >= Integer.MAX_VALUE) {
			mWaveFactor = 0L;
		}

        // 画进度条背景
        /*mRingPaint.setColor(mRingBgColor);
        canvas.drawCircle(width / 2, width / 2, waterHeightCount / 2
                + waterPadding - mRingWidth / 2, mRingPaint);
        mRingPaint.setColor(mRingColor);

        canvas.drawArc(oval, -90, (mProgress * 1f) / mMaxProgress * 360f, false,
                mRingPaint);*/
        mRotate += 1;
        if (mRotate >= 360) {
            mRotate = mRotate % 360;
        }
        invalidate();
        getArc(canvas, mCenterPoint.x, mCenterPoint.y, 305, 0, 360, mPaint);

        if (mProgress == mMaxProgress) {
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, width / 2
                    - mProgress2WaterWidth - mRingWidth - waterPadding / 2 - 10, mPaintWater);
        }else{
            // 计算出水的高度
            float waterHeight = waterHeightCount * (1 - (mProgress * 1f) / mMaxProgress)
                    + waterPadding;
            int staticHeight = (int) (waterHeight + mAmplitude);
            Path mPath = new Path();
            mPath.reset();
            if (mShowProgress) {
                mPath.addCircle(width / 2, width / 2, waterHeightCount / 2,
                        Direction.CCW);
            } else {
                mPath.addCircle(width / 2, width / 2, waterHeightCount / 2,
                        Direction.CCW);
            }
            // canvas添加限制,让接下来的绘制都在园内
            canvas.clipPath(mPath, Op.REPLACE);
            Paint bgPaint = new Paint();
            bgPaint.setColor(mWaterBgColor);
            // 绘制背景
            //canvas.drawRect(waterPadding, waterPadding, waterHeightCount
            //        + waterPadding, waterHeightCount + waterPadding, bgPaint);
            // 绘制静止的水
            canvas.drawRect(waterPadding, staticHeight, waterHeightCount
                    + waterPadding, waterHeightCount + waterPadding, mPaintWater);

            // 待绘制的波浪线的x坐标
            int xToBeDrawed = (int) waterPadding;
            int waveHeight = (int) (waterHeight - mAmplitude
                    * Math.sin(Math.PI
                    * (2.0F * (xToBeDrawed + (mWaveFactor * width)
                    * mWaveSpeed)) / width));
            // 波浪线新的高度
            int newWaveHeight = waveHeight;
            while (true) {
                if (xToBeDrawed >= waterHeightCount + waterPadding) {
                    break;
                }
                // 根据当前x值计算波浪线新的高度
                newWaveHeight = (int) (waterHeight - mAmplitude
                        * Math.sin(Math.PI
                        * (crestCount * (xToBeDrawed + (mWaveFactor * waterHeightCount)
                        * mWaveSpeed)) / waterHeightCount));

                // 先画出梯形的顶边
                //canvas.drawLine(xToBeDrawed, waveHeight, xToBeDrawed + 1,
                //        newWaveHeight, mPaintWater);

                // 画出动态变化的柱子部分
                canvas.drawLine(xToBeDrawed, newWaveHeight, xToBeDrawed + 1,
                        staticHeight, mPaintWater);
                xToBeDrawed++;
                waveHeight = newWaveHeight;
            }
        }

        if (mShowNumerical) {
            if (mChangStatus != null) {
                float changedWidth = mChanging.measureText(mChangStatus, 0,
                        mChangStatus.length());
                canvas.drawText(mChangStatus, mCenterPoint.x - changedWidth / 2,
                        220, mChanging);
            }
            String progressTxt = String.format("%.0f", (mProgress * 1f) / mMaxProgress
                    * 100f);
            String progressPrecent = " %";

            float txtWidth = mTextPaint.measureText(progressTxt, 0,
                    progressTxt.length());

            float parcentWidth = mTextPercent.measureText(progressPrecent, 0,
                    progressPrecent.length());

            canvas.drawText(progressTxt, mCenterPoint.x - txtWidth / 2 - parcentWidth / 2,
                    mCenterPoint.x * 1.5f - mFontSize / 2, mTextPaint);

            canvas.drawText(progressPrecent, mCenterPoint.x + txtWidth / 2 - parcentWidth * 3 / 4,
                    mCenterPoint.x * 1.45f - mFontSize / 2, mTextPercent);
        }

        //canvas.drawBitmap(mBitmap, -18 ,-18, mPaint2);
    }

    public void getArc(Canvas canvas, float o_x, float o_y, float r,
                       float startangel, float endangel, Paint paint) {
        RectF rect = new RectF(o_x - r, o_y - r, o_x + r, o_y + r);
        Path path = new Path();
        path.moveTo(o_x, o_y);
        path.lineTo((float) (o_x + r * Math.cos(startangel * Math.PI / 180))
                , (float) (o_y + r * Math.sin(startangel * Math.PI / 180)));
        path.lineTo((float) (o_x + r * Math.cos(endangel * Math.PI / 180))
                , (float) (o_y + r * Math.sin(endangel * Math.PI / 180)));
        path.addArc(rect, startangel, endangel - startangel);
        canvas.clipPath(path);
        canvas.drawCircle(o_x, o_y, 232, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //int width = widthMeasureSpec;
        //int height = heightMeasureSpec;
		int widthMesasure = MeasureSpec.getSize(widthMeasureSpec);
        int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if(MeasureSpec.EXACTLY == widthMode){
            width = widthMeasureSpec;
        }else{
            width = getPaddingLeft() + getPaddingRight() + widthMesasure;
        }

        if(MeasureSpec.EXACTLY == heightMode){
            height = heightMeasure;
        }else{
            height = getPaddingTop() + heightMeasure + getPaddingBottom();
        }
        width = height = (width < height) ? width : height;
        setMeasuredDimension(width, height);        
    }

    /**
     * 设置波浪的振幅
     */
    public void setAmplitude(float amplitude) {
        mAmplitude = amplitude;
    }

    /**
     * 设置水的透明度
     *
     * @param alpha 透明的百分比，值为0到1之间的小数，越接近0越透明
     */
    public void setWaterAlpha(float alpha) {
        mWaterAlpha = (int) (255.0F * alpha);
        mPaintWater.setAlpha(mWaterAlpha);
    }

    /**
     * 设置水的颜色
     */
    public void setWaterColor(int color) {
        mWaterColor = color;
    }

    /**
     * 设置当前进度
     */
    public void setProgress(int progress) {
        progress = progress > 100 ? 100 : progress < 0 ? 0 : progress;
        mProgress = progress;
        invalidate();
    }

    /**
     * 获取进度 动画时会用到
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * 设置波浪速度
     */
    public void setWaveSpeed(float speed) {
        mWaveSpeed = speed;
    }

    /**
     * 是否显示进度条
     *
     * @param boolean
     */
    public void setShowProgress(boolean b) {
        mShowProgress = b;
    }

    /**
     * 是否显示进度值
     *
     * @param boolean
     */
    public void setShowNumerical(boolean b) {
        mShowNumerical = b;
    }

    /**
     * 设置进度条前景色
     *
     * @param mRingColor
     */
    public void setmRingColor(int mRingColor) {
        this.mRingColor = mRingColor;
    }

    /**
     * 设置进度条背景色
     *
     * @param mRingBgColor
     */
    public void setmRingBgColor(int mRingBgColor) {
        this.mRingBgColor = mRingBgColor;
    }

    /**
     * 设置水波颜色
     *
     * @param mWaterColor
     */
    public void setmWaterColor(int mWaterColor) {
        this.mWaterColor = mWaterColor;
    }

    /**
     * 设置水波背景色
     *
     * @param mWaterBgColor
     */
    public void setWaterBgColor(int mWaterBgColor) {
        this.mWaterBgColor = mWaterBgColor;
    }

    /**
     * 设置进度值显示字体大小
     *
     * @param mFontSize
     */
    public void setFontSize(int mFontSize) {
        this.mFontSize = mFontSize;
    }

    /**
     * 设置进度值显示字体颜色
     *
     * @param mTextColor
     */
    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    /**
     * 设置进度条最大值
     *
     * @param mMaxProgress
     */
    public void setMaxProgress(int mMaxProgress) {
        this.mMaxProgress = mMaxProgress;
    }

    /**
     * 设置浪峰个数
     *
     * @param crestCount
     */
    public void setCrestCount(float crestCount) {
        this.crestCount = crestCount;
    }

    /**
     * 设置进度条宽度
     *
     * @param mRingWidth
     */
    public void setRingWidth(float mRingWidth) {
        this.mRingWidth = mRingWidth;
    }

    /**
     * 设置水波到进度条之间的距离
     *
     * @param mProgress2WaterWidth
     */
    public void setProgress2WaterWidth(float mProgress2WaterWidth) {
        this.mProgress2WaterWidth = mProgress2WaterWidth;
    }

    public void setChangStatus(String changStatus) {
        this.mChangStatus = changStatus;
    }
}
