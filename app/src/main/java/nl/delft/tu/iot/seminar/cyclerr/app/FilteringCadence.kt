package nl.delft.tu.iot.seminar.cyclerr.app

import android.util.Log

private val TAG = FilteringCadence::class.java.simpleName

class FilteringCadence : AccelerationDataReceiver{

    private val filter= MovingAverageFirFilter(70)
    private val filterCadence= MovingAverageFirFilter(5)

    private val average = RunningAverageOfLastN(4000)


    var isBelow=true
    var lastDetection =0L


    override fun onAccelerationDataReceived(accelerationData: AccelerationData) {

        val accX = accelerationData.accelerationX.toDouble()
        val accY = accelerationData.accelerationY.toDouble()
        val accZ = accelerationData.accelerationZ.toDouble()

        val magnitude = Math.sqrt(accX*accX+ accY*accY + accZ* accZ)
        val filteredMagnitude = filter.apply(magnitude)
        val avg = average.apply(filteredMagnitude)

        val lowerLimit = avg*0.95
        val upperLimit = avg*1.05

        if (isBelow && filteredMagnitude > upperLimit){
            isBelow = false
        }

        if (!isBelow && filteredMagnitude < lowerLimit){
            isBelow = true
            newPeriodDetected(accelerationData.timestamp-lastDetection)
            lastDetection = accelerationData.timestamp


        }

        Log.d(TAG, "$magnitude\t$filteredMagnitude\t$avg")
    }

    private fun newPeriodDetected(period: Long) {
        val freq = 1e9 / period
        val filteredFreq = filterCadence.apply(freq)
        Log.d("DETECTED FREQUENCE", "FREQ:$freq \t FLATTENED FREQ: $filteredFreq")
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
        val values = DoubleArray(n)

        var sum :Double = 0.0

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