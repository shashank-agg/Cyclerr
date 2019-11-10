package nl.delft.tu.iot.seminar.cyclerr.app.speed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;

import static java.time.Instant.ofEpochMilli;


public class SpeedCalculator {

    private final String TAG = SpeedCalculator.class.getSimpleName();

    private LocationManager locationManager;
    private SpeedUpdateListener speedUpdateListener;

    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location received: " + location);
            // Called when a new location is found by the network location provider.
            Instant timeNow = ofEpochMilli(System.currentTimeMillis());
            speedUpdateListener.onSpeedUpdateListener(timeNow, location.getSpeed());
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {

        }
    };

    public SpeedCalculator(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void registerListener(SpeedUpdateListener speedUpdateListener) {
        this.speedUpdateListener = speedUpdateListener;
    }

    @SuppressLint("MissingPermission")
    public void start(Context context) {

        Log.d(TAG, "GPS Service started.");
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//        boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
    }

    public void stop() {
        locationManager.removeUpdates(locationListener);
    }
}

interface SpeedUpdateListener {
    void onSpeedUpdateListener(Instant time, Float speed);
}