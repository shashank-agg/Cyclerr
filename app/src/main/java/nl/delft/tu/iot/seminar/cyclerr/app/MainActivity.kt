package nl.delft.tu.iot.seminar.cyclerr.app

import android.Manifest
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.TextView.BufferType.NORMAL
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {


    private lateinit var toggleButton: ToggleButton
    private lateinit var textView: TextView
    private lateinit var cadenceTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var altitudeTextView: TextView

    private val serviceConnector by lazy { ServiceConnector(this) }

    private val viewModel: MyViewModel by lazy {
        ViewModelProviders.of(this).get(MyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Got a view model $viewModel")

        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)

        toggleButton = findViewById<ToggleButton>(R.id.toggleButton)
        textView = findViewById<TextView>(R.id.textview)
        cadenceTextView = findViewById(R.id.cadenceTextView)
        speedTextView = findViewById(R.id.speedTextView)
        altitudeTextView= findViewById(R.id.altitudeTextView)

        //init with data from view model
        textView.setText("Click to start tracking", NORMAL)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Toggel button changed: $isChecked")

            if (isChecked) {
                serviceConnector.startMeasuring()
                textView.setText("Measuring activated", NORMAL)
            } else {
                textView.setText("Measuring deactivated", NORMAL)
                serviceConnector.stopMeasuring()
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val cadenceValue =
                        intent.getDoubleExtra("CADENCE_VALUE", 0.0)
                    val speedValue =
                        intent.getFloatExtra("SPEED_VALUE", 0.0f)
                    val altitudeValue =
                        intent.getDoubleExtra("ALTITUDE_VALUE", 0.0)
                    cadenceTextView.setText(String.format("CADENCE: %.2f", cadenceValue), NORMAL)
                    speedTextView.setText(String.format("SPEED: %.2f", speedValue), NORMAL)
                    altitudeTextView.setText(String.format("ALTITUDE: %.2f", altitudeValue), NORMAL)
                }
            }, IntentFilter("MEASURING_SERVICE_BROADCAST_INTENT")
        )
    }

    override fun onStart() {
        super.onStart()
        serviceConnector.bindToService()

        var writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        var accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1);
        }
        if(accessFineLocationPermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
        }
    }

    override fun onStop() {
        serviceConnector.unbindFromService()
        super.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}


class ServiceConnector(val context: Context) : ServiceConnection {

    // A reference to the service used to get location updates.
    private var mService: MeasuringService? = null

    // Tracks the bound state of the service.
    private var mBound = false


    // Monitors the state of the connection to the service.
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "onServiceConnected")
        val binder = service as MeasuringService.LocalBinder
        mService = binder.service
        mBound = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "onServiceDisconnected")
        mService = null
        mBound = false
    }

    fun startMeasuring() {
        mService?.startMeasuring()
    }

    fun stopMeasuring() {
        mService?.stopMeasuring()
    }

    fun bindToService() {
        context.bindService(Intent(context, MeasuringService::class.java), this, BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            context.unbindService(this)
            mBound = false
        }
    }
}
