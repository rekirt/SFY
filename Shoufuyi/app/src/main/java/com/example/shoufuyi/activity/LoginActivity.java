package com.example.shoufuyi.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.example.shoufuyi.uitls.view.Gesture;
import com.example.shoufuyi.uitls.view.InputView;
import com.itech.message.APP_120033;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity implements OnClickListener{
	private Button butlogin;
	private EditText editphone, editlogin;
	private String strcode, strphone, strserect;
	private SharedPreferencesHelper sharedPreferencesHelper;
	private SharedPreferences.Editor edit;

	private String message;
	private String deskey;
	private String des3key;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		sharedPreferencesHelper = SharedPreferencesHelper.getInstance();
		initView();
		initData();
	}

	private InputView iplogin;
	private InputView ipphone;

	private void initView() {
		iplogin = (InputView) findViewById(R.id.login);
		ipphone = (InputView) findViewById(R.id.phone);
		butlogin = (Button) findViewById(R.id.loginbut);
	}

	private void initData(){
		deskey = sharedPreferencesHelper.getString("deskey", "");
		des3key = sharedPreferencesHelper.getString("des3key", "");
		editphone = ipphone.getInputEt();
		editlogin = iplogin.getInputEt();
		strphone = sharedPreferencesHelper.getString("phone", "");
		strserect = sharedPreferencesHelper.getString("LoginSerect", "");
		editphone.setText(strphone);
		butlogin.setOnClickListener(this);
	}

	// 登录
	private void login() {
		APP_120033 app = new APP_120033();
		app.setUserName(strphone);
		app.setLoginState("0000");
		try {
			app.setUserPass(des3key, editlogin.getText().toString().trim());
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
				String detailCode = returnapp.getDetailCode();
				if (detailCode.equals("0000")) {
					String merchantid = returnapp.getMerchantId();
					sharedPreferencesHelper.setString("merchant", merchantid);
					sharedPreferencesHelper.setString("token", returnapp.getToken());
					sharedPreferencesHelper.setString("deskey", returnapp.getDesKey());
					sharedPreferencesHelper.setString("des3key", returnapp.getDes3Key());
					boolean log = sharedPreferencesHelper.getBoolean("login", false);
					if (log) {
						sharedPreferencesHelper.setBoolean("login", false);
						finish();
						return;
					}
					String is_exit = sharedPreferencesHelper.getString("Is_exit", "");
					// 如果是退出就不要修改字段的值
					if (is_exit.equals("1")) {
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this,Gesture.class);
						startActivity(intent);
						finish();
					} else {
						sharedPreferencesHelper.setString("Is_Login", "");// 保存字符串
						sharedPreferencesHelper.setString("mima", "");// 保存字符串
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this,Gesture.class);
						startActivity(intent);
						finish();
						}

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
		if(view.getId() == R.id.loginbut){
			login();
		}
	}
}
