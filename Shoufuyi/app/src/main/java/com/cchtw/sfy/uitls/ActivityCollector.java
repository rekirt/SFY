package com.cchtw.sfy.uitls;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * MiGo
 * Description:
 * Created by FuHL on
 * Date:2016-01-14
 * Time:下午3:18
 * Copyright © 2016年 FuHL. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity)
    {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        List delList = new ArrayList();//用来装需要删除的元素
        for(Activity activity:activities)
            if(!activity.isFinishing()){
                delList.add(activity);
                activity.finish();
            }
        activities.removeAll(delList);//遍历完成后执行删除
    }
}
