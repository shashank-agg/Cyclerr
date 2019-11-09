package nl.delft.tu.iot.seminar.cyclerr.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.time.Instant


private val TAG = MeasuringService::class.java.simpleName

class MeasuringService : Service() {

    private val mBinder :Binder = LocalBinder()

    private val notificationHolder by lazy { NotificationHolder() }

    private val filteringCadence = FilteringCadence()
    private val fileLogger = RawDataFileLogger()

    private val dataUploader by lazy {DataUploader(this)}

    private val accelerationSensorDataLogger: AccelerationSensorDataLogger by lazy {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rawDataLogger = AccelerationSensorDataLogger(sensorManager)
        rawDataLogger.register(filteringCadence)
        rawDataLogger.register(fileLogger)
        rawDataLogger
    }

    private val speedCalculator: SpeedCalculator by lazy {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val speedCalculator = SpeedCalculator(locationManager)
        speedCalculator.registerListener { time, speed -> dataUploader.newData(time, speed, filteringCadence.getCurrentCadenceByTime(time)) }
        speedCalculator
    }

    private val foregroundController :ForegroundController = ForegroundController()


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        foregroundController.configurationChangeDetected()
    }

    override fun onBind(intent: Intent): IBinder? {
        foregroundController.bindingDetected()
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        foregroundController.bindingDetected()
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        foregroundController.unbindingDetected()
        return true // Ensures onRebind() is called when a client re-binds.
    }



//
//    var mServiceHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "in onCreate()")
//        val handlerThread = HandlerThread(TAG)
//        handlerThread.start()
//        mServiceHandler = Handler(handlerThread.looper)

    }

    override fun onDestroy() {
        Log.i(TAG, "in onDestroy()")
//        mServiceHandler?.removeCallbacksAndMessages(null)
    }


    fun startMeasuring(){
        Log.i(TAG, "Start Measuring")
        startService(Intent(applicationContext, MeasuringService::class.java))
        accelerationSensorDataLogger.start()
        speedCalculator.start(applicationContext)
        dataUploader.newData(Instant.now(), 10.0f, 23.2) // first mock value

        fileLogger.startNewMeasurement()
    }

    fun stopMeasuring(){
        Log.i(TAG, "Stop Measuring")
        stopSelf()
        speedCalculator.stop()
        accelerationSensorDataLogger.stop()
        dataUploader.finishSending();
    }

    inner class ForegroundController{

        var mChangingConfiguration = false;

        fun bindingDetected(){
            Log.i(TAG, "Stopping foreground service")
            stopForeground(true)
            mChangingConfiguration = false
        }

        fun configurationChangeDetected(){
            mChangingConfiguration = true
        }

        fun unbindingDetected(){
            if (!mChangingConfiguration) {
                Log.i(TAG, "Starting foreground service")
                startForeground(notificationHolder.id, notificationHolder.notification)
            }
        }
    }

    /**
     *
     */
    inner class NotificationHolder {

        private val CHANNEL_ID = "channel_cyclerr"

        val notificationManager: NotificationManager

        val id = 12343232

        init {
            notificationManager = this@MeasuringService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Android O requires a Notification Channel.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = this@MeasuringService.getString(R.string.app_name)
                // Create the channel for the notification
                val mChannel =
                    NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

                // Set the Notification Channel for the Notification Manager.
                notificationManager.createNotificationChannel(mChannel)
            }
        }



        val notification :Notification
        get() {
            val text = "Measurign data"

            // The PendingIntent to launch activity.
            val activityPendingIntent = PendingIntent.getActivity(
                this@MeasuringService, 0,
                Intent(this@MeasuringService, MainActivity::class.java), 0
            )

            val builder = NotificationCompat.Builder(this@MeasuringService, CHANNEL_ID)
                .setContentText(text)
                .setContentTitle("Cyclerr active")
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(activityPendingIntent)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())

            // Set the Channel ID for Android O.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID) // Channel ID
            }

            return builder.build()
        }

    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: MeasuringService
            get() = this@MeasuringService
    }
}