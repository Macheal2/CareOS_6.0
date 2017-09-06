
package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class I99ThemeToast {

    public static void makeText(Context context, int resId, int duration) throws Resources.NotFoundException {
        makeText(context, context.getResources().getText(resId), duration);
    }
    
    public static void makeText(Context context, CharSequence text, int duration) {
        if(duration == Toast.LENGTH_SHORT){
            toast(context, text.toString(), "s", Color.parseColor("#6d6d6d"));
        }else{
            toast(context, text.toString(), "l", Color.parseColor("#6d6d6d"));
        }
        
    }
    /**
     * @param context 上下文对象
     * @param msg 要显示的信息
     * @param timeTag 时间参数 若是“s”表示短时间显示 若是“l”（小写L）表示长时间显示
     */
    public static void toast(Context context, String msg, String timeTag, int textColor) {
        int time = Toast.LENGTH_SHORT;
        if (timeTag == null || "l".equals(timeTag)) {
            time = Toast.LENGTH_LONG;
        }

        Toast toast = Toast.makeText(context, null, time);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 290);
        LinearLayout layout = (LinearLayout) toast.getView();
        /*
         * layout.setLayoutParams(new WindowManager.LayoutParams(10000,
         * android.view.WindowManager.LayoutParams.WRAP_CONTENT,
         * WindowManager.LayoutParams.TYPE_TOAST,
         * WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
         * WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
         * WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
         * PixelFormat.TRANSLUCENT));
         */
        layout.setBackgroundResource(R.drawable.i99_toast_bg);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tv.setPadding(10, 10, 10, 10);
        tv.setText(msg);
        layout.addView(tv);
        toast.show();
    }
    
    //add by wangyang 2016.9.28
    public static void toast(Context context, String msg, String timeTag, int textColor, int flag) {

        Toast toast = Toast.makeText(context, null, 100);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        LinearLayout layout = (LinearLayout) toast.getView();
        /*
         * layout.setLayoutParams(new WindowManager.LayoutParams(10000,
         * android.view.WindowManager.LayoutParams.WRAP_CONTENT,
         * WindowManager.LayoutParams.TYPE_TOAST,
         * WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
         * WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
         * WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
         * PixelFormat.TRANSLUCENT));
         */
        layout.setBackgroundResource(R.drawable.i99_toast_bg);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tv.setPadding(10, 10, 10, 10);
        tv.setText(msg);
        layout.addView(tv);
        toast.show();
    }
}
