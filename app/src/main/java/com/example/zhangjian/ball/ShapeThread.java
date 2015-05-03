package com.example.zhangjian.ball;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by zhangjian on 2015/5/3.
 */
class ShapeThread extends Thread {
    private SurfaceHolder mSurfaceHolder;
    private ShapeView mShapeView;
    private boolean mRun = false;

    public ShapeThread(SurfaceHolder surfaceHolder, ShapeView shapeView) {
        mSurfaceHolder = surfaceHolder;
        mShapeView = shapeView;
    }

    public void setRunning(boolean run) {
        mRun = run;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public void run() {
        Canvas c;
        while (mRun) {
            mShapeView.updateOvalCenter();
            c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    mShapeView.fix_onDraw(c);
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}