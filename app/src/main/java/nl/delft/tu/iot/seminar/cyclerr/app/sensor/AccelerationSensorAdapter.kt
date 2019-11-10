package nl.delft.tu.iot.seminar.cyclerr.app.sensor

import android.hardware.Sensor.TYPE_LINEAR_ACCELERATION
import android.hardware.SensorEvent
import android.hardware.SensorManager


class AccelerationSensorAdapter(sensorManager: SensorManager) : SensorAdapter<AccelerationSensorValue>(sensorManager, TYPE_LINEAR_ACCELERATION) {

    override fun processSensorEvent(event: SensorEvent): AccelerationSensorValue {
        return AccelerationSensorValue(event);
    }
}

class AccelerationSensorValue(event: SensorEvent):SensorValue {

    override val time: Long = event.timestamp
    val accX: Float = event.values.get(0)
    val accY: Float = event.values.get(1)
    val accZ: Float = event.values.get(2)
    val accuracy: Int = event.accuracy

    override val scalar: Float
        get() = Math.sqrt( (accX*accX+ accY*accY + accZ* accZ).toDouble()).toFloat()

    override fun asCSVRow(): String {
        return "$time,$accX,$accY,$accZ,$accuracy\n"
    }
}

interface AccelerationDataReceiver {

    fun onAccelerationDataReceived(accelerationSensorValue: AccelerationSensorValue)
}