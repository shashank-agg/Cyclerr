package nl.delft.tu.iot.seminar.cyclerr.app;

import android.util.Log;

import org.jetbrains.annotations.NotNull;


public class FourierCadence implements AccelerationDataReceiver {

    private static final String TAG = FourierCadence.class.getSimpleName();

    @Override
    public void onAccelerationDataReceived(@NotNull AccelerationData data) {

        Log.d(TAG, String.format("%d: X=%.3f\t Y=%.3f\t Z=%.3f", data.getTimestamp()/1000000L,
                data.getAccelerationX(), data.getAccelerationY(), data.getAccelerationZ() ));

    }

}
