package com.cchtw.sfy.uitls.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.cchtw.sfy.R;

public class CustomProgressDialog extends Dialog {

    private static CustomProgressDialog customProgressDialog = null;

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static CustomProgressDialog createDialog(Context paramContext, String paramString,boolean cancelable,
                                                    boolean canceledOnTouchOutside) {
        customProgressDialog = createDialog(paramContext, cancelable,canceledOnTouchOutside);
        customProgressDialog.setMessage(paramString);
        return customProgressDialog;
    }

    public static CustomProgressDialog createDialog(Context context, boolean cancelable,
                                                    boolean canceledOnTouchOutside) {
        customProgressDialog = new CustomProgressDialog(context, R.style.CustomProgressDialog);
        customProgressDialog.setContentView(R.layout.dialog_custom_progress);
        customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        customProgressDialog.setCancelable(cancelable);
        customProgressDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        customProgressDialog.setOnCancelListener(new ProgressDialogCancelListener());
        return customProgressDialog;
    }

    public void onWindowFocusChanged(boolean hasFocus) {

        if (customProgressDialog == null) {
            return;
        }

//        ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.iv_loading);
//        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
//        animationDrawable.start();
    }

    /**
     * 
     * [Summary] setMessage 提示内容
     * @param strMessage
     * @return
     * 
     */
    public CustomProgressDialog setMessage(String strMessage) {
        TextView tvMsg = (TextView) customProgressDialog.findViewById(R.id.tv_msg);
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
        return customProgressDialog;
    }
}
