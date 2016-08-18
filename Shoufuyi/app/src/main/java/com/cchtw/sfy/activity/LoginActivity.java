package com.cchtw.sfy.activity;

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
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.AccountHelper;
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
		APP_120033 app = new APP_120033();
		app.setUserName(mEditPhone.getText().toString());
		app.setLoginState("0000");
		String des3key = SharedPreferencesHelper.getString(mEditPhone.getText().toString()+Constant.DESK3KEY, "");
        if (TextUtils.isEmpty(des3key)){
            ToastHelper.ShowToast("第一次在该设备登录，请先启用！");
            Intent intent = new Intent();
			intent.putExtra("mEditPhone",mEditPhone.getText().toString());
            intent.setClass(LoginActivity.this, StartToUseActivity.class);
            startActivity(intent);
            return;
        }
		try {
			app.setUserPass(des3key, mEditPwd.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DialogHelper.showProgressDialog(LoginActivity.this, "正在登录...", true, false);

		ApiRequest.login(app, mEditPhone.getText().toString(), new JsonHttpHandler() {
			@Override
			public void onDo(JSONObject responseJsonObject) {
				APP_120033 returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120033.class);
				if ("0000".equals(returnapp.getDetailCode())) {
					AccountHelper.setUser(returnapp);
					if (cb_remember.isChecked()) {
						SharedPreferencesHelper.setString(Constant.PHONE, mEditPhone.getText().toString());
					}
                    SharedPreferencesHelper.setString(mEditPhone.getText().toString() + Constant.DESK3KEY, AccountHelper.getDes3Key());
                    SharedPreferencesHelper.setString(mEditPhone.getText().toString() + Constant.DESKEY, AccountHelper.getDesKey());
                    SharedPreferencesHelper.setString(mEditPhone.getText().toString()+Constant.TOKEN, AccountHelper.getToken());

                    Intent intent = new Intent();
					intent.setClass(LoginActivity.this, CreateGestureActivity.class);
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

//	private long mExitTime;
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if ((System.currentTimeMillis() - mExitTime) > 2000) {
//				Toast.makeText(this, "在按一次退出",
//						Toast.LENGTH_SHORT).show();
//				mExitTime = System.currentTimeMillis();
//			} else {
//				LoginActivity.this.finish();
//				ActivityCollector.finishAll();
//			}
//			return true;
//		}
//		//拦截MENU按钮点击事件，让他无任何操作
//		if (keyCode == KeyEvent.KEYCODE_MENU) {
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
}
