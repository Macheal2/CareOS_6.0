
package com.cappu.music;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Show a volume control bar
 */
public class VSeekBarWithText extends VerticalSeekBar {

    static final String TAG = "VSeekBarWithText";
    static final boolean LOCAL_LOGV = false;

    private static DrawableWithText myDraw = null;
    private static Drawable OrigenDraw = null;

    public VSeekBarWithText(Context context) {
        super(context);
        setDefault();
    }

    public VSeekBarWithText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefault();
    }

    public VSeekBarWithText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefault();
    }

    /*
     * (non-Javadoc)
     * @see
     * android.widget.AbsSeekBar#setThumb(android.graphics.drawable.Drawable)
     */
    @Override
    public void setThumb(Drawable thumb) {
        // TODO Auto-generated method stub
        OrigenDraw = thumb;
        super.setThumb(thumb);
    }

    /**
     * Make a standard hint that just contains a text view.
     * 
     * @param context The context to use. Usually your
     *            {@link android.app.Application} or
     *            {@link android.app.Activity} object.
     * @param text The text to show. Can be formatted text.
     */
    public void setDefault() {

        /*************************************************************
         * draw an thumb shape.
         ********/

        // float[] R = new float[]
        // { 10, 10, 10, 10, 10, 10, 10, 10 };
        // RoundRectShape rect_shape = new RoundRectShape(R, null, null);
        // rect_shape.resize(20, 20);

        if ((myDraw == null) && (OrigenDraw != null) && (OrigenDraw != myDraw)) {
            // myDraw = new DrawableWithText();
            // myDraw.setBounds(OrigenDraw.getBounds());

            // myDraw.setPadding(0, 0, 0, 0);
            // myDraw.setIntrinsicHeight(OrigenDraw.getIntrinsicHeight());
            // myDraw.setIntrinsicWidth(OrigenDraw.getIntrinsicWidth());

            // Paint mPaint = myDraw.getPaint();
            // mPaint.setStyle(Paint.Style.FILL);
            // mPaint.setARGB(255, 200, 0, 0);
            // mPaint.setAlpha(255);
            //
            // myDraw.setVisible(true, true);
            // myDraw.setOriginDrawable(OrigenDraw);
        }

        if (myDraw != null)
            super.setThumb(myDraw);

        /**************************************************************
         * draw an thumb shape.
         ********/
        super.setOnSeekBarChangeListener(mListener);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.widget.SeekBar#setOnSeekBarChangeListener(android.widget.SeekBar
     * .OnSeekBarChangeListener)
     */
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        child_listener = l;
    }

    private SeekBar.OnSeekBarChangeListener child_listener = null;

    private SeekBar.OnSeekBarChangeListener mListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (myDraw != null)
                myDraw.setPress(false);

            if (child_listener != null)
                child_listener.onStopTrackingTouch(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (myDraw != null)
                myDraw.setPress(true);

            if (child_listener != null)
                child_listener.onStartTrackingTouch(seekBar);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            if (myDraw != null)
                myDraw.setText("" + progress);

            if (child_listener != null)
                child_listener.onProgressChanged(seekBar, progress, fromUser);
        }
    };

    /**
     * An drawable shape with text on it
     */
    class DrawableWithText extends Drawable {

        private String text = null;
        private float textSize = 20;

        private boolean isPress = false;
        private Drawable OrigenDraw = null;
        private int origin_height = 0;
        private int origin_width = 0;
        private Rect origin_padding = new Rect(0, 0, 0, 0);

        /*
         * (non-Javadoc)
         * @see
         * android.graphics.drawable.ShapeDrawable#draw(android.graphics.Canvas)
         */
        @Override
        public void draw(Canvas canvas) {
            Rect r = getBounds();

            canvas.translate(r.left, r.top);

            /* we'll draw seekbar's origin drawable first */
            if (OrigenDraw != null) {
                OrigenDraw.setBounds(0, 0, origin_width, origin_height);
                OrigenDraw.draw(canvas);
            }

            /* and draw text on it. */
            canvas.save();
            canvas.rotate(90);
            canvas.translate(0, r.top - r.bottom);
            drawText(canvas);
            canvas.restore();
        }

        private void drawText(Canvas canvas) {

            if (isPress)
                return;

            Paint mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setARGB(255, 0, 200, 0);

            mPaint.setTextSize(textSize);
            mPaint.setTextAlign(Paint.Align.CENTER);

            if (text != null)
                canvas.drawText(text, origin_height >> 1, origin_width >> 1,
                        mPaint);

        }

        /*
         * (non-Javadoc)
         * @see android.graphics.drawable.Drawable#onStateChange(int[])
         */
        @Override
        protected boolean onStateChange(int[] state) {
            // TODO Auto-generated method stub
            return super.onStateChange(state);
        }

        public void setText(String t) {
            this.text = t;
        }

        /*
         * (non-Javadoc)
         * @see
         * android.graphics.drawable.Drawable#getPadding(android.graphics.Rect)
         */
        @Override
        public boolean getPadding(Rect padding) {
            // // TODO Auto-generated method stub
            // return super.getPadding(padding);
            padding = origin_padding;

            return true;

        }

        public void setTextSize(float textSize) {
            this.textSize = textSize;
        }

        public void setPress(boolean press) {
            this.isPress = press;
        }

        /*
         * (non-Javadoc)
         * @see android.graphics.drawable.Drawable#getIntrinsicWidth()
         */
        @Override
        public int getIntrinsicWidth() {
            return origin_width;
        }

        /*
         * (non-Javadoc)
         * @see android.graphics.drawable.Drawable#getIntrinsicHeight()
         */
        @Override
        public int getIntrinsicHeight() {
            return origin_height;
        }

        public void setOriginDrawable(Drawable d) {
            OrigenDraw = d;
            origin_height = d.getIntrinsicHeight();
            origin_width = d.getIntrinsicWidth();
            d.getPadding(origin_padding);
        }

        @Override
        public void setAlpha(int alpha) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getOpacity() {
            // TODO Auto-generated method stub
            return 0;
        }
    }
}
