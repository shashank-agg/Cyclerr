package nl.delft.tu.iot.seminar.cyclerr.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.time.Instant;

import static java.time.Instant.ofEpochMilli;

public class SpeedCalculator {

    private LocationManager locationManager;
    private SpeedUpdateListener speedUpdateListener;

    public SpeedCalculator(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void registerListener(SpeedUpdateListener speedUpdateListener) {
        this.speedUpdateListener = speedUpdateListener;
    }

    @SuppressLint("MissingPermission")
    public void start(Context context) {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
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
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
//        boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
    }
}

interface SpeedUpdateListener {
    public void onSpeedUpdateListener(Instant time, Float speed);
}