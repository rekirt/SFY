package com.example.shoufuyi.api;

public class ApiUrl {

    //开发环境
    public static final String DEV_BASE_URL = "http://114.215.238.44/FreePage/public/api/";
    //生产环境美服IP
    public static final String ONLINE_URL = "http://47.88.35.61/FreePage/public/api/";

    public static String getAbsoluteUrl(String relativeUrl) {
        String url = ONLINE_URL + relativeUrl;
        return url;
    }
    public static final String LOGIN = "user/login";
}
