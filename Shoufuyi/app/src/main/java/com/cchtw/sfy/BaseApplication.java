package com.cchtw.sfy;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

/**
 * MiGo
 * Description:
 * Created by FuHL on
 * Date:2016-01-14
 * Time:上午10:58
 * Copyright © 2016年 FuHL. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class BaseApplication extends Application {

    private static BaseApplication instance;//应用实例
    public static Context applicationContext;//App上下文
    public static DisplayImageOptions options;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applicationContext = this;
        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.default_image) // 加载图片时的图片
//                .showImageForEmptyUri(R.drawable.empty_photo) //没有图片资源时的默认图片
//                .showImageOnFail(R.drawable.ic_full_image_failed) //加载失败时的图片
                .resetViewBeforeLoading(true)
//                .delayBeforeLoading(100)//开启之后图片会有一个闪一下的感觉
                .cacheInMemory(true) //启用内存缓存
                .cacheOnDisk(true)  //启用外存缓存
                .considerExifParams(true) //启用EXIF和JPEG图像格式
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer()) // default
//                .displayer(new RoundedBitmapDisplayer(20)) //设置显示风格这里是圆角矩
                .handler(new Handler()) // default
                .build();

        initImageLoader();//初始化ImageLoader
        CrashReport.initCrashReport(getApplicationContext(), "900029836", false);
    }

    private void initImageLoader(){
        File cacheDir = StorageUtils.getOwnCacheDirectory(this, "imageloader/Cache");
        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY -2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2* 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheFileCount(2000)//缓存的文件数量
                .diskCache(new UnlimitedDiskCache(cacheDir))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(this,10 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for releaseapp
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }

    //获取应用实例
    public static BaseApplication getInstance() {
        return instance;
    }

    public static String string(int id) {
        return instance.getResources().getString(id);
    }
}
