package com.example.shoufuyi.activity;

import android.os.Bundle;
import android.view.View;

import com.example.shoufuyi.R;

/**
 * Shoufuyi
 * Description:
 * Created by fuhongliang on
 * Date:16/5/18
 * Time:下午2:48
 * Copyright © 2016-05-16/5/18 Jason. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class NewSignActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sign);
        initView();
        initData();
        setCanBack(true);
    }


    private void initView(){

    }

    private void initData(){

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
