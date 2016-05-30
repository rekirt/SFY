package com.example.shoufuyi.uitls.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public final class ViewfinderView extends View {
    private int checkLeftFrame = 0;// 检测证件左边是否存在或者对齐
    private int checkTopFrame = 0;// 检测证件上边是否存在或者对齐
    private int checkRightFrame = 0;// 检测证件右边是否存在或者对齐
    private int checkBottomFrame = 0;// 检测证件下边是否存在或者对齐
    private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
            128, 64 };
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 50L;

    public int getCheckLeftFrame() {
        return checkLeftFrame;
    }

    public void setCheckLeftFrame(int checkLeftFrame) {
        this.checkLeftFrame = checkLeftFrame;
    }

    public int getCheckTopFrame() {
        return checkTopFrame;
    }

    public void setCheckTopFrame(int checkTopFrame) {
        this.checkTopFrame = checkTopFrame;
    }

    public int getCheckRightFrame() {
        return checkRightFrame;
    }

    public void setCheckRightFrame(int checkRightFrame) {
        this.checkRightFrame = checkRightFrame;
    }

    public int getCheckBottomFrame() {
        return checkBottomFrame;
    }

    public void setCheckBottomFrame(int checkBottomFrame) {
        this.checkBottomFrame = checkBottomFrame;
    }

    private static final int OPAQUE = 0xFF;
    /**
     * 判断屏幕的旋转的度数对应的方向值如：0,1,2,3
     */
    private static int directtion = 0;
    /**
     * 当idcardType==0时，就用MRZ识别，当idcardType==1时，就用全幅面识别
     */
    private static int idcardType = 0;

    public static int getIdcardType() {
        return idcardType;
    }

    public static void setIdcardType(int idcardType) {
        ViewfinderView.idcardType = idcardType;
    }

    public int getDirecttion() {
        return directtion;
    }

    public void setDirecttion(int directtion) {
        this.directtion = directtion;
    }

    private final Paint paint;
    private Bitmap resultBitmap;
    // private final int maskColor;
    // private final int resultColor;
    // private final int frameColor;
    // private final int laserColor;
    private int scannerAlpha;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;
    private int slideTop1;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 10;
    /**
     * 扫描框中的线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 6;
    private boolean isFirst = false;
    /**
     * 四周边框的宽度
     */
    private static final int FRAME_LINE_WIDTH = 4;
    private Rect frame;
    private int width;
    private int height;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        paint = new Paint();
        scannerAlpha = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();
        // 一般的设备都是当宽度小于高度的时候，手机旋转的角度为0或2；但三星的设备当宽度大于高度的时候，手机旋转的角度为0或2
        if (directtion == 0 || directtion == 2) {
            if (width > height) {
                width = 3 * width / 4;

            }
            if (idcardType == 3000) {
                // MRZ识别
                /**
                 * 这个矩形就是中间显示的那个框框
                 */
                frame = new Rect((int) (width * 0.15), height / 3,
                        (int) (width * 0.8), (2 * height) / 3);
            } else if (idcardType == 2 || idcardType == 22
                    || idcardType == 1030 || idcardType == 1031
                    || idcardType == 1032 || idcardType == 1005
                    || idcardType == 1001 || idcardType == 2001
                    || idcardType == 2004 || idcardType == 2002
                    || idcardType == 2003 || idcardType == 14
                    || idcardType == 15) {
               
                frame = new Rect((int) (width * 0.2),
                        (int) (height - 0.41004673 * width) / 2,
                        (int) (width * 0.85),
                        (int) (height + 0.41004673 * width) / 2);
            }
            // else if (idcardType == 12 || idcardType == 2 || idcardType == 13
            // || idcardType == 9 || idcardType == 11 || idcardType == 22
            // || idcardType == 14 || idcardType == 15 || idcardType == 5
            // || idcardType == 10)
            else {
                // for page
                frame = new Rect((int) (width * 0.15),
                        (int) (height - 0.48 * width) / 2, (int) (width * 0.8),
                        (int) (height + 0.48 * width) / 2);
            }

            if (frame == null) {
                return;
            }
            // 初始化中间线滑动的最上边和最下边
            if (!isFirst) {
                isFirst = true;
                slideTop = height / 3;
                slideBottom = 2 * height / 3;
                slideTop1 = width / 2;
            }
            // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
            // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
            paint.setColor(Color.argb(48, 0, 0, 0));
            canvas.drawRect(0, 0, width, frame.top, paint);
            canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
            canvas.drawRect(frame.right + 1, frame.top, width,
                    frame.bottom + 1, paint);
            canvas.drawRect(0, frame.bottom + 1, width, height, paint);

            // 绘制两个像素边宽的绿色线框
            paint.setColor(Color.rgb(243, 153, 18));
            if (idcardType == 3000) {
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.top
                                + FRAME_LINE_WIDTH, paint);// 上边
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH + 2, frame.bottom
                                + FRAME_LINE_WIDTH, paint);// 左边
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.bottom
                                + FRAME_LINE_WIDTH, paint);// 右边
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                        frame.bottom, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.bottom + FRAME_LINE_WIDTH, paint);// 底边
                // 绘制一条不断扫描的红线
                // paint.setColor(laserColor);
                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            }

            {
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH - 2 + 50, frame.top
                                + FRAME_LINE_WIDTH, paint);
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH + 2, frame.top + 50,
                        paint);// 左上角
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.top + 50,
                        paint);
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                        frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.top + FRAME_LINE_WIDTH, paint);// 右上角
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                        frame.bottom - 50, frame.left + FRAME_LINE_WIDTH + 2,
                        frame.bottom, paint);
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.bottom
                        - FRAME_LINE_WIDTH, frame.left + FRAME_LINE_WIDTH - 2
                        + 50, frame.bottom, paint); // 左下角
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                        frame.bottom - 50, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.bottom, paint);
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                        frame.bottom - FRAME_LINE_WIDTH, frame.right
                                - FRAME_LINE_WIDTH - 2, frame.bottom, paint); // 右下角
                // 如果检测到证件左边就会画出左边的提示线
                if (checkLeftFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.left + FRAME_LINE_WIDTH + 2,
                            frame.bottom, paint);// 左边
                // 如果检测到证件上边就会画出左边的提示线
                if (checkTopFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.top + FRAME_LINE_WIDTH, paint);// 上边
                // 如果检测到证件右边就会画出左边的提示线
                if (checkRightFrame == 1)
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.bottom, paint);// 右边
                // 如果检测到证件底边就会画出左边的提示线
                if (checkRightFrame == 1)
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.bottom - FRAME_LINE_WIDTH, frame.right
                                    - FRAME_LINE_WIDTH - 2, frame.bottom, paint); // 右下角
            }

            // 一般的设备都是当宽度大于高度的时候，手机旋转的角度为1或3；但三星的设备当宽度小于高度的时候，手机旋转的角度为1或3
        } else if (directtion == 1 || directtion == 3) {
            if (width < height) {
                width = 4 * width / 3;
                height = 3 * height / 4;

                /**
                 * 这个矩形就是中间显示的那个框框
                 */
                frame = new Rect(0, height / 2, 3 * width / 4, 2 * height / 3);

                if (frame == null) {
                    return;
                }
                // 初始化中间线滑动的最上边和最下边
                if (!isFirst) {
                    isFirst = true;
                    slideTop = width / 3;
                    slideBottom = 2 * width / 3;
                    slideTop1 = height / 2;
                }
                // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
                // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
                paint.setColor(Color.argb(48, 0, 0, 0));
                canvas.drawRect(0, 0, width, frame.top, paint);
                canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1,
                        paint);
                canvas.drawRect(frame.right + 1, frame.top, width,
                        frame.bottom + 1, paint);
                canvas.drawRect(0, frame.bottom + 1, width, height, paint);

                // 绘制两个像素边宽的绿色线框
                paint.setColor(Color.rgb(243, 153, 18));
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.top
                                + FRAME_LINE_WIDTH, paint);// 上边
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                        frame.left + FRAME_LINE_WIDTH + 2, frame.bottom
                                + FRAME_LINE_WIDTH, paint);// 左边
                canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                        frame.right - FRAME_LINE_WIDTH + 2, frame.bottom
                                + FRAME_LINE_WIDTH, paint);// 右边
                canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                        frame.bottom, frame.right - FRAME_LINE_WIDTH + 2,
                        frame.bottom + FRAME_LINE_WIDTH, paint);// 底边
                // 绘制一条不断扫描的红线
                // paint.setColor(laserColor);
                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

            } else {
                if (idcardType == 3000) {
                    // MRZ识别
                    /**
                     * 这个矩形就是中间显示的那个框框
                     */
                    frame = new Rect((int) (width * 0.2), height / 3,
                            (int) (width * 0.85), 2 * height / 3);
                } else if (idcardType == 2 || idcardType == 22
                        || idcardType == 1030 || idcardType == 1031
                        || idcardType == 1032 || idcardType == 1005
                        || idcardType == 1001 || idcardType == 2001
                        || idcardType == 2004 || idcardType == 2002
                        || idcardType == 2003 || idcardType == 14
                        || idcardType == 15) {
                    frame = new Rect((int) (width * 0.2),
                            (int) (height - 0.41004673 * width) / 2,
                            (int) (width * 0.85),
                            (int) (height + 0.41004673 * width) / 2);
                    
                }else if (idcardType == 5
                        || idcardType == 6) {
                    frame = new Rect((int) (width * 0.24),
                            (int) (height - 0.41004673 * width) / 2,
                            (int) (width * 0.81),
                            (int) (height + 0.41004673 * width) / 2);
                } else
                // if (idcardType == 12 || idcardType == 2
                // || idcardType == 13 || idcardType == 9
                // || idcardType == 11 || idcardType == 22
                // || idcardType == 14 || idcardType == 15
                // || idcardType == 5 || idcardType == 10)
                {
                    // for page
                    frame = new Rect((int) (width * 0.2),
                            (int) (height - 0.45 * width) / 2,
                            (int) (width * 0.85),
                            (int) (height + 0.45 * width) / 2);
                }
                if (frame == null) {
                    return;
                }
                // 初始化中间线滑动的最上边和最下边
                if (!isFirst) {
                    isFirst = true;
                    slideTop = width / 3;
                    slideBottom = 2 * width / 3;
                    slideTop1 = height / 3;
                }

                // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
                // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
                paint.setColor(Color.argb(48, 0, 0, 0));
                canvas.drawRect(0, 0, width, frame.top, paint);
                canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1,
                        paint);
                canvas.drawRect(frame.right + 1, frame.top, width,
                        frame.bottom + 1, paint);
                canvas.drawRect(0, frame.bottom + 1, width, height, paint);

                // 绘制两个像素边宽的绿色线框
                paint.setColor(Color.rgb(243, 153, 18));
                if (idcardType == 3000) {
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.top + FRAME_LINE_WIDTH, paint);// 上边
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.left + FRAME_LINE_WIDTH + 2,
                            frame.bottom + FRAME_LINE_WIDTH, paint);// 左边
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.bottom + FRAME_LINE_WIDTH, paint);// 右边
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.bottom, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.bottom + FRAME_LINE_WIDTH, paint);// 底边
                } else
                // if (idcardType == 12 || idcardType == 2
                // || idcardType == 13 || idcardType == 9
                // || idcardType == 11 || idcardType == 22
                // || idcardType == 14 || idcardType == 15
                // || idcardType == 5 || idcardType == 10)
                {

                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.left + FRAME_LINE_WIDTH - 2 + 50,
                            frame.top + FRAME_LINE_WIDTH, paint);
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.top, frame.left + FRAME_LINE_WIDTH + 2,
                            frame.top + 50, paint);// 左上角
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.top + 50, paint);
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                            frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                            frame.top + FRAME_LINE_WIDTH, paint);// 右上角
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.bottom - 50, frame.left + FRAME_LINE_WIDTH
                                    + 2, frame.bottom, paint);
                    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                            frame.bottom - FRAME_LINE_WIDTH, frame.left
                                    + FRAME_LINE_WIDTH - 2 + 50, frame.bottom,
                            paint); // 左下角
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                            frame.bottom - 50, frame.right - FRAME_LINE_WIDTH
                                    + 2, frame.bottom, paint);
                    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50,
                            frame.bottom - FRAME_LINE_WIDTH, frame.right
                                    - FRAME_LINE_WIDTH - 2, frame.bottom, paint); // 右下角
                    // 如果检测到证件左边就会画出左边的提示线
                    if (checkLeftFrame == 1)
                        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                                frame.top, frame.left + FRAME_LINE_WIDTH + 2,
                                frame.bottom, paint);// 左边
                    // 如果检测到证件上边就会画出左边的提示线
                    if (checkTopFrame == 1)
                        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                                frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                                frame.top + FRAME_LINE_WIDTH, paint);// 上边
                    // 如果检测到证件右边就会画出左边的提示线
                    if (checkRightFrame == 1)
                        canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2,
                                frame.top, frame.right - FRAME_LINE_WIDTH + 2,
                                frame.bottom, paint);// 右边
                    // 如果检测到证件底边就会画出左边的提示线
                    if (checkBottomFrame == 1)
                        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2,
                                frame.bottom - FRAME_LINE_WIDTH, frame.right
                                        - FRAME_LINE_WIDTH - 2, frame.bottom,
                                paint); // 右下角
                }

            }
        }

        /**
         * 当我们获得结果的时候，我们更新整个屏幕的内容
         */

        postInvalidateDelayed(ANIMATION_DELAY, 0, 0, width, height);

    }
}
