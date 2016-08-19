package com.cchtw.sfy.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.adapter.RecycleBaseAdapter;
import com.cchtw.sfy.adapter.SignRecycleAdapter;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.bean.ListEntity;
import com.cchtw.sfy.bean.SignList;
import com.cchtw.sfy.cache.v2.CacheManager;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.DatePickListener;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.TDevice;
import com.cchtw.sfy.uitls.TimeUtils;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.WeakAsyncTask;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.cchtw.sfy.uitls.view.DividerItemDecoration;
import com.cchtw.sfy.uitls.view.EmptyLayout;
import com.itech.message.APP_120023;
import com.itech.message.Page;
import com.itech.message.Result_120023;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class QuerySignActivity extends BaseActivity implements
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
    private Spinner spinstatus;
    private EditText edt_date;
    private ImageView iv_more;
    // 采集状态默认待采集
    private int collstate = 0;
    private int totalPage = 1000;
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
        setContentView(R.layout.activity_query_sign);
        initView();
        initData();
        setCanBack(true);
    }

    private void initView(){
        spinstatus = (Spinner) findViewById(R.id.spinstatus);
        edt_date = (EditText) findViewById(R.id.edt_date);
        iv_more = (ImageView) findViewById(R.id.iv_more);
        mErrorLayout = (EmptyLayout) findViewById(R.id.error_layout);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mRecycleView = (RecyclerView) findViewById(R.id.recycleView);
        mLayoutManager = new LinearLayoutManager(QuerySignActivity.this);
    }

    private void initData(){
        edt_date.setOnClickListener(new DatePickListener(this, edt_date));
        iv_more.setOnClickListener(new DatePickListener(this, edt_date));
        setDate(edt_date);
        spinstatus.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 > 0) {
                    collstate = arg2;
                    refresh();
                }else if (collstate != 0){
                    collstate = arg2;
                    refresh();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        mSwipeRefresh.setColorSchemeResources(R.color.main_green, R.color.main_gray, R.color.main_black, R.color.main_purple);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mRecycleView.setOnScrollListener(mScrollListener);
        if(isNeedListDivider()) {
            mRecycleView.addItemDecoration(new DividerItemDecoration(QuerySignActivity.this,
                    DividerItemDecoration.VERTICAL_LIST));
        }
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
                refresh();
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

    private void setDate(EditText edt){
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Date beginDate = TimeUtils.getDaysAgo(0);
        Calendar beginCalendar = Calendar.getInstance();
        if (beginDate == null){
            return;
        }
        beginCalendar.setTime(beginDate);
        edt.setText(beginCalendar.get(Calendar.YEAR) + "." + changeNumber(beginCalendar.get(Calendar.MONTH)+1) + "." + changeNumber(beginCalendar.get(Calendar.DATE)) + "至\n" + calendar.get(Calendar.YEAR) + "." + changeNumber(calendar.get(Calendar.MONTH)+1) + "." + changeNumber(calendar.get(Calendar.DATE)) );
    }



    private String changeNumber(int number){
        if (number<10){
            return ("0"+number);
        }
        return number+"";
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            default:
                break;
        }
    }


    APP_120023 mReturnApp = new APP_120023();
    private ArrayList<Result_120023> mSignList = new ArrayList<Result_120023>();
    private RequestHandle requestHandle;
    protected void sendRequestData() {
        String endTime ="";
        String startTime = "";
        String mDate = edt_date.getText().toString();
        if ("提交日期".equals(mDate)){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            endTime = format.format(date);
        }else {
            String date[] = mDate.split("至\n");
            startTime = date[0].replace(".","");
            endTime = date[1].replace(".","");
        }

        APP_120023 app = new APP_120023();
        app.setMerchantId(AccountHelper.getMerchantId());
        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        app.setCreateUser(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        Page page = new Page();
        if (mCurrentPage>totalPage){
            mAdapter.setState(RecycleBaseAdapter.STATE_NO_MORE);
            return;
        }
        page.setPageNo(String.valueOf(mCurrentPage));
        page.setPageSize(String.valueOf(TDevice.getPageSize()));
        app.setPage(page);
        if (collstate != 0){
            app.setState(String.valueOf(collstate));
        }
        startTime = startTime.replaceAll(" ", "");
        endTime = endTime.replaceAll(" ","");
        app.setCreateDateStart(startTime);
        app.setCreateDateEnd(endTime);

        requestHandle = ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler(QuerySignActivity.this) {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        try {
                            mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120023.class);
                            totalPage =  Integer.parseInt(mReturnApp.getPage().getPageTotal());
                            mSignList = (ArrayList<Result_120023>) mReturnApp.getResultList();
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

                    @Override
                    public void onFinish() {
                        DialogHelper.dismissProgressDialog();
                    }
                }
        );
    }


    public void refresh() {
        mCurrentPage = 1;
        mState = STATE_REFRESH;
        totalPage = 1000;
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
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
        signRecycleAdapter = new SignRecycleAdapter(QuerySignActivity.this);
        return signRecycleAdapter;
    }

    @Override
    public void onItemClick(View view) {

    }

    @Override
    public boolean onItemLongClick(View view) {
        return false;
    }



    static class ReadCacheTask extends WeakAsyncTask<Void, Void, byte[], QuerySignActivity> {

        public ReadCacheTask(QuerySignActivity target) {
            super(target);
        }

        @Override
        protected byte[] doInBackground(QuerySignActivity target,
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
        protected void onPostExecute(QuerySignActivity target,
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
        private WeakReference<QuerySignActivity> mInstance;
        private byte[] responseData;
        private boolean parserError;
        private boolean fromCache;
        private List<?> list;

        public ParserTask(QuerySignActivity instance, byte[] data, boolean fromCache) {
            this.mInstance = new WeakReference<>(instance);
            this.responseData = data;
            this.fromCache = fromCache;
        }

        @Override
        protected String doInBackground(Void... params) {
            QuerySignActivity instance = mInstance.get();
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
            QuerySignActivity instance = mInstance.get();
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


    private static final String CACHE_KEY_PREFIX = "QuerySignList_";
    private static final long MAX_CACAHE_TIME = 12 * 3600 * 1000;// 缓存最长时间为12小时
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
