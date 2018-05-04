package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * https://stackoverflow.com/questions/3616676/how-to-draw-a-line-in-android
 * solution adapted to better suit RenderLines.java
 */
public class RenderLines extends View{

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

    @Override
    public void onDraw(Canvas canvas) {
        Paint paintX = new Paint();
        paintX.setStyle(Paint.Style.FILL);
        paintX.setColor(Color.GREEN);

        Paint paintY = new Paint();
        paintY.setStyle(Paint.Style.FILL);
        paintY.setColor(Color.RED);

        Paint paintZ = new Paint();
        paintZ.setStyle(Paint.Style.FILL);
        paintZ.setColor(Color.BLUE);

        Paint paintMag = new Paint();
        paintMag.setStyle(Paint.Style.FILL);
        paintMag.setColor(Color.WHITE);


        int width = getWidth();
        int height = getHeight();

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
