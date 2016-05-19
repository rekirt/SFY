package com.example.shoufuyi.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.adapter.RecycleBaseAdapter;
import com.example.shoufuyi.adapter.SignRecycleAdapter;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.bean.ListEntity;
import com.example.shoufuyi.bean.SignList;
import com.example.shoufuyi.cache.v2.CacheManager;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.GsonUtils;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.TDevice;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.WeakAsyncTask;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.example.shoufuyi.uitls.view.DividerItemDecoration;
import com.example.shoufuyi.uitls.view.EmptyLayout;
import com.itech.message.APP_120023;
import com.itech.message.Page;
import com.itech.message.Result_120023;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UnfinishedActivity extends BaseActivity implements
        RecycleBaseAdapter.OnItemClickListener, RecycleBaseAdapter.OnItemLongClickListener {
    protected static final int STATE_NONE = 0;
    protected static final int STATE_REFRESH = 1;
    protected static final int STATE_LOADMORE = 2;
    protected LinearLayoutManager mLayoutManager;
    protected int mState = STATE_REFRESH;

    protected SwipeRefreshLayout mSwipeRefresh;
    protected RecyclerView mRecycleView;
    protected RecycleBaseAdapter mAdapter;
    protected int mStoreEmptyState = -1;
    protected String mStoreEmptyMessage;
    protected EmptyLayout mErrorLayout;//错误页
    protected int mCurrentPage = 1;//列表第几页
    private ParserTask mParserTask;


    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (mState == STATE_NONE && mAdapter != null
                        && mAdapter.getDataSize() > 0) {
                    loadMore();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unfinished);
        initView();
        setCanBack(true);
    }

    private void initView(){
        mErrorLayout = (EmptyLayout) findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentPage = 1;
                mState = STATE_REFRESH;//错误页面点击后刷新
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                sendRequestData();
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.main_green, R.color.main_gray, R.color.main_black, R.color.main_purple);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecycleView = (RecyclerView) findViewById(R.id.recycleView);
        mRecycleView.setOnScrollListener(mScrollListener);

        if(isNeedListDivider()) {
            mRecycleView.addItemDecoration(new DividerItemDecoration(UnfinishedActivity.this,
                    DividerItemDecoration.VERTICAL_LIST));
        }

        mLayoutManager = new LinearLayoutManager(UnfinishedActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);

        if (mAdapter != null) {
            mRecycleView.setAdapter(mAdapter);
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        } else {
            mAdapter = getListAdapter();
            mAdapter.setOnItemClickListener(this);
            mAdapter.setOnItemLongClickListener(this);
            mRecycleView.setAdapter(mAdapter);

            if (requestDataIfViewCreated()) {
                mCurrentPage = 1;
                mState = STATE_REFRESH;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                new ReadCacheTask(this).execute();
            } else {
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }

        if (mStoreEmptyState != -1) {
            mErrorLayout.setErrorType(mStoreEmptyState);
        }
        if (!TextUtils.isEmpty(mStoreEmptyMessage)) {
            mErrorLayout.setErrorMessage(mStoreEmptyMessage);
        }
    }


    APP_120023 mReturnApp = new APP_120023();
    private ArrayList<Result_120023> mSignList = new ArrayList<Result_120023>();

    protected void sendRequestData() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        String endtime = format.format(date);
        APP_120023 app = new APP_120023();
        app.setMerchantId(SharedPreferencesHelper.getString(Constant.MERCHANT, ""));
        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        app.setCreateUser(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        Page page = new Page();
        page.setPageNo(String.valueOf(mCurrentPage));
        page.setPageSize(String.valueOf(200));
        app.setPage(page);
        app.setState(String.valueOf(1));
        app.setCreateDateStart("20151121");
        app.setCreateDateEnd(endtime);
        DialogHelper.showProgressDialog(UnfinishedActivity.this, "正在查询，请稍候...", true, false);
        ApiRequest.requestData(app,SharedPreferencesHelper.getString(Constant.PHONE, ""),new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        try {
                            mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120023.class);
                            mSignList = GsonUtils.fromJsonArrayToArrayList(mReturnApp.getResultList().toString(), Result_120023.class);
                            //更换解析方法
                            // save the cache
                            if (mCurrentPage == 1 && !TextUtils.isEmpty(getCacheKey())) {
                                CacheManager.setCache(getCacheKey(), responseJsonObject.getJSONArray("data").toString().getBytes(),
                                        getCacheExpire(), CacheManager.TYPE_INTERNAL);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        executeOnLoadDataSuccess(mSignList);
                        executeOnLoadFinish();
                    }

                    @Override
                    public void onDo(JSONArray responseJsonArray) {
                    }

                    @Override
                    public void onDo(String responseString) {
                    }

                    @Override
                    public void onFail(String msg) {
                        executeOnLoadDataError(msg);
                        executeOnLoadFinish();
                    }


                }.setProgressDialogCanceledOnTouchOutside(true)
                        .setProgressDialogCancleable(true)
                        .setIsNeedToReturnResponseBody(true)
        );
    }


    public void refresh() {
        mCurrentPage = 1;
        mState = STATE_REFRESH;
        sendRequestData();
    }

    public void loadMore() {
        if (mState == STATE_NONE) {
            if (mAdapter.getState() == RecycleBaseAdapter.STATE_LOAD_MORE) {
                mCurrentPage = mCurrentPage + 1;
                mState = STATE_LOADMORE;
                sendRequestData();
            }
        }
    }

    protected boolean requestDataIfViewCreated() {
        return true;
    }


    private SignRecycleAdapter signRecycleAdapter;

    protected SignRecycleAdapter getListAdapter() {
        signRecycleAdapter = new SignRecycleAdapter(UnfinishedActivity.this);
        return signRecycleAdapter;
    }

    @Override
    public void onItemClick(View view) {

    }

    @Override
    public boolean onItemLongClick(View view) {
        return false;
    }



    static class ReadCacheTask extends WeakAsyncTask<Void, Void, byte[], UnfinishedActivity> {

        public ReadCacheTask(UnfinishedActivity target) {
            super(target);
        }

        @Override
        protected byte[] doInBackground(UnfinishedActivity target,
                                        Void... params) {
            if (target == null) {
                return null;
            }
            if (TextUtils.isEmpty(target.getCacheKey())) {
                return null;
            }

            byte[] data = CacheManager.getCache(target.getCacheKey());
            if (data == null) {
                return null;
            }
            return data;
        }

        @Override
        protected void onPostExecute(UnfinishedActivity target,
                                     byte[] result) {
            super.onPostExecute(target, result);
            if (target == null)
                return;
            if (result != null) {
                try {
                    target.executeParserTask(result, true);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            target.refresh();
        }
    }

    private void executeParserTask(byte[] data, boolean fromCache) {
        cancelParserTask();
        mParserTask = new ParserTask(this, data, fromCache);
        mParserTask.execute();
    }

    private void cancelParserTask() {
        if (mParserTask != null) {
            mParserTask.cancel(true);
            mParserTask = null;
        }
    }

    // Parse model when request data success.
    private static class ParserTask extends AsyncTask<Void, Void, String> {
        private WeakReference<UnfinishedActivity> mInstance;
        private byte[] responseData;
        private boolean parserError;
        private boolean fromCache;
        private List<?> list;

        public ParserTask(UnfinishedActivity instance, byte[] data, boolean fromCache) {
            this.mInstance = new WeakReference<>(instance);
            this.responseData = data;
            this.fromCache = fromCache;
        }

        @Override
        protected String doInBackground(Void... params) {
            UnfinishedActivity instance = mInstance.get();
            if (instance == null){
                return null;
            }
            try {
                ListEntity data =instance.parseList(mCatalog,1, TDevice.getPageSize(),responseData);
                list = data.getList();
            } catch (Exception e) {
                e.printStackTrace();
                parserError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            UnfinishedActivity instance = mInstance.get();
            if (instance != null) {
                if (parserError) {
                    instance.executeOnLoadDataError(null);
                } else {
                    instance.executeOnLoadDataSuccess(list);
//                    if(!fromCache) {
//                        if (instance.mState == STATE_REFRESH) {
//                            instance.onRefreshNetworkSuccess();
//                        }
//                    }
                    instance.executeOnLoadFinish();
                }
                if (fromCache) {
                    instance.refresh();
                }
            }
        }
    }


    private static final String CACHE_KEY_PREFIX = "unfinishedSignList_";
    private static final long MAX_CACAHE_TIME = 12 * 3600 * 1000;// 资讯的缓存最长时间为12小时
    protected static int mCatalog = 1;

    protected String getCacheKeyPrefix() {
        return CACHE_KEY_PREFIX;
    }

    protected String getCacheKey() {
        return new StringBuffer(getCacheKeyPrefix()).append(mCatalog)
                .append("_").append(mCurrentPage).append("_")
                .append(TDevice.getPageSize()).toString();
    }
    public long getCacheExpire() {
        return Constant.CACHE_EXPIRE_OND_DAY;
    }

    protected ListEntity parseList(int catalog,int currentPage,int pageSize,byte[] is) throws Exception {
        SignList list = SignList.parse(catalog,currentPage,pageSize,is);
        return list;
    }



    protected boolean isNeedListDivider() {
        return false;
    }

    protected void executeOnLoadDataSuccess(List<?> data) {
        if (mState == STATE_REFRESH){
            mAdapter.clear();
        }
        mAdapter.addData(data);
        mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        if (data.size() == 0 && mState == STATE_REFRESH) {
            mErrorLayout.setErrorType(EmptyLayout.NODATA);
        } else if (data.size() < TDevice.getPageSize()) {
            if (mState == STATE_REFRESH)
                mAdapter.setState(RecycleBaseAdapter.STATE_LESS_ONE_PAGE);
            else
                mAdapter.setState(RecycleBaseAdapter.STATE_NO_MORE);
        } else {
            mAdapter.setState(RecycleBaseAdapter.STATE_LOAD_MORE);
        }
    }

    protected void executeOnLoadDataError(String error) {
        if (mCurrentPage == 1) {
            if (mAdapter.getDataSize() == 0) {
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
                ToastHelper.ShowToast(error);
            } else {
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
                String message = error;
                if (TextUtils.isEmpty(error)) {
                    if (TDevice.hasInternet()) {
                        message = getString(R.string.tip_load_data_error);
                    } else {
                        message = getString(R.string.tip_network_error);
                    }
                }
                ToastHelper.ShowToast(message);
            }
        } else {
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            mAdapter.setState(RecycleBaseAdapter.STATE_NETWORK_ERROR);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void executeOnLoadFinish() {
        mSwipeRefresh.setRefreshing(false);
        mState = STATE_NONE;
    }

}