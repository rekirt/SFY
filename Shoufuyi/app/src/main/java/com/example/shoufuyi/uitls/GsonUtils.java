package com.example.shoufuyi.uitls;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

/**
 * Rrtim
 * Description:
 * Created by FuHL on
 * Date:2015-09-08
 * Time:上午9:01
 * Copyright © 2015年 广东亿迅科技有限公司. All rights reserved.
 */
public class GsonUtils {
    /**
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json,Class<T> cls){
        Gson gson = new Gson();
        return gson.fromJson(json, cls);
    }

    public static <T> ArrayList<T> fromJsonArrayToArrayList(String json, Class<T> clazz) throws Exception {
        ArrayList<T> lst = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            lst.add(new Gson().fromJson(elem, clazz));
        }
        return lst;
    }
}
