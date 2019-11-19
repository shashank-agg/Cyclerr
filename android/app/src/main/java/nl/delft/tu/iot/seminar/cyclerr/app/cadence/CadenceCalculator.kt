package nl.delft.tu.iot.seminar.cyclerr.app.cadence

import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValueReceiver
import java.time.Instant

interface CadenceCalculator : SensorValueReceiver {

    fun getCurrentCadenceByTime(time: Instant): Double
}