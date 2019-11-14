package nl.delft.tu.iot.seminar.cyclerr.app.cadence;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValue;

public class FourierCadence implements CadenceCalculator {

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
        if(count >= 512) {
            cadence = getCadence();
            System.out.println("Cadence detected : " + cadence);
            accelerationData.clear();
            count = 0;
        }
    }

    @Override
    public double getCurrentCadenceByTime(@NotNull Instant time) {
        return cadence;
    }

    private double getCadence()
    {
        double [] input = new double[accelerationData.size()];

        for(int i=0; i < accelerationData.size(); i++) {
            input[i] = accelerationData.get(i).getScalar();
        }

        double frequencyOfCycling = 0;
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        try {
            Complex[] complx = transformer.transform(input, TransformType.FORWARD);

            int largestComponentIndex = -1;
            double largestComponent = -1;
            double[] absVals = new double[complx.length];
            for (int i = 2; i < complx.length/2; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());
                double frequencyIntensity = Math.sqrt((rr * rr) + (ri * ri));
                absVals[i] = frequencyIntensity;
                if(frequencyIntensity > largestComponent) {
                    largestComponentIndex = i;
                    largestComponent = frequencyIntensity;
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
