package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mis.sensor.FFT;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //example variables
    private static final String TAG = "MainActivity";
    private double[] rndAccExamplevalues;
    private double[] freqCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initiate and fill example array with random values
        rndAccExamplevalues = new double[64];
        randomFill(rndAccExamplevalues);
        new FFTAsynctask(64).execute(rndAccExamplevalues);

        thisSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = thisSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        thisSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

        //implement RenderLines.java to draw lines
        RenderLines renderAccelerometerLines = new RenderLines(this);
        //renderAccelerometerLines.setBackgroundColor(Color.WHITE);
        setContentView(renderAccelerometerLines);
    }
    /**
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
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

    //: https://developer.android.com/guide/topics/sensors/sensors_overview
    private SensorManager thisSensorManager;
    private Sensor accelerometerSensor;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d(TAG, "TYPE_ACCELEROMETER: reading accelerometer values");
            //Toast.makeText(MainActivity.this,"accelerometer values",Toast.LENGTH_SHORT).show();

        //sensor return three values


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
// do something here if sensor value changes
    }

}
