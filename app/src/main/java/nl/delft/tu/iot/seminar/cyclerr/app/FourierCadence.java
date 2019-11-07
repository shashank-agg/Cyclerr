package nl.delft.tu.iot.seminar.cyclerr.app;

import android.util.Log;

import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.math3.transform.*;
import org.apache.commons.math3.complex.Complex;

public class FourierCadence implements AccelerationDataReceiver {

    private static final String TAG = FourierCadence.class.getSimpleName();
    private List<AccelerationData> accelerationData;
    private int count = 0;

    public FourierCadence() {
        accelerationData = new ArrayList<>();
    }

    @Override
    public void onAccelerationDataReceived(@NotNull AccelerationData data) {

//        Log.d(TAG, String.format("%d: X=%.3f\t Y=%.3f\t Z=%.3f", data.getTimestamp()/1000000L,
//                data.getAccelerationX(), data.getAccelerationY(), data.getAccelerationZ() ));
        accelerationData.add(data);

    }

    public void finish() {
        double[] fftOutput =  transform();
        double delta_time = (accelerationData.get(accelerationData.size() - 1).getTimestamp()
                - accelerationData.get(0).getTimestamp())/1000000000;
        int maxOutputIndex = 0;
        for (int i = 1; i < accelerationData.size(); i++) {
            if(fftOutput[i] > fftOutput[maxOutputIndex]) {
                maxOutputIndex = i;
            }
        }

        double frequencyOfCycling = Double.valueOf(maxOutputIndex) / delta_time;
        Log.d(TAG, "Frequency detected : " + frequencyOfCycling);
        accelerationData = new ArrayList<>();
    }

    private double[] transform()
    {
//        accelerationData = accelerationData.subList(0, 255)
        int powerOf2 = (int) FastMath.log(2, accelerationData.size());
        powerOf2 += 1;
        double [] input = new double[(int) Math.pow(2, powerOf2)];

        for(int i=0; i < accelerationData.size(); i++) {
            input[i] = accelerationData.get(i).getAccelerationX();
        }

        //pad with 0s since FastFourierTransformer expects input size to be a power of 2
        for(int i=accelerationData.size(); i < input.length; i++) {
            input[i] = 0;
        }

        double[] tempConversion = new double[input.length];

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        try {
            Complex[] complx = transformer.transform(input, TransformType.FORWARD);

            for (int i = 0; i < complx.length; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());

                tempConversion[i] = Math.sqrt((rr * rr) + (ri * ri));
//                System.out.println(i + " : " + tempConversion[i]);
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return tempConversion;
    }

}
