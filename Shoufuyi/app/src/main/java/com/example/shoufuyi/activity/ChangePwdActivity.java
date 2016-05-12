package com.example.shoufuyi.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.view.InputView;
import com.itech.message.APP_120034;

import java.io.UnsupportedEncodingException;

public class ChangePwdActivity extends BaseActivity {
	private EditText editold, editnew, editsure;
	private Button butsubmit;
	private String strold, strnew, strsure;
	private SharedPreferences.Editor edit;
	private SharedPreferences share;
	// 商户手机号
	private String userNmae;
	private APP_120034 returnapp;
	private String deskey;
	private String des3key;
	private TextView texttitle;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_password);
		Init();
	}

	private void Init() {
		share = ChangePwdActivity.this.getSharedPreferences("FILENAME",
				Activity.MODE_PRIVATE);// 指定操作的文件名称
		deskey = share.getString("deskey", "");
		des3key = share.getString("des3key", "");
		userNmae = share.getString("phone", "");
		System.out.println("des3key=" + share.getString("des3key", ""));
		System.out.println("deskey=" + share.getString("deskey", ""));

		InputView ipold = (InputView) findViewById(R.id.editold);
		editold = ipold.getInputEt();
		InputView ipnew = (InputView) findViewById(R.id.editnew);
		editnew = ipnew.getInputEt();
		InputView ipsure = (InputView) findViewById(R.id.editsure);
		editsure = ipsure.getInputEt();

		butsubmit = (Button) findViewById(R.id.butsubmit);
		butsubmit.setOnClickListener(submitlis);
		strold = editold.getText().toString().trim();
		strnew = editnew.getText().toString().trim();
		strsure = editsure.getText().toString().trim();

	}

	private OnClickListener submitlis = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Init();
			if (Is_formed()) {
				// 修改登录密码
				APP_120034 app = new APP_120034();
				app.setUserName(userNmae);
				try {
					app.setOriPass(des3key, strold);
					app.setNewPass(des3key, strnew);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
//				tonxunutil.transferPacket(app, userNmae);
//				DialogHelper.showProgressDialog(ChangePwdActivity.this,
//						"正在请求...", true, true);
//				tonxunutil.setOnResultListener(new OnResultListener() {
//					@Override
//					public void onSessionResult(final String msg,
//							final byte[] response) {
//						DialogHelper.dismissProgressDialog();
//						// TODO Auto-generated method stub
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								if (response == null) {
//									CommonUtils.showMessage(
//											ChangePwdActivity.this, "连接服务器失败");
//									return;
//
//								}
//								returnapp = JSON.parseObject(new String(
//										response), APP_120034.class);
//
//								if (msg.equals("0000")) {
//									// 在第一次登录激活的时候在发一次登录报文获取所属商户号
//									//
//									if (returnapp.getDetailCode()
//											.equals("0000")) {
//										CommonUtils.showMessage(
//												ChangePwdActivity.this,
//												returnapp.getDetailInfo());
//										edit = share.edit();// 编辑文件
//										edit.putString("LoginSerect", strnew);// 保存字符串
//										edit.putString("gaimima", "0000");
//										edit.putString("Is_exit", "1");
//										edit.commit();// 提交更新
//										mam = com.example.util.MangerActivity
//												.getInstance();
//										mam.finishAllActivity();
//										ChangePwdActivity.this.finish();
//										Intent intent = new Intent(
//												ChangePwdActivity.this,
//												LoginAndForget.class);
//										startActivity(intent);
//
//									} else {
//										CommonUtils.showMessage(
//												ChangePwdActivity.this,
//												returnapp.getDetailInfo());
//									}
//								} else {
//
//									CommonUtils.showMessage(
//											ChangePwdActivity.this,
//											returnapp.getErrMsg());
//
//								}
//
//							}
//						});
//					}
//
//				});

			}

		}

	};

	private boolean Is_formed() {
//		if (!strnew.equals(strsure)) {
//			CommonUtils.showMessage(ChangePwdActivity.this, "两次输入密码不相同");
//			editnew.setText("");
//			editsure.setText("");
//			return false;
//		}
//		if (strnew.equals(strsure) && strnew.length() < 6) {
//			CommonUtils.showMessage(ChangePwdActivity.this, "新密码长度不能少于6位");
//			editnew.setText("");
//			editsure.setText("");
//			return false;
//		}
//		if (strnew.equals("")) {
//			CommonUtils.showMessage(ChangePwdActivity.this, "新密码不能为空");
//			return false;
//		}
//		if (strold.equals("")) {
//			CommonUtils.showMessage(ChangePwdActivity.this, "旧密码不能为空");
//			return false;
//		}
		return true;

	}
}
