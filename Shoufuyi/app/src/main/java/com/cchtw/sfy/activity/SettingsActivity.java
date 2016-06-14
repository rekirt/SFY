package com.cchtw.sfy.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.UpdateManager;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120033;
import com.itech.message.APP_Parameters;
import com.itech.message.APP_RunParm;
import com.itech.message.APP_UpdateLimit;
import com.itech.message.APP_Version;
import com.itech.message.AppPrivilage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SettingsActivity extends BaseActivity {

	private Button mBtnLogout;
    private RelativeLayout mRlBusinessAuthority;
    private RelativeLayout mRlModifyGesture_pwd;
    private RelativeLayout mRlChangePwd;
    private RelativeLayout mRlAppUpdate;
    private RelativeLayout mRlSetting;

	private String phone;
    private String strtoclass;
    private UpdateManager update;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settings);
		initView();
        initData();
	}

	private void initView() {
        mBtnLogout = (Button) findViewById(R.id.btn_logout);
        mRlBusinessAuthority = (RelativeLayout) findViewById(R.id.rl_business_authority);
        mRlModifyGesture_pwd = (RelativeLayout) findViewById(R.id.rl_modify_gesture_pwd);
        mRlChangePwd = (RelativeLayout) findViewById(R.id.rl_change_pwd);
        mRlAppUpdate = (RelativeLayout) findViewById(R.id.rl_app_update);
        mRlSetting = (RelativeLayout) findViewById(R.id.rl_setting);
		update = new UpdateManager(SettingsActivity.this);
		phone = SharedPreferencesHelper.getString("phone", "");
	}

    private void initData(){
        mBtnLogout.setOnClickListener(this);
        mRlBusinessAuthority.setOnClickListener(this);
        mRlModifyGesture_pwd.setOnClickListener(this);
        mRlChangePwd.setOnClickListener(this);
        mRlAppUpdate.setOnClickListener(this);
        mRlSetting.setOnClickListener(this);
        setCanBack(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_logout:
                DiologToJump("确定退出登录");
                strtoclass = "退出登录";
                break;
            case R.id.rl_business_authority:
                busslimit();
                break;
            case R.id.rl_modify_gesture_pwd:
                DiologToJump("确定修改手势密码");
                strtoclass = "手势密码";
                break;
            case R.id.rl_change_pwd:
                DiologToJump("确定修改登录密码");
                strtoclass = "登录密码";
                break;
            case R.id.rl_app_update:
                update();
                break;
            case R.id.rl_setting:
                runparameter();
                break;
        }
    }

	// 业务权限
	private void busslimit() {
		DialogHelper.showProgressDialog(SettingsActivity.this, "正在查询，请稍候...", true, true);
		APP_UpdateLimit app = new APP_UpdateLimit();
		app.setBusSysCode("00000001");
		app.setUserName(phone);
        ApiRequest.requestData(app, phone, new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                final APP_UpdateLimit returnapp = JSON.parseObject(responseJsonObject.toString(), APP_UpdateLimit.class);
                if (returnapp.getDetailCode().equals("0000")) {
                    StringBuffer strprivilage = new StringBuffer();
                    List<AppPrivilage> privilagelist = returnapp.getAppPrivilageList();
                    for (AppPrivilage pri : privilagelist) {
                        strprivilage.append(pri.getAppPrvCode()).append(",");
                    }
                    SharedPreferencesHelper.setString(Constant.PRIVILAGE, strprivilage.toString() + "");// 保存字符串
                    ToastHelper.ShowToast(returnapp.getDetailInfo());
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
                DialogHelper.dismissProgressDialog();
            }
        });
	}

	// 运行参数功能
	private void runparameter() {
		DialogHelper.showProgressDialog(SettingsActivity.this, "正在查询，请稍候...", true, false);
		APP_RunParm app = new APP_RunParm();
		app.setType("1000");
        ApiRequest.requestData(app, phone, new JsonHttpHandler("detailCode","errMsg","data") {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                final APP_RunParm returnapp = JSON.parseObject(responseJsonObject.toString(), APP_RunParm.class);
                if (returnapp.getDetailCode().equals("0000")) {
                    List<APP_Parameters> list = returnapp.getParametersList();
                    for (APP_Parameters par : list) {
                        switch (par.getParCode()){
                            case "0001":
                                SharedPreferencesHelper.setString(Constant.PAGESIZE,par.getParValue());
                                break;
                            case "0002":
                                SharedPreferencesHelper.setString(Constant.FINGERPASSWORDTIMES,par.getParValue());
                                break;
                            case "0003":
                                SharedPreferencesHelper.setString(Constant.VEDIOLONG,par.getParValue());
                                break;
                            case "0004":
                                SharedPreferencesHelper.setString(Constant.VEDIOANDPHOTOCACHELONG,par.getParValue());
                                break;
                            case "0005":
                                SharedPreferencesHelper.setString(Constant.TIMEOUT,par.getParValue());
                                break;
                            default:
                                break;
                        }
                    }
                    Intent intent = new Intent(SettingsActivity.this, RunningSettingActivity.class);
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
                DialogHelper.dismissProgressDialog();
            }
        });
	}

	// 退出登录
	private void exit() {
		// 修改登录密码
		APP_120033 app = new APP_120033();
		app.setTrxCode("120033");
		app.setUserName(phone);
		app.setUserPass("");
		app.setLoginState("0001");
        DialogHelper.showProgressDialog(SettingsActivity.this, "正在退出，请稍候...", true, false);
        ApiRequest.requestData(app, phone, new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120033 returnapp = null;
                try {
                    returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120033.class);
                    if (returnapp.getDetailCode().equals("0000")) {
                        SharedPreferencesHelper.setBoolean(Constant.ISLOGIN, false);
//                        SharedPreferencesHelper.setString(Constant.MIMA, "");
//                        SharedPreferencesHelper.setBoolean(Constant.HAVESETFINGERPWD, false);
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                        SettingsActivity.this.finish();
                    } else {
                        ToastHelper.ShowToast(returnapp.getDetailInfo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                DialogHelper.dismissProgressDialog();
            }
        });
	}

	// 更新操作；
	private void update() {
		APP_Version app = new APP_Version();
		app.setTerminalType("1");
		PackageManager pm = getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
			String oldCode = pi.versionName;
			app.setVersion(oldCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		DialogHelper.showProgressDialog(SettingsActivity.this, "正在查询，请稍候...", true, true);
		ApiRequest.requestData(app, phone, new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                final APP_Version returnapp = JSON.parseObject(responseJsonObject.toString(), APP_Version.class);
                if ("0000".equals(returnapp.getDetailCode())) {
                    update.checkUpdateInfo(returnapp);
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
                DialogHelper.dismissProgressDialog();
            }
        });
	}

	@SuppressWarnings("deprecation")
	private void DiologToJump(String diostr) {
		// 创建退出对话框
		AlertDialog isExit = new AlertDialog.Builder(SettingsActivity.this).create();
		// 设置对话框标题
		isExit.setTitle("系统提示");
		// 设置对话框消息
		isExit.setMessage(diostr);
		// 添加选择按钮并注册监听
		isExit.setButton("确定", listener);
		isExit.setButton2("取消", listener);
		// 显示对话框
		isExit.show();
	}

	/** 监听对话框里面的button点击事件 */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if (strtoclass.equals("手势密码")) {
				switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        Intent intent = new Intent();
                        intent.putExtra("isChangeFingerPwd",true);
                        intent.setClass(SettingsActivity.this, SetGestureActivity.class);
                        startActivity(intent);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
				}
			} else if (strtoclass.equals("登录密码")) {
				switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        Intent intent = new Intent();
                        intent.setClass(SettingsActivity.this, ChangePwdActivity.class);
                        startActivity(intent);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                        break;
                    default:
                        break;
				}

			} else if (strtoclass.equals("退出登录")) {
				switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        exit();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
				}
			}
		}
	};

}
