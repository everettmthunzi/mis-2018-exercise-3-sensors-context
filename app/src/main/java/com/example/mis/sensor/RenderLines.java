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
 * solution adapted to better suit RenderLines.java
 */
public class RenderLines extends View{

    private static final String TAG = "RenderLines";

    public RenderLines(Context context) {
        super(context);
        init();
    }

    Paint paint = new Paint();

    private void init() {
        paint.setColor(Color.BLACK);
    }


    public RenderLines(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RenderLines(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


  private static float xValue;
  private static  float yValue;
  private static  float zValue;
    //float magnitudeValue=0;

    public void setAccelerometerReadings(float x, float y, float z){
     xValue = x;
     yValue = y;
     zValue = z;
     //validate our values are updating
     //Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);
    }


    //flag to indicate start of the application
    Boolean isFirstRun = true;

    @Override
    public void onDraw(Canvas canvas) {
        //Log.d(TAG, "onDraw: UPDATE!");
        Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);
        //get the width and height of screen
        int width = getWidth();
        int height = getHeight();

        // set colors outlined in the assignment doc
        Paint paintX = new Paint();
        paintX.setStyle(Paint.Style.FILL);
        paintX.setColor(Color.GREEN);

        Paint paintY = new Paint();
        paintY.setStyle(Paint.Style.FILL);
        paintY.setColor(Color.RED);

        Paint paintZ = new Paint();
        paintZ.setStyle(Paint.Style.FILL);
        paintZ.setColor(Color.BLUE);

        // ~ side note: white barely visible on the top of the application
        Paint paintMag = new Paint();
        paintMag.setStyle(Paint.Style.FILL);
        paintMag.setColor(Color.WHITE);

        /*
         * All accelerometer values multiplied by a constant factor of 50
         * This allows better visualization of line movement w.r.t.
         * accelerometer readings
         */

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

        //points to note increase thickness of lines
        // can line movement be smoother? 

        //Redraw
        invalidate();

    }
}
