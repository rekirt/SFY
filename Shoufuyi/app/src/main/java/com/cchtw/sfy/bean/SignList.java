package com.cchtw.sfy.bean;

import com.alibaba.fastjson.JSON;
import com.itech.message.APP_120023;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SignList extends Entity implements ListEntity {

    private static final long serialVersionUID = 1067118838408833362L;
    public static final String NODE_NEWS_COUNT = "businessCount";
    public final static int CATALOG_ALL = 1;
    public final static int CATALOG_INTEGRATION = 2;
    public final static int CATALOG_SOFTWARE = 3;

    private int catalog;
    private int pageSize;
    private int businessCount;
    private List<APP_120023> signlist = new ArrayList<APP_120023>();

    public List<APP_120023> getBusinesslist() {
        return signlist;
    }

    public void setBusinesslist(List<APP_120023> businesslist) {
        this.signlist = businesslist;
    }

    public int getBusinessCount() {
        return businessCount;
    }

    public void setBusinessCount(int businessCount) {
        this.businessCount = businessCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCatalog() {
        return catalog;
    }

    public void setCatalog(int catalog) {
        this.catalog = catalog;
    }

    public static SignList parse(int catalog,int pageSize,int businessCount, byte[]  inputStream) throws IOException{
        SignList businessList = new SignList();
        try {
            ArrayList<APP_120023> mBusinessList = new ArrayList<APP_120023>();
//            mBusinessList = GsonUtils.fromJsonArrayToArrayList(new String(inputStream, "UTF-8"), APP_120023.class);
            mBusinessList = (ArrayList<APP_120023>) JSON.parseArray(new String(inputStream, "UTF-8"), APP_120023.class);
            businessList.setCatalog(catalog);
            businessList.setPageSize(pageSize);
            businessList.setBusinessCount(businessCount);
            businessList.setBusinesslist(mBusinessList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return businessList;
    }

    @Override
    public List<?> getList() {
        return signlist;
    }
}
