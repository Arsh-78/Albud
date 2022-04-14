package com.example.albud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider

import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import android.provider.MediaStore

import android.content.ContentValues
import android.content.Intent
import android.media.Image
import android.os.Build
import android.provider.Telephony
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import com.example.albud.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import java.io.IOException
import java.lang.IllegalStateException
import kotlin.collections.ArrayList



class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    public val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var imageCapture: ImageCapture? = null
    private lateinit var imageAnalyzer: ImageAnalysis



    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }


        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),object :
            ImageCapture.OnImageCapturedCallback()
        {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                 imageAnalyzer = ImageAnalysis.Builder().setImageQueueDepth(STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also {
                        fun onTextFound(s: String) {
                            Log.d(TAG, "We got new text: $s")
                        }
                        it.setAnalyzer(cameraExecutor,TextReaderAnalyzer(::onTextFound))
                    }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        })
        /*imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )*/
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder().build()




            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }


    class TextReaderAnalyzer(
        private val textFoundListener: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            // We will do some interesting things here in just a bit
            val mediaImage = imageProxy.image
            if (mediaImage !=null)
            {
                val image=InputImage.fromMediaImage(mediaImage,imageProxy.imageInfo.rotationDegrees)
                 val result= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).
                 process(image)
                     .addOnSuccessListener {
                             visionText ->
                         processTextFromImage(visionText, imageProxy)
                         imageProxy.close()
                     }
                     .addOnFailureListener{
                         Log.d(TAG, "Failed to process the image")
                         it.printStackTrace()
                         imageProxy.close()
                     }
            }
        }



        private fun processTextFromImage(visionText: Text?, imageProxy: ImageProxy) {
            for (block in visionText!!.textBlocks) {
                // You can access whole block of text using block.text
                for (line in block.lines) {
                    // You can access whole line of text using line.text
                    for (element in line.elements) {
                        textFoundListener(element.text)
                    }
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}