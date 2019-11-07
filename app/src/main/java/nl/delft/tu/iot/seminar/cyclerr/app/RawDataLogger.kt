package nl.delft.tu.iot.seminar.cyclerr.app

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import java.io.File
import java.io.OutputStreamWriter
import java.time.Instant

private val TAG = RawDataLogger::class.java.simpleName

class RawDataLogger(val sensorManager: SensorManager) : SensorEventListener {

    private val sensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var accelerationDataReceiver: AccelerationDataReceiver? = null

    fun register(accelerationDataReceiver: AccelerationDataReceiver){
        this.accelerationDataReceiver = accelerationDataReceiver;
    }

    fun start() {
        sensorManager.registerListener(this, sensor, SENSOR_DELAY_FASTEST)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed: $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val time = event.timestamp
        val accuracy = event.accuracy
        val accX = event.values.get(0)
        val accY = event.values.get(1)
        val accZ = event.values.get(2)

        val data = AccelerationData(time, accX, accY, accZ, accuracy)

        accelerationDataReceiver?.onAccelerationDataReceived(data)

//        Log.d(TAG, "$time $accX $accY $accZ $accuracy")
    }

    fun stop() {
        sensorManager.unregisterListener(this, sensor)
        accelerationDataReceiver?.finish()
    }
}

data class AccelerationData(
    val timestamp: Long,
    val accelerationX: Float,
    val accelerationY: Float,
    val accelerationZ: Float,
    val accuracy: Int
)

interface AccelerationDataReceiver {

    fun onAccelerationDataReceived(accelerationData: AccelerationData)
    fun finish()
}