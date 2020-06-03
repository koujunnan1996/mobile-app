package edu.uw.eep523.takepicture

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent

import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.divyanshu.draw.widget.DrawView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.ArrayList



class MainActivity : AppCompatActivity() {
    //private var exchangeBitMap1: Bitmap? = null
    //private var exchangeBitMap2: Bitmap? = null
    private var OriginalBitMap1: Bitmap? = null
    private var OriginalBitMap2: Bitmap? = null
    private var tempBitMap1: Bitmap? = null
    private var tempBitMap2: Bitmap? = null
    private var isLandScape: Boolean = false
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var drawView1: DrawView? = null
    private var drawView2: DrawView? = null
    private var b1_left_x:Int = 0
    private var b1_left_y:Int = 0
    private var b1_width:Int = 0
    private var b1_height:Int = 0
    private var b2_left_x:Int = 0
    private var b2_left_y:Int = 0
    private var b2_width:Int = 0
    private var b2_height:Int = 0

    private var bm1_arr = ArrayList<Int>()
    private var bm2_arr = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        getRuntimePermissions()
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
       isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri1 = it.getParcelable(KEY_IMAGE_URI1)
            imageUri2 = it.getParcelable(KEY_IMAGE_URI2)
        }
        drawView1 = findViewById(R.id.draw_view1)
        drawView2 = findViewById(R.id.draw_view2)

        drawView1?.setStrokeWidth(10.0f)
        drawView1?.setColor(Color.RED)

        // Setup classification trigger so that it classify after every stroke drew
        drawView1?.setOnTouchListener { _, event ->
            // As we have interrupted DrawView's touch event,
            // we first need to pass touch events through to the instance for the drawing to show up
            drawView1?.onTouchEvent(event)
            // Then if user finished a touch event, run classification
            if (event.action == MotionEvent.ACTION_UP) {
            }

            true
        }

        drawView2?.setStrokeWidth(10.0f)
        drawView2?.setColor(Color.RED)

        drawView2?.setOnTouchListener { _, event ->
// As we have interrupted DrawView's touch event,
// we first need to pass touch events through to the instance for the drawing to show up
            drawView2?.onTouchEvent(event)
// Then if user finished a touch event, run an action
            if (event.action == MotionEvent.ACTION_UP) {
//action goes here
            }
            true
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
       with(outState) {
            putParcelable(KEY_IMAGE_URI1, imageUri1)
           putParcelable(KEY_IMAGE_URI2, imageUri2)
        }
    }

     fun startCameraIntentForResult1(view:View) {
        // Clean up last time's image
        imageUri1 = null
        //previewPane1?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "This is New Picture_1")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri1 = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri1)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE1)
        }
    }

    fun startCameraIntentForResult2(view:View) {
        // Clean up last time's image
        imageUri2 = null
        //previewPane2?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "This New Picture_2")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri2 = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri2)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE2)
        }
    }

     fun startChooseImageIntentForResult1(view:View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE1)
    }

    fun startChooseImageIntentForResult2(view:View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE1 && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage1()
            //tryReloadAndDetectInImage2()
        }else if (requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage2()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE1 && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri1 = data!!.data
            tryReloadAndDetectInImage1()
        }else if (requestCode == REQUEST_CHOOSE_IMAGE2 && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri2 = data!!.data
            tryReloadAndDetectInImage2()

        }
    }

    private fun tryReloadAndDetectInImage1() {
        try {
            Log.d("check","def")
            if (imageUri1 == null) {
                return
            }
            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri1)
           } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri1!!)
                ImageDecoder.decodeBitmap(source)
            }

            OriginalBitMap1 = imageBitmap
            facedetection1(imageBitmap)
            drawView1?.background = BitmapDrawable(getResources(),imageBitmap) //mydrawView is the drawView from the layout

        } catch (e: IOException) {
        }
    }

    private fun tryReloadAndDetectInImage2() {
        try {
            Log.d("check","abc")
            if (imageUri2 == null) {
                return
            }
            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri2!!)
                ImageDecoder.decodeBitmap(source)
            }


            OriginalBitMap2 = imageBitmap
            facedetection2(imageBitmap)
            drawView2?.background = BitmapDrawable(getResources(),imageBitmap) //previewPane is the ImageView from the layout
        } catch (e: IOException) {
        }
    }

    fun facedetection1(sentBitmap1: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(sentBitmap1)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        val result = detector.detectInImage(image).addOnSuccessListener { faces ->
            processFaces1(faces,sentBitmap1)
        }.addOnFailureListener { e ->
                Toast.makeText(applicationContext,"There is no face", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFaces1(faces: List<FirebaseVisionFace>,sentBitmap1: Bitmap){
        for (face in faces) {
            val imagebound = face.boundingBox
            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

            if (imagebound != null) {
                Toast.makeText(applicationContext,"image1 uploaded",Toast.LENGTH_SHORT).show()
                b1_left_x = imagebound!!.left
                b1_left_y = imagebound!!.top
                b1_width = imagebound!!.width()
                b1_height = imagebound!!.height()


                //drawView1?.background = BitmapDrawable(getResources(),tempBitMap1

            }


            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
            leftEar?.let {
                val leftEarPos = leftEar.position
            }



            // If classification was enabled:

            var smileProb:Float = 0.0f
            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                val smileProb = face.smilingProbability
            }
            if ( smileProb < 0.7){
                Toast.makeText(applicationContext,"Please smile",Toast.LENGTH_SHORT).show()
            }
            var rightEyeOpenProb:Float = 0.0f
            var leftEyeOpenProb:Float = 0.0f
            if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProb = face.rightEyeOpenProbability
            }
            if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProb = face.leftEyeOpenProbability
            }
            //according to the assignment2 requirements,we need to keep the possibility of open eye to greater than 0.6
            if( leftEyeOpenProb < 0.6 || rightEyeOpenProb < 0.6 )
            {
                Toast.makeText(applicationContext,"Open your eyes and take a new picture",Toast.LENGTH_SHORT).show()
            }

            // If face tracking was enabled:
            if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                val id = face.trackingId
            }
        }
    }

    fun facedetection2(sentBitmap2: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(sentBitmap2)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        val result = detector.detectInImage(image).addOnSuccessListener { faces ->
            processFaces2(faces,sentBitmap2)
        }.addOnFailureListener { e ->
            Toast.makeText(applicationContext,"There is no face", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFaces2(faces: List<FirebaseVisionFace>,sentBitmap2: Bitmap){
        for (face in faces) {
            val imagebound = face.boundingBox
            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

            if (imagebound != null) {
                b2_left_x = imagebound!!.left
                b2_left_y = imagebound!!.top
                b2_width = imagebound!!.width()
                b2_height = imagebound!!.height()
                Toast.makeText(applicationContext,"image2 uploaded",Toast.LENGTH_SHORT).show()
            }

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
            leftEar?.let {
                val leftEarPos = leftEar.position
            }



            // If classification was enabled:

            var smileProb:Float = 0.0f
            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                val smileProb = face.smilingProbability
            }
            if ( smileProb < 0.7){
                Toast.makeText(applicationContext,"Please smile",Toast.LENGTH_SHORT).show()
            }
            var rightEyeOpenProb:Float = 0.0f
            var leftEyeOpenProb:Float = 0.0f
            if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProb = face.rightEyeOpenProbability
            }
            if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProb = face.leftEyeOpenProbability
            }
            //according to the assignment2 requirements,we need to keep the possibility of open eye to greater than 0.7
            if( leftEyeOpenProb < 0.6 || rightEyeOpenProb < 0.6 )
            {
                Toast.makeText(applicationContext,"Open your eyes and take a new picture",Toast.LENGTH_SHORT).show()
            }

            // If face tracking was enabled:
            if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                val id = face.trackingId
            }
        }
    }


    fun blur1(view:View){
        var tempBm1 = Bitmap.createBitmap(OriginalBitMap1!!, b1_left_x, b1_left_y, b1_width, b1_height)
        var width = tempBm1.width
        var height = tempBm1.height
        var resized_bm1 = Bitmap.createScaledBitmap(tempBm1, width * 3, height * 3, true)
        val result1: Bitmap? = bitmapBlur(resized_bm1!!,0.4f,60)
        var width1 = OriginalBitMap1!!.width
        var height1 = OriginalBitMap1!!.height
        var config1 = OriginalBitMap1!!.config
        var tempBoard1 = Bitmap.createBitmap(width1,height1,config1)
        var can1 = Canvas(tempBoard1)
        can1.drawBitmap(OriginalBitMap1!!, Matrix(), null )
        can1.drawBitmap(result1!!, b1_left_x.toFloat(), b1_left_y.toFloat(), null )

        drawView1?.setStrokeWidth(10.0f)
        drawView1?.setColor(Color.RED)
        drawView1?.background = BitmapDrawable(getResources(),tempBoard1)
    }


    fun blur2(view:View){
        var tempBm2 = Bitmap.createBitmap(OriginalBitMap2!!, b2_left_x, b2_left_y, b2_width, b2_height)
        var width = tempBm2.width
        var height = tempBm2.height
        var resized_bm2 = Bitmap.createScaledBitmap(tempBm2, width * 3, height * 3, true)
        val result2: Bitmap? = bitmapBlur(resized_bm2!!,0.4f,60)
        var width2 = OriginalBitMap2!!.width
        var height2 = OriginalBitMap2!!.height
        var config2 = OriginalBitMap2!!.config
        var tempBoard2 = Bitmap.createBitmap(width2,height2,config2)
        var can1 = Canvas(tempBoard2)
        can1.drawBitmap(OriginalBitMap2!!, Matrix(), null )
        can1.drawBitmap(result2!!, b2_left_x.toFloat(), b2_left_y.toFloat(), null )

        drawView2?.setStrokeWidth(10.0f)
        drawView2?.setColor(Color.RED)
        drawView2?.background = BitmapDrawable(getResources(),tempBoard2)
    }

    fun clear1(view:View){
        drawView2?.setStrokeWidth(10.0f)
        drawView2?.setColor(Color.RED)
        drawView1?.background = BitmapDrawable(getResources(),OriginalBitMap1)
    }

    fun clear2(view:View){
        drawView2?.setStrokeWidth(10.0f)
        drawView2?.setColor(Color.RED)
        drawView2?.background = BitmapDrawable(getResources(),OriginalBitMap2)
    }



    fun resizeBm1() {
        var tempBm = Bitmap.createBitmap(OriginalBitMap1!!, b1_left_x, b1_left_y, b1_width, b1_height)
        var resized_bm1 = Bitmap.createScaledBitmap(tempBm, b2_width, b2_height, true)
        for (y in 0 until b2_height) {
            for (x in 0 until b2_width) {
                resized_bm1?.getPixel(x,y)?.let {bm1_arr.add(it)}
            }
        }
    }

    fun resizeBm2() {
        var tempBm = Bitmap.createBitmap(OriginalBitMap2!!, b2_left_x, b2_left_y, b2_width, b2_height)
        var resized_bm2 = Bitmap.createScaledBitmap(tempBm, b1_width, b1_height, true)
        for (y in 0 until b1_height) {
            for (x in 0 until b1_width) {
                resized_bm2?.getPixel(x,y)?.let {bm2_arr.add(it)}
            }
        }
    }
    fun Swap(view: View) {
        var tempBm1 = Bitmap.createBitmap(OriginalBitMap1!!, b1_left_x, b1_left_y, b1_width, b1_height)
        var resized_bm1 = Bitmap.createScaledBitmap(tempBm1, b2_width, b2_height, true)
        var tempBm2 = Bitmap.createBitmap(OriginalBitMap2!!, b2_left_x, b2_left_y, b2_width, b2_height)
        var resized_bm2 = Bitmap.createScaledBitmap(tempBm2, b1_width, b1_height, true)
        if( OriginalBitMap1 != null && OriginalBitMap2 != null) {
            var width1 = OriginalBitMap1!!.width
            var height1 = OriginalBitMap1!!.height
            var config1 = OriginalBitMap1!!.config

            var tempBoard1 = Bitmap.createBitmap(width1,height1,config1)

            var width2 = OriginalBitMap2!!.width
            var height2 = OriginalBitMap2!!.height
            var config2 = OriginalBitMap2!!.config

            var tempBoard2 = Bitmap.createBitmap(width2,height2,config2)

            var result1 = Canvas(tempBoard1)
            var result2 = Canvas(tempBoard2)
            result1.drawBitmap(OriginalBitMap1!!, Matrix(), null )
            result1.drawBitmap(resized_bm2!!, b1_left_x.toFloat(), b1_left_y.toFloat(), null )


            result2.drawBitmap(OriginalBitMap2!!, Matrix(), null )
            result2.drawBitmap(resized_bm1!!, b2_left_x.toFloat(), b2_left_y.toFloat(), null )

            drawView1?.background= BitmapDrawable(getResources(), tempBoard1);
            drawView2?.background= BitmapDrawable(getResources(), tempBoard2);
        }
    }

    fun Swapback(view:View){
        clear1(view)
        clear2(view)
    }
//    fun Swap(view:View){
//        resizeBm1()
//        resizeBm2()
//        var bm = OriginalBitMap1?.copy(Bitmap.Config.RGB_565,true)
//        for (y in 0 until b1_height) {
//            for (x in 0 until b1_width) {
//                bm!!.setPixel(x+b1_left_x,y+b1_left_y, bm2_arr[x+y])
//            }
//        }
//        drawView1?.background = BitmapDrawable(getResources(),bm)
//
//        bm = OriginalBitMap2?.copy(Bitmap.Config.RGB_565,true)
//        for (y in 0 until b2_height) {
//            for (x in 0 until b2_width) {
//                bm!!.setPixel(x+b2_left_x,y+b2_left_y, bm1_arr[x+y])
//            }
//        }
//        drawView2?.background = BitmapDrawable(getResources(),bm)
//    }

    fun bitmapBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }


    companion object {
        private const val KEY_IMAGE_URI1 = "edu.uw.eep523.takepicture.KEY_IMAGE_URI1"
        private const val KEY_IMAGE_URI2 = "edu.uw.eep523.takepicture.KEY_IMAGE_URI2"
        private const val REQUEST_IMAGE_CAPTURE1 = 1001
        private const val REQUEST_IMAGE_CAPTURE2 = 1002
        private const val REQUEST_CHOOSE_IMAGE1 = 1003
        private const val REQUEST_CHOOSE_IMAGE2 = 1004
        private const val PERMISSION_REQUESTS = 1
    }
}
