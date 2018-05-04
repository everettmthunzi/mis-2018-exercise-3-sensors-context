package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * https://stackoverflow.com/questions/3616676/how-to-draw-a-line-in-android
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
        int width = getWidth();
        int height = getHeight();

        //the y axis
        canvas.drawLine((width / 2), 0, (width / 2), height, paint);

        //the x axis
        canvas.drawLine(0, (height / 2), width, (height / 2), paint);

        //the diagonal
        canvas.drawLine(0, 0, width, height, paint);

    }
}
