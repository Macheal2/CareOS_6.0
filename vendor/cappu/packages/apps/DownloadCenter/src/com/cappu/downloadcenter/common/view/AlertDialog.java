package com.cappu.downloadcenter.common.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cappu.downloadcenter.R;

/**
 * 
 * @author maqj
 */
public class AlertDialog extends Dialog {

    public AlertDialog(Context context, int theme) {
        super(context, theme);
    }

    public AlertDialog(Context context) {
        super(context);
    }

    public static class Builder {

        private Context context;
        // private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;

        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setTitle(int title) {
            // this.title = (String)context.getText(title);
            return this;
        }

        public Builder setTitle(String title) {
            // this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;

            if (listener == null) {

            }
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        @SuppressLint("InflateParams")
        @SuppressWarnings("deprecation")
        public AlertDialog create() {
            LayoutInflater inflater = null;
            if (context != null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            } else {
                return null;
            }
            final AlertDialog dialog = new AlertDialog(context, R.style.OnlinefolderDialog);
            View layout = inflater.inflate(R.layout.dialog_onlinefolder_confirm_, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // ((TextView) layout.findViewById(R.id.txt_title)).setText(title);
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.btn_positive)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.btn_positive)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_positive).setVisibility(View.GONE);
            }

            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.btn_negative)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.btn_negative)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_negative).setVisibility(View.GONE);
            }

            if (message != null) {
                ((TextView) layout.findViewById(R.id.txt_message)).setText(message);
            } else if (contentView != null) {
                // ((LinearLayout)
                // layout.findViewById(R.id.content)).removeAllViews();
                // ((LinearLayout)
                // layout.findViewById(R.id.content)).addView(contentView, new
                // LayoutParams(
                // LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                Log.e("AlertDialog", "contentView != null...........");
            }

            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

}