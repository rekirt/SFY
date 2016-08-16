//package com.cchtw.sfy.activity;
//
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.cchtw.sfy.R;
//import com.cchtw.sfy.uitls.AccountHelper;
//import com.cchtw.sfy.uitls.ToastHelper;
//import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;
//import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;
//import com.cchtw.sfy.uitls.view.ContentView;
//import com.cchtw.sfy.uitls.view.DrawlRoute;
//
//
//public class ChangeGestureActivity extends BaseActivity {
//
//	private FrameLayout body_layout;
//	private ContentView content;
//	private TextView mTvShowMsg;
//    private int errorTime;
//    private boolean isChangeFingerPwd;
//    private String oldFingerPwd;
//
//    @Override
//	protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_change_gesture);
//        AccountHelper.setUserFingerPwdTimes(5);
//        initView();
//        setCanBack(true);
//        checkFingerPwd();
//    }
//
//    private void initView() {
//        body_layout = (FrameLayout) findViewById(R.id.body_layout);
//        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);
//        String temp = AccountHelper.getUserFingerPwdTimes();
//        errorTime =  Integer.parseInt(temp)-1;
//        oldFingerPwd = AccountHelper.getUserFingerPwd();
//    }
//
//    private void checkFingerPwd(){
//        content = new ContentView(this, oldFingerPwd, new DrawlRoute.GestureCallBack() {
//
//            @Override
//            public void checkedSuccess() {
//                    resetPwd();
//                    ToastHelper.ShowToast("请输入新的手势密码!");
//                    mTvShowMsg.setText("请输入新的手势密码!");
//            }
//
//            @Override
//            public void checkedFail() {
//                    errorTime--;
//                    if (errorTime < 1) {
//                        AccountHelper.setUser(null);
//                        Intent intent = new Intent(ChangeGestureActivity.this,LoginActivity.class);
//                        startActivity(intent);
//                        ChangeGestureActivity.this.finish();
//                    }
//                    AccountHelper.setUserFingerPwdTimes(errorTime);
//                    ToastHelper.ShowToast("今天还允许输入错误" + errorTime + "次");
//            }
//
//            @Override
//            public void register() {
//
//            }
//        });
//        content.setParentView(body_layout);
//    }
//
//    private void resetPwd(){
//
//        content = new ContentView(this, AccountHelper.getUserFingerPwd(), new DrawlRoute.GestureCallBack() {
//
//            @Override
//            public void checkedSuccess() {
//                ToastHelper.ShowToast("设置成功!");
//                ChangeGestureActivity.this.finish();
//            }
//
//            @Override
//            public void checkedFail() {
//                AccountHelper.haveFingerPwdChange(false);
//                AccountHelper.setUserFingerPwd("");
//                resetPwd();
//                ToastHelper.ShowToast("两次手势不一致,请重新设置！");
//            }
//
//            @Override
//            public void register() {
//                ToastHelper.ShowToast("请再输入一次!");
//                mTvShowMsg.setText("请再次输入手势密码");
//            }
//        });
//        content.setParentView(body_layout);
//    }
//
//
//    @Override
//    public void onClick(View view) {
//        super.onClick(view);
//        if ("忘记手势密码".equals(mTvShowMsg.getText())){
//            Intent intent = new Intent(ChangeGestureActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (TextUtils.isEmpty(oldFingerPwd)){
//            AlertDialogHelper.showAlertDialog(ChangeGestureActivity.this,
//                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {
//
//                        @Override
//                        public void doClick(DialogInterface dialog,
//                                            int which) {
//                            AccountHelper.setUserFingerPwd("");
//                            ChangeGestureActivity.this.finish();
//                        }
//                    });
//        }else {
//            AlertDialogHelper.showAlertDialog(ChangeGestureActivity.this,
//                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {
//
//                        @Override
//                        public void doClick(DialogInterface dialog,
//                                            int which) {
//                            AccountHelper.setUserFingerPwd(oldFingerPwd);
//                            ChangeGestureActivity.this.finish();
//                        }
//                    });
//        }
//    }
//
//    @Override
//    public void goBack(View view) {
//        if (TextUtils.isEmpty(oldFingerPwd)){
//            AlertDialogHelper.showAlertDialog(ChangeGestureActivity.this,
//                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {
//
//                        @Override
//                        public void doClick(DialogInterface dialog,
//                                            int which) {
//                            AccountHelper.setUserFingerPwd("");
//                            ChangeGestureActivity.this.finish();
//                        }
//                    });
//        }else {
//            AlertDialogHelper.showAlertDialog(ChangeGestureActivity.this,
//                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {
//
//                        @Override
//                        public void doClick(DialogInterface dialog,
//                                            int which) {
//                            AccountHelper.setUserFingerPwd(oldFingerPwd);
//                            ChangeGestureActivity.this.finish();
//                        }
//                    });
//        }
//    }
//}
