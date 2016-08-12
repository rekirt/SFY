package com.cchtw.sfy.uitls;

import java.util.Calendar;
import java.util.Date;

/**
 * Shoufuyi
 * Description:
 * Created by Sunny on
 * Date:2016-08-12
 * Time:下午1:00
 * Copyright © 2016年 FuHongLiang All rights reserved.
 */
public class TimeUtils {

    /**
     * 如果传入参数小于0将会返回null
     * @param i
     * @return
     */
    public static Date getDaysAgo(int i) {
        if (i<0){
            return null;
        }
        Date date = null;
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
//        long ct = cal.getTimeInMillis() / 86400000;
        long ct = System.currentTimeMillis() / 86400000;
        long days = (ct - i) * 86400000;
        date = new Date(days);
        return date;
    }

}
