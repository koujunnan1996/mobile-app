package edu.uw.eep523.sensorrtplot

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


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mSeriesXaccel: LineGraphSeries<DataPoint>
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
    //private var magnitude:Float? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        mSensor = if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Sorry, there are no accelerometers on your device.
            null!!
        }
        mSensorG =  (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE))
        mediaPlayer = MediaPlayer.create(this, R.raw.ring)

        mSensorManager.registerListener(this, mSensor, 40000)

        mSeriesXaccel = LineGraphSeries()
        mSeriesYaccel = LineGraphSeries()
        mSeriesZaccel = LineGraphSeries()
        //initGraphRT(mGraphX,mSeriesXaccel!!)
        //initGraphRT(mGraphY,mSeriesYaccel!!)
        //initGraphRT(mGraphZ,mSeriesZaccel!!)
    }

    fun calibration(){
        var sum:Double = 0.0
        for (i in 1 until slidingWindow.size) {
            sum += slidingWindow[i].toFloat()
        }
        mean = sum/slidingWindow.size
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
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
        addToWindow(cal)
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


    private fun initGraphRT(mGraph: GraphView, mSeriesXaccel :LineGraphSeries<DataPoint>){

        mGraph.getViewport().setXAxisBoundsManual(true)
        //mGraph.getViewport().setMinX(0.0)
        //mGraph.getViewport().setMaxX(4.0)
        mGraph.getViewport().setYAxisBoundsManual(true);


        mGraph.getViewport().setMinY(0.0);
        mGraph.getViewport().setMaxY(10.0);
        mGraph.getGridLabelRenderer().setLabelVerticalWidth(100)

        // first mSeries is a line
        mSeriesXaccel.setDrawDataPoints(false)
        mSeriesXaccel.setDrawBackground(false)
        mGraph.addSeries(mSeriesXaccel)
        setLabelsFormat(mGraph,1,2)

    }

    /* Formatting the plot*/
    fun setLabelsFormat(mGraph:GraphView,maxInt:Int,maxFraction:Int){
        val nf = NumberFormat.getInstance()
        nf.setMaximumFractionDigits(maxFraction)
        nf.setMaximumIntegerDigits(maxInt)

        mGraph.getGridLabelRenderer().setVerticalAxisTitle("Acceleration (m/s^2)")
        mGraph.getGridLabelRenderer().setHorizontalAxisTitle("Time")

        mGraph.getGridLabelRenderer().setLabelFormatter(object : DefaultLabelFormatter(nf,nf) {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    super.formatLabel(value, isValueX)+ "s"
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        })

    }


    fun addToWindow(x: Double) { // Shift everything one to the left
        for (i in 1 until slidingWindow.size) {
            slidingWindow[i - 1] = slidingWindow[i]
        }
        // Add the new data point
        slidingWindow[slidingWindow.size - 1] = x
    }

    fun desiredMode(view:View){
        if(shake == true){
            val intent1 = Intent(this, MainActivity2::class.java)
            startActivity(intent1)
        }
    }

    fun freeMode(view:View){
        if(shake == true){
            val intent2 = Intent(this, MainActivity3::class.java)
            startActivity(intent2)
        }
    }

    override fun onResume() {
        Log.d("tag","onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("tag","onPause")
        mediaPlayer?.stop()
        mSensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager.unregisterListener(this)
    }


}
