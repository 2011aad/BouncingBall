package com.example.zhangjian.ball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zhangjian on 2015/5/3.
 */
public class ShapeView extends SurfaceView implements SurfaceHolder.Callback{

    private final BouncingBallActivity mball;

    private final int RADIUS = 30;
    private final float FACTOR_BOUNCEBACK = 0.75f;
    private final float mDeltaT = 0.5f; // imaginary time interval between each acceleration updates

    private int mXCenter;
    private int mYCenter;
    private RectF mRectF, slide_brick;
    private int brick_number = 2;
    private RectF [] brick = new RectF[brick_number];
    private final Paint mPaint = new Paint();
    private Paint [] brick_Paint = new Paint[brick_number];
    private ShapeThread mThread;

    private int [] brick_Xcenter = {200,500};
    private int [] brick_Ycenter = {300,200};
    private int brick_height = 50, brick_width = 150;
    private int slide_brickX=500,slide_brickY=1150;
    private final Paint slide_brick_Paint = new Paint();


    // screen size
    private int mWidthScreen;
    private int mHeightScreen;

    private float mVx;
    private float mVy;

    private boolean destroyed = false;
    private int color_index = 0;
    private float direction[] = {1,1,1};

    public ShapeView(Context context) {
        super(context);

        this.mball = (BouncingBallActivity) context;

        getHolder().addCallback(this);
        mThread = new ShapeThread(getHolder(), this);
        setFocusable(true);

        EmbossMaskFilter emboss = new EmbossMaskFilter(direction,0.4f,6,3.5f);

        mRectF = new RectF();
        mPaint.setColor(getResources().getColor(R.color.ball_color));
        mPaint.setAlpha(192);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        for(int i=0;i<brick.length;i++){
            brick_Paint[i] = new Paint();
            brick_Paint[i].setColor(getResources().getColor(R.color.brick_color));
            brick_Paint[i].setAlpha(192);
            brick_Paint[i].setStyle(Paint.Style.FILL);
            brick_Paint[i].setAntiAlias(true);
            brick_Paint[i].setMaskFilter(emboss);
            brick[i] = new RectF();
            brick[i].set(brick_Xcenter[i]-brick_width/2,brick_Ycenter[i]-brick_height/2,
                    brick_Xcenter[i]+brick_width/2, brick_Ycenter[i]+brick_height/2);
        }
        slide_brick = new RectF();
        slide_brick_Paint.setColor(getResources().getColor(R.color.slide_brick_color));
        slide_brick_Paint.setMaskFilter(emboss);
        slide_brick.set(slide_brickX-brick_width/2,slide_brickY-brick_height/2,
                slide_brickX+brick_width/2,slide_brickY+brick_height/2);
    }

    public boolean onTouchEvent(MotionEvent event){

        if(event.getX()>slide_brickX-brick_width/2 && event.getX()<slide_brickX+brick_width/2 &&
            event.getY()>slide_brickY-brick_height/2 && event.getY()<slide_brickY+brick_height/2){
            slide_brickX = (int)event.getX();
        }

        return true;
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
            if(Math.abs(mVx)>6)collision();
        }

        if(mYCenter < RADIUS)
        {
            mYCenter = RADIUS;
            mVy = -mVy * FACTOR_BOUNCEBACK;
            if(Math.abs(mVy)>6)collision();
        }
        if(mXCenter > mWidthScreen - RADIUS)
        {
            mXCenter = mWidthScreen - RADIUS;
            mVx = -mVx * FACTOR_BOUNCEBACK;
            if(Math.abs(mVx)>6)collision();
        }

        if(mYCenter > mHeightScreen - 2 * RADIUS)
        {
            mYCenter = mHeightScreen - 2 * RADIUS;
            mVy = -mVy * FACTOR_BOUNCEBACK;
            if(Math.abs(mVy)>6)collision();
        }

        for(int i=0;i<brick_number;i++){
            switch (collision_with_brick(i)){
                case 0:break;
                case 1:mXCenter = brick_Xcenter[i] - brick_width/2 - RADIUS;
                    mVx = -mVx;
                    collision();
                    break;
                case 2:mYCenter = brick_Ycenter[i] + brick_width/2 + RADIUS;
                    mVy = -mVy;
                    collision();
                    break;
                case 3:mXCenter = brick_Xcenter[i] + brick_width/2 + RADIUS;
                    mVx = -mVx;
                    collision();
                    break;
                case 4:mYCenter = brick_Ycenter[i] - brick_width/2 - RADIUS;
                    mVy = -mVy;
                    collision();
                    break;
                default:break;
            }
        }

        if(mYCenter+RADIUS>slide_brickY-brick_height/2){
            if(mXCenter+RADIUS>(slide_brickX-brick_width/2) && mXCenter+RADIUS<(slide_brickX+brick_width/2) ||
                    mXCenter-RADIUS>(slide_brickX-brick_width/2) && mXCenter-RADIUS<(slide_brickX+brick_width/2)){
                mYCenter = slide_brickY-brick_height/2-RADIUS;
                mVy = -mVy * FACTOR_BOUNCEBACK;
                if(Math.abs(mVy)>6)collision();
            }

            else {
                mXCenter = RADIUS;
                mYCenter = RADIUS;
                mVx = 0;
                mVy = 0;
            }
        }

        return true;
    }

    // update the canvas
    protected void fix_onDraw(Canvas canvas)
    {
        canvas.drawColor(0XFF000000);
        for(int i=0;i<brick.length;i++){
            canvas.drawRect(brick[i], brick_Paint[i]);
        }

        slide_brick.set(slide_brickX-brick_width/2,slide_brickY-brick_height/2,
                slide_brickX+brick_width/2,slide_brickY+brick_height/2);
        canvas.drawRect(slide_brick,slide_brick_Paint);

        if(mRectF != null && !destroyed )
        {
            mRectF.set(mXCenter - RADIUS, mYCenter - RADIUS, mXCenter + RADIUS, mYCenter + RADIUS);
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

    private int collision_with_brick(int i){
        if(mXCenter+RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter+RADIUS<(brick_Xcenter[i]+brick_width/2) &&
                mXCenter-RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter-RADIUS<(brick_Xcenter[i]+brick_width/2)){
            if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))){
                if(Math.abs(mVx)> Math.abs(mVy)){
                    if(mVx>0) return 1;
                    else return 3;
                }
                else{
                    if(mVy>0) return 4;
                    else return 2;
                }
            }

            else if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && !(mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                return 2;
            }

            else if(!(mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                return 4;
            }

            else return 0;
        }

        if((mXCenter+RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter+RADIUS<(brick_Xcenter[i]+brick_width/2)) &&
                !(mXCenter-RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter-RADIUS<(brick_Xcenter[i]+brick_width/2))){
            if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))){
                return 1;
            }

            else if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && !(mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                if(Math.abs(mVx)>Math.abs(mVy)) return 1;
                else return 2;
            }

            else if(!(mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                if(Math.abs(mVx)>Math.abs(mVy)) return 1;
                else return 4;
            }

            else return 0;
        }

        if(!(mXCenter+RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter+RADIUS<(brick_Xcenter[i]+brick_width/2)) &&
                (mXCenter-RADIUS>(brick_Xcenter[i]-brick_width/2) && mXCenter-RADIUS<(brick_Xcenter[i]+brick_width/2))){
            if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))){
                return 3;
            }

            else if((mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && !(mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                if(Math.abs(mVx)>Math.abs(mVy)) return 3;
                else return 2;
            }

            else if(!(mYCenter-RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter-RADIUS<(brick_Ycenter[i]+brick_height/2))
                    && (mYCenter+RADIUS>(brick_Ycenter[i]-brick_height/2) && mYCenter+RADIUS<(brick_Ycenter[i]+brick_height/2))) {
                if(Math.abs(mVx)>Math.abs(mVy)) return 3;
                else return 4;
            }

            else return 0;
        }

        return 0;
    }
}