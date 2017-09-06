package com.cappu.launcherwin.applicationList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.cappu.launcherwin.R;

@SuppressLint("NewApi")
public class I99ThemeDialogUI extends Dialog implements View.OnClickListener, DialogInterface.OnClickListener {

    View mContentView;
    View mBottomLine;
    TextView mTitle;
    TextView mMessage;
    private Button mButtonLeft, mButtonMiddle, mButtonRight, mButtonOnlyOne;

    public static final int DIALOG_STYLE_ONE_BUTTON = 0x0001;
    public static final int DIALOG_STYLE_TWO_BUTTONS = 0x0002;
    public static final int DIALOG_STYLE_THREE_BUTTONS = 0x0004;

    private int mDialogStyle = DIALOG_STYLE_TWO_BUTTONS;
    private View mLinearLayoutOne, mLinearLayoutButtons, mDivide;

    public I99ThemeDialogUI(Context context, int dialogStyle) {
        super(context, R.style.I99Dialog);
        this.mDialogStyle = dialogStyle;
        this.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = getLayoutInflater();
        mContentView = inflater.inflate(R.layout.i99_style_dialog_info_notify, null);
        this.setContentView(mContentView);

        mTitle = (TextView) mContentView.findViewById(R.id.i99_theme_dialog_title);

        mLinearLayoutOne = mContentView.findViewById(R.id.i99_dialog_content_only_one);
        mLinearLayoutButtons = mContentView.findViewById(R.id.i99_dialog_content_buttons);

        mMessage = (TextView) findViewById(R.id.dialog_info_show);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
        mMessage.setGravity(Gravity.CENTER);

        mButtonLeft = (Button) mContentView.findViewById(R.id.i99_dialog_left);
        mButtonLeft.setOnClickListener(this);

        mButtonMiddle = (Button) mContentView.findViewById(R.id.i99_dialog_middle);
        mButtonMiddle.setOnClickListener(this);

        mDivide = mContentView.findViewById(R.id.second_divid);

        mButtonRight = (Button) mContentView.findViewById(R.id.i99_dialog_right);
        mButtonRight.setOnClickListener(this);

        mButtonOnlyOne = (Button) mContentView.findViewById(R.id.i99_dialog_only_one);
        mButtonOnlyOne.setOnClickListener(this);

        switch (mDialogStyle) {
        case DIALOG_STYLE_ONE_BUTTON: {
            mLinearLayoutOne.setVisibility(View.VISIBLE);
            mLinearLayoutButtons.setVisibility(View.GONE);
        }
            break;
        case DIALOG_STYLE_TWO_BUTTONS: {
            mLinearLayoutOne.setVisibility(View.GONE);
            mLinearLayoutButtons.setVisibility(View.VISIBLE);
            mButtonMiddle.setVisibility(View.GONE);
            mDivide.setVisibility(View.GONE);
        }
            break;
        case DIALOG_STYLE_THREE_BUTTONS:
            mLinearLayoutOne.setVisibility(View.GONE);
            mLinearLayoutButtons.setVisibility(View.VISIBLE);
            break;
        default:
            break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTitle.setText(titleId);
    }

    public void setTitleColor(ColorStateList color) {
        mTitle.setTextColor(color);
    }

    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setTitleBackground(int resid) {
        mTitle.setBackgroundResource(resid);
    }

    public void setTitleBackground(Drawable drawable) {
        mTitle.setBackground(drawable);
    }

    public void setMessage(CharSequence message) {
        mMessage.setText(message);
    }

    public void setMessage(int resid) {
        mMessage.setText(resid);
    }

    public void setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mButtonLeft.setText(text);
        }

        if (listener != null) {
            mButtonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEGATIVE);
                }
            });
        }
    }

    public void setNegativeButton(int textId, final DialogInterface.OnClickListener listener) {
        mButtonLeft.setText(textId);
        if (listener != null) {
            mButtonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEGATIVE);
                }
            });
        }
    }

    public void setOnlyOneButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mButtonOnlyOne.setText(text);
        }
        if (listener != null) {
            mButtonOnlyOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEGATIVE);
                }
            });
        }
    }

    public void setOnlyOneButton(int textId, final DialogInterface.OnClickListener listener) {
        mButtonOnlyOne.setText(textId);
        if (listener != null) {
            mButtonOnlyOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEGATIVE);
                }
            });
        }
    }

    public void setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mButtonRight.setText(text);
        }
        if (listener != null) {
            mButtonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_POSITIVE);
                }
            });
        }
    }

    public void setPositiveButton(int textId, final DialogInterface.OnClickListener listener) {
        mButtonRight.setText(textId);
        if (listener != null) {
            mButtonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_POSITIVE);
                }
            });
        }
    }

    public void setCancelButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mButtonMiddle.setText(text);
        }
        if (listener != null) {
            mButtonMiddle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEUTRAL);
                }
            });
        }
    }

    public void setCancelButton(int textId, final DialogInterface.OnClickListener listener) {
        mButtonMiddle.setText(textId);
        if (listener != null) {
            mButtonMiddle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(I99ThemeDialogUI.this, DialogInterface.BUTTON_NEUTRAL);
                }
            });
        }
    }

    @Override
    public void onClick(android.content.DialogInterface listener, int which) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.i99_dialog_left:
        case R.id.i99_dialog_middle:
        case R.id.i99_dialog_right:
        case R.id.i99_dialog_only_one:
            this.dismiss();
            break;
        default:
            break;
        }
    }

    @Override
    public void show() {
        super.show();
        final int width = getScreenSize(getContext())[0];
        final int height = getScreenSize(getContext())[1];
        LayoutParams lp = mContentView.getLayoutParams();
        if (width > height) {
            lp.width = getScreenSize(getContext())[0] / 2;
        } else {
            lp.width = getScreenSize(getContext())[0] - 10;
        }
        mContentView.setLayoutParams(lp);
    }
}
