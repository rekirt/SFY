package com.cchtw.sfy.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wintone.bankcard.BankCardAPI;
import com.wintone.bankcard.BankCardRecogUtils;
import com.wintone.utils.Utils;
import com.wintone.view.ViewfinderView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CardScanCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/SFY/Camera/";
    private Camera camera;
    private SurfaceView surfaceView;
    private RelativeLayout re_c;
    private SurfaceHolder surfaceHolder;
    private ImageButton back;
    private ImageButton flash;
    private ImageButton eject_btn;
    private ImageButton tackPic;
    private ImageView help_word;
    private ImageView wintone_logo;
    private ViewfinderView myView;
    private String TAG = this.getClass().getName();
    private BankCardAPI api;
    private Bitmap bitmap;
    private int preWidth = 0;
    private int preHeight = 0;
    private boolean isROI = false;
    private int width;
    private int height;
    private TimerTask timer;
    private Vibrator mVibrator;
    private ToneGenerator tone;
    private byte[] tackData;
    private TelephonyManager telephonyManager;
    private int WTAPP;
    private String placeActivity;
    private String countStrs;
    private String devCode;
    private static String query_sql = "select * from wt_lsc where _id=1";
    private static String insert_sql = "insert into wt_lsc(_id,wt_content) values(?,?)";
    private BankCardRecogUtils bankCardRecogUtils;
    private boolean isFatty;
    private String returnAciton;
    private String resultAciton;
    private String copyright;
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            try {
                if(CardScanCameraActivity.this.tone == null) {
                    CardScanCameraActivity.this.tone = new ToneGenerator(1, 0);
                }

                CardScanCameraActivity.this.tone.startTone(24);
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        }
    };
    private Camera.PictureCallback picturecallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(camera != null) {
                camera.setPreviewCallback((Camera.PreviewCallback)null);
            }

            Camera.Parameters parameters = camera.getParameters();
            int pW = parameters.getPreviewSize().width;
            int pH = parameters.getPreviewSize().height;
            if(CardScanCameraActivity.this.tackData != null) {
                int[] $t = Utils.convertYUV420_NV21toARGB8888(CardScanCameraActivity.this.tackData, pW, pH);
                BitmapFactory.Options t = new BitmapFactory.Options();
                t.inInputShareable = true;
                t.inPurgeable = true;
                CardScanCameraActivity.this.bitmap = Bitmap.createBitmap($t, pW, pH, Bitmap.Config.ARGB_8888);
            }

            if(CardScanCameraActivity.this.bitmap != null) {
                int $t1 = CardScanCameraActivity.this.height / 10;
                int b = CardScanCameraActivity.this.height - $t1;
                int $l = (int)((double)(b - $t1) * 1.58577D);
                int l = (CardScanCameraActivity.this.width - $l) / 2;
                int r = CardScanCameraActivity.this.width - l;
                double proportion = (double)CardScanCameraActivity.this.width / (double)CardScanCameraActivity.this.preWidth;
                l = (int)((double)l / proportion);
                int t1 = (int)((double)$t1 / proportion);
                r = (int)((double)r / proportion);
                b = (int)((double)b / proportion);
                String path = CardScanCameraActivity.this.savePicture(CardScanCameraActivity.this.bitmap);
                CardScanCameraActivity.this.tackData = null;
                camera.startPreview();
                parameters.setFlashMode("off");
                camera.setParameters(parameters);
                camera.stopPreview();
                Intent intent = new Intent();
                intent.putExtra("Success", 3);
                intent.putExtra("Path", path);
                intent.putExtra("l", l);
                intent.putExtra("t", t1);
                intent.putExtra("w", r - l);
                intent.putExtra("h", b - t1);
                CardScanCameraActivity.this.setResult(RESULT_OK, intent);
                CardScanCameraActivity.this.finish();
            }

        }
    };
    private int counter = 0;
    private int counterFail = 0;
    private int counterCut = 0;

    public CardScanCameraActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(0);
        this.requestWindowFeature(1);
        this.getWindow().setFlags(1024, 1024);
        this.getWindow().setFlags(128, 128);
        int activity_scan_camera = this.getResources().getIdentifier("activity_scan_camera", "layout", this.getPackageName());
        this.setContentView(activity_scan_camera);
        this.bankCardRecogUtils = new BankCardRecogUtils(this.getApplicationContext());
        Intent intent = this.getIntent();
        this.WTAPP = intent.getIntExtra("WTAPP", -1);
        this.placeActivity = intent.getStringExtra("Action");
        this.devCode = intent.getStringExtra("devCode");
        this.returnAciton = intent.getStringExtra("ReturnAciton");
        this.resultAciton = this.intiResultAction(intent.getStringExtra("ResultAciton"));
        this.copyright = intent.getStringExtra("CopyrightInfo");
        this.findView();
    }

    protected void onRestart() {
        if(this.tackPic != null && this.tackPic.getVisibility() != 8) {
            this.tackPic.setVisibility(8);
            this.eject_btn.setVisibility(0);
        }

        if(this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }

        this.counterFail = 0;
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
        this.api = new BankCardAPI();
        this.api.WTInitCardKernal("", 0);
    }

    private String intiResultAction(String action) {
        return action != null && action.equals("")?"com.wintone.smartvision_bankCard.ShowResult":(action == null?"com.wintone.smartvision_bankCard.ShowResult":action);
    }

    private void findView() {
        int surfaceViwe = this.getResources().getIdentifier("surfaceViwe", "id", this.getPackageName());
        this.surfaceView = (SurfaceView)this.findViewById(surfaceViwe);
        int re_c_ = this.getResources().getIdentifier("re_c", "id", this.getPackageName());
        this.re_c = (RelativeLayout)this.findViewById(re_c_);
        int help_word_ = this.getResources().getIdentifier("help_word", "id", this.getPackageName());
        this.help_word = (ImageView)this.findViewById(help_word_);
        int back_camera_ = this.getResources().getIdentifier("back_camera", "id", this.getPackageName());
        this.back = (ImageButton)this.findViewById(back_camera_);
        int flash_camera_ = this.getResources().getIdentifier("flash_camera", "id", this.getPackageName());
        this.flash = (ImageButton)this.findViewById(flash_camera_);
        int tackPic_btn_ = this.getResources().getIdentifier("tackPic_btn", "id", this.getPackageName());
        this.tackPic = (ImageButton)this.findViewById(tackPic_btn_);
        int eject_btn_ = this.getResources().getIdentifier("eject_btn", "id", this.getPackageName());
        this.eject_btn = (ImageButton)this.findViewById(eject_btn_);
        this.wintone_logo = this.bankCardRecogUtils.intiCopyrightLogo(this.devCode, this.copyright);
        DisplayMetrics metric = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metric);
        this.width = metric.widthPixels;
        this.height = metric.heightPixels;
        if(this.width * 3 == this.height * 4) {
            this.isFatty = true;
        }

        int back_w = (int)((double)this.width * 0.066796875D);
        int back_h = back_w * 1;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
        layoutParams.addRule(9, -1);
        layoutParams.addRule(12, -1);
        int Fheight = this.height;
        if(this.isFatty) {
            Fheight = (int)((double)this.height * 0.75D);
        }

        layoutParams.leftMargin = (int)((((double)this.width - (double)Fheight * 0.8D * 1.585D) / 2.0D - (double)back_h) / 2.0D);
        layoutParams.bottomMargin = (int)((double)this.height * 0.10486111111111111D);
        this.back.setLayoutParams(layoutParams);
        int flash_w = (int)((double)this.width * 0.066796875D);
        int flash_h = flash_w * 1;
        layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
        layoutParams.addRule(9, -1);
        layoutParams.addRule(10, -1);
        if(this.isFatty) {
            Fheight = (int)((double)this.height * 0.75D);
        }

        layoutParams.leftMargin = (int)((((double)this.width - (double)Fheight * 0.8D * 1.585D) / 2.0D - (double)back_h) / 2.0D);
        layoutParams.topMargin = (int)((double)this.height * 0.10486111111111111D);
        this.flash.setLayoutParams(layoutParams);
        int help_word_w = (int)((double)this.width * 0.474609375D);
        int help_word_h = (int)((double)help_word_w * 0.05185185185185185D);
        layoutParams = new RelativeLayout.LayoutParams(help_word_w, help_word_h);
        layoutParams.addRule(14, -1);
        layoutParams.addRule(15, -1);
        this.help_word.setLayoutParams(layoutParams);
        int wintone_logo_w = (int)((double)this.width * 0.33046875D);
        int wintone_logo_h = (int)((double)wintone_logo_w * 0.0673758865248227D);
        layoutParams = new RelativeLayout.LayoutParams(wintone_logo_w, wintone_logo_h);
        layoutParams.addRule(14, -1);
        layoutParams.addRule(12, -1);
        if(this.isFatty) {
            layoutParams.bottomMargin = this.height / 10 - help_word_h / 2;
        } else {
            layoutParams.bottomMargin = this.height / 20 - help_word_h / 2;
        }

        this.wintone_logo.setLayoutParams(layoutParams);
        this.re_c.addView(this.wintone_logo);
        int eject_btn_w = (int)((double)this.width * 0.024453125D);
        int eject_btn_h = (int)((double)eject_btn_w * 7.8108108108108105D);
        layoutParams = new RelativeLayout.LayoutParams(eject_btn_w, eject_btn_h);
        layoutParams.addRule(15, -1);
        layoutParams.addRule(11, -1);
        this.eject_btn.setLayoutParams(layoutParams);
        int tackPic_w = (int)((double)this.width * 0.105859375D);
        int tackPic_h = tackPic_w * 1;
        layoutParams = new RelativeLayout.LayoutParams(tackPic_w, tackPic_h);
        layoutParams.addRule(15, -1);
        layoutParams.addRule(11, -1);
        layoutParams.rightMargin = (int)((((double)this.width - (double)Fheight * 1.58577D * 0.8D) / 2.0D - (double)tackPic_w) / 2.0D);
        this.tackPic.setLayoutParams(layoutParams);
        this.tackPic.setVisibility(8);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(3);
        this.back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CardScanCameraActivity.this.finish();
            }
        });
        this.eject_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CardScanCameraActivity.this.tackPic.setVisibility(0);
                CardScanCameraActivity.this.eject_btn.setVisibility(8);
            }
        });
        this.flash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!CardScanCameraActivity.this.getPackageManager().hasSystemFeature("android.hardware.camera.flash")) {
                    Toast.makeText(CardScanCameraActivity.this, CardScanCameraActivity.this.getResources().getString(CardScanCameraActivity.this.getResources().getIdentifier("no_flash", "string", CardScanCameraActivity.this.getPackageName())), 1).show();
                } else if(CardScanCameraActivity.this.camera != null) {
                    Camera.Parameters parameters = CardScanCameraActivity.this.camera.getParameters();
                    String flashMode = parameters.getFlashMode();
                    if(flashMode.equals("torch")) {
                        parameters.setFlashMode("off");
                        parameters.setExposureCompensation(0);
                    } else {
                        parameters.setFlashMode("torch");
                        parameters.setExposureCompensation(-1);
                    }

                    try {
                        CardScanCameraActivity.this.camera.setParameters(parameters);
                    } catch (Exception var5) {
                        Toast.makeText(CardScanCameraActivity.this, CardScanCameraActivity.this.getResources().getString(CardScanCameraActivity.this.getResources().getIdentifier("no_flash", "string", CardScanCameraActivity.this.getPackageName())), 1).show();
                    }
                    CardScanCameraActivity.this.camera.startPreview();
                }

            }
        });
        this.tackPic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CardScanCameraActivity.this.api.WTUnInitCardKernal();
                if(CardScanCameraActivity.this.timer != null) {
                    CardScanCameraActivity.this.timer.cancel();
                    CardScanCameraActivity.this.timer = null;
                }

                if(CardScanCameraActivity.this.camera != null) {
                    try {
                        CardScanCameraActivity.this.isFocusTakePicture(CardScanCameraActivity.this.camera);
                    } catch (Exception var3) {
                        var3.printStackTrace();
                    }
                }

            }
        });
        //默认显示拍照按钮
        CardScanCameraActivity.this.tackPic.setVisibility(0);
        CardScanCameraActivity.this.eject_btn.setVisibility(8);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if(this.camera == null) {
            this.camera = Camera.open();
        }

        try {
            this.camera.setPreviewDisplay(holder);
            this.initCamera();
            Timer e = new Timer();
            if(this.timer == null) {
                this.timer = new TimerTask() {
                    public void run() {
                        if(CardScanCameraActivity.this.camera != null) {
                            try {
                                CardScanCameraActivity.this.camera.autoFocus(new Camera.AutoFocusCallback() {
                                    public void onAutoFocus(boolean success, Camera camera) {
                                    }
                                });
                            } catch (Exception var2) {
                                var2.printStackTrace();
                            }
                        }

                    }
                };
            }

            e.schedule(this.timer, 500L, 2500L);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(this.camera != null) {
            this.camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success) {
                        synchronized(camera) {
                            (new Thread() {
                                public void run() {
                                    CardScanCameraActivity.this.initCamera();
                                    super.run();
                                }
                            }).start();
                        }
                    }

                }
            });
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if(this.camera != null) {
                if(this.timer != null) {
                    this.timer.cancel();
                    this.timer = null;
                }
                this.camera.setPreviewCallback((Camera.PreviewCallback)null);
                this.camera.stopPreview();
                this.camera.release();
                this.camera = null;
            }
        } catch (Exception var3) {
            ;
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 4) {
            try {
                if(this.camera != null) {
                    this.camera.setPreviewCallback((Camera.PreviewCallback)null);
                    this.camera.stopPreview();
                    this.camera.release();
                    this.camera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        CardScanCameraActivity.this.finish();
    }

    private void initCamera() {
        Camera.Parameters parameters = this.camera.getParameters();
        List list = parameters.getSupportedPreviewSizes();
        int length = list.size();
        int previewWidth = 640;
        int previewheight = 480;
        boolean second_previewWidth = false;
        boolean second_previewheight = false;
        Camera.Size size;
        int $t;
        if(length == 1) {
            size = (Camera.Size)list.get(0);
            previewWidth = size.width;
            previewheight = size.height;
        } else {
            for($t = 0; $t < length; ++$t) {
                size = (Camera.Size)list.get($t);
                int var19;
                int var18;
                if(this.isFatty) {
                    if(size.height <= 960 || size.width <= 1280) {
                        var18 = size.width;
                        var19 = size.height;
                        if(previewWidth < var18 && var18 * 3 == var19 * 4) {
                            previewWidth = var18;
                            previewheight = var19;
                        }
                    }
                } else if(this.width * 9 == this.height * 16) {
                    if((size.height <= 960 || size.width <= 1280) && size.width * 9 == size.height * 16) {
                        var18 = size.width;
                        var19 = size.height;
                        if(previewWidth <= var18) {
                            previewWidth = var18;
                            previewheight = var19;
                        }
                    }
                } else if(size.height <= 960 || size.width <= 1280) {
                    var18 = size.width;
                    var19 = size.height;
                    if(previewWidth <= var18) {
                        previewWidth = var18;
                        previewheight = var19;
                    }
                }
            }
        }

        this.preWidth = previewWidth;
        this.preHeight = previewheight;
        if(!this.isROI) {
            $t = this.height / 10;
            int t = $t;
            int b = this.height - $t;
            int $l = (int)((double)(b - $t) * 1.58577D);
            int l = (this.width - $l) / 2;
            int r = this.width - l;
            if(this.isFatty) {
                $t = this.height / 5;
                t = $t;
                b = this.height - $t;
                $l = (int)((double)(b - $t) * 1.58577D);
                l = (this.width - $l) / 2;
                r = this.width - l;
            }

            double proportion = (double)this.width / (double)this.preWidth;
            l = (int)((double)l / proportion);
            t = (int)((double)t / proportion);
            r = (int)((double)r / proportion);
            b = (int)((double)b / proportion);
            int[] borders = new int[]{l, t, r, b};
            this.api.WTSetROI(borders, this.preWidth, this.preHeight);
            this.isROI = true;
            if(this.isFatty) {
                this.myView = new ViewfinderView(this, this.width, this.height, this.isFatty);
            } else {
                this.myView = new ViewfinderView(this, this.width, this.height);
            }

            this.re_c.addView(this.myView);
        }

        parameters.setPictureFormat(256);
        parameters.setPreviewSize(this.preWidth, this.preHeight);
        if(this.getPackageManager().hasSystemFeature("android.hardware.camera.autofocus")) {
            parameters.setFocusMode("auto");
        }

        this.camera.setPreviewCallback(this);
        this.camera.setParameters(parameters);
        this.camera.startPreview();
    }

    public String savePicture(Bitmap bitmap) {
        String strCaptureFilePath = PATH + "bankcard_" + this.pictureName() + ".jpg";
        File dir = new File(PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(strCaptureFilePath);
        if(file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
            BufferedOutputStream e = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, e);
            e.flush();
            e.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return strCaptureFilePath;
    }

    public String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow();
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour;
        int minute = t.minute;
        int second = t.second;
        if(month < 10) {
            str = String.valueOf(year) + "0" + month;
        } else {
            str = String.valueOf(year) + String.valueOf(month);
        }

        if(date < 10) {
            str = str + "0" + date + "_";
        } else {
            str = str + date + "_";
        }

        if(hour < 10) {
            str = str + "0" + hour;
        } else {
            str = str + String.valueOf(hour);
        }

        if(minute < 10) {
            str = str + "0" + minute;
        } else {
            str = str + String.valueOf(minute);
        }

        if(second < 10) {
            str = str + "0" + second;
        } else {
            str = str + String.valueOf(second);
        }

        return str;
    }

    /**
     * 读取摄像头数据
     * @param data 读取的数据
     * @param camera 摄像头
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.tackData = data;
        Camera.Parameters parameters = camera.getParameters();
        int[] isBorders = new int[4];
        ++this.counter;
        if(this.counter == 2) {
            this.counter = 0;
            int[] bRotated = new int[1];
            int[] pLineWarp = new int[32000];
            //屏蔽掉解析摄像头扫描到的数据
            String[] results = this.bankCardRecogUtils.getRecogResult(data, parameters.getPreviewSize().width, parameters.getPreviewSize().height, isBorders, bRotated, pLineWarp, this.bankCardRecogUtils.intiDevCode(this.devCode));
//            int r = Integer.valueOf(results[0]).intValue();
            if(isBorders[0] == 1) {
                if(this.myView != null) {
                    this.myView.setLeftLine(1);
                }
            } else if(this.myView != null) {
                this.myView.setLeftLine(0);
            }

            if(isBorders[1] == 1) {
                if(this.myView != null) {
                    this.myView.setTopLine(1);
                }
            } else if(this.myView != null) {
                this.myView.setTopLine(0);
            }

            if(isBorders[2] == 1) {
                if(this.myView != null) {
                    this.myView.setRightLine(1);
                }
            } else if(this.myView != null) {
                this.myView.setRightLine(0);
            }

            if(isBorders[3] == 1) {
                if(this.myView != null) {
                    this.myView.setBottomLine(1);
                }
            } else if(this.myView != null) {
                this.myView.setBottomLine(0);
            }

//            if(isBorders[0] == 1 && isBorders[1] == 1 && isBorders[2] == 1 && isBorders[3] == 1) {
//                if((results[2] == null || results[2].equals("")) && results[1] != null && !results[1].equals("")) {
//                    if(r == 0) {
//                        this.api.WTUnInitCardKernal();
//                        this.mVibrator = (Vibrator)this.getApplication().getSystemService("vibrator");
//                        this.mVibrator.vibrate(100L);
//                        camera.stopPreview();
//                        camera.setPreviewCallback((Camera.PreviewCallback)null);
//                        camera = null;
//                        Intent intent = new Intent(this.resultAciton);
//                        intent.putExtra("PicR", pLineWarp);
//                        intent.putExtra("StringR", results[1].toCharArray());
//                        intent.putExtra("StringS", results[1]);
//                        intent.putExtra("Success", 2);
//                        this.startActivity(intent);
//                        this.finish();
//                    }
//                } else if(results[2] != null && !results[2].equals("")) {
//                    Toast.makeText(this.getBaseContext(), this.getResources().getString(this.getResources().getIdentifier("toast_code", "string", this.getPackageName())) + results[2], 200).show();
//                }
//            } else {
//                ++this.counterCut;
//                if(this.counterCut == 5) {
//                    this.counterFail = 0;
//                    this.counterCut = 0;
//                }
//            }
        }

    }

    protected void onStop() {
        super.onStop();
        if(this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        if(this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }

        try {
            if(this.camera != null) {
                this.camera.setPreviewCallback((Camera.PreviewCallback)null);
                this.camera.stopPreview();
                this.camera.release();
                this.camera = null;
            }
        } catch (Exception var2) {
            ;
        }

    }

    private void isFocusTakePicture(Camera camera) {
        final Camera.Parameters parameters = camera.getParameters();
        final boolean flashEnable = this.getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        if(flashEnable) {
            parameters.setFlashMode("on");
        }

        if(!this.getPackageManager().hasSystemFeature("android.hardware.camera.autofocus")) {
            camera.stopPreview();
            camera.startPreview();
            camera.takePicture(this.shutterCallback, (Camera.PictureCallback)null, this.picturecallback);
        } else {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success) {
                        camera.stopPreview();
                        camera.startPreview();
                        camera.takePicture(CardScanCameraActivity.this.shutterCallback, (Camera.PictureCallback)null, CardScanCameraActivity.this.picturecallback);
                        if(flashEnable) {
                            try {
                                camera.setParameters(parameters);
                            } catch (Exception var4) {
                                var4.printStackTrace();
                            }
                        }
                    }

                }
            });
        }
    }

}
