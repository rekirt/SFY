package com.example.shoufuyi.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.view.ViewfinderView;
import com.example.shoufuyi.uitls.wintone.FrameCapture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import wintone.idcard.android.IDCardAPI;
import wintone.idcard.android.RecogParameterMessage;
import wintone.idcard.android.RecogService;
import wintone.idcard.android.ResultMessage;

/**
 * 
 * 
 * 项目名称：idcard_sample_sdk 类名称：CameraActivity 类描述：手动拍照护照MRZ码识别的类。创建人：huangzhen
 * 创建时间：2014年7月10日 下午3:22:10 修改人：huanzhen 修改时间：2014年11月11日 下午3:22:10
 * 修改备注：在原来的基础上更改拍照界面使程序变得更加美观
 * 
 * @version
 * 
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, OnClickListener {
    public String PATH = Environment.getExternalStorageDirectory().toString()
            + "/wtimage/";
    private String strCaptureFilePath;
    private int width, height, srcwidth, srcheight, WIDTH, HEIGHT;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private RelativeLayout rlayout, layout_set;
    private boolean isPort = false;// 判断是否是竖屏
    private RelativeLayout rightlyaout, bg_camera_doctype;
    private ToneGenerator tone;
    private Bitmap bitmap;
    private byte[] imageData;
    private Canvas canvas;
    public RecogService.recogBinder recogBinder;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private boolean istakePic = false;// 判断是否已经拍照，如果已经拍照则提示正在识别中请稍等
    private float scale = 1;
    private long time, recogTime;
    private boolean isCompress = false;// 是否将分辨率大的图片压缩成小的图片
    private ViewfinderView viewfinder_view;
    private int uiRot = 0;
    private Bitmap rotate_bitmap;
    private int rotationWidth, rotationHeight;

    private boolean isOpenFlash = false;
    private ImageButton imbtn_flash, imbtn_camera_back;
    private IDCardAPI api = new IDCardAPI();
    // 设置全局变量，进行自动检测参数的传递 start
    private byte[] data1;
    private int regWidth, regHeight, left, right, top, bottom, nRotateType;
    private TextView tv_camera_doctype;
    private int lastPosition = 0;
    // end
    private int quality = 100;
    private final String IDCardPath = Environment.getExternalStorageDirectory()
            .toString() + "/AndroidWT/IdCapture/";
    private String picPathString = PATH + "WintoneIDCard.jpg";
    private String HeadJpgPath = PATH + "head.jpg";
    private String recogResultPath = PATH + "idcapture.txt",
            recogResultString = "";
    private double screenInches;
    private int[] nflag = new int[4];
    private boolean isTakePic = false;
    private int count = 0;
    private String devcode = "";
    public static int nMainIDX;
    private Vibrator mVibrator;
    private int Format = ImageFormat.NV21;// .YUY2
    private String name = "";
    private boolean isFocusSuccess = false;
    private boolean isTouched = false;
    private boolean isFirstGetSize = true;
    private Size size;
    private boolean isScroll = false;
    private TimerTask timer;
    Handler handler2 = new Handler();
    Handler handler = new Handler();
    Handler handler1 = new Handler();
    Runnable touchTimeOut = new Runnable() {
        @Override
        public void run() {

            isTouched = false;
        }
    };
    private int DelayedFrames = 0;
    private boolean isConfirmSideLine = true;
    private int ConfirmSideSuccess = 0;
    private int CheckPicIsClearCounts = 0;

    @Override
    protected void onStart() {
        super.onStart();
        RecogService.isRecogByPath = false;
        findView();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Intent intent = getIntent();
        nMainIDX = intent.getIntExtra("nMainId", 2);
        devcode = intent.getStringExtra("devcode");
        viewfinder_view.setIdcardType(nMainIDX);
        tv_camera_doctype.setTextColor(Color.rgb(243, 153, 18));
        switch (nMainIDX) {
        case 3000:
            tv_camera_doctype.setText(getString(R.string.mrz));
            break;
        case 13:
            tv_camera_doctype.setText(getString(R.string.passport));
            break;
        case 2:
            tv_camera_doctype.setText(getString(R.string.ID_card));
            break;
        case 9:
            tv_camera_doctype.setText(getString(R.string.EPT_HK_Macau));
            break;
        case 11:
            tv_camera_doctype.setText(getString(R.string.MRTTTP));
            break;
        case 12:
            tv_camera_doctype.setText(getString(R.string.visa));
            break;
        case 22:
            tv_camera_doctype.setText(getString(R.string.NEEPT_HK_Macau));
            break;
        case 5:
            tv_camera_doctype.setText(getString(R.string.china_driver));
            break;
        case 6:
            tv_camera_doctype
                    .setText(getString(R.string.china_driving_license));
            break;
        case 1001:
            tv_camera_doctype.setText(getString(R.string.HK_IDcard));
            break;
        case 14:
            tv_camera_doctype.setText(getString(R.string.HRPO));
            break;
        case 15:
            tv_camera_doctype.setText(getString(R.string.HRPR));
            break;
        case 1005:
            tv_camera_doctype.setText(getString(R.string.IDCard_Macau));
            break;
        case 10:
            tv_camera_doctype.setText(getString(R.string.TRTTTMTP));
            break;
        case 1031:
            tv_camera_doctype.setText(getString(R.string.Taiwan_IDcard_front));
            break;
        case 1032:
            tv_camera_doctype
                    .setText(getString(R.string.Taiwan_IDcard_reverse));
            break;
        case 1030:
            tv_camera_doctype
                    .setText(getString(R.string.National_health_insurance_card));
            break;
        case 2001:
            tv_camera_doctype.setText(getString(R.string.MyKad));
            break;
        case 2004:
            tv_camera_doctype.setText(getString(R.string.Singapore_IDcard));
            break;
        case 2003:
            tv_camera_doctype.setText(getString(R.string.Driver_license));
            break;
        case 2002:
            tv_camera_doctype
                    .setText(getString(R.string.California_driver_license));
            break;
        default:
            break;
        }

    }

    @Override
    @SuppressLint("NewApi")
    @TargetApi(19)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        setContentView(R.layout.wintone_demo_carmera);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        // android设备的物理尺寸
        double x = Math
                .pow(displayMetrics.widthPixels / displayMetrics.xdpi, 2);
        double y = Math.pow(displayMetrics.heightPixels / displayMetrics.ydpi,
                2);
        screenInches = Math.sqrt(x + y);
        // android设备的物理尺寸 end
        rotationWidth = displayMetrics.widthPixels;
        rotationHeight = displayMetrics.heightPixels;

    }

    /**
     * 
     * @Title: findView
     * @Description: 总界面布局
     * @return void 返回类型
     * @throws
     */
    public void findView() {
        bg_camera_doctype = (RelativeLayout) findViewById(R.id.bg_camera_doctype);
        viewfinder_view = (ViewfinderView) this
                .findViewById(R.id.viewfinder_view);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        imbtn_flash = (ImageButton) this.findViewById(R.id.imbtn_flash);
        imbtn_camera_back = (ImageButton) this
                .findViewById(R.id.imbtn_camera_back);
        imbtn_camera_back.setOnClickListener(this);
        tv_camera_doctype = (TextView) this
                .findViewById(R.id.tv_camera_doctype);
        imbtn_flash.setOnClickListener(this);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        uiRot = getWindowManager().getDefaultDisplay().getRotation();
        viewfinder_view.setDirecttion(uiRot);
        // 闪光灯的UI布局
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) (width * 0.05), (int) (width * 0.05));
        layoutParams.leftMargin = (int) (width * 0.04);
        layoutParams.topMargin = (int) (height * 0.05);
        imbtn_flash.setLayoutParams(layoutParams);

        // 返回按钮的UI布局
        layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.05),
                (int) (width * 0.05));
        layoutParams.leftMargin = (int) (width * 0.04);
        layoutParams.topMargin = (int) (height * 0.97) - (int) (width * 0.06);
        imbtn_camera_back.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, height);
        surfaceView.setLayoutParams(layoutParams);

        // 证件类型背景UI布局
        layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.65),
                (int) (width * 0.05));
        layoutParams.leftMargin = (int) (width * 0.2);
        layoutParams.topMargin = (int) (height * 0.46);
        bg_camera_doctype.setLayoutParams(layoutParams);
        if (screenInches >= 8) {
            tv_camera_doctype.setTextSize(25);
        } else {
            tv_camera_doctype.setTextSize(20);
        }

    }

    @SuppressLint("NewApi")
    @TargetApi(14)
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        if (camera != null) {

            Camera.Parameters parameters = camera.getParameters();
            List<Size> list = parameters.getSupportedPreviewSizes();
            Size size;
            int previewWidth = 640;
            int previewheight = 480;
            int second_previewWidth = 0;
            int second_previewheight = 0;
            if ((float) width / height == (float) 4 / 3
                    || (float) width / height == (float) 3 / 4) {

                int length = list.size();

                if (length == 1) {
                    size = list.get(0);
                    previewWidth = size.width;
                    previewheight = size.height;
                } else {

                    for (int i = 0; i < length; i++) {
                        size = list.get(i);

                        if ((size.height <= 960 || size.width <= 1280)
                                && (((float) size.width / size.height == (float) 4 / 3) || (float) size.width
                                        / size.height == (float) 3 / 4)) {
                            second_previewWidth = size.width;
                            second_previewheight = size.height;
                            if (previewWidth < second_previewWidth) {
                                previewWidth = second_previewWidth;
                                previewheight = second_previewheight;
                            }

                        }

                    }
                }
            } else {
                int length = list.size();

                if (length == 1) {
                    size = list.get(0);
                    previewWidth = size.width;
                    previewheight = size.height;
                } else {
                    for (int i = 0; i < length; i++) {
                        size = list.get(i);
                        /*
                         * if (size.width == 1920 && size.height == 1080) {
                         * previewWidth = 1920; previewheight = 1080; break; }
                         * else
                         */if (size.height <= 960
                                || size.width <= 1280
                                && (((float) size.width / size.height != (float) 4 / 3) && (float) size.width
                                        / size.height != (float) 3 / 4)) {
                            second_previewWidth = size.width;
                            second_previewheight = size.height;
                            if (previewWidth <=second_previewWidth) {
                                previewWidth = second_previewWidth;
                                previewheight = second_previewheight;
                            }
                        }
                    }
                }
            }
            WIDTH = previewWidth;
            HEIGHT = previewheight;
            // }

            if (parameters.getSupportedFocusModes().contains(
                    parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
            }

            parameters.setPictureFormat(PixelFormat.JPEG);

            parameters.setExposureCompensation(0);
            parameters.setPreviewSize(WIDTH/* 1920 */, HEIGHT/* 1080 */);
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            camera.setPreviewCallback(CameraActivity.this);
            camera.setParameters(parameters);
            camera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // 获得Camera对象
        try {
            if (null == camera) {
                camera = Camera.open();
            }

            Timer time = new Timer();
            if (timer == null) {
                timer = new TimerTask() {
                    public void run() {
                        if (camera != null) {
                            try {
                                isFocusSuccess = false;
                                autoFocus();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                };
            }
            time.schedule(timer, 200, 2500);
        } catch (Exception e) {

        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        synchronized (this) {
            try {
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                Log.i("TAG", e.getMessage());
            }
        }
    }

    public void closeCamera() {
        synchronized (this) {
            try {
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                Log.i("TAG", e.getMessage());
            }
        }
    }

    public void autoFocus() {

        if (camera != null) {
            synchronized (camera) {
                try {
                    if (camera.getParameters().getSupportedFocusModes() != null
                            && camera
                                    .getParameters()
                                    .getSupportedFocusModes()
                                    .contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        camera.autoFocus(new AutoFocusCallback() {
                            public void onAutoFocus(boolean success,
                                    Camera camera) {
                                if (success) {
                                    isFocusSuccess = true;
                                    System.out.println("isFocusSuccess:"
                                            + isFocusSuccess);
                                }

                            }
                        });
                    } else {

                        Toast.makeText(getBaseContext(),
                                getString(R.string.unsupport_auto_focus),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    camera.stopPreview();
                    camera.startPreview();
                    Toast.makeText(this, R.string.toast_autofocus_failure,
                            Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    // 监听返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            closeCamera();
            Intent intent = new Intent();

            intent.setClass(CameraActivity.this, MainActivity.class);
            // 设置切换动画，从右边进入，左边退出

            CameraActivity.this.finish();
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPreviewFrame(byte[] data, final Camera camera) {
        if (isTouched) {
            return;
        }
        if (isFirstGetSize) {
            isFirstGetSize = false;
            size = camera.getParameters().getPreviewSize();

        }

        if (nMainIDX != 3000) {
            // 非机读码识别
            if (!isTakePic) {
                if (isFocusSuccess) {
                    int CheckPicIsClear = 0;
                    if (nMainIDX == 2 || nMainIDX == 22 || nMainIDX == 1030
                            || nMainIDX == 1031 || nMainIDX == 1032
                            || nMainIDX == 1005 || nMainIDX == 1001
                            || nMainIDX == 2001 || nMainIDX == 2004
                            || nMainIDX == 2002 || nMainIDX == 2003
                            || nMainIDX == 14 || nMainIDX == 15) {
                        // 预留参数
                        // api.SetROI(
                        // (int) (size.width * 0.1),
                        // (int) (size.height - 0.47313085 * size.width) / 2,
                        // (int) (size.width * 0.9),
                        // (int) (size.height + 0.47313085 * size.width) / 2);//
                        // 预留参数
                        // 预留参数
                        api.SetROI(
                                (int) (size.width * 0.2),
                                (int) (size.height - 0.41004673 * size.width) / 2,
                                (int) (size.width * 0.85),
                                (int) (size.height + 0.41004673 * size.width) / 2);// 预留参数
                        left=(int) (size.width * 0.2);
                        top=(int) (size.height - 0.41004673 * size.width) / 2;
                        right= (int) (size.width * 0.85);
                        bottom=(int) (size.height + 0.41004673 * size.width) / 2;

                    } else if (nMainIDX == 5 || nMainIDX == 6) {
                        // 预留参数
                        api.SetROI(
                                (int) (size.width * 0.24),
                                (int) (size.height - 0.41004673 * size.width) / 2,
                                (int) (size.width * 0.81),
                                (int) (size.height + 0.41004673 * size.width) / 2);// 预留参数
                        left=(int) (size.width * 0.24);
                        top=(int) (size.height - 0.41004673 * size.width) / 2;
                        right= (int) (size.width * 0.81);
                        bottom=(int) (size.height + 0.41004673 * size.width) / 2;
                    } else {
                        // api.SetROI(
                        // (int) (size.width * 0.1),
                        // (int) (size.height - 0.51923077 * size.width) / 2,
                        // (int) (size.width * 0.85),
                        // (int) (size.height + 0.51923077 * size.width) / 2);//
                        // 预留参数
                        api.SetROI((int) (size.width * 0.2),
                                (int) (size.height - 0.45 * size.width) / 2,
                                (int) (size.width * 0.85),
                                (int) (size.height + 0.45 * size.width) / 2);// 预留参数
                        left=(int) (size.width * 0.2);
                        top=(int) (size.height - 0.45 * size.width) / 2;
                        right= (int) (size.width * 0.85);
                        bottom=(int) (size.height + 0.45 * size.width) / 2;
                    }
                    if (isConfirmSideLine) {
                        // 0是二代证、健保卡等，1适用于驾照行驶证,2适用于护照类的
                        if (nMainIDX == 5 || nMainIDX == 6) {
                            api.SetConfirmSideMethod(1);
                        } else if (nMainIDX == 13 || nMainIDX == 9
                                || nMainIDX == 10 || nMainIDX == 11
                                || nMainIDX == 12) {
                            api.SetConfirmSideMethod(2);
                        } else {
                            api.SetConfirmSideMethod(0);
                        }
                        ConfirmSideSuccess = api.ConfirmSideLine(data,
                                size.width, size.height, nflag);
                    }
                    if (ConfirmSideSuccess == 1) {
                        isConfirmSideLine = false;
                        CheckPicIsClearCounts = CheckPicIsClearCounts + 1;
                        if (CheckPicIsClearCounts > 3) {
                            isConfirmSideLine = true;
                            CheckPicIsClearCounts = 0;
                            return;
                        }
                        CheckPicIsClear = api.CheckPicIsClear(data, size.width,
                                size.height);
                        // CheckPicIsClear=1;
                        if (CheckPicIsClear == 1) {
                            // data1 = data;
                            // ConfirmSideSuccess = api.ConfirmSideLine(data1,
                            // size.width, size.height, nflag);
                            isConfirmSideLine = true;
                            CheckPicIsClearCounts = 0;
                            viewfinder_view.setCheckLeftFrame(nflag[0]);
                            viewfinder_view.setCheckTopFrame(nflag[1]);
                            viewfinder_view.setCheckRightFrame(nflag[2]);
                            viewfinder_view.setCheckBottomFrame(nflag[3]);
                        }
                    }
                    // System.out.println("ConfirmSideSuccess:"
                    // + ConfirmSideSuccess + "--" + "CheckPicIsClear:"
                    // + CheckPicIsClear);
                    if (ConfirmSideSuccess == 1 && CheckPicIsClear == 1) {

                        data1 = data;
                        // // huangzhen测试
                        // // closeCamera();
                        // name = pictureName();
                        // picPathString = PATH + "WintoneIDCard_" + name +
                        // ".jpg";
                        // recogResultPath = PATH + "idcapture_" + name +
                        // ".txt";
                        // HeadJpgPath = PATH + "head_" + name + ".jpg";
                        // String picPathString1 = PATH + "WintoneIDCard_" +
                        // name
                        // + "_full.jpg";
                        // File file = new File(PATH);
                        // if (!file.exists())
                        // file.mkdirs();
                        // // ////////////start
                        // // 如果有证件就将nv21数组保存成jpg图片 huangzhen
                        // YuvImage yuvimage = new YuvImage(data1, Format,
                        // size.width, size.height, null);
                        // ByteArrayOutputStream baos = new
                        // ByteArrayOutputStream();
                        // if (nMainIDX == 2 || nMainIDX == 22 || nMainIDX ==
                        // 1030
                        // || nMainIDX == 1031 || nMainIDX == 1032
                        // || nMainIDX == 1005 || nMainIDX == 1001
                        // || nMainIDX == 2001 || nMainIDX == 2004
                        // || nMainIDX == 2002 || nMainIDX == 2003
                        // || nMainIDX == 14 || nMainIDX == 15
                        // || nMainIDX == 5 || nMainIDX == 6
                        // || nMainIDX == 13) {
                        //
                        // yuvimage.compressToJpeg(new Rect(0, 0, size.width,
                        // size.height), quality, baos);
                        //
                        // } else {
                        // yuvimage.compressToJpeg(
                        // new Rect(
                        // (int) (size.width * 0.15),
                        // (int) (size.height - 0.47 * size.width) / 2,
                        // (int) (size.width * 0.8),
                        // (int) (size.height + 0.47 * size.width) / 2),
                        // quality, baos);
                        // }
                        // FileOutputStream outStream;
                        // try {
                        // outStream = new FileOutputStream(picPathString1);
                        // outStream.write(baos.toByteArray());
                        // outStream.close();
                        // baos.close();
                        // } catch (IOException e) {
                        // // TODO Auto-generated catch block
                        // e.printStackTrace();
                        // }
                        // // ////////////end
                        // // huangzhen测试

                        name = pictureName();
                        picPathString = PATH + "WintoneIDCard_" + name + ".jpg";
                        isTakePic = true;                      
    				
    					new FrameCapture(data1,WIDTH,HEIGHT,left, top,right, bottom,"11");
                        if (timer != null)
                            timer.cancel();
                        time = System.currentTimeMillis();
                        RecogService.isRecogByPath = false;
                        Intent recogIntent = new Intent(CameraActivity.this,
                                RecogService.class);
                        bindService(recogIntent, recogConn,
                                Service.BIND_AUTO_CREATE);
                    }
                }
            }

        } else {
            // 机读码识别

            data1 = data;
            regWidth = size.width;
            regHeight = size.height;
            left = (int) (0.15 * size.width);
            right = (int) (size.width * 0.85);
            top = size.height / 3;
            bottom = 2 * size.height / 3;
            int returnType = api.GetAcquireMRZSignalEx(data1, size.width,
                    size.height, left, right, top, bottom, 0);
            switch (returnType) {
            case 1:
                if (!istakePic) {
                    nMainIDX = 1034;

                    istakePic = true;
                    time = System.currentTimeMillis();

                    name = pictureName();
                    picPathString = PATH + "WintoneIDCard_" + name + ".jpg";
                    recogResultPath = PATH + "idcapture_" + name + ".txt";
                    HeadJpgPath = PATH + "head_" + name + ".jpg";
                    createPreviewPicture(data1, "WintoneIDCard_" + name
                            + ".jpg", PATH, regWidth, regHeight, left, top,
                            right, bottom);
                   
                    
                    Intent recogIntent = new Intent(CameraActivity.this,
                            RecogService.class);
                    bindService(recogIntent, recogConn,
                            Service.BIND_AUTO_CREATE);
                    new FrameCapture(data1,regWidth, regHeight,left, top,right, bottom,"11");
                }
                break;
            case 2:
                if (!istakePic) {
                    nMainIDX = 1036;

                    istakePic = true;
                    time = System.currentTimeMillis();

                    name = pictureName();
                    picPathString = PATH + "WintoneIDCard_" + name + ".jpg";
                    recogResultPath = PATH + "idcapture_" + name + ".txt";
                    HeadJpgPath = PATH + "head_" + name + ".jpg";
                    createPreviewPicture(data1, "WintoneIDCard_" + name
                            + ".jpg", PATH, regWidth, regHeight, left, top,
                            right, bottom);

                    Intent recogIntent = new Intent(CameraActivity.this,
                            RecogService.class);
                    bindService(recogIntent, recogConn,
                            Service.BIND_AUTO_CREATE);
                    new FrameCapture(data1,regWidth, regHeight,left, top,right, bottom,"11");
                }
                break;
            case 3:
                if (!istakePic) {
                    nMainIDX = 1033;

                    istakePic = true;
                    time = System.currentTimeMillis();

                    name = pictureName();
                    picPathString = PATH + "WintoneIDCard_" + name + ".jpg";
                    recogResultPath = PATH + "idcapture_" + name + ".txt";
                    HeadJpgPath = PATH + "head_" + name + ".jpg";
                    createPreviewPicture(data1, "WintoneIDCard_" + name
                            + ".jpg", PATH, regWidth, regHeight, left, top,
                            right, bottom);

                    Intent recogIntent = new Intent(CameraActivity.this,
                            RecogService.class); 
                    bindService(recogIntent, recogConn,
                            Service.BIND_AUTO_CREATE);
                   
                    new FrameCapture(data1,regWidth, regHeight,left, top,right, bottom,"11");
                }
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 返回按鈕点击事件
        case R.id.imbtn_camera_back:
            closeCamera();
//            Intent intent = new Intent();
//            intent.setClass(CameraActivity.this, MainActivity.class);
            // 设置切换动画，从右边进入，左边退出
            CameraActivity.this.finish();
//            startActivity(intent);
            break;
        // 闪光灯点击事件
        case R.id.imbtn_flash:
            // TODO Auto-generated method stub
           if(camera==null)camera=Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<String> flashList = parameters.getSupportedFlashModes();
            if (flashList != null
                    && flashList.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                if (!isOpenFlash) {
                    imbtn_flash.setBackgroundResource(R.drawable.flash_off);
                    isOpenFlash = true;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                } else {
                    imbtn_flash.setBackgroundResource(R.drawable.flash_on);
                    isOpenFlash = false;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.unsupportflash), Toast.LENGTH_SHORT)
                        .show();
            }

            break;
        default:
            break;
        }
    }

    // 创建文件
    public void createFile(String path, String content, boolean iscreate) {
        if (iscreate) {
            System.out.println("path:" + path);
            File file = new File(path.substring(0, path.lastIndexOf("/")));
            if (!file.exists()) {
                file.mkdirs();
            }
            File newfile = new File(path);
            if (!newfile.exists()) {

                try {
                    newfile.createNewFile();
                    OutputStream out = new FileOutputStream(path);
                    byte[] buffer = content.toString().getBytes();
                    out.write(buffer, 0, buffer.length);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                newfile.delete();
                try {
                    newfile.createNewFile();
                    OutputStream out = new FileOutputStream(path);
                    byte[] buffer = content.toString().getBytes();
                    out.write(buffer, 0, buffer.length);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        ;
    }

    private ArrayList<Size> splitSize(String str, Camera camera) {
        if (str == null)
            return null;
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        ArrayList<Size> sizeList = new ArrayList<Size>();
        while (tokenizer.hasMoreElements()) {
            Size size = strToSize(tokenizer.nextToken(), camera);
            if (size != null)
                sizeList.add(size);
        }
        if (sizeList.size() == 0)
            return null;
        return sizeList;
    }

    private Size strToSize(String str, Camera camera) {
        if (str == null)
            return null;
        int pos = str.indexOf('x');
        if (pos != -1) {
            String width = str.substring(0, pos);
            String height = str.substring(pos + 1);
            return camera.new Size(Integer.parseInt(width),
                    Integer.parseInt(height));
        }
        return null;
    }

    // 识别验证
    public ServiceConnection recogConn = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            recogBinder = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {

            recogBinder = (RecogService.recogBinder) service;

            RecogParameterMessage rpm = new RecogParameterMessage();
            rpm.nTypeLoadImageToMemory = 0;
            rpm.nMainID = nMainIDX;
            rpm.nSubID = null;
            rpm.GetSubID = true;
            rpm.GetVersionInfo = true;
            rpm.logo = "";
            rpm.userdata = "";
            rpm.sn = "";
            rpm.authfile = "";
            rpm.isCut = false;
            rpm.triggertype = 0;
            rpm.devcode = devcode;
            rpm.isOnlyClassIDCard = true;
            if (nMainIDX == 3000) {
                // 自动识别参数 start
                rpm.nv21bytes = data1;
                rpm.top = top;
                rpm.bottom = bottom;
                rpm.left = left;
                rpm.right = right;
                rpm.nRotateType = nRotateType;
                rpm.width = regWidth;
                rpm.height = regHeight;
                rpm.lpFileName="";
            } else if (nMainIDX == 2) {
                rpm.isAutoClassify = true;
                rpm.nv21bytes = data1;
                rpm.nv21_width = WIDTH;
                rpm.nv21_height = HEIGHT;
                rpm.lpHeadFileName = HeadJpgPath;
                rpm.lpFileName = picPathString; // rpm.lpFileName当为空时，会执行自动识别函数
            } else {
                rpm.nv21bytes = data1;
                rpm.nv21_width = WIDTH;
                rpm.nv21_height = HEIGHT;
                rpm.lpHeadFileName = HeadJpgPath;
                rpm.lpFileName = picPathString; // rpm.lpFileName当为空时，会执行自动识别函数
            }
            // end
            try {

                camera.stopPreview();
                ResultMessage resultMessage;
                System.out.println("开发码++++:"+rpm.devcode);
                resultMessage = recogBinder.getRecogResult(rpm);
                if (resultMessage.ReturnAuthority == 0
                        && resultMessage.ReturnInitIDCard == 0
                        && resultMessage.ReturnLoadImageToMemory == 0
                        && resultMessage.ReturnRecogIDCard > 0) {
                    String iDResultString = "";
                    String[] GetFieldName = resultMessage.GetFieldName;
                    String[] GetRecogResult = resultMessage.GetRecogResult;
                    // 获得字段位置坐标的函数
                    // List<int[]>listdata= resultMessage.textNamePosition;
                    istakePic = false;
                    for (int i = 1; i < GetFieldName.length; i++) {
                        if (GetRecogResult[i] != null) {
                            if (!recogResultString.equals(""))
                                recogResultString = recogResultString
                                        + GetFieldName[i] + ":"
                                        + GetRecogResult[i] + ",";
                            else {
                                recogResultString = GetFieldName[i] + ":"
                                        + GetRecogResult[i] + ",";
                            }
                        }
                    }
                    camera.setPreviewCallback(null);
                    mVibrator = (Vibrator) getApplication().getSystemService(
                            Service.VIBRATOR_SERVICE);
                    mVibrator.vibrate(200);
                    System.out
                            .println("recogResultString:" + recogResultString);
                    closeCamera();

//                    Intent intent = new Intent(CameraActivity.this,
//                            ShowResultActivity.class);
//                    intent.putExtra("recogResult", recogResultString);
//                    if (devcode != null)
//                        intent.putExtra("devcode", devcode);
//                    CameraActivity.this.finish();
//                    startActivity(intent);

                    Intent intent = new Intent();
//                    int length = GetRecogResult.length - 1;
                    intent.putExtra("recogResult", GetRecogResult[6]);
                    CameraActivity.this.setResult(RESULT_OK, intent);
                    CameraActivity.this.finish();
                } else {
                    String string = "";
                    if (resultMessage.ReturnAuthority == -100000) {
                        string = getString(R.string.exception)
                                + resultMessage.ReturnAuthority;
                    } else if (resultMessage.ReturnAuthority != 0) {
                        string = getString(R.string.exception1)
                                + resultMessage.ReturnAuthority;
                    } else if (resultMessage.ReturnInitIDCard != 0) {
                        string = getString(R.string.exception2)
                                + resultMessage.ReturnInitIDCard;
                    } else if (resultMessage.ReturnLoadImageToMemory != 0) {
                        if (resultMessage.ReturnLoadImageToMemory == 3) {
                            string = getString(R.string.exception3)
                                    + resultMessage.ReturnLoadImageToMemory;
                        } else if (resultMessage.ReturnLoadImageToMemory == 1) {
                            string = getString(R.string.exception4)
                                    + resultMessage.ReturnLoadImageToMemory;
                        } else {
                            string = getString(R.string.exception5)
                                    + resultMessage.ReturnLoadImageToMemory;
                        }
                    } else if (resultMessage.ReturnRecogIDCard <= 0) {
                        if (resultMessage.ReturnRecogIDCard == -6) {
                            string = getString(R.string.exception9);
                        } else {
                            string = getString(R.string.exception6)
                                    + resultMessage.ReturnRecogIDCard;
                        }
                    }
                    closeCamera();
                    Intent intent = new Intent();
                    intent.putExtra("recogResult", recogResultString);
                    intent.putExtra("exception", string);
                    CameraActivity.this.finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("错误信息："+e);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.recognized_failed),
                        Toast.LENGTH_SHORT).show();

            } finally {
                if (recogBinder != null) {
                    unbindService(recogConn);
                }
            }

        }
    };

    public void createPreviewPicture(byte[] reconData, String pictureName,
            String path, int preWidth, int preHeight, int left, int top,
            int right, int bottom) {
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        // 如果有证件就将nv21数组保存成jpg图片 huangzhen
        YuvImage yuvimage = new YuvImage(reconData, Format, preWidth,
                preHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(left, top, right, bottom), quality,
                baos);

        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(path + pictureName);
            outStream.write(baos.toByteArray());
            outStream.close();
            baos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 如果有证件就将nv21数组保存成jpg图片 huangzhen
    }

    /**
     * 
     * @Title: pictureName
     * @Description: 将文件命名
     * @param @return 设定文件
     * @return String 文件以时间命的名字
     * @throws
     */
    public String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow(); // 取得系统时间。
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        if (month < 10)
            str = String.valueOf(year) + "0" + String.valueOf(month);
        else {
            str = String.valueOf(year) + String.valueOf(month);
        }
        if (date < 10)
            str = str + "0" + String.valueOf(date);
        else {
            str = str + String.valueOf(date);
        }
        if (hour < 10)
            str = str + "0" + String.valueOf(hour);
        else {
            str = str + String.valueOf(hour);
        }
        if (minute < 10)
            str = str + "0" + String.valueOf(minute);
        else {
            str = str + String.valueOf(minute);
        }
        if (second < 10)
            str = str + "0" + String.valueOf(second);
        else {
            str = str + String.valueOf(second);
        }
        return str;
    }

}
