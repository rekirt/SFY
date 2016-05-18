package com.example.shoufuyi.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.ActivityCollector;
import com.example.shoufuyi.uitls.dialog.WaitDialog;


/**
 * MiGo
 * Description:
 * Created by FuHL on
 * Date:2016-01-14
 * Time:下午1:20
 * Copyright © 2016年 FuHL. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public Context mContext;
    public TextView toolbarTitle;
    public WaitDialog _waitDialog;
    public LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);//将Activity加入list集合进行管理
        mContext = this;
        mInflater = getLayoutInflater();
    }

    public View mToolbar;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
            mToolbar = findViewById(R.id.toolbar);
            if (mToolbar != null) {
                Toolbar toolbar = (Toolbar) mToolbar;
                setSupportActionBar(toolbar);
                toolbarTitle = (TextView) mToolbar.findViewById(R.id.tv_toolbar_title);
                if (toolbarTitle != null) {
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
            }
    }


    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);//移除掉所有内存中的活动
    }

    protected View inflateView(int resId) {
        return mInflater.inflate(resId, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mToolbar != null && canGoBack()) {
            ImageView mIvBack = (ImageView) mToolbar.findViewById(R.id.img_back);
            mIvBack.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 判断是否短时间内重复点击
     */
    private long lastClick = 0;

    public boolean IsNotDuplication() {
        if (System.currentTimeMillis() - lastClick <= 2500) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }

    /**
     * 点击返回按钮调用该方法
     *
     * @param view 绑定的组件
     */
    public void goBack(View view) {
        if (IsNotDuplication()){
            finish();
        }
    }

    /**
     * 点击右上角完成按钮调用该方法
     *
     * @param view 绑定的组件
     */
    public void haveComplete(View view) {

    }

    public boolean canBack = false;

    public boolean canGoBack() {
        return canBack;
    }

    public void setCanBack(boolean goBack) {
        canBack = goBack;
    }


    @Override
    public void onClick(View view) {
        if (!IsNotDuplication()) {
            return;
        }
    }

    public void setRightText(String rightText) {
        if (mToolbar != null) {
            TextView mTvRight = (TextView) mToolbar.findViewById(R.id.tv_right);
            mTvRight.setVisibility(View.VISIBLE);
            mTvRight.setText(rightText);
        }
    }
    public void setRightView() {
        if (mToolbar != null) {
            ImageView mIvRight = (ImageView) mToolbar.findViewById(R.id.img_right);
            mIvRight.setVisibility(View.VISIBLE);
            mIvRight.setOnClickListener(this);
        }
    }
    public void setLeftText(String leftText) {
        if (mToolbar != null) {
            TextView tv_toolbar_left_title = (TextView) mToolbar.findViewById(R.id.tv_left);
            tv_toolbar_left_title.setVisibility(View.VISIBLE);
            tv_toolbar_left_title.setText(leftText);
        }
    }

}