package com.cchtw.sfy.api;

public class ApiUrl {

    //开发环境
    public static final String DEV_BASE_URL = "";
    //开发环境
//    public static final String ONLINE_URL = "http://www.itisfs.com/SfyRelayServices_v1/appRevServer";
    //正式环境
    public static final String ONLINE_URL = "http://cps.gnete.com/appRevServer";
    //测试环境
//    public static final String ONLINE_URL = "http://cps.gnete.com:65/appRevServer";

    public static String getAbsoluteUrl(String relativeUrl) {
        String url = ONLINE_URL + relativeUrl;
        return url;
    }
    public static final String LOGIN = "user/login";
}
