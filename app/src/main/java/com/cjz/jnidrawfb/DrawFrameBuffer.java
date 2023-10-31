package com.cjz.jnidrawfb;

import static com.dazzle.whiteboard.Config.SCREEN_HEIGHT;
import static com.dazzle.whiteboard.Config.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.util.Log;



public class DrawFrameBuffer {
    private static final String TAG = "accAPI";

    static boolean isFrameLibEndWith4K() {
        return true;
    }

    static {
        //V3112板子
        System.loadLibrary("JNIdrawFbLib_4k");
    }

    private static boolean isClose = true;
    private static int canvasDataTop[] = null;

    private static native void init();

    private static native void close();

    public static native void uiLock(int sw);

    public static void closeFb() {
        close();
        isClose = true;
    }

    public static void initFb() {
        if (isClose) {
            init();
            isClose = false;
        }
    }

    /**
     * 粘贴像素到FrameBuffer
     *
     * @param x      绘制的x坐标范围
     * @param y      绘制的y坐标范围
     * @param width  被绘制图像的宽度
     * @param height 被绘制图像的高度
     * @param pixels 被绘制图像的像素数据
     **/
    public static native void drawPixelRect(int x, int y, int width, int height, int[] pixels);

    public static void setRefreshUI(boolean enable) {
        lockSurfaceFlinger(!enable);
    }

    public static void lockSurfaceFlinger(final boolean isLockFlinger) {
        uiLock(isLockFlinger ? 1 : 0);
    }

    public static void drawFBCanvas(int x, int y, int width, int height, Bitmap canvasBitmap) {

        if (x >= 0 && x + width < SCREEN_WIDTH && y >= 0 && y + height < SCREEN_HEIGHT) {
            if (canvasDataTop == null || canvasDataTop.length < width * height) {
                canvasDataTop = new int[width * height];
            } else {
                //Arrays.fill(canvasDataTop, 0);
            }
        } else { //超出屏幕的范围处理
            if (x < 0 && x + width > 0) {
                x = 0;
                width = Math.min(width + x, SCREEN_WIDTH - 1);
            } else if (x + width <= 0) {
                return;
            } else if (x + width >= SCREEN_WIDTH) {
                if (x < SCREEN_WIDTH) {
                    width = SCREEN_WIDTH - x - 1;
                } else {
                    return;
                }
            }

            if (y < 0 && y + height > 0) {
                y = 0;
                height = Math.min(height + y, SCREEN_HEIGHT - 1);
            } else if (y + height <= 0) {
                return;
            } else if (y + height >= SCREEN_HEIGHT) {
                if (y < SCREEN_HEIGHT) {
                    if (x < 0) {
                        x = 0;
                    }
                    height = SCREEN_HEIGHT - y - 1;
                } else {
                    return;
                }
            }
            if (canvasDataTop == null || canvasDataTop.length < width * height) {
                canvasDataTop = new int[width * height];
            } else {
                //Arrays.fill(canvasDataTop, 0);
            }
        }

        if (canvasDataTop == null) {
            return;
        }

        //Log.d("drawHal", "x=" + x + " y=" + y + " width=" + width + " height=" + height);
        if (canvasDataTop.length < width * height) {
            throw new RuntimeException("test");
        }
        if ((height + y) >= SCREEN_HEIGHT || (width + x) >= SCREEN_WIDTH) {
            throw new RuntimeException("height or width too large");
        }
        canvasBitmap.getPixels(canvasDataTop, 0, width, x, y, width, height);

        DrawFrameBuffer.drawPixelRect(x, y, width, height, canvasDataTop);
    }
}
