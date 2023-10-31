package com.dazzle.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.cjz.jnidrawfb.DrawFrameBuffer;

public class WhiteboardSurfaceEraseDemoView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "WhiteboardSurfaceView";
    private static final int padding = 0;

    protected Bitmap mBackgroundBitmap;
    protected Canvas mBackgroundCanvas;

    protected SurfaceHolder mSurfaceHolder;

    Bitmap mContentImage = Bitmap.createBitmap(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas mContentCanvas = new Canvas(mContentImage);
    Paint mPaint = new Paint();

    Rect mCurrentEraseRect = new Rect();
    Rect mPreviousEraseRect = new Rect();

    Bitmap mEraseBitmap;
    int mEraseWidth;
    int mEraseHeight;

    Handler mHandler = new Handler();
    Runnable delayUiLock = () -> DrawFrameBuffer.uiLock(0);

    public WhiteboardSurfaceEraseDemoView(Context context) {
        super(context);
    }

    public WhiteboardSurfaceEraseDemoView(Context paramContext,
                                          AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);
    }

    public WhiteboardSurfaceEraseDemoView(Context paramContext,
                                          AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
    }

    private void init(Context paramContext) {
        System.gc();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mEraseBitmap = changeBitmapSize(R.drawable.erase2, 150, 250);
        mEraseWidth = mEraseBitmap.getWidth();
        mEraseHeight = mEraseBitmap.getHeight();

        final BitmapFactory.Options bgoptions = new BitmapFactory.Options();
        bgoptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bgoptions.inMutable = false;
        if (Config.is4K()) {
            bgoptions.inSampleSize = 2;
        }
        mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), Config.is4K() ? R.drawable.bg_grid : R.drawable.bg_grid_2k, bgoptions).copy(Bitmap.Config.ARGB_8888, true);
        ;
        mBackgroundCanvas = new Canvas(mBackgroundBitmap);

        initPaint();
        initSurface();
    }

    private void initSurface() {
        this.mSurfaceHolder = getHolder();
        this.mSurfaceHolder.addCallback(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d(TAG, "action:" + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                DrawFrameBuffer.uiLock(1);
                moveTo(x, y);
                DrawFrameBuffer.drawFBCanvas(mCurrentEraseRect.left, mCurrentEraseRect.top,
                        mCurrentEraseRect.width(), mCurrentEraseRect.height(), mContentImage);
                return true;

            case MotionEvent.ACTION_MOVE:
                lineTo(x, y);
                DrawFrameBuffer.drawFBCanvas(mPreviousEraseRect.left, mPreviousEraseRect.top,
                        mPreviousEraseRect.width(), mPreviousEraseRect.height(), mContentImage);
                DrawFrameBuffer.drawFBCanvas(mCurrentEraseRect.left, mCurrentEraseRect.top,
                        mCurrentEraseRect.width(), mCurrentEraseRect.height(), mContentImage);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                endLine();
                DrawFrameBuffer.drawFBCanvas(mCurrentEraseRect.left, mCurrentEraseRect.top,
                        mCurrentEraseRect.width(), mCurrentEraseRect.height(), mContentImage);
                mHandler.postDelayed(delayUiLock, 250);
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        drawContent(null);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //DrawFrameBuffer.initFb();
        drawContent(null);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void initPaint() {
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
    }

    private void moveTo(int x, int y) {
        mCurrentEraseRect.set(x - mEraseWidth / 2 - padding,
                y - mEraseHeight / 2 - padding,
                x + mEraseWidth / 2 + padding,
                y + mEraseHeight / 2 + padding);

        mContentCanvas.drawBitmap(mEraseBitmap, mCurrentEraseRect.left, mCurrentEraseRect.top, null);
    }

    private void lineTo(int x, int y) {
        mPreviousEraseRect.set(mCurrentEraseRect);
        mCurrentEraseRect.set(x - mEraseWidth / 2 - padding,
                y - mEraseHeight / 2 - padding,
                x + mEraseWidth / 2 + padding,
                y + mEraseHeight / 2 + padding);

        mContentCanvas.drawBitmap(mBackgroundBitmap, mPreviousEraseRect, mPreviousEraseRect, null);

        mContentCanvas.drawBitmap(mEraseBitmap, mCurrentEraseRect.left, mCurrentEraseRect.top, null);
    }

    private void endLine() {
        drawContent(mCurrentEraseRect);
    }

    private void drawContent(Rect r) {
        if (r == null)
            mContentCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
        else
            mContentCanvas.drawBitmap(mBackgroundBitmap, r, r, null);
        //TODO draw content to content image
        Canvas localCanvas = mSurfaceHolder.lockCanvas();
        if (localCanvas != null) {
            localCanvas.drawBitmap(mContentImage, 0, 0, null);
        }
        mSurfaceHolder.unlockCanvasAndPost(localCanvas);
    }


    private Bitmap changeBitmapSize(int resId, int newWidth, int newHeight) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        bitmap.getWidth();
        bitmap.getHeight();

        return bitmap;

    }

}
