package com.cappu.launcherwin.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoAjustSizeTextView extends TextView {

    private Paint textPaint;
    private float preferredTextSize;
    private float minTextSize;

    public AutoAjustSizeTextView(Context context) {
        this(context, null);
    }

    public AutoAjustSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.textPaint = new Paint();
        this.minTextSize = 5f;
        this.preferredTextSize = this.getTextSize();
    }

    /**
     * Set the minimum text size for this view
     * 
     * @param minTextSize
     *            The minimum text size
     */
    public void setMinTextSize(float minTextSize) {
        this.minTextSize = minTextSize;
    }

    /**
     * Resize the text so that it fits
     * 
     * @param text
     *            The text. Neither <code>null</code> nor empty.
     * @param textWidth
     *            The width of the TextView. > 0
     */
    private void refitText(String text, int textWidth) {
        if (textWidth <= 0 || text == null || text.length() == 0)
            return;

        // the width
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();

        final float threshold = 0.5f; // How close we have to be

        this.textPaint.set(this.getPaint());

        while ((this.preferredTextSize - this.minTextSize) > threshold) {
            float size = (this.preferredTextSize + this.minTextSize) / 2;
            this.textPaint.setTextSize(size);
            if (this.textPaint.measureText(text) >= targetWidth)
                this.preferredTextSize = size; // too big
            else
                this.minTextSize = size; // too small
        }
        // Use min size so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.minTextSize);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        this.refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        if (width != oldwidth)
            this.refitText(this.getText().toString(), width);
    }
}