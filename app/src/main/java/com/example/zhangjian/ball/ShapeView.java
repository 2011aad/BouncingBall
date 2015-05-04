package com.example.zhangjian.ball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zhangjian on 2015/5/3.
 */
public class ShapeView extends SurfaceView implements SurfaceHolder.Callback{

    private final BouncingBallActivity mball;

    private final int RADIUS = 50;
    private final float FACTOR_BOUNCEBACK = 0.75f;
    private final float mDeltaT = 0.5f; // imaginary time interval between each acceleration updates

    private int mXCenter;
    private int mYCenter;
    private RectF mRectF;
    private final Paint mPaint;
    private ShapeThread mThread;

    // screen size
    private int mWidthScreen;
    private int mHeightScreen;

    private float mVx;
    private float mVy;

    private boolean destroyed = false;
    private int color_index = 0;

    public ShapeView(Context context) {
        super(context);

        this.mball = (BouncingBallActivity) context;

        getHolder().addCallback(this);
        mThread = new ShapeThread(getHolder(), this);
        setFocusable(true);

        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.ball_color));
        mPaint.setAlpha(192);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mRectF = new RectF();
    }

    // set the position of the ball
    public boolean setOvalCenter(int x, int y)
    {
        mXCenter = x;
        mYCenter = y;
        return true;
    }

    // calculate and update the ball's position
    public boolean updateOvalCenter()
    {
        mVx -= mball.get_mAx() * mDeltaT;
        mVy += mball.get_mAy() * mDeltaT;

        mXCenter += (int)(mDeltaT * (mVx + 0.5 * mball.get_mAx() * mDeltaT));
        mYCenter += (int)(mDeltaT * (mVy + 0.5 * mball.get_mAy() * mDeltaT));

        if(mXCenter < RADIUS)
        {
            mXCenter = RADIUS;
            mVx = -mVx * FACTOR_BOUNCEBACK;
            collision();
        }

        if(mYCenter < RADIUS)
        {
            mYCenter = RADIUS;
            mVy = -mVy * FACTOR_BOUNCEBACK;
            collision();
    }
        if(mXCenter > mWidthScreen - RADIUS)
        {
            mXCenter = mWidthScreen - RADIUS;
            mVx = -mVx * FACTOR_BOUNCEBACK;
            collision();
        }

        if(mYCenter > mHeightScreen - 2 * RADIUS)
        {
            mYCenter = mHeightScreen - 2 * RADIUS;
            mVy = -mVy * FACTOR_BOUNCEBACK;
            collision();
        }

        return true;
    }

    // update the canvas
    protected void fix_onDraw(Canvas canvas)
    {
        if(mRectF != null && !destroyed )
        {
            mRectF.set(mXCenter - RADIUS, mYCenter - RADIUS, mXCenter + RADIUS, mYCenter + RADIUS);
            canvas.drawColor(0XFF000000);
            canvas.drawOval(mRectF, mPaint);
        }
        mWidthScreen = getWidth();
        mHeightScreen = getHeight() + (95-RADIUS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.setRunning(true);
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroyed = true;
        boolean retry = true;
        mThread.setRunning(false);
        while(retry)
        {
            try{
                mThread.join();
                retry = false;
            } catch (InterruptedException e){

            }
        }
    }

    public int get_screen_width(){return mWidthScreen;}

    public int get_screen_height(){return mHeightScreen;}

    private void collision(){
        int[] colors = {
            getResources().getColor(R.color.ball_color),
            getResources().getColor(R.color.ball_color1),
            getResources().getColor(R.color.ball_color2),
            getResources().getColor(R.color.ball_color3),
            getResources().getColor(R.color.ball_color4),
            getResources().getColor(R.color.ball_color5),
            getResources().getColor(R.color.ball_color6),
            getResources().getColor(R.color.ball_color7),
        };

        color_index++;
        color_index = color_index % colors.length;
        mball.vibrate(100);
        mball.play_tones(color_index);
        mPaint.setColor(colors[color_index]);
    }
}