package com.example.shoufuyi.uitls.dialog;

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

	public static void dismissProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
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
