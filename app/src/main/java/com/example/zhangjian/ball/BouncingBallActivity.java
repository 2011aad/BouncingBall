package com.example.zhangjian.ball;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;


public class BouncingBallActivity extends Activity implements SensorEventListener{

    // sensor-related
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    // animated view
    private ShapeView mShapeView;

    // motion parameters
    private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
    private final float GRAVITY = 9.8f; // acceleration of gravity
    private float mAx; // acceleration along x axis
    private float mAy; // acceleration along y axis


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the screen always portait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initializing sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShapeView = new ShapeView(this);
        setContentView(mShapeView);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // obtain the three accelerations from sensors
        mAx = event.values[0];
        mAy = event.values[1];

        float mAz = event.values[2];

        // taking into account the frictions
        mAx *= (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);
        mAy *= (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initializing the view that renders the ball
        mShapeView = new ShapeView(this);
        mShapeView.setOvalCenter((int)(mShapeView.get_screen_width() * 0.6), (int)(mShapeView.get_screen_height() * 0.6));

        setContentView(mShapeView);
        // start sensor sensing
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop senser sensing
        // mSensorManager.unregisterListener(this);
    }

    protected void onDestory()
    {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    // the view that renders the ball

    public float get_mAx(){return mAx;}

    public float get_mAy(){return mAy;}
}