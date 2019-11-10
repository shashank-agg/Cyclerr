package nl.delft.tu.iot.seminar.cyclerr.app.cadence

import android.util.Log
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.AccelerationSensorValue
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.AccelerationDataReceiver
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValue
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValueReceiver
import java.time.Duration
import java.time.Instant

private val TAG = FilteringCadence::class.java.simpleName

class FilteringCadence : CadenceCalculator {

    //parameters
    private val n_filter = 70
    private val n_average = 1000
    private val band_width = 0.08

    private val filter= MovingAverageFirFilter(n_filter)

    private val average = MovingAverageFirFilter(n_average)

    var isBelow=true
    var lastDetectionInNanos =0L


    private var lastMeasuredCadenceTime: Instant = Instant.now()
    private var lastMeasuredCadence: Double = 0.0

    override fun getCurrentCadenceByTime(time: Instant): Double {

        val timeSinceLastMeasurement = Duration.between(lastMeasuredCadenceTime, time).toMillis()

        if (timeSinceLastMeasurement > 2000 || lastMeasuredCadence<30) { //Next cadence will be smaller than 30
            return 0.0
        }
        return lastMeasuredCadence
    }

    override fun onSensorValueReceived(sensorValue: SensorValue) {


        val magnitude = sensorValue.scalar

        val filteredMagnitude = filter.apply(magnitude)
        val avg = average.apply(filteredMagnitude)

        val lowerLimit = avg*(1.0-band_width/2)
        val upperLimit = avg*(1.0+band_width/2)

        if (isBelow && filteredMagnitude > upperLimit){
            isBelow = false
        }

        if (!isBelow && filteredMagnitude < lowerLimit){
            isBelow = true
            newPeriodDetected(sensorValue.time,sensorValue.time-lastDetectionInNanos)
            lastDetectionInNanos = sensorValue.time
        }
    }

    private fun newPeriodDetected(timeInNano: Long, period: Long) {
        val freq = 1e9 / period
        val cadence = 60.0 / freq

        if (cadence>200){ //cadence greater then 250 is probably a false positive -> ignore
            return
        }

        //TODO Is exact calculation necessary? But how to sync with GPS?
        // val millisSinceNow = (SystemClock.elapsedRealtimeNanos() - timeInNano)/1000
        // val time= ofEpochMilli(System.currentTimeMillis() + millisSinceNow)
        lastMeasuredCadenceTime = Instant.now();
        lastMeasuredCadence = cadence

        Log.d("TAG", "Cadence measured: $lastMeasuredCadence")
    }

    inner class MovingAverageFirFilter(val n:Int){

        val values = FloatArray(n)
        var index = 0;

        fun apply(value:Float):Float{
            values[index] = value
            index = (index+1)%n
            return values.average().toFloat()
        }
    }
}