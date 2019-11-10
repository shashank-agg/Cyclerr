package nl.delft.tu.iot.seminar.cyclerr.app.sensor

import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorManager

private val TAG = RotationSensorAdapter::class.java.simpleName

class RotationSensorAdapter(sensorManager: SensorManager) :
    SensorAdapter<RotationSensorValue>(sensorManager, TYPE_ROTATION_VECTOR) {


    override fun processSensorEvent(event: SensorEvent): RotationSensorValue {
        return RotationSensorValue(event)
    }
}

class RotationSensorValue(event: SensorEvent) : SensorValue {

    override val time: Long = event.timestamp
    val rotX: Float = event.values.get(0)
    val rotY: Float = event.values.get(2)
    val rotZ: Float = event.values.get(3)
    override val scalar: Float = event.values.get(4)
    val accuracy: Int = event.accuracy

    override fun asCSVRow(): String {
        return "$time,$rotX,$rotY,$rotZ,$scalar,$accuracy"
    }
}