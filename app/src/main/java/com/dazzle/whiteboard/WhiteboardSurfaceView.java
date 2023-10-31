package com.dazzle.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.cjz.jnidrawfb.DrawFrameBuffer;

public class WhiteboardSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "WhiteboardSurfaceView";

    protected SurfaceHolder mSurfaceHolder;
    protected Bitmap mBackgroundBitmap;

    Path mPath = new Path();
    Bitmap mScreenImage = Bitmap.createBitmap(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas mCanvasImage = new Canvas(mScreenImage);
    Paint mPaint = new Paint();

    RectF tempLocationF = new RectF();
    RectF currentLineBound = new RectF();
    Rect currentBound = new Rect();

    Handler mHandler = new Handler();
    Runnable delayUiLock = () -> DrawFrameBuffer.uiLock(0);


    public WhiteboardSurfaceView(Context context) {
        super(context);
    }

    public WhiteboardSurfaceView(Context paramContext,
                                 AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);
    }

    public WhiteboardSurfaceView(Context paramContext,
                                 AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
    }


    private void init(Context paramContext) {
        System.gc();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (Config.is4K()) {
            options.inSampleSize = 2;
        }

        mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), Config.is4K() ? R.drawable.bg_grid : R.drawable.bg_grid_2k, options);
        mCanvasImage.drawBitmap(mBackgroundBitmap, 0, 0, null);
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
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                DrawFrameBuffer.uiLock(1);
                moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                lineTo(x, y);
                Log.d(TAG, "tempLocationF:" + tempLocationF.toShortString());

                DrawFrameBuffer.drawFBCanvas((int) tempLocationF.left, (int) tempLocationF.top,
                        (int) tempLocationF.width(), (int) tempLocationF.height(), mScreenImage);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawSurface(false);
                //DrawFrameBuffer.uiLock(0);
                clear();
                mHandler.postDelayed(delayUiLock, 230);
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        drawSurface(true);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //DrawFrameBuffer.initFb();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void initPaint() {
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    private void moveTo(float x, float y) {
        mPath.moveTo(x, y);
        currentLineBound.setEmpty();
    }

    private void lineTo(float x, float y) {
        //if(reset)
        mPath.lineTo(x, y);
        mCanvasImage.drawPath(mPath, mPaint);

        mPath.computeBounds(tempLocationF, true);
        int padding = 5;
        tempLocationF.set(tempLocationF.left - padding, tempLocationF.top - padding,
                tempLocationF.right + padding, tempLocationF.bottom + padding);
        currentLineBound.union(tempLocationF);
        mPath.rewind();
        mPath.moveTo(x, y);
    }

    private void drawSurface(boolean background) {
        Canvas c;
        Bitmap bitmap = background ? mBackgroundBitmap : mScreenImage;

        if (!background) {
            currentLineBound.round(currentBound);
            int padding = 2;
            currentBound.set(currentBound.left - padding, currentBound.top - padding, currentBound.right + padding, currentBound.bottom + padding);

            c = mSurfaceHolder.lockCanvas(currentBound);
            c.drawBitmap(bitmap, currentBound, currentBound, mPaint);
        } else {
            c = mSurfaceHolder.lockCanvas();
            c.drawBitmap(bitmap, 0, 0, mPaint);
        }
        mSurfaceHolder.unlockCanvasAndPost(c);
    }

    private void clear() {
        mPath.rewind();

        //mCanvasImage.drawBitmap(mBackgroundBitmap,0, 0, null);
    }
}
