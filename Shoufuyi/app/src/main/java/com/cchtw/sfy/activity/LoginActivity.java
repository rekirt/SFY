package com.cchtw.sfy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.StartToUseJsonHttpHandler;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.TLog;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.cache.ACache;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120033;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity implements OnClickListener,DialogInterface{
	private Button butlogin;
	private EditText mEditPhone;
	private EditText mEditPwd;
	private CheckBox cb_remember;
	private TextView tv_start_to_use;
	private TextView tv_forget_pwd;
	private String strphone;

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		initData();
	}

	private void initView() {
		mEditPhone = (EditText) findViewById(R.id.edt_phone);
		mEditPwd = (EditText) findViewById(R.id.edt_pwd);
		butlogin = (Button) findViewById(R.id.btn_login);
		cb_remember = (CheckBox) findViewById(R.id.cb_remember);
		tv_start_to_use = (TextView) findViewById(R.id.tv_start_to_use);
		tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);

	}

	private void initData(){
		Boolean isRemmber = SharedPreferencesHelper.getBoolean(Constant.ISREMMBER, false);//是否记住密码
		if (isRemmber){
			strphone = SharedPreferencesHelper.getString(Constant.PHONE, "");
		}
		if (!TextUtils.isEmpty(strphone)){
			mEditPhone.setText(strphone);
		}
		butlogin.setOnClickListener(this);
		tv_start_to_use.setOnClickListener(this);
		tv_forget_pwd.setOnClickListener(this);
	}

	private void login() {
		SharedPreferencesHelper.setString(Constant.PHONE, mEditPhone.getText().toString());

		APP_120033 app = new APP_120033();
		app.setUserName(mEditPhone.getText().toString());
		app.setLoginState("0000");
		String des3key = SharedPreferencesHelper.getString(mEditPhone.getText().toString()+Constant.DESK3KEY, "");
		boolean ForcedOffLine = SharedPreferencesHelper.getBoolean(mEditPhone.getText().toString()+Constant.ISFORCEDOFFLINE, false);
		//为了解决由于不同步造成的密码错误的问题
//		if (TextUtils.isEmpty(des3key)){
//			if (ForcedOffLine){
//				ToastHelper.ShowToast("账户在其它设备登录，请重新启用！");
//			}else {
//				ToastHelper.ShowToast("第一次在该设备登录，请先启用！");
//			}
//            SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, true);//是否记住密码
//            Intent intent = new Intent();
//			intent.putExtra("mEditPhone",mEditPhone.getText().toString());
//            intent.setClass(LoginActivity.this, StartToUseActivity.class);
//            startActivity(intent);
//            return;
//        }
		if (ForcedOffLine){
			ToastHelper.ShowToast("账户在其它设备登录，请重新启用！");
			SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, true);
			Intent intent = new Intent();
			intent.putExtra("mEditPhone",mEditPhone.getText().toString());
			intent.setClass(LoginActivity.this, StartToUseActivity.class);
			startActivity(intent);
			return;
		}

		try {
			app.setUserPass("", mEditPwd.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DialogHelper.showProgressDialog(LoginActivity.this, "正在登录...", true, false);
		ApiRequest.login(app, mEditPhone.getText().toString(), new StartToUseJsonHttpHandler(LoginActivity.this) {
			@Override
			public void onDo(JSONObject responseJsonObject) {
				APP_120033 returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120033.class);
				if ("0000".equals(returnapp.getDetailCode())) {
					AccountHelper.setUser(returnapp);
					if (cb_remember.isChecked()) {
						SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, true);//是否记住密码
					}else {
						SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, false);//是否记住密码
					}
					SharedPreferencesHelper.setString(Constant.PHONE, mEditPhone.getText().toString());
					SharedPreferencesHelper.setBoolean(mEditPhone.getText().toString()+Constant.ISFORCEDOFFLINE, false);
					SharedPreferencesHelper.setString(mEditPhone.getText().toString() + Constant.DESK3KEY, AccountHelper.getDes3Key());
					SharedPreferencesHelper.setString(mEditPhone.getText().toString() + Constant.DESKEY, AccountHelper.getDesKey());
					SharedPreferencesHelper.setString(mEditPhone.getText().toString()+Constant.TOKEN, AccountHelper.getToken());
					SharedPreferencesHelper.setBoolean(mEditPhone.getText().toString() + Constant.ACTIVATION, true);

					ACache aCache = ACache.get(LoginActivity.this);
					byte[] gesturePassword= aCache.getAsBinary(AccountHelper.getUserName() + Constant.GESTURE_PASSWORD);
					Intent intent = new Intent();
					if (gesturePassword == null){
						intent.setClass(LoginActivity.this, CreateGestureActivity.class);
					}else {
						intent.setClass(LoginActivity.this, MainActivity.class);
						intent.putExtra("isCheckUpdate",true);
					}
					startActivity(intent);
					LoginActivity.this.finish();
				} else if ("3998".equals(returnapp.getDetailCode())) {
					ToastHelper.ShowToast(returnapp.getDetailInfo());
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, StartToUseActivity.class);
					startActivity(intent);
				} else if ("1017".equals(returnapp.getDetailCode())) {
					ToastHelper.ShowToast(returnapp.getDetailInfo());
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, StartToUseActivity.class);
					startActivity(intent);
				} else {
					ToastHelper.ShowToast(returnapp.getDetailInfo());
				}
			}

			@Override
			public void onDo(JSONArray responseJsonArray) {

			}

			@Override
			public void onDo(String responseString) {
			}

			@Override
			public void onFinish() {
				super.onFinish();
				DialogHelper.dismissProgressDialog();
			}
		});
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		switch (view.getId()){
			case R.id.tv_forget_pwd:
			case R.id.tv_start_to_use:
				startActivity(new Intent(LoginActivity.this,StartToUseActivity.class));
				break;
			case R.id.btn_login:
				if (TextUtils.isEmpty(mEditPwd.getText().toString())){
					ToastHelper.ShowToast("请输入密码！");
					return;
				}else {
					login();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void cancel() {
		TLog.analytics("dialog-cancel");
	}

	@Override
	public void dismiss() {
		TLog.analytics("dialog-cancel");
	}
}
