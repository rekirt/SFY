package com.cchtw.sfy.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120033;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity implements OnClickListener{
	private Button butlogin;
	private EditText mEditPhone;
    private EditText mEditPwd;
	private TextView tv_start_to_use;
	private String strcode, strphone, strserect;
	private SharedPreferences.Editor edit;

	private String message;
	private String deskey;
	private String des3key;

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
        tv_start_to_use = (TextView) findViewById(R.id.tv_start_to_use);
	}

	private void initData(){
		deskey = SharedPreferencesHelper.getString(Constant.DESKEY, "");
		des3key = SharedPreferencesHelper.getString(Constant.DESK3KEY, "");
		strphone = SharedPreferencesHelper.getString(Constant.PHONE, "");
		strserect = SharedPreferencesHelper.getString(Constant.LOGINSERECT, "");
		if (!TextUtils.isEmpty(strphone)){
			mEditPhone.setText(strphone);
		}
		butlogin.setOnClickListener(this);
        tv_start_to_use.setOnClickListener(this);
	}

	// 登录
	private void login() {
		APP_120033 app = new APP_120033();
		app.setUserName(mEditPhone.getText().toString());
		app.setLoginState("0000");
		try {
			app.setUserPass(des3key, mEditPwd.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DialogHelper.showProgressDialog(LoginActivity.this, "正在登录...", true, false);

		ApiRequest.login(app, strphone, new JsonHttpHandler() {
			@Override
			public void onDo(JSONObject responseJsonObject) {
				// 在第一次登录激活的时候在发一次登录报文获取所属商户号
				APP_120033 returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120033.class);
				message = returnapp.getDetailInfo();
				if ("0000".equals(returnapp.getDetailCode())) {
                    SharedPreferencesHelper.setString(Constant.MERCHANT, returnapp.getMerchantId());
                    SharedPreferencesHelper.setString(Constant.TOKEN, returnapp.getToken());
                    SharedPreferencesHelper.setString(Constant.DESKEY, returnapp.getDesKey());
                    SharedPreferencesHelper.setString(Constant.DESK3KEY, returnapp.getDes3Key());
					SharedPreferencesHelper.setBoolean(Constant.ISLOGIN, true);// 保存字符串
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this,SetGestureActivity.class);
					startActivity(intent);
					LoginActivity.this.finish();
				}else {
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
}
