package com.cappu.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.cappu.internal.R;
/**
 * Created by lenovo on 15-12-3.
 */
public class Constant {
    public static final int STYLE_SINGLE = 0, TYPE_SWIT = 0; //单行, Switch
    public static final int STYLE_TOP = 1, TYPE_CBOX = 1; //多行,首行 CheckBox
    public static final int STYLE_MID = 2, TYPE_MULE = 2; //多行, 中间行 有返回值
    public static final int STYLE_BOTTOM = 3, TYPE_MORE = 3; //多行, 最后一行 无返回值


    public static int getPaddings(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.item_padding_left_right);
    }

    public static int getDefaultTextSize(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.default_text_size);
    }

    public static ColorStateList getDefaultTextColor(Context context) {
        return ColorStateList.valueOf(Color.BLACK);
    }

    public static String getDefaultTitle(Context context) {
        return context.getResources().getString(R.string.default_string);
    }

    public static int getTipViewSize(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.tipview_size);
    }

    public static int getIconViewSize(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.iconview_size);
    }

    public static Drawable getBgDrawable(Context context, int rsId) {
        return context.getResources().getDrawable(rsId);
    }
}
