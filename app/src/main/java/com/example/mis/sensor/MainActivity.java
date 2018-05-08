package com.example.mis.sensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static int windowSize = 64;
    private int sampleRate = SensorManager.SENSOR_DELAY_GAME;
    public static double[] freqCounts;
    private static float accMagnitude;
    private double[] magnitudes;
    private int count = 0;
    //private double[] rndAccExamplevalues;

    private Sensor accelerometerSensor;
    private SensorManager thisSensorManager;

    public static LocationManager locationManager;
    private ActivityDetector activityDetector;

    private Thread thread;
    private Boolean plotData = true;
    private SeekBar sampleRateSeekBar;
    private SeekBar windowSizeSeekBar;
    private TextView textViewSampleRate;
    private TextView textViewFFRWindow;

    private AsyncTask fftAsynctask;

    private LineChart lineChart;                        //https://github.com/PhilJay/MPAndroidChart
    private static final String TAG = "MainActivity";   //Logger: Debugging Tag
    RenderLines renderAccelerometerLines;               //RenderLines :View Class Object

    /* MediaPlayer has to be static to persist through rotation
     * (via https://stackoverflow.com/a/17921927)
     */
    public static MediaPlayer mediaPlayerDistrictFour = null;
    public static MediaPlayer mediaPlayerRoadTrip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Music player - setup
         * This will not happen when the device is merely rotated!
         * (via https://stackoverflow.com/a/17921927)
         */
        if (mediaPlayerDistrictFour == null) {
            mediaPlayerDistrictFour = MediaPlayer.create(MainActivity.this, R.raw.district_four);
            mediaPlayerDistrictFour.setLooping(true);
        }
        if (mediaPlayerRoadTrip == null) {
            mediaPlayerRoadTrip = MediaPlayer.create(MainActivity.this, R.raw.road_trip);
            mediaPlayerRoadTrip.setLooping(true);
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        Log.d(TAG, "ActivityDetector update time is: " + sampleRate * windowSize);
        activityDetector = new ActivityDetector(this);

        lineChart = (LineChart) findViewById(R.id.chart1);

        sampleRateSeekBar  = (SeekBar) findViewById(R.id.seekBar_SR);
        sampleRateSeekBar.setProgress(sampleRate / 1000);

        windowSizeSeekBar  = (SeekBar) findViewById(R.id.seekBar_FFT_window);
        textViewFFRWindow  = (TextView) findViewById(R.id.textViewFFTW);
        textViewSampleRate = (TextView) findViewById(R.id.textViewSR);

        //initiate and fill example array with random values
        //rndAccExamplevalues = new double[windowSize];
        //randomFill(rndAccExamplevalues);

        magnitudes = new double[windowSize];
        fftAsynctask = new FFTAsynctask(windowSize).execute(magnitudes);

        //second fragment: sample rate and FFT window size seek-bars
        FragmentManager fragmentManager = getSupportFragmentManager();
        SecondFragment secondFragment = new SecondFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.firstLayout, secondFragment, secondFragment.getTag())
                .commit();

        // add RenderLines ~ View Class with x, y, z cardinal axis canvas
        ViewGroup layout = (ViewGroup) findViewById(R.id.firstLayout);
        renderAccelerometerLines = new RenderLines(MainActivity.this);
        layout.addView(renderAccelerometerLines);

        //Accelerometer Manager
        thisSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = thisSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //if accelerometer hardware present
        if(accelerometerSensor != null){
            //Register Listener
            thisSensorManager.registerListener(this, accelerometerSensor, sampleRate);
        }

        init(); // initialize seekBars

        //MPAndroidChart Specified : https://github.com/PhilJay/MPAndroidChart/blob/ ...
        // ... master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartTime.java

        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Accelerometer Magnitude FFT Data (real-time)");
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setGridBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        lineChart.setData(data);

        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setAxisMinimum(1); // to get rid of the annoying peak at 0
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(800f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.setDrawBorders(true);

        chartLineGraph();      // start the plot
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //check if sensor is supported in hardware
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        renderAccelerometerLines.setAccelerometerReadings(x,y,z);
        accMagnitude = renderAccelerometerLines.getMagnitude();

        if (count == magnitudes.length) {
            count = 0;
            fftAsynctask = new FFTAsynctask(windowSize).execute(magnitudes);
        }
        else {
            magnitudes[count++] = accMagnitude;
        }

        //validate real-time data
        // Log.d(TAG," x:" + x + " y:" + y + " z:" + z + " magnitude:" + accMagnitude);

//        if(plotData) {
//            dataEntry();
//            plotData = false;
//        }
    }

    private void dataEntry(){

        //append to existing data series
        LineData data = lineChart.getData();
        //if no data is present initialize data using createSet()
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            set.clear();
            for (int i = 0; i < freqCounts.length; i++) {
                //offset with a value of 200f for better visualization
                Entry entry = new Entry(i, (float) freqCounts[i]+ 200f); //play around with this value
                // Log.d(TAG,"------THE ENTRY COUNT IS :" + set.getEntryCount());
                data.addEntry(entry, 0);
            }

            // Refresh LineGraph in Real-time
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(windowSize);
            lineChart.invalidate();
            //lineChart.moveViewToX(data.getEntryCount());
        }
    }
    // adapted from example sets provided in : https://github.com/PhilJay/MPAndroidChart
    private LineDataSet createSet (){
        LineDataSet set = new LineDataSet(null ,"Accelerometer Magnitude");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setColor(Color.YELLOW);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        thisSensorManager.unregisterListener(this);
        thread.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread !=null){
            thread.interrupt();
        }
        thisSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        thisSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void init(){
        configureSeekBars();
    }

    public void configureSeekBars(){
        sampleRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSampleRate.setText("" + progress);
                sampleRate = progress;
                if (accelerometerSensor != null) {
                    thisSensorManager.registerListener(MainActivity.this,
                            accelerometerSensor, sampleRate);
                }
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

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                thisSensorManager.unregisterListener(MainActivity.this);
                int progress = seekBar.getProgress();
                textViewFFRWindow.setText("" + progress );
                windowSize = (int) Math.pow(2, progress);
                Log.d(TAG, "Window size: " + windowSize);
                int isPowerOf2 = (int) (Math.log(windowSize) / Math.log(2));
                if (windowSize == (1 << isPowerOf2)) {
                    magnitudes = new double[windowSize];
                    count = 0;
                    fftAsynctask = new FFTAsynctask(windowSize).execute(magnitudes);
                }
                thisSensorManager.registerListener(MainActivity.this, accelerometerSensor, sampleRate);
            }
        });
    }

    private void chartLineGraph(){
        if(thread != null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    plotData = true;
                    try {
                        Thread.sleep(sampleRate); //control over the sampling rate 100
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /* -----------------
     * --FFTAsync Class:
     * -----------------
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
            if (activityDetector.getStatus().equals(Status.PENDING)) {
                activityDetector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            dataEntry();
        }
    }

    // Fill example with random double values
//    public void randomFill(double[] array){
//        Random rand = new Random();
//        for(int i = 0; array.length > i; i++){
//            array[i] = rand.nextDouble();
//        }
//    }

    /* Copy an InputStream to a File.
     * (via https://stackoverflow.com/a/28131358)
     */
    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

}
