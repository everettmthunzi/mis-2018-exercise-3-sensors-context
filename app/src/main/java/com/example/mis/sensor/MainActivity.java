package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mis.sensor.FFT;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //example variables
    private double[] rndAccExamplevalues;
    private double[] freqCounts;

    //instance variable RenderLines Object
    RenderLines renderAccelerometerLines;

    //Logger: Debugging Tag
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initiate and fill example array with random values
        rndAccExamplevalues = new double[64];
        randomFill(rndAccExamplevalues);
        new FFTAsynctask(64).execute(rndAccExamplevalues);

        //Accelerometer Manager
        thisSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = thisSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Register Listener
        thisSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

        //new RenderLines Object within the MainActivity Context
        renderAccelerometerLines = new RenderLines(MainActivity.this);
        //setContentView(renderAccelerometerLines);

        //using fragments to display multiple views instantaneously
        FirstFragment firstFragment = new FirstFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.secondLayout, firstFragment, firstFragment.getTag())
                .commit();

        SecondFragment secondFragment = new SecondFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.firstLayout, secondFragment, secondFragment.getTag())
                .commit();

        ViewGroup layout = (ViewGroup) findViewById(R.id.firstLayout);
     //   renderAccelerometerLines.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        layout.addView(renderAccelerometerLines);


    }

    //: https://developer.android.com/guide/topics/sensors/sensors_overview
    private SensorManager thisSensorManager;
    private Sensor accelerometerSensor;

    @Override
    public void onSensorChanged(SensorEvent event) {

        //update for every new sensor value
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

        //Log.d(TAG,"" + x + "" + y + "" + z);
        renderAccelerometerLines.setAccelerometerReadings(x,y,z);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // do something here if sensor accuracy value changes
    }

    /*---------------------------------------------------------------------------------
     *
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
     *
     */

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        private int wsize; //window size must be power of 2

        // constructor to set window size
        FFTAsynctask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {

            double[] realPart = values[0].clone(); // actual acceleration values
            double[] imagPart = new double[wsize]; // init empty

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);
            //init new double array for magnitude (e.g. frequency count)
            double[] magnitude = new double[wsize];


            //fill array with magnitude values of the distribution
            for (int i = 0; wsize > i ; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }
            return magnitude;
        }

        @Override
        protected void onPostExecute(double[] values) {
            //hand over values to global variable after background task is finished
            freqCounts = values;
        }
    }
    /**
     * little helper function to fill example with random double values
     */
    public void randomFill(double[] array){
        Random rand = new Random();
        for(int i = 0; array.length > i; i++){
            array[i] = rand.nextDouble();
        }
    }
}
