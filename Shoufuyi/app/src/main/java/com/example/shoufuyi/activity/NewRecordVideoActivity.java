package com.example.shoufuyi.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cchtw.videorecorderlib.utils.FileUtil;
import com.cchtw.videorecorderlib.wxlikevideo.camera.CameraHelper;
import com.cchtw.videorecorderlib.wxlikevideo.recorder.WXLikeVideoRecorder;
import com.cchtw.videorecorderlib.wxlikevideo.views.CameraPreviewView;
import com.cchtw.videorecorderlib.wxlikevideo.views.RecordProgressBar;
import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.ToastHelper;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 新视频录制页面
 *
 * @author Martin
 */
public class NewRecordVideoActivity extends BaseActivity implements View.OnTouchListener {

    private static final String TAG = "NewRecordVideoActivity";

    // 输出宽度
    private static final int OUTPUT_WIDTH = 320;
    // 输出高度
    private static final int OUTPUT_HEIGHT = 240;
    // 宽高比
    private static final float RATIO = 1f * OUTPUT_WIDTH / OUTPUT_HEIGHT;

    private Camera mCamera;

    private WXLikeVideoRecorder mRecorder;

    private static final int CANCEL_RECORD_OFFSET = -100;
    private float mDownX, mDownY;
    private boolean isCancelRecord = true;
    private boolean isTimeOutOfRecord = false;
    private boolean isRecordTimeNotLong = false;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 42;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recorder);

        // check Android 6 permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    initCamera();
                } else {
                    setResult(Activity.RESULT_CANCELED);
                    ToastHelper.ShowToast("请同意访问你的手机相机以及录音功能");
                    finish();
                }
            }
        }
    }

    private RecordProgressBar recordProgressBar;
    CameraPreviewView preview;
    private TextView mTvCancelTip;
    private void initCamera() {
        int cameraId = CameraHelper.getDefaultCameraID();
        // Create an instance of Camera
        mCamera = CameraHelper.getCameraInstance(cameraId);
        if (null == mCamera) {
            Toast.makeText(this, "打开相机失败！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 初始化录像机
        mRecorder = new WXLikeVideoRecorder(this, FileUtil.MEDIA_FILE_DIR);
        mRecorder.setOutputSize(OUTPUT_WIDTH, OUTPUT_HEIGHT);

        preview = (CameraPreviewView) findViewById(R.id.camera_preview);
        preview.setCamera(mCamera, cameraId);
        mRecorder.setCameraPreviewView(preview);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvCancelTip = (TextView) findViewById(R.id.tv_cancel_tip);

        findViewById(R.id.button_start).setOnTouchListener(this);
        findViewById(R.id.capture_top_back).setOnClickListener(this);
        recordProgressBar = (RecordProgressBar) findViewById(R.id.record_progressbar);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.capture_top_back){
            NewRecordVideoActivity.this.finish();
        }
    }

    private int mTimeCount = 0;// 时间计数
    private Timer mTimer;// 计时器
    private TextView mTvTime;
    private TimerTask mTimerTask;

    Handler handlerTime = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    mTvTime.setText(msg.what+"秒");
                    break;
                case -1:
                    mTvCancelTip.setVisibility(View.INVISIBLE);
                    recordProgressBar.stop();
                    if (mTimer != null){
                        if (mTimerTask != null){
                            mTimerTask.cancel();  //将原任务从队列中移除
                            mTimer.cancel();
                        }
                    }
                    isCancelRecord = false;
                    isRecordTimeNotLong = false;
                    isTimeOutOfRecord = true;
                    mTvTime.setText("6" + "秒");
                    stopRecord();
                    mTimeCount = 0;
                    break;
                case -2:
                    mTvCancelTip.setVisibility(View.INVISIBLE);
                    recordProgressBar.stop();
                    if (mTimer != null){
                        if (mTimerTask != null){
                            mTimerTask.cancel();  //将原任务从队列中移除
                            mTimer.cancel();
                        }
                    }
                    mTimeCount = 0;
                    mTvTime.setText("0"+"秒");
                    stopRecord();
                    break;
                default:
                    mTvTime.setText(msg.what+"秒");
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mRecorder != null) {
            // 页面不可见就要停止录制
            mRecorder.stopRecording();
            boolean recording = mRecorder.isRecording();
            // 录制时退出，直接舍弃视频
            if (recording) {
                FileUtil.deleteFile(mRecorder.getFilePath());
            }
        }
        releaseCamera();              // release the camera immediately on pause event
//        finish();
    }

    private void releaseCamera() {
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            // 释放前先停止预览
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mRecorder.isRecording()) {
            Toast.makeText(this, "正在录制中…", Toast.LENGTH_SHORT).show();
            return;
        }

        // initialize video camera
        if (prepareVideoRecorder()) {
            // 录制视频
            if (!mRecorder.startRecording()){
                Toast.makeText(this, "录制失败…", Toast.LENGTH_SHORT).show();
            }else {
                StartCount();
                recordProgressBar.start();
            }
        }
    }

    /**
     * 准备视频录制器
     * @return
     */
    private boolean prepareVideoRecorder(){
        if (!FileUtil.isSDCardMounted()) {
            Toast.makeText(this, "SD卡不可用！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        mTvTime.setText("0"+"秒");
        mRecorder.stopRecording();
        String videoPath = mRecorder.getFilePath();
        // 没有录制视频
        if (null == videoPath) {
            return;
        }
        // 若取消录制，则删除文件，否则通知宿主页面发送视频
        if (isCancelRecord || isRecordTimeNotLong) {
            FileUtil.deleteFile(videoPath);
        } else {
            // 实例化 Bundle，设置需要传递的参数
            Bundle bundle = new Bundle();
            bundle.putString(PlayVideoActiviy.KEY_FILE_PATH, videoPath);
            NewRecordVideoActivity.this.setResult(RESULT_OK, NewRecordVideoActivity.this.getIntent().putExtras(bundle));
            NewRecordVideoActivity.this.finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        // 实例化 Bundle，设置需要传递的参数 
//            Bundle bundle = new Bundle();
//            bundle.putString("phoneNO", "020-123");
            setResult(RESULT_CANCELED, this.getIntent());
            this.finish();
            return true;
            }else {
                return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isCancel;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTvCancelTip.setVisibility(View.VISIBLE);
                mTvCancelTip.setText("向上取消");
                mTvCancelTip.setTextColor(getResources().getColor(R.color.light_blue));
                mDownX = event.getX();
                mDownY = event.getY();
                startRecord();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mRecorder.isRecording())
                    return false;
                float y = event.getY();
                if (y - mDownY < CANCEL_RECORD_OFFSET) {
                    //recordProgressBar.cancel();
                    mTvCancelTip.setText("松开取消");
                    mTvCancelTip.setTextColor(getResources().getColor(R.color.red));
                    isCancelRecord = true;
                } else {
                    mTvCancelTip.setText("向上取消");
                    mTvCancelTip.setTextColor(getResources().getColor(R.color.light_blue));
                    isCancelRecord = false;
                    //recordProgressBar.resumeRunning();
                }
//                if (mTimeCount>6){
//                    isCancelRecord = false;
//                    isRecordTimeNotLong = false;
//                    mTvCancelTip.setVisibility(View.INVISIBLE);
//                    recordProgressBar.stop();
//                    if (mTimer != null){
//                        if (mTimerTask != null){
//                            mTimerTask.cancel();  //将原任务从队列中移除
//                            mTimer.cancel();
//                        }
//                    }
//                    Message messageTimeOut = new Message();
//                    messageTimeOut.what = -1;
//                    handlerTime.sendMessage(messageTimeOut);
//                    mTimeCount = 0;
//                    stopRecord();
//                }
                break;
            case MotionEvent.ACTION_UP:
                mTvCancelTip.setVisibility(View.INVISIBLE);
                recordProgressBar.stop();
                if (mTimer != null){
                    if (mTimerTask != null){
                        mTimerTask.cancel();  //将原任务从队列中移除
                        mTimer.cancel();
                    }
                }
                if (!isTimeOutOfRecord){
                    if (mTimeCount<4){
                        isRecordTimeNotLong = true;
                    }else {
                        isRecordTimeNotLong = false;
                    }
                    Message message = new Message();
                    message.what = -2;
                    handlerTime.sendMessage(message);
                }
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
//                if (mTimeCount>6){
//                    mTvCancelTip.setVisibility(View.INVISIBLE);
//                    recordProgressBar.stop();
//                    if (mTimer != null){
//                        if (mTimerTask != null){
//                            mTimerTask.cancel();  //将原任务从队列中移除
//                            mTimer.cancel();
//                        }
//                    }
//                    isCancelRecord = false;
//                    isRecordTimeNotLong = false;
//                    Message messageTimeOut = new Message();
//                    messageTimeOut.what = -1;
//                    handlerTime.sendMessage(messageTimeOut);
//                    mTimeCount = 0;
//                    stopRecord();
//                }
                break;
        }
        return true;
    }

    private void StartCount(){
        mTimeCount = 0;// 时间计数器重新赋值
        mTimer = new Timer();
        mTimerTask =new TimerTask() {

            @Override
            public void run() {
                ++mTimeCount;
                if (mTimeCount>6){
                    Message messageTimeOut = new Message();
                    messageTimeOut.what = -1;
                    handlerTime.sendMessage(messageTimeOut);
                }else {
                    Message message = new Message();
                    message.what = mTimeCount;
                    handlerTime.sendMessage(message);
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    /**
     * 开始录制失败回调任务
     *
     * @author Martin
     */
    public static class StartRecordFailCallbackRunnable implements Runnable {

        private WeakReference<NewRecordVideoActivity> mNewRecordVideoActivityWeakReference;

        public StartRecordFailCallbackRunnable(NewRecordVideoActivity activity) {
            mNewRecordVideoActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            NewRecordVideoActivity activity;
            if (null == (activity = mNewRecordVideoActivityWeakReference.get()))
                return;

            String filePath = activity.mRecorder.getFilePath();
            if (!TextUtils.isEmpty(filePath)) {
                FileUtil.deleteFile(filePath);
                Toast.makeText(activity, "Start record failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 停止录制回调任务
     *
     * @author Martin
     */
    public static class StopRecordCallbackRunnable implements Runnable {

        private WeakReference<NewRecordVideoActivity> mNewRecordVideoActivityWeakReference;

        public StopRecordCallbackRunnable(NewRecordVideoActivity activity) {
            mNewRecordVideoActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            NewRecordVideoActivity activity;
            if (null == (activity = mNewRecordVideoActivityWeakReference.get()))
                return;

            String filePath = activity.mRecorder.getFilePath();
            if (!TextUtils.isEmpty(filePath)) {
                if (activity.isCancelRecord) {
                    FileUtil.deleteFile(filePath);
                } else {
                    Toast.makeText(activity, "Video file path: " + filePath, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
