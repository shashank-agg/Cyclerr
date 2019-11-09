package nl.delft.tu.iot.seminar.cyclerr.app

import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import java.io.File
import java.io.OutputStreamWriter
import java.time.Instant

class RawDataFileLogger : AccelerationDataReceiver {

    fun newWriter(): OutputStreamWriter {
        val f =
            File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS),
                "0rawdata_${Instant.now()}.csv"
            )
        f.createNewFile()
        return f.writer()
    }

    private var csvFileWriter: OutputStreamWriter? = null;

    fun startNewMeasurement() {
        csvFileWriter?.close()
        csvFileWriter = newWriter()
    }

    override fun onAccelerationDataReceived(accelerationData: AccelerationData) {

        val time = accelerationData.timestamp
        val accX = accelerationData.accelerationX
        val accY = accelerationData.accelerationY
        val accZ = accelerationData.accelerationZ
        val accuracy = accelerationData.accuracy

        csvFileWriter?.append("$time,$accX,$accY,$accZ,$accuracy\n")
    }

}