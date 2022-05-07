package com.example.qrscannerapp.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qrscannerapp.databinding.ActivityScannerBinding
import com.example.qrscannerapp.ui.MyApplication
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {

    private lateinit var activityScannerBinding: ActivityScannerBinding
    private var qrScanIntegrator: IntentIntegrator? = null
    private var startTime: String = ""
    private var endTime: String = ""
    private var locationId: String = ""
    private var perMinPrice: Float = 0F
    private var endSession = false
    private var timerTask: TimerTask? = null
    var timer: Timer? = null
    var time = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityScannerBinding = ActivityScannerBinding.inflate(layoutInflater)
        val view = activityScannerBinding.root
        setContentView(view)

        setOnClickListener()
        setupScanner()

        activityScannerBinding.btnEndSession.visibility = View.GONE
        activityScannerBinding.tvTotalAmount.visibility = View.GONE
        activityScannerBinding.tvEndTime.visibility = View.GONE
        endSession = false
        timer = Timer()
    }

    private fun setupScanner() {
        qrScanIntegrator = IntentIntegrator(this)
        qrScanIntegrator?.setOrientationLocked(true)
    }


    private fun setOnClickListener() {
        activityScannerBinding.btnScanNow.setOnClickListener { scanNow() }
        activityScannerBinding.btnEndSession.setOnClickListener { endSession() }
    }

    private fun scanNow() {
        qrScanIntegrator?.initiateScan()
        activityScannerBinding.btnEndSession.visibility = View.VISIBLE
        endSession = false
        startTime = SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(Date())
        activityScannerBinding.tvStartTime.text = "Start Time :$startTime"
        activityScannerBinding.tvTotalAmount.visibility = View.GONE
    }

    private fun endSession() {
        qrScanIntegrator?.initiateScan()
        activityScannerBinding.btnEndSession.visibility = View.GONE
        endSession = true
        timerTask!!.cancel()
    }

    private fun getTimerText(): String {
        val rounded = time.roundToInt()

        val seconds = rounded % 86400 % 3600 % 60
        val minutes = rounded % 86400 % 3600 / 60
        val hours = rounded % 86400 / 3600

        return formatTime(seconds, minutes, hours)
    }

    private fun formatTime(seconds: Int, minutes: Int, hours: Int): String {
        return String.format("%02d", hours) + " : " + String.format(
            "%02d",
            minutes
        ) + " : " + String.format("%02d", seconds);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
            } else {
                // If QRCode contains data.
                try {
                    // Converting the data to json format
                    val response = result.contents
                    val result = response.replace("\\", "")
                    val final: String = result.substring(1, result.length - 1)
                    val obj = JSONObject(final)
                    locationId = obj.getString("location_id")
                    perMinPrice = obj.getDouble("price_per_min").toFloat()
                    timerTask = object : TimerTask() {
                        override fun run() {
                            runOnUiThread {
                                time++
                                activityScannerBinding.tvTimer.text = getTimerText()
                            }
                        }
                    }
                    timer?.scheduleAtFixedRate(timerTask, 0, 1000)

                    activityScannerBinding.tvLocationId.text =
                        "Location ID :" + obj.getString("location_id")
                    activityScannerBinding.tvLocationDetails.text =
                        "Location Details :" + obj.getString("location_details")
                    activityScannerBinding.tvPricePerMin.text =
                        "Price/Min :$perMinPrice"
                    (this.applicationContext as MyApplication).saveLocationId(locationId)

                    if (endSession && (this.applicationContext as MyApplication).getLocationId()
                            ?.contains(locationId) == true
                    ) {
                        timerTask!!.cancel()
                        endTime =
                            SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(Date())
                        activityScannerBinding.tvEndTime.visibility = View.VISIBLE
                        activityScannerBinding.tvEndTime.text = "End Time :$endTime"
                        val format = SimpleDateFormat("HH:mm:ss")
                        var d1: Date? = null
                        var d2: Date? = null
                        try {
                            d1 = format.parse(startTime)
                            d2 = format.parse(endTime)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                        val diff: Long = d2!!.time - d1!!.time
                        val diffMinutes = diff / (60 * 1000) % 60
                        val diffHours = diff / (60 * 60 * 1000)

                        val hrsToMins = diffHours * 60
                        val totalTimeSpent = hrsToMins + diffMinutes
                        val totalAmtToBePaid = totalTimeSpent * perMinPrice
                        activityScannerBinding.tvTotalAmount.visibility = View.VISIBLE
                        activityScannerBinding.tvTotalAmount.text =
                            "Total amount to be paid : $totalAmtToBePaid/- \n Thank you.. \nVisit Again "
                        makeApiCall(locationId, totalTimeSpent.toInt(), endTime)
                    } else if (endSession) {
                        Toast.makeText(this, "QR Code MisMatched", Toast.LENGTH_LONG).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Data not in the expected format. So, whole object as toast message.
                    Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun makeApiCall(
        locationId: String,
        timeSpent: Int,
        endTime: String
    ): ScannerViewModel {
        val viewModel = ViewModelProvider(
            this
        ).get(ScannerViewModel::class.java)
        viewModel.sessionSuccess(locationId, timeSpent, endTime)
        return viewModel
    }

}