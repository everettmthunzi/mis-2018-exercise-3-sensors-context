package com.example.mis.sensor;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityDetector extends AsyncTask<Void, Void, Void> {
    /* Ability to load assets
     * (via https://stackoverflow.com/a/22364008)
     */
    private Context context;

    private MediaPlayer mediaPlayer = MainActivity.mediaPlayerDistrictFour;

    public ActivityDetector(Context c) {
        this.context = c;
    }

    @Override
    protected Void doInBackground(Void ... voids) {
        while (!isCancelled()) {
            double avgLastFiveSeconds = 0;
            double max = 0;
            Timer musicTimeout = null;
            TimerTask stopMusic = new TimerTask() {
                @Override
                public void run() {
                    mediaPlayer.pause();
                }
            };

            long t = System.currentTimeMillis();
            long end = t + 5000;
            int cycles = 0;
            while (System.currentTimeMillis() < end) {
                for (int i = 1; i < MainActivity.freqCounts.length; i++) {
                    if (MainActivity.freqCounts[i] > max) max = MainActivity.freqCounts[i];
                }
                avgLastFiveSeconds += max;
                cycles++;
            }
            avgLastFiveSeconds /= cycles;
            Log.d("ActivityDetector", "Average motion over last 5 sec: " + avgLastFiveSeconds);

            if ((avgLastFiveSeconds > 50) && (avgLastFiveSeconds < 200)) {
                @SuppressWarnings({"ResourceType"}) // Android Studio doesn't get that we're doing what it wants here
                final Location location = MainActivity.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null && location.hasSpeed()) {
                    int speed = (int) (location.getSpeed() * 3.6);
                    Log.d("ActivityDetector", "Speed: " + speed);
                    if ((speed > 10) && (speed < 30)) { // probably cycling
                        if (!mediaPlayer.isPlaying()) {
                            // mediaPlayer.reset();
                            mediaPlayer = MainActivity.mediaPlayerDistrictFour;
                            mediaPlayer.start();
                        }
                        if (musicTimeout != null) musicTimeout.cancel();
                        musicTimeout = new Timer();
                        musicTimeout.schedule(stopMusic, 30000); // keep playing for half a minute
                    }
                }
                else if (location == null || !location.hasSpeed()) {
                    if (!mediaPlayer.isPlaying()) {
                        // mediaPlayer.reset();
                        mediaPlayer = MainActivity.mediaPlayerDistrictFour;
                        mediaPlayer.start();
                    }
                    if (musicTimeout != null) musicTimeout.cancel();
                    musicTimeout = new Timer();
                    musicTimeout.schedule(stopMusic, 30000); // keep playing for half a minute
                }
            }
            else if (avgLastFiveSeconds > 200) { // probably running
                if (!mediaPlayer.isPlaying()) {
                    // mediaPlayer.reset();
                    mediaPlayer = MainActivity.mediaPlayerRoadTrip;
                    mediaPlayer.start();
                }
                if (musicTimeout != null) musicTimeout.cancel();
                musicTimeout = new Timer();
                musicTimeout.schedule(stopMusic, 30000); // keep playing for a minute
            }
        }
        return null;
    }
}
