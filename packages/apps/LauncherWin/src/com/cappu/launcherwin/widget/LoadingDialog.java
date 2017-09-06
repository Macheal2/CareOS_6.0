package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.tools.CareUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class LoadingDialog extends Dialog {

    private TextView mLabel;
    private ImageView mIcon;

    /**
     * @param context
     */
    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.loading_dialog, null);
        mLabel = (TextView) v.findViewById(R.id.label);
        mIcon = (ImageView) v.findViewById(R.id.icon);
        setCancelable(false);
        addContentView(v,getLayoutParams());
    }

    @Override
    public void show() {
        super.show();
        Animation ani = AnimationUtils.loadAnimation( getContext(), R.anim.loading_anim); 
        mIcon.startAnimation(ani);
    }

    private LayoutParams getLayoutParams(){
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = CareUtils.getScreenSize(getContext())[0] - 40;
        return lp;
    }

}
