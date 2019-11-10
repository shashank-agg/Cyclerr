package nl.delft.tu.iot.seminar.cyclerr.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import nl.delft.tu.iot.seminar.cyclerr.app.MeasurementProcessor


abstract class SensorAdapter<T : SensorValue>(val sensorManager: SensorManager, sensorType: Int) :
    SensorEventListener, MeasurementProcessor {

    val TAG = SensorAdapter::class.java.simpleName

    private val sensor: Sensor = sensorManager.getDefaultSensor(sensorType)

    private var sensorValueReceivers = mutableListOf<SensorValueReceiver>()

    fun register(sensorValueReceiver: SensorValueReceiver) {
        this.sensorValueReceivers.add(sensorValueReceiver)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val sensorValue = processSensorEvent(event)
        sensorValueReceivers.forEach { it.onSensorValueReceived(sensorValue) }
    }

    abstract fun processSensorEvent(event: SensorEvent): T


    override fun onMeasurementStart(context: Context) {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onMeasurementEnd() {
        sensorManager.unregisterListener(this, sensor)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed: $accuracy")
    }

}


interface SensorValue {
    fun asCSVRow(): String

    val scalar: Float

    val time : Long
}

interface SensorValueReceiver {

    fun onSensorValueReceived(sensorValue: SensorValue)
}