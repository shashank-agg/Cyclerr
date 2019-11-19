package nl.delft.tu.iot.seminar.cyclerr.app.csv

import android.content.Context
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import nl.delft.tu.iot.seminar.cyclerr.app.MeasurementProcessor
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValue
import nl.delft.tu.iot.seminar.cyclerr.app.sensor.SensorValueReceiver
import java.io.File
import java.io.OutputStreamWriter
import java.time.Instant

class CsvFileLogger(private val tag: String) : SensorValueReceiver, MeasurementProcessor {

    private fun newWriter(): OutputStreamWriter {
        val f =
            File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS),
                "$tag-${Instant.now()}.csv"
            )
        f.createNewFile()
        return f.writer()
    }

    private var csvFileWriter: OutputStreamWriter? = null;

    override fun onMeasurementStart(context: Context) {
        csvFileWriter = newWriter()
    }

    override fun onSensorValueReceived(sensorValue: SensorValue) {
        csvFileWriter?.append(sensorValue.asCSVRow())?.append('\n')
    }

    override fun onMeasurementEnd() {
        csvFileWriter?.close()
    }
}

