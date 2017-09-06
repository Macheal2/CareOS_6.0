
package com.cappu.launcherwin.netinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;


public class BobyListView extends ListView {
    public BobyListView(Context context) {
        super(context);

    }

    public BobyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
