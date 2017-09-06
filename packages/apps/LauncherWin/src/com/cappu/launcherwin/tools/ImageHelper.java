package com.cappu.launcherwin.tools;

import com.cappu.launcherwin.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.TypedValue;

public class ImageHelper {
    //public static final int mColor = 0xff424242;
    
    /**
     * @param bitmap
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 10;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 5;
            dst_top = 3;
            dst_right = width - 5;
            dst_bottom = width - 5;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * 获取圆角位图的方法
     * 
     * @param bitmap
     *            需要转化成圆角的位图
     * @param 宽
     * @param 高
     * @param pixels
     *            圆角的度数，数值越大，圆角越大
     * @return 处理后的圆角位图
     */
    public static Bitmap toRoundCorner(Context context,Bitmap bitmap,int width,int height,int pixels) {
        Bitmap output;
        if(width>0 && height>0){
            output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        }else{
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(output);
        
        Rect rect;
        if(width>0 && height>0){
            rect = new Rect(0, 0, width, height);
        }else{
            rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
        
        RectF rectF = new RectF(rect);
        float roundPx = pixels;
        Paint mPaint = new Paint();//这里的Paint 必须实例化一次/因为每一个canvas 对应一个paint/不然后面的第二次进来的图片就显示不出来
        mPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        mPaint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, roundPx, roundPx, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, mPaint);
        return output;
    }


    /**缩小*/
    public static Bitmap Reduce(Bitmap bitmap,int width,int height){
        Matrix resize = new Matrix();
        /** 下面是等比缩放 */
        resize.postScale((float)width / (float) bitmap.getWidth(),(float) height / (float) bitmap.getHeight());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, resize, false);
        return bitmap;
    }
    
    /**将联系人自定义头像换成圆角*/
    public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            final float roundPx = 30;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }
    
}
