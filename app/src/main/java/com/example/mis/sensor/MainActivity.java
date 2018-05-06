package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mis.sensor.FFT;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //variables

    //using GraphView : http://www.android-graphview.org/
    private LineGraphSeries<DataPoint> FFT_series;

    private double[] rndAccExamplevalues;
    private double[] freqCounts;

    private SensorManager thisSensorManager;
    private Sensor accelerometerSensor;

    private SeekBar sampleRateSeekBar;
    private SeekBar windowSizeSeekBar;

    private TextView textViewSampleRate;
    private TextView textViewFFRWindow;
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

        // Seek Bar - setup
        sampleRateSeekBar  = (SeekBar) findViewById(R.id.seekBar_SR);
        windowSizeSeekBar  = (SeekBar) findViewById(R.id.seekBar_FFT_window);
        textViewFFRWindow  = (TextView) findViewById(R.id.textViewFFTW);
        textViewSampleRate = (TextView) findViewById(R.id.textViewSR);

        initializeGraph();
        initializeAccelerometerLineRendering();
    }

    //Real-time Graphing
    public void initializeGraph(){
        double x;
        double y;
        x = 0;

        sampleRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSampleRate.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        windowSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewFFRWindow.setText("" + progress );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Create Graph View
        GraphView graph = (GraphView) findViewById(R.id.graph);
        FFT_series = new LineGraphSeries <>();
        graph.addSeries(FFT_series);

        // add the amount of data points
        int dataPoints =  100;

        //append real-time data to GRAPH_
        for(int i = 0; i < dataPoints; i++){
            x = x + 0.1;
            y = Math.sin(x);
            FFT_series.appendData(new DataPoint(x,y), true, 100);
           }
        graph.addSeries(FFT_series);
    }

    // Processing Accelerometer Values
    public void initializeAccelerometerLineRendering(){
        //Accelerometer Manager
        thisSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = thisSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Register Listener
        thisSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

        //FRAGMENTS TO DISPLAY MULTI-VIEWS
        FragmentManager fragmentManager = getSupportFragmentManager();

        //second fragment: sample rate and FFT window size seek-bars
        SecondFragment secondFragment = new SecondFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.firstLayout, secondFragment, secondFragment.getTag())
                .commit();

        // add RenderLines ~ (View Class) to firstLayout
        ViewGroup layout = (ViewGroup) findViewById(R.id.firstLayout);
        renderAccelerometerLines = new RenderLines(MainActivity.this);
        layout.addView(renderAccelerometerLines);
    }

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

    //------------------------------------------------------------------------FFTAsync Class:

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
}
