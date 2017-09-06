package com.cappu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import com.cappu.internal.R;

import com.cappu.theme.ThemeManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class CareButton extends Button{
    
    ThemeManager mThemeManager;
    private int mBackGround;

    public CareButton(Context context) {
        super(context);
    }
    
    public CareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if(mThemeManager == null){
            ThemeManager.init(context);
            mThemeManager = ThemeManager.getInstance();
        }
        if(mThemeManager.getThemeType(context) == ThemeManager.THEME_CLASSICAL){
            mBackGround = R.drawable.shape_green;//a.getResourceId(R.styleable.TopBar_background, R.drawable.shape_green);
        }else{
            mBackGround = R.drawable.topbar_default_bg;
        }
        
        setBackgroundResource(mBackGround);
    }
    
    public CareButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
		if (drawables != null) {
			Drawable drawableLeft = drawables[0];
				if (drawableLeft != null) {
			
				float textWidth = getPaint().measureText(getText().toString());
				int drawablePadding = getCompoundDrawablePadding();
				int drawableWidth = 0;
				drawableWidth = drawableLeft.getIntrinsicWidth();
				float bodyWidth = textWidth + drawableWidth + drawablePadding;
				setPadding(0, 0, (int)(getWidth() - bodyWidth), 0);
				canvas.translate((getWidth() - bodyWidth) / 2, 0);
			}
		}
		super.onDraw(canvas);
	}
}
