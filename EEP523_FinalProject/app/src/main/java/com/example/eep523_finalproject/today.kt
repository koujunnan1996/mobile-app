package com.example.eep523_finalproject


import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.NumberFormat
import kotlin.math.sqrt

class today : AppCompatActivity(), SensorEventListener {
    private  lateinit var mSeriesXaccel: LineGraphSeries<DataPoint>
    private lateinit var mSeriesYaccel: LineGraphSeries<DataPoint>
    private lateinit var mSeriesZaccel: LineGraphSeries<DataPoint>
    private var mean:Double? = 0.0
    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    private lateinit var mSensorG: Sensor
    private var mediaPlayer: MediaPlayer? = null
    private var shake:Boolean? = false
    internal var slidingWindow = DoubleArray(5)
    private var pre_time = System.currentTimeMillis()

    val linear_acceleration: Array<Float> = arrayOf(0.0f,0.0f,0.0f)
    private var thresHold:Int? = 12
    private var average:Double? = 9.83


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER)
            return

        /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
       */

        linear_acceleration[0] = event.values[0]
        linear_acceleration[1] = event.values[1]
        linear_acceleration[2] = event.values[2]
        var cal = event.values[0].toDouble() + event.values[1].toDouble() + event.values[2].toDouble()

        //println(Arrays.toString(linear_acceleration))
        //println(slidingWindow)
        var magnitude = sqrt(linear_acceleration[0]*linear_acceleration[0] + linear_acceleration[1]*linear_acceleration[1]+ linear_acceleration[2] * linear_acceleration[2])
        var value = magnitude!! - average!!

        val xval = System.currentTimeMillis()/1000.toDouble()//graphLastXValue += 0.1
        mSeriesXaccel!!.appendData(DataPoint(xval, linear_acceleration[0].toDouble()), true, 50)
        mSeriesYaccel!!.appendData(DataPoint(xval, linear_acceleration[1].toDouble()), true, 50)
        mSeriesZaccel!!.appendData(DataPoint(xval, value.toDouble()), true, 50)

        //println(value)

        var cur_time = System.currentTimeMillis()
        if (value > thresHold!!){

            if (cur_time - pre_time > 2000){
                Log.e("shake","shake detected")
                shake = true
                pre_time = cur_time
                Toast.makeText(applicationContext,"shake detected",Toast.LENGTH_SHORT).show()
                mediaPlayer?.start()
            }
        }
    }
}
