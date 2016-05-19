package com.example.shoufuyi.bean;

import java.io.Serializable;

/**
 * FreePage
 * Description:
 * Created by FuHL on
 * Date:2016-01-22
 * Time:下午4:34
 * Copyright © 2016年 FuHL. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class SignBean implements Serializable{
    public String business_id;//商务信息id
    public String title;//信息标题
    public String gallery;//图片链接
    public String summary;//简介
    public String start_time;//业务开始时间
    public String end_time;//业务结束时间
    public String nice_start_time;//时间
    public boolean vip_limited;//是否VIP可见
    public String views;//查阅数量


}
