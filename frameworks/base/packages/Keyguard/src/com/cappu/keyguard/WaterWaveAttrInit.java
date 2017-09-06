package com.cappu.keyguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.android.keyguard.R;

public class WaterWaveAttrInit {

    private int progressWidth; // 进度条宽度
    private int progressColor;
    private int progressBgColor;
    private int waterWaveColor;
    private int waterWaveBgColor;
    private int progress2WaterWidth; // 进度条和水波之间的间距
    private boolean showProgress; // 是否显示进度条
    private boolean showNumerical; // 是否显示百分比
    private int fontSize;
    private int textColor;
    private int textChanging;

    private int progress;
    private int maxProgress;
    private String mChangStatus;

    @SuppressLint("Recycle")
    public WaterWaveAttrInit(Context context, AttributeSet attrs, int defStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.WaterWaveProgress, defStyle, 0);
        progressWidth = typedArray.getDimensionPixelOffset(
                R.styleable.WaterWaveProgress_progressWidth, 0);
        progressColor = typedArray.getColor(
                R.styleable.WaterWaveProgress_progressColor, 0xDD009900);
        progressBgColor = typedArray.getColor(
                R.styleable.WaterWaveProgress_progressBgColor, 0xFFBEBEBE);
        waterWaveColor = typedArray.getColor(
                R.styleable.WaterWaveProgress_waterWaveColor, 0xDD009900);
        waterWaveBgColor = typedArray.getColor(
                R.styleable.WaterWaveProgress_waterWaveBgColor, 0xFFDDDDDD);
        progress2WaterWidth = typedArray.getDimensionPixelOffset(
                R.styleable.WaterWaveProgress_progress2WaterWidth, 0);
        showProgress = typedArray.getBoolean(
                R.styleable.WaterWaveProgress_showProgress, true);
        showNumerical = typedArray.getBoolean(
                R.styleable.WaterWaveProgress_showNumerical, true);
        fontSize = typedArray.getDimensionPixelOffset(
                R.styleable.WaterWaveProgress_fontSize, 0);
        textColor = typedArray.getColor(
                R.styleable.WaterWaveProgress_textColor, 0xFFFFFFFF);

        textChanging = typedArray.getColor(
                R.styleable.WaterWaveProgress_textChanging, 0xFF808080);

        progress = typedArray.getInteger(
                R.styleable.WaterWaveProgress_progress, 15);
        maxProgress = typedArray.getInteger(
                R.styleable.WaterWaveProgress_maxProgress, 100);

        mChangStatus = typedArray.getString(R.styleable.WaterWaveProgress_changstatus);
        typedArray.recycle();
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public int getProgressColor() {
        return progressColor;
    }

    public int getProgressBgColor() {
        return progressBgColor;
    }

    public int getWaterWaveColor() {
        return waterWaveColor;
    }

    public int getWaterWaveBgColor() {
        return waterWaveBgColor;
    }

    public int getProgress2WaterWidth() {
        return progress2WaterWidth;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public boolean isShowNumerical() {
        return showNumerical;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextChangingColor(){
        return textChanging;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public String getChangStatus() {
        return mChangStatus;
    }
}
