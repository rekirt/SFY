package com.cchtw.sfy.uitls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.cchtw.sfy.BaseApplication;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Rrtim
 * Description:
 * Created by FuHL on
 * Date:2015-09-08
 * Time:上午11:31
 * Copyright © 2015年 广东亿迅科技有限公司. All rights reserved.
 */
public class ImageHelper {

    public static boolean savePngToSd(Bitmap bm, String url) {
        if (bm == null) {
            return false;
        }
        String filename = convertUrlToFileName(url);
        String dir = getDirectory(filename);
        File file = new File(dir + "/" + filename);
        try {
            file.createNewFile();
            file.setWritable(Boolean.TRUE);
            OutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
             TLog.analytics("FileNotFoundException");
        } catch (Error e) {
            TLog.analytics("SD卡已满");
        }
        return false;
    }

    public static boolean saveBmpToSd(Bitmap bm, String url) {
        if (bm == null) {
            return false;
        }
        try {
            File mFolder = new File(url);
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            File f = new File(url);
            f.createNewFile();
            f.setWritable(Boolean.TRUE);
            OutputStream outStream = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            TLog.analytics("保存图片报错:" + sw.toString());
        } catch (Error e) {
            TLog.analytics("SD卡已满");
        }
        return false;
    }

    public static boolean saveBmpToSd(Bitmap bm, String url,String name) {
        if (bm == null) {
            return false;
        }
        try {
            File mFolder = new File(url);
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            File f = new File(mFolder.getAbsolutePath(), name);
            f.createNewFile();
            f.setWritable(Boolean.TRUE);
            OutputStream outStream = new FileOutputStream(f);

            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            TLog.analytics("保存图片报错:" + sw.toString());
        } catch (Error e) {
            TLog.analytics("SD卡已满");
        }

        return false;
    }

    public static String convertUrlToFileName(String url) {
        String filename = url;
        filename = filename.replace("http://10.18.96.185:8080/", "");
        filename = filename.replace("http://14.31.15.41:18080/", "");
        filename = filename.replace("http://113.108.186.158/", "");
        filename = filename.replace("http://14.31.15.39/", "");
        filename = filename.replace("http://tp3.sinaimg.cn/", "");
        filename = filename.replace("http://113.108.186.137/", "");
        filename = filename.replace("http://113.108.186.137:81/", "");
        filename = filename.replace("http://192.168.115.37/", "");
        filename = filename.replace("/", "_");
        return filename;
    }

    // 根据文件名组成全路径前缀
    private final static String IMUSIC_DIR = "cn.eshore.rrtim";
    public static String getDirectory(String filename) {
        String extStorageDirectory = "/mnt/sdcard";
        String dirPath = extStorageDirectory + "/" + IMUSIC_DIR;
        File dirFile = new File(dirPath);
        dirFile.mkdirs();
        dirPath = dirPath + "/image_cache";
        dirFile = new File(dirPath);
        dirFile.mkdir();
        return dirPath;
    }

    public static void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, new ImageViewAware(imageView), BaseApplication.options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }
            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public static void displayCircleImage(String uri, ImageView imageView) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false)
                .cacheInMemory(true) //启用内存缓存
                .cacheOnDisk(true)  //启用外存缓存
                .considerExifParams(true) //启用EXIF和JPEG图像格式
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new CircleBitmapDisplayer())
                .handler(new Handler()) // default
                .build();
        ImageLoader.getInstance().displayImage(uri, new ImageViewAware(imageView),options);
    }

    /**
     * 获得图像
     *
     * @param path
     * @param options
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap getBitmapByPath(String path, BitmapFactory.Options options,
                                         int width, int height) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        FileInputStream in = null;
        in = new FileInputStream(file);
        if (options != null) {
            final int minSideLength = Math.min(width, height);
            int inSimpleSize = computeSampleSize(options, minSideLength, width
                    * height);
            options.inSampleSize = inSimpleSize; // 设置缩放比例
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
        }
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(in, null, options);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            System.out.println("Exception:" + sw.toString());

        } catch (Error e2) {
            StringWriter sw2 = new StringWriter();
            e2.printStackTrace(new PrintWriter(sw2, true));
            System.out.println("Error:" + sw2.toString());
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }


    /**
     * 获取需要进行缩放的比例，即options.inSampleSize
     *
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    public static final int UNCONSTRAINED = -1;
    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
                .ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED)
                && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


    /*
     * 获得设置信息
     */
    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 只描边，不读取数据
        // BitmapFactory.decodeFile(path, options);
        try {
            BitmapFactory
                    .decodeStream(new FileInputStream(path), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return options;
    }


}
