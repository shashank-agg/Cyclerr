package nl.delft.tu.iot.seminar.cyclerr.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.TextView.BufferType.NORMAL
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {


    private lateinit var toggleButton: ToggleButton
    private lateinit var textView: TextView

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

        //init with data from view model
        textView.setText("${viewModel.currentIndex}", NORMAL)

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
    }

    override fun onStart() {
        super.onStart()
        serviceConnector.bindToService()

        // Check whether this app has write external storage permission or not.
            var writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
// If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1);
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
