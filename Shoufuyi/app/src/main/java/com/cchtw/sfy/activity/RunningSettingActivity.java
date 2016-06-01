package com.cchtw.sfy.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;

/**
 * Shoufuyi
 * Description:
 * Created by fuhongliang on
 * Date:16/5/18
 * Time:上午9:11
 * Copyright © 2016-05-16/5/18 Jason. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class RunningSettingActivity extends BaseActivity{
    private TextView tv_page_size;
    private TextView tv_time_out;
    private TextView tv_finger_times;
    private TextView tv_video_time;
    private TextView tv_video_cache_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_settings);
        setCanBack(true);
        initView();
        initData();
    }
    private void initView(){
        tv_page_size = (TextView) findViewById(R.id.tv_page_size);
        tv_time_out = (TextView) findViewById(R.id.tv_time_out);
        tv_finger_times = (TextView) findViewById(R.id.tv_finger_times);
        tv_video_time = (TextView) findViewById(R.id.tv_video_time);
        tv_video_cache_time = (TextView) findViewById(R.id.tv_video_cache_time);
    }

    private void initData(){
        tv_page_size.setText(new StringBuilder().append(SharedPreferencesHelper.getString(Constant.PAGESIZE, "4")).append("条").toString());
        tv_time_out.setText(new StringBuilder().append(SharedPreferencesHelper.getString(Constant.TIMEOUT, "30")).append("秒").toString());
        tv_finger_times.setText(new StringBuilder().append(SharedPreferencesHelper.getString(Constant.FINGERPASSWORDTIMES, "5")).append("次").toString());
        tv_video_time.setText(new StringBuilder().append(SharedPreferencesHelper.getString(Constant.VEDIOLONG, "10")).append("秒").toString());
        tv_video_cache_time.setText(new StringBuilder().append(SharedPreferencesHelper.getString(Constant.VEDIOANDPHOTOCACHELONG, "10")).append("天").toString());
    }
}
