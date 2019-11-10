package nl.delft.tu.iot.seminar.cyclerr.app.cadence;

import android.util.Log;

import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.transform.*;
import org.apache.commons.math3.complex.Complex;

import nl.delft.tu.iot.seminar.cyclerr.app.sensor.AccelerationSensorValue;
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.AccelerationDataReceiver;
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValue;
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValueReceiver;

public class FourierCadence implements SensorValueReceiver {

    private static final String TAG = FourierCadence.class.getSimpleName();
    private List<SensorValue> accelerationData;
    private int count = 0;
    private double cadence = 0;

    public FourierCadence() {
        accelerationData = new ArrayList<>();
    }


    @Override
    public void onSensorValueReceived(@NotNull SensorValue sensorValue) {
        accelerationData.add(sensorValue);
        count++;
        if(count >= 500) {
            cadence = getCadence();
            Log.d(TAG, "Frequency detected : " + cadence);
            accelerationData.clear();
            count = 0;
        }
    }

    private double getCadence()
    {
        int powerOf2 = (int) FastMath.log(2, accelerationData.size());
        powerOf2 += 1;
        double [] input = new double[(int) Math.pow(2, powerOf2)];

        for(int i=0; i < accelerationData.size(); i++) {
            input[i] = accelerationData.get(i).getScalar();
        }

        //pad with 0s since FastFourierTransformer expects input size to be a power of 2
        for(int i=accelerationData.size(); i < input.length; i++) {
            input[i] = 0;
        }

        double frequencyOfCycling = 0;
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        try {
            Complex[] complx = transformer.transform(input, TransformType.FORWARD);

            int largestComponentIndex = 0;
            double largestComponent = -1;

            for (int i = 0; i < complx.length; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());
                double frequencyIntensity = Math.sqrt((rr * rr) + (ri * ri));
                if(frequencyIntensity < 5) {
                    continue;
                }
                if(frequencyIntensity > largestComponent) {
                    largestComponentIndex = i;
                    largestComponent = Math.sqrt((rr * rr) + (ri * ri));
                }
            }

            double delta_time = (accelerationData.get(accelerationData.size() - 1).getTime()
                - accelerationData.get(0).getTime())/Double.valueOf(1000000000);

            frequencyOfCycling = Double.valueOf(largestComponentIndex) / delta_time;
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return frequencyOfCycling;
    }


}
