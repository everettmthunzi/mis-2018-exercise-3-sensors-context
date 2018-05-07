package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * https://stackoverflow.com/questions/3616676/how-to-draw-a-line-in-android
 * proposed solution adapted to better suit assignment requirements
 */
public class RenderLines extends View {

    private static float xValue;
    private static float yValue;
    private static float zValue;
    private static float magnitude;
    private static final String TAG = "RenderLines";

    public RenderLines(Context context) {
        super(context);
        init();
    }
    Paint paintX = new Paint();
    Paint paintY = new Paint();
    Paint paintZ = new Paint();
    Paint paintMag = new Paint();
    Paint paintAxis = new Paint();

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

        //Grey cardinal axis
        paintAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        paintAxis.setColor(Color.LTGRAY);
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

        //evaluating the magnitude
        magnitude = (float) Math.sqrt((xValue*xValue) + (yValue*yValue) + (zValue*zValue));
        // Log.d(TAG,"" + magnitude + "");
    }

    @Override
    public void onDraw(Canvas canvas) {

        //validate values are indeed updating
        //Log.d(TAG,"" + xValue + "" + yValue + "" + zValue);

        //get the width and height of screen
        int width = getWidth();
        int height = getHeight();

        // accelerometer input to line visualization ratio [ 1:50 ]
        // i.e. every accelerometer reading is scaled by a factor of 50

        //grey x y cardinal axis
        canvas.drawLine(0,(height/2),width,(height/2), paintAxis);
        canvas.drawLine(width/2,0,width/2,height, paintAxis);

        //update x axis
        canvas.drawLine(width/2, height/2, width/2 + xValue*50, height/2, paintX);

        //the y axis
        canvas.drawLine(width/2, height/2, width/2, height/2 + yValue*50, paintY);

        //the z axis
        canvas.drawLine(width/2, height/2, width/2 + zValue*50, height/2 - zValue*50, paintZ);

        //the magnitude
        canvas.drawLine(width/2, height/2, width/2 + magnitude*50, height/2 + magnitude*50, paintMag);

        // Redraw lines ~ update lines
        invalidate();
    }
}