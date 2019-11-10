package nl.delft.tu.iot.seminar.cyclerr.app

import java.time.Duration
import java.time.Instant

private val TAG = FilteringCadence::class.java.simpleName

class FilteringCadence : AccelerationDataReceiver{

    //parameters
    private val n_filter = 70
    private val n_average = 1000
    private val band_width = 0.08

    private val filter= MovingAverageFirFilter(n_filter)

    private val average = RunningAverageOfLastN(n_average)

    var isBelow=true
    var lastDetectionInNanos =0L


    private var lastMeasuredCadenceTime: Instant = Instant.now()
    private var lastMeasuredCadence: Double = 0.0

    fun getCurrentCadenceByTime(time: Instant): Double {

        val timeSinceLastMeasurement = Duration.between(lastMeasuredCadenceTime, time).toMillis()

        if (timeSinceLastMeasurement > 2000 || lastMeasuredCadence<30) { //Next cadence will be smaller than 30
            return 0.0
        }
        return lastMeasuredCadence
    }

    override fun onAccelerationDataReceived(accelerationData: AccelerationData) {

        val accX = accelerationData.accelerationX.toDouble()
        val accY = accelerationData.accelerationY.toDouble()
        val accZ = accelerationData.accelerationZ.toDouble()

        val magnitude = Math.sqrt(accX*accX+ accY*accY + accZ* accZ)

        val filteredMagnitude = filter.apply(magnitude)
        val avg = average.apply(filteredMagnitude)

        val lowerLimit = avg*(1.0-band_width/2)
        val upperLimit = avg*(1.0+band_width/2)

        if (isBelow && filteredMagnitude > upperLimit){
            isBelow = false
        }

        if (!isBelow && filteredMagnitude < lowerLimit){
            isBelow = true
            newPeriodDetected(accelerationData.timestamp,accelerationData.timestamp-lastDetectionInNanos)
            lastDetectionInNanos = accelerationData.timestamp
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

//        Log.d("DETECTED FREQUENCE", "FREQ:$lastMeasuredCadence")
    }

    inner class MovingAverageFirFilter(val n:Int){

        val values = DoubleArray(n)
        var index = 0;

        fun apply(value:Double):Double{
            values[index] = value
            index = (index+1)%n
            return values.average()
        }
    }

    inner class RunningAverageOfLastN(val n:Int){
        private val values = DoubleArray(n)

        private var sum :Double = 0.0

        var index = 0;

        var nElements =0


        fun apply(value:Double):Double{

            sum = sum - values[index] + value

            values[index] = value
            index = (index+1)%n

            nElements = Math.max(index+1, nElements)

            return sum/nElements
        }
    }
}