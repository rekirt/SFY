package com.cchtw.sfy.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.listener.DialogListener;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.ActivityCollector;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.UpdateManager;
import com.cchtw.sfy.uitls.cache.ACache;
import com.itech.message.APP_Parameters;
import com.itech.message.APP_RunParm;
import com.itech.message.APP_Version;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {
    /** ViewPager中ImageView的容器 */
    private List<ImageView> imageViewContainer = null;

    /** 上一个被选中的小圆点的索引，默认值为0 */
    private int preDotPosition = 0;

    /** Banner文字描述数组 */
//    private String[] bannerTextDescArray = {
//            "巩俐不低俗，我就不能低俗",
//            "朴树又回来了，再唱经典老歌引万人大合唱",
//            "揭秘北京电影如何升级",
//            "乐视网TV版大派送", "热血屌丝的反杀"
//    };

    /** Banner滚动线程是否销毁的标志，默认不销毁 */
    private boolean isStop = false;

    /** Banner的切换下一个page的间隔时间 */
    private long scrollTimeOffset = 5000;

    private ViewPager viewPager;

    /** Banner的文字描述显示控件 */
//    private TextView tvBannerTextDesc;

    /** 小圆点的父控件 */
    private LinearLayout llDotGroup;

    private GridView gridview;

    private boolean isCheckUpdate = false;
    private boolean isShowingDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        isCheckUpdate = getIntent().getBooleanExtra("isCheckUpdate",false);

        initView();
        initData();
        startBannerScrollThread();
        setRightView();
        updataRunParameter();//更新运行参数

        update = new UpdateManager(MainActivity.this, new DialogListener() {
            @Override
            public void cancel() {
                isCheckUpdate = false;
                isShowingDialog = false;
            }

            @Override
            public void ok() {
                isShowingDialog= true;
            }
        });
    }

    private UpdateManager update;

    // 更新操作；
    private void checkUpdate() {
        APP_Version app = new APP_Version();
        app.setTerminalType("1");
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
            String oldCode = pi.versionName;
            app.setVersion(oldCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ApiRequest.requestData(app, AccountHelper.getUserName(), new JsonHttpHandler(MainActivity.this) {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                final APP_Version returnapp = JSON.parseObject(responseJsonObject.toString(), APP_Version.class);
                if ("0000".equals(returnapp.getDetailCode())) {
                    if (isCheckUpdate){
                        isCheckUpdate = !isCheckUpdate;
                        update.checkUpdateInfo(returnapp);
                    }else if (returnapp.isForceUpgrade()){
                        update.checkUpdateInfo(returnapp);
                    }
                }
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFinish() {
            }
        });
    }


    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        llDotGroup = (LinearLayout) findViewById(R.id.ll_dot_group);
//        tvBannerTextDesc = (TextView) findViewById(R.id.tv_banner_text_desc);
        gridview = (GridView) findViewById(R.id.GridView);
    }

    private void initData(){
        ArrayList<HashMap<String, Object>> mMenuList = new ArrayList<HashMap<String, Object>>();
        int[] res = { R.drawable.new_sign, R.drawable.unfinish_sign, R.drawable.search_sign };
        String[] mMenuName = { "新建签约", "未完成签约", "签约查询", "设置" };
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", res[i]);
            map.put("ItemText", "" + mMenuName[i]);
            mMenuList.add(map);
        }

        SimpleAdapter saItem = new SimpleAdapter(this, mMenuList, // 数据源
                R.layout.item_gridview, // xml实现
                new String[] { "ItemImage", "ItemText" }, // 对应map的Key
                new int[] { R.id.ItemImage, R.id.ItemText }); // 对应R的Id

        // 添加Item到网格中
        gridview.setAdapter(saItem);
        // 添加点击事件
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                switch (arg2){
                    case 0:
                        Intent intent = new Intent();
                        intent.putExtra("where", "1");
                        intent.setClass(MainActivity.this, NewSignActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Intent intentToUnfinished = new Intent();
                        intentToUnfinished.setClass(MainActivity.this, UnfinishedActivity.class);
                        startActivity(intentToUnfinished);
                        break;
                    case 2:
                        Intent intentQuery = new Intent();
                        intentQuery.setClass(MainActivity.this, QuerySignActivity.class);
                        startActivity(intentQuery);
                        break;
                    default:
                        break;
                }
            }
        });


        imageViewContainer = new ArrayList<ImageView>();
        int[] imageIDs = new int[] {
                R.drawable.banner,
                R.drawable.banner,
                R.drawable.banner,
                R.drawable.banner,
                R.drawable.banner,
        };

        ImageView imageView = null;
        View dot = null;
        LayoutParams params = null;
        for (int id : imageIDs) {
            imageView = new ImageView(this);
            imageView.setBackgroundResource(id);
            imageViewContainer.add(imageView);

            // 每循环一次添加一个点到线行布局中
            dot = new View(this);
            dot.setBackgroundResource(R.drawable.dot_bg_selector);
            params = new LayoutParams(20, 20);
            params.leftMargin = 15;
            dot.setEnabled(false);
            dot.setLayoutParams(params);
            llDotGroup.addView(dot); // 向线性布局中添加"点"
        }

        viewPager.setAdapter(new BannerAdapter());
        viewPager.setOnPageChangeListener(new BannerPageChangeListener());

        // 选中第一个图片、文字描述
//        tvBannerTextDesc.setText(bannerTextDescArray[0]);
        llDotGroup.getChildAt(0).setEnabled(true);
        viewPager.setCurrentItem(0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUpdate();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.img_right:
                gotoSetting();
                break;
            default:
                break;
        }
    }

    public void gotoSetting() {
        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
    }

    /**
     * ViewPager的适配器
     */
    private class BannerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewContainer.get(position % imageViewContainer.size()));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = imageViewContainer.get(position % imageViewContainer.size());

            // 为每一个page添加点击事件
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
//                    Toast.makeText(MainActivity.this, "Page 被点击了", Toast.LENGTH_SHORT).show();
                }

            });

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    /**
     * Banner的Page切换监听器
     */
    private class BannerPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Nothing to do
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // Nothing to do
        }

        @Override
        public void onPageSelected(int position) {
            // 取余后的索引，得到新的page的索引
            int newPositon = position % imageViewContainer.size();
            // 根据索引设置图片的描述
//            tvBannerTextDesc.setText(bannerTextDescArray[newPositon]);
            // 把上一个点设置为被选中
            llDotGroup.getChildAt(preDotPosition).setEnabled(false);
            // 根据索引设置那个点被选中
            llDotGroup.getChildAt(newPositon).setEnabled(true);
            // 新索引赋值给上一个索引的位置
            preDotPosition = newPositon;
        }
    }

    /**
     * 开启Banner滚动线程
     */
    private void startBannerScrollThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!isStop) {
                    //每个两秒钟发一条消息到主线程，更新viewpager界面
                    SystemClock.sleep(scrollTimeOffset);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            int newindex = viewPager.getCurrentItem() + 1;
                            viewPager.setCurrentItem(newindex);
                        }
                    });
                }
            }
        }).start();
    }

    private long mExitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出",
                            Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    MainActivity.this.finish();
                    ActivityCollector.finishAll();
                }
            return true;
        }
        //拦截MENU按钮点击事件，让他无任何操作
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // 更新运行参数功能
    private void updataRunParameter() {
        APP_RunParm app = new APP_RunParm();
        app.setType("1000");
        if (!AccountHelper.isLogin()){
            return;
        }
        ApiRequest.requestData(app, AccountHelper.getUserName(), new JsonHttpHandler("detailCode","errMsg","data") {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                final APP_RunParm returnapp = JSON.parseObject(responseJsonObject.toString(), APP_RunParm.class);
                if (returnapp.getDetailCode().equals("0000")) {
                    List<APP_Parameters> list = returnapp.getParametersList();
                    for (APP_Parameters par : list) {
                        switch (par.getParCode()){
                            case "0001":
                                SharedPreferencesHelper.setString(Constant.PAGESIZE,par.getParValue());
                                break;
                            case "0002":
                                SharedPreferencesHelper.setString(Constant.FINGERPASSWORDTIMES,par.getParValue());
                                AccountHelper.setUserFingerPwdTimes(Integer.parseInt(par.getParValue()));
                                break;
                            case "0003":
                                SharedPreferencesHelper.setString(Constant.VEDIOLONG,par.getParValue());
                                break;
                            case "0004":
                                SharedPreferencesHelper.setString(Constant.VEDIOANDPHOTOCACHELONG,par.getParValue());
                                ACache.TIME_CACHE = ACache.TIME_DAY*(Integer.parseInt(par.getParValue()));
                                break;
                            case "0005":
                                SharedPreferencesHelper.setString(Constant.TIMEOUT,par.getParValue());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFinish() {
            }
        });
    }
}
