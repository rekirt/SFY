package com.cchtw.sfy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;
import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;
import com.cchtw.sfy.uitls.view.ContentView;
import com.cchtw.sfy.uitls.view.DrawlRoute;

/**
 * 
 * @author huanghp
 *字段解释： shareprefer中当"Is_exit"设置为 "1"的时候表示重新登录 "mima"当它有值的时候表示已经保存的手势密码
 *         Is_Login是用来判断有手势密码文字是显示“再次输入手势密码”还是“忘记手势密码”
 */
public class SetGestureActivity extends BaseActivity {

	private FrameLayout body_layout;
	private ContentView content;
	private TextView mTvShowMsg;
    private int errorTime;
    private boolean isChangeFingerPwd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture);
        isChangeFingerPwd = getIntent().getBooleanExtra("isChangeFingerPwd",false);
		body_layout = (FrameLayout) findViewById(R.id.body_layout);
        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);
        String temp = AccountHelper.getUserFingerPwdTimes();
        errorTime =  Integer.parseInt(temp)-1;
        oldFingerPwd = AccountHelper.getUserFingerPwd();
        initView();
        setCanBack(true);
        AccountHelper.setUserFingerPwdTimes(5);
    }

    private String oldFingerPwd;
    private String is_regserect = "";

    private boolean haveSetFingerPwd;
	private void initView() {
		// 初始化一个显示各个点的viewGroup
		is_regserect = AccountHelper.getUserFingerPwd();
        haveSetFingerPwd = AccountHelper.haveSetFingerPwd();

		if (haveSetFingerPwd) {//如果已经设置了手势密码
            mTvShowMsg.setText("忘记手势密码");
			mTvShowMsg.setOnClickListener(this);
		} else {//如果是没有设置过手势密码
			if (is_regserect.equals("")) {
				mTvShowMsg.setText("请输入手势密码");
			} else {
				mTvShowMsg.setText("请再次输入手势密码");
			}
		}
        content = new ContentView(this, is_regserect, new DrawlRoute.GestureCallBack() {

            @Override
            public void checkedSuccess() {
                if (haveSetFingerPwd && isChangeFingerPwd){
                    AccountHelper.haveFingerPwdChange(false);
                    AccountHelper.setUserFingerPwd("");
                    initView();
                    ToastHelper.ShowToast("请输入新的手势密码!");
                }else {
                    AccountHelper.haveFingerPwdChange(true);
                    AccountHelper.setUserFingerPwd(is_regserect);
                    ToastHelper.ShowToast("设置成功!");
                    Intent intent = new Intent(SetGestureActivity.this, MainActivity.class);
                    startActivity(intent);
                    SetGestureActivity.this.finish();
                }
            }

            @Override
            public void checkedFail() {
                errorTime--;
                if (errorTime < 1) {
                    AccountHelper.setUser(null);
                    Intent intent = new Intent(SetGestureActivity.this,LoginActivity.class);
                    startActivity(intent);
                    SetGestureActivity.this.finish();
                }else {
                    if (haveSetFingerPwd){
                        AccountHelper.setUserFingerPwdTimes(errorTime);
                        ToastHelper.ShowToast("今天还允许输入错误" + errorTime + "次");
                    }else {
                        ToastHelper.ShowToast("两次手势不一致!");
                    }
                }
            }

            @Override
            public void register() {
                initView();
                ToastHelper.ShowToast("请再输入一次!");
            }
        });
        // 设置手势解锁显示到哪个布局里面
        content.setParentView(body_layout);
	}

    private String tempFingerPwd = "";
//    private void initView() {
//        String mFingerPwd = AccountHelper.getUserFingerPwd();
//
//        if (TextUtils.isEmpty(mFingerPwd)) {
//            mTvShowMsg.setText("请输入手势密码");
//            mTvShowMsg.setOnClickListener(this);
//        } else {//如果是没有设置过手势密码
//            if (isChangeFingerPwd) {
//                mTvShowMsg.setText("忘记手势密码");
//            } else {
//                mTvShowMsg.setText("请再次输入手势密码");
//            }
//        }
//
//        content = new ContentView(this, tempFingerPwd, new DrawlRoute.GestureCallBack() {
//
//            @Override
//            public void checkedSuccess() {
//                if (haveSetFingerPwd && isChangeFingerPwd){
//                    AccountHelper.haveFingerPwdChange(false);
//                    AccountHelper.setUserFingerPwd("");
//                    initView();
//                    ToastHelper.ShowToast("请输入新的手势密码!");
//                }else {
//                    AccountHelper.haveFingerPwdChange(true);
//                    AccountHelper.setUserFingerPwd(tempFingerPwd);
//                    Intent intent = new Intent(SetGestureActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    SetGestureActivity.this.finish();
//                }
//            }
//
//            @Override
//            public void checkedFail() {
//                errorTime--;
//                if (errorTime < 1) {
//                    AccountHelper.setUser(null);
//                    Intent intent = new Intent(SetGestureActivity.this,LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                    SetGestureActivity.this.finish();
//                }else {
//                    if (haveSetFingerPwd){
//                        AccountHelper.setUserFingerPwdTimes(errorTime);
//                        ToastHelper.ShowToast("今天还允许输入错误" + errorTime + "次");
//                    }else {
//                        ToastHelper.ShowToast("两次手势不一致!");
//                    }
//                }
//            }
//
//            @Override
//            public void register() {
//
//                initView();
//            }
//        });
//        // 设置手势解锁显示到哪个布局里面
//        content.setParentView(body_layout);
//    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if ("忘记手势密码".equals(mTvShowMsg.getText())){
            Intent intent = new Intent(SetGestureActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(oldFingerPwd)){
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            AccountHelper.setUserFingerPwd("");
                            SetGestureActivity.this.finish();
                        }
                    });
        }else {
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            AccountHelper.setUserFingerPwd(oldFingerPwd);
                            SetGestureActivity.this.finish();
                        }
                    });
        }
    }

    @Override
    public void goBack(View view) {
        if (TextUtils.isEmpty(oldFingerPwd)){
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            AccountHelper.setUserFingerPwd("");
                            SetGestureActivity.this.finish();
                        }
                    });
        }else {
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            AccountHelper.setUserFingerPwd(oldFingerPwd);
                            SetGestureActivity.this.finish();
                        }
                    });
        }
    }
}
