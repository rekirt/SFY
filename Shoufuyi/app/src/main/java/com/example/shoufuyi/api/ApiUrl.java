package com.example.shoufuyi.api;

public class ApiUrl {

    //开发环境
    public static final String DEV_BASE_URL = "";
    //生产环境美服IP
    public static final String ONLINE_URL = "http://www.itisfs.com/SfyRelayServices_v1/appRevServer";

    public static String getAbsoluteUrl(String relativeUrl) {
        String url = ONLINE_URL + relativeUrl;
        return url;
    }
    public static final String LOGIN = "user/login";
}
