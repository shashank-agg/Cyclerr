package nl.delft.tu.iot.seminar.cyclerr.app.upload

import android.content.Context
import android.os.Handler
import android.os.SystemClock.uptimeMillis
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import nl.delft.tu.iot.seminar.cyclerr.app.MeasurementProcessor
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import java.io.UnsupportedEncodingException as Exception


private val TAG = DataUploader::class.java.simpleName

class DataUploader(context: Context) : Runnable,
    MeasurementProcessor {

    private val TIME_INTERVAL = 5000

    private var handler = Handler()

    private val url = "https://iot.nonnenmacher.dev/data"
    private val queue = Volley.newRequestQueue(context)

    private var tripId: String? = null
    private var index = 0

    private var buffer: MutableList<DataPoint> = mutableListOf()

    override fun run() {
        handler.postAtTime(this, uptimeMillis() + TIME_INTERVAL) //reschedule
        if (buffer.count() > 0) {
            sendBuffer()
            index++
        }
    }

    private fun sendBuffer(isLast: Boolean = false) {
        // Instantiate the RequestQueue.
        val tId = tripId ?: return
        val b = RequestBody(tId, index, isLast, buffer)
        Log.d(TAG,"Sending to API: $b")

        //clear buffer now
        buffer = mutableListOf()

        // Request a string response from the provided URL.
        val request = GsonPostRequest(
            url, b,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
//                Log.d(TAG, "Response is: $response")
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "That didn't work! $error")
            }

        )
        // Add the request to the RequestQueue.
        queue.add(request)
    }


    fun newData(time: Instant, speed: Float, cadence: Double, altitude: Double): String? {
        if (tripId == null) {
            startSending()
        }
        val dataPoint = DataPoint(time, speed, cadence, altitude)
        buffer.add(dataPoint)
        return tripId
//        Log.d(TAG,"New datapoint added: $dataPoint")
    }

    override fun onMeasurementStart(context: Context) {
        startSending()
    }

    private fun startSending() {
        tripId = UUID.randomUUID().toString()
        val b = handler.postAtTime(this, uptimeMillis() + TIME_INTERVAL) //reschedule
    }

    override fun onMeasurementEnd() {
        //stop automatic callbacks
        handler.removeCallbacks(this)

        sendBuffer(true)

        //reset counters
        tripId = null
        index = 0
    }
}


data class RequestBody(
    val tripId: String,
    val index: Int,
    val isLast: Boolean,
    val data: List<DataPoint>
)


class DataPoint(
    time: Instant, @Expose val speed: Float, @Expose val cadence: Double, @Expose val altitude: Double) {

    @Expose
    val timestamp: String = time.toString()

    override fun toString(): String =
        "$timestamp: Speed = $speed, Cadence = $cadence, Altitude = $altitude";
}


class GsonPostRequest(
    url: String,
    private val body: RequestBody,
    private val listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : Request<String>(POST, url, errorListener) {
    private val gson = Gson()

    override fun getBody(): ByteArray {
        return gson.toJson(body).toByteArray()
    }

    override fun getHeaders(): MutableMap<String, String> {
        return mutableMapOf(
            //TODO
            "Authorization" to "Basic ZWl0OndlYXJlYW1hemluZw==",
            "Content-Type" to "application/json"
        )
    }

    override fun deliverResponse(response: String) = listener.onResponse(response)

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        return try {
            val jsonString = String(
                response?.data ?: ByteArray(0),
                Charset.forName(HttpHeaderParser.parseCharset(response?.headers))
            )

            Response.success(
                jsonString,
                HttpHeaderParser.parseCacheHeaders(response)
            )

        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }
}