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


    float xValue = 0;
    float yValue = 0;
    float zValue = 0;
    //float magnitudeValue=0;

    public void setAccelerometerReadings(float x, float y, float z){
     xValue = x;
     yValue = y;
     zValue = z;

     //validate our values are updating 
     Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);

    }

    //flag to indicate start of the application
    Boolean isFirstRun = true;

    @Override
    public void onDraw(Canvas canvas) {

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

        if( isFirstRun == true){
            Log.d(TAG, "FIRST_RUN:  successful first run !");
            //the y axis
            canvas.drawLine((width / 2), 0, (width / 2), height, paintY);

            //the x axis
            canvas.drawLine(0, (height / 2), width, (height / 2), paintX);

            //the z axis
            canvas.drawLine(0, 0, width, height, paintZ);

            //the magnitude
            canvas.drawLine(0, 0, width, 0, paintMag);

            // remember to update properly
            isFirstRun = false;
        }
        else{

            //the y axis
            canvas.drawLine((width / 2), 0, (width / 2), height, paintY);

            //the x axis
            canvas.drawLine(0, (height / 2), width, (height / 2), paintX);

            //the z axis
            canvas.drawLine(0, 0, width, height, paintZ);

            //the magnitude
            canvas.drawLine(0, 0, width, 0, paintMag);

        }


    }
}
