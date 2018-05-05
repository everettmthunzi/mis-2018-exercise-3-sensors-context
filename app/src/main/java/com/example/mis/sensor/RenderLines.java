package com.example.mis.sensor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * https://stackoverflow.com/questions/3616676/how-to-draw-a-line-in-android
 * solution adapted to better suit assignment requirements
 */
public class RenderLines extends View{

    private static float xValue;
    private static  float yValue;
    private static  float zValue;
    //private float magnitudeValue=0;
    private static final String TAG = "RenderLines";

    public RenderLines(Context context) {
        super(context);
        init();
    }
    Paint paintX = new Paint();
    Paint paintY = new Paint();
    Paint paintZ = new Paint();
    Paint paintMag = new Paint();

    private void init() {
        //GREEN x-axis
        paintX.setStyle(Paint.Style.FILL);
        paintX.setColor(Color.GREEN);
        //RED y-axis
        paintY.setStyle(Paint.Style.FILL);
        paintY.setColor(Color.RED);
        //BLUE z-axis
        paintZ.setStyle(Paint.Style.FILL);
        paintZ.setColor(Color.BLUE);
        //WHITE accelerometer magnitude
        paintMag.setStyle(Paint.Style.FILL);
        paintMag.setColor(Color.WHITE);

        // ~ side note: white barely visible on the top of the application
    }

    public RenderLines(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RenderLines(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setAccelerometerReadings(float x, float y, float z){

        xValue = x;
        yValue = y;
        zValue = z;

        //validate our values are updating
        //Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);
    }

    @Override
    public void onDraw(Canvas canvas) {
        //Log.d(TAG, "onDraw: UPDATE!");
        Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);

        //get the width and height of screen
        int width = getWidth();
        int height = getHeight();

        //accelerometer input to line visualization ratio [ 1:50 ]
        // i.e. every accelerometer reading *K  where K is 50

        //update x axis
        Log.d(TAG, "onDraw: x axis updating!");
        canvas.drawLine((width / 2) + xValue*50, 0, (width / 2) +
                xValue*50, height, paintX);

        //the y axis
        canvas.drawLine(0, (height / 2) + yValue*50, width, (height / 2) +
                yValue*50, paintY);

        //the z axis (needs revision)
        canvas.drawLine(0, 0, width + zValue*50, height
                + zValue*50, paintZ);

        //the magnitude (not showing the magnitude yet!!!!)
        canvas.drawLine(0, 0, width, 0, paintMag);

        //Redraw
        invalidate();
    }
}
/*
        Side Notes:
        -- magnitude no indicating
        -- z axis not set
        -- can line thickness be increased ?
        -- can line movement be smoother?
*/
