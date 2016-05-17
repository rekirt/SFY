package com.example.shoufuyi.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.util.CommonUtils;
import com.example.util.TonxinUtil;
import com.example.util.TonxinUtil.OnResultListener;
import com.example.util.UpdateManager;
import com.example.util.dialog.DialogHelper;
import com.itech.message.APP_120033;
import com.itech.message.APP_Parameters;
import com.itech.message.APP_RunParm;
import com.itech.message.APP_UpdateLimit;
import com.itech.message.APP_Version;
import com.itech.message.AppPrivilage;
import com.shanglutong.database.DBManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends BaseActivity {

	private Button mBtnLogout;
    private RelativeLayout mRlBusinessAuthority;
    private RelativeLayout mRlModifyGesture_pwd;
    private RelativeLayout mRlChangePwd;
    private RelativeLayout mRlAppUpdate;
    private RelativeLayout mRlSetting;

	// 商户手机号；
	private String phone;
	// 数据库管理类
	DBManager db;
	// 权限
	private String privilagestr;
	private String[] privilagearr;
	private LinearLayout lintitle;
	private TextView texttitle;

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

        db = DBManager.getInstance();
		update = new UpdateManager(Person.this);
		phone = share.getString("phone", "");
		privilagestr = share.getString("privilage", "");
		privilagearr = privilagestr.split(",");
		tonxunutil = TonxinUtil.getInstance(Person.this);

		if (!useSet(privilagearr, "007")) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("details", Arr[0]);
			map.put("image", imgarr[0]);
			map.put("check", "0000");
			mylist.remove(map);
		}
		if (!useSet(privilagearr, "008")) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("details", Arr[3]);
			map.put("image", imgarr[3]);
			map.put("check", "0000");
			mylist.remove(map);
		}
	}

    private void initData(){
        mBtnLogout.setOnClickListener(this);
        mRlBusinessAuthority.setOnClickListener(this);
        mRlModifyGesture_pwd.setOnClickListener(this);
        mRlChangePwd.setOnClickListener(this);
        mRlAppUpdate.setOnClickListener(this);
        mRlSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
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

    public static boolean useSet(String[] arr, String targetValue) {
		Set<String> set = new HashSet<String>(Arrays.asList(arr));
		return set.contains(targetValue);
	}

	// 业务权限

	private void busslimit() {
		DialogHelper.showProgressDialog(Person.this, "正在查询，请稍候...", true, true);
		APP_UpdateLimit app = new APP_UpdateLimit();
		app.setBusSysCode("00000001");
		app.setUserName(phone);
		tonxunutil.transferPacket(app, phone);

		tonxunutil.setOnResultListener(new OnResultListener() {
			public void onSessionResult(final String msg, final byte[] response) {
				DialogHelper.dismissProgressDialog();
				final APP_UpdateLimit returnapp = JSON.parseObject(new String(
						response), APP_UpdateLimit.class);
				runOnUiThread(new Runnable() {
					public void run() {
						if (msg.equals("0000")
								&& returnapp.getDetailCode().equals("0000")) {
							StringBuffer strprivilage = new StringBuffer();
							List<AppPrivilage> privilagelist = returnapp
									.getAppPrivilageList();
							for (AppPrivilage pri : privilagelist) {
								strprivilage.append(pri.getAppPrvCode())
										.append(",");
							}
							System.out.println("1111111111"
                                    + strprivilage.toString() + "");
							SharedPreferences.Editor edit = share.edit();
							edit.putString("privilage", strprivilage.toString()
									+ "");// 保存字符串
							edit.commit();
							CommonUtils.showMessage(Person.this,
                                    returnapp.getDetailInfo());
							init();

						} else {

						}

					}
				});
			}

		});
	}

	// 运行参数功能

	private void runparameter() {
		DialogHelper.showProgressDialog(Person.this, "正在查询，请稍候...", true, true);
		APP_RunParm app = new APP_RunParm();
		app.setType("1000");
		tonxunutil.transferPacket(app, phone);
		tonxunutil.setOnResultListener(new OnResultListener() {
			@Override
			public void onSessionResult(final String msg, final byte[] response) {
				DialogHelper.dismissProgressDialog();
				final APP_RunParm returnapp = JSON.parseObject(new String(
						response), APP_RunParm.class);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (msg.equals("0000")
								&& returnapp.getDetailCode().equals("0000")) {
							StringBuffer sb = new StringBuffer();
							List<APP_Parameters> list = returnapp
									.getParametersList();
							String str001 = "", str002 = "", str003 = "", str004 = "", str005 = "";
							for (APP_Parameters par : list) {

								if (par.getParCode().equals("0001")) {
									str001 = par.getParValue();
								} else if (par.getParCode().equals("0002")) {
									str002 = par.getParValue();
								} else if (par.getParCode().equals("0003")) {
									str003 = par.getParValue();
								} else if (par.getParCode().equals("0004")) {
									str004 = par.getParValue();
								} else if (par.getParCode().equals("0005")) {
									str005 = par.getParValue();
								}

							}
							sb.append(str001).append(",").append(str002)
									.append(",").append(str003).append(",")
									.append(str004).append(",").append(str005);
							Intent intent = new Intent(Person.this,
									Setting.class);
							Bundle bundle = new Bundle();
							bundle.putString("info", sb.toString());
							intent.putExtras(bundle);
							startActivity(intent);

						} else {

						}

					}
				});
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
		tonxunutil.transferPacket(app, phone);
		DialogHelper.showProgressDialog(Person.this, "正在退出，请稍候...", true, true);
		tonxunutil.setOnResultListener(new OnResultListener() {
			@Override
			public void onSessionResult(final String msg, final byte[] response) {
				DialogHelper.dismissProgressDialog();
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						APP_120033 returnapp = null;
						try {
							returnapp = JSON.parseObject(new String(response),
									APP_120033.class);
							if (msg.equals("0000")
									&& returnapp.getDetailCode().equals("0000")) {
								CommonUtils.showMessage(Person.this,
										returnapp.getDetailInfo());
								edit = share.edit();// 编辑文件
								// Is_exit 设置为1的时候表示要重新登录
								edit.putString("Is_exit", "1");// 保存字符串
								edit.commit();// 提交更新
								Person.this.finish();
								mam.finishAllActivity();

							} else {
								CommonUtils.showMessage(Person.this,
										returnapp.getDetailInfo());
							}
						} catch (Exception e) {

						}

					}
				});
			}

		});

	}

	// 更新操作；
	private void update() {
		APP_Version app = new APP_Version();
		app.setTerminalType("1");
		PackageManager pm = Person.this.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(Person.this.getPackageName(), 0);
			String oldCode = pi.versionName;
			app.setVersion(oldCode);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DialogHelper.showProgressDialog(Person.this, "正在查询，请稍候...", true, true);
		tonxunutil.transferPacket(app, phone);
		tonxunutil.setOnResultListener(new OnResultListener() {

			public void onSessionResult(final String msg, final byte[] response) {
				DialogHelper.dismissProgressDialog();
				final APP_Version returnapp = JSON.parseObject(new String(
						response), APP_Version.class);
				runOnUiThread(new Runnable() {
					public void run() {
						if (msg.equals("0000")) {
							update.checkUpdateInfo(returnapp);

						}
					}
				});

			}

		});

	}


	@SuppressWarnings("deprecation")
	private void DiologToJump(String diostr) {
		// 创建退出对话框
		AlertDialog isExit = new AlertDialog.Builder(Person.this).create();
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
				case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序

					SharedPreferences.Editor edit = share.edit();
					edit.putString("Is_Login", "");// 保存字符串
					edit.putString("mima", "");// 保存字符串
					edit.commit();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setClass(Person.this,
									com.seeyon.line.Gesture.class);
							startActivity(intent);
							Person.this.finish();

						}
					}, 500);
					break;
				case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
					break;
				default:
					break;
				}
			} else if (strtoclass.equals("登录密码")) {
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setClass(Person.this, ChangePwdActivity.class);
							startActivity(intent);
							Person.this.finish();

						}
					}, 500);
					break;
				case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
					break;
				default:
					break;
				}

			} else if (strtoclass.equals("退出登录")) {
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {

							exit();

						}
					}, 500);
					break;
				case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
					break;
				default:
					break;
				}
			}
		}
	};

}
