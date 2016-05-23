package com.example.shoufuyi.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cchtw.videorecorderlib.utils.FileUtil;
import com.cchtw.videorecorderlib.wxlikevideo.camera.CameraHelper;
import com.cchtw.videorecorderlib.wxlikevideo.recorder.WXLikeVideoRecorder;
import com.cchtw.videorecorderlib.wxlikevideo.views.CameraPreviewView;
import com.example.shoufuyi.R;

import java.lang.ref.WeakReference;


public class NewRecordVideoActivity extends BaseActivity implements View.OnTouchListener {

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
    private boolean isCancelRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        int cameraId = CameraHelper.getFrontCameraID();
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

        setContentView(R.layout.activity_capture_video);
        CameraPreviewView preview = (CameraPreviewView) findViewById(R.id.camera_preview);
        preview.setCamera(mCamera, cameraId);

        mRecorder.setCameraPreviewView(preview);

        findViewById(R.id.capture_video_record).setOnTouchListener(this);

      //  ((TextView) findViewById(R.id.filePathTextView)).setText("请在" + FileUtil.MEDIA_FILE_DIR + "查看录制的视频文件");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRecorder != null) {
            boolean recording = mRecorder.isRecording();
            // 页面不可见就要停止录制
            mRecorder.stopRecording();
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
            if (!mRecorder.startRecording())
                Toast.makeText(this, "录制失败…", Toast.LENGTH_SHORT).show();
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
        mRecorder.stopRecording();
        String videoPath = mRecorder.getFilePath();
        // 没有录制视频
        if (null == videoPath) {
            return;
        }
        // 若取消录制，则删除文件，否则通知宿主页面发送视频
        if (isCancelRecord) {
            FileUtil.deleteFile(videoPath);
        } else {
            startActivity(new Intent(this, NewRecordVideoActivity.class).putExtra(PlayVideoActiviy.KEY_FILE_PATH, videoPath));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
             v.setPressed(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isCancelRecord = false;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    startRecord();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mRecorder.isRecording())
                        return false;

                    float y = event.getY();
                    if (y - mDownY < CANCEL_RECORD_OFFSET) {
                        if (!isCancelRecord) {
                            // cancel record
                            isCancelRecord = true;
                            Toast.makeText(this, "cancel record", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isCancelRecord = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stopRecord();
                    break;
            }

        return true;
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
