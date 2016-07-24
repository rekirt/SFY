package com.cchtw.sfy.uitls.dialog;

import android.content.Context;


public class DialogHelper {

	public static CustomProgressDialog progressDialog;

	public static void showProgressDialog(Context context,
										  String paramMsg,
										  boolean cancelable,
										  boolean canceledOnTouchOutside) {

		if (progressDialog == null) {
			progressDialog = CustomProgressDialog.createDialog(context, paramMsg, cancelable,canceledOnTouchOutside);
		}else {
			progressDialog.dismiss();
			progressDialog = null;
			progressDialog = CustomProgressDialog.createDialog(context, paramMsg, cancelable,canceledOnTouchOutside);
		}
		progressDialog.show();
	}
    static ProgressDialogDoClickHelper mProgressDialogDoClickHelper;
    public static void showProgressDialog(Context context,
                                          String paramMsg,
                                          ProgressDialogDoClickHelper progressDialogDoClickHelper,
                                          boolean cancelable,
                                          boolean canceledOnTouchOutside) {

        if (progressDialog == null) {
            progressDialog = CustomProgressDialog.createDialog(context, paramMsg, cancelable,canceledOnTouchOutside);
        }else {
            progressDialog.dismiss();
            progressDialog = null;
            progressDialog = CustomProgressDialog.createDialog(context, paramMsg, cancelable,canceledOnTouchOutside);
        }
        mProgressDialogDoClickHelper = progressDialogDoClickHelper;
        progressDialog.show();
    }
	public static void dismissProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
//			if (mProgressDialogDoClickHelper != null){
//				mProgressDialogDoClickHelper.doClick();
//			}
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public static void upDateProgressDialog(Context context,
											String newMsg,
											boolean cancelable,
											boolean canceledOnTouchOutside) {
		if (progressDialog == null) {
			progressDialog = CustomProgressDialog.createDialog(context, newMsg, cancelable,canceledOnTouchOutside);
		}else {
			progressDialog.setMessage(newMsg);
		}
		progressDialog.show();
	}

}
