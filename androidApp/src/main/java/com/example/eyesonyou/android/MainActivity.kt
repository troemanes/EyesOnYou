package com.example.eyesonyou.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import androidx.annotation.OptIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
//import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions



class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {

                GameScreen()

                }
        }
    }
}

@ExperimentalGetImage
@Composable
fun GameScreen() {

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView = remember { PreviewView(context) }

        var lastBlinkTime by remember { mutableStateOf(System.currentTimeMillis()) }
        var elapsedTime by remember { mutableStateOf(0L) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                elapsedTime = System.currentTimeMillis() - lastBlinkTime
            }
        }

        val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera(context, lifecycleOwner, previewView) { blinkDetected, faceDetected ->
                    if (faceDetected) {
                        if (blinkDetected) {
                            lastBlinkTime = System.currentTimeMillis()
                        }
                    } else {
                        // Eğer yüz algılanmazsa, süreyi sıfırla
                        lastBlinkTime = System.currentTimeMillis()
                    }
                }
            } else {
                Toast.makeText(context, "Kamera izinleri verilmedi!", Toast.LENGTH_SHORT).show()
            }
        }

        val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            LaunchedEffect(Unit) {
                startCamera(context, lifecycleOwner, previewView) { blinkDetected, faceDetected ->
                    if (faceDetected) {
                        if (blinkDetected) {
                            lastBlinkTime = System.currentTimeMillis()
                        }
                    } else {
                        lastBlinkTime = System.currentTimeMillis()
                    }
                }
            }
        }

        // UI Kodu aynı kalıyor
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Üst panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Son göz kırpmadan bu yana geçen süre: ${elapsedTime / 1000} saniye",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Kamera önizlemesi
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Alt panel (butonlar için)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        // Sol buton işlevi
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(text = "Sol Buton")
                }

                Button(
                    onClick = {
                        // Sağ buton işlevi (kamerayı yeniden başlatma)
                        if (hasCameraPermission) {
                            startCamera(context, lifecycleOwner, previewView) { blinkDetected, faceDetected ->
                                if (faceDetected) {
                                    if (blinkDetected) {
                                        lastBlinkTime = System.currentTimeMillis()
                                    }
                                } else {
                                    lastBlinkTime = System.currentTimeMillis()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(text = "Kamerayı Yeniden Başlat")
                }
            }
        }
    }

    @ExperimentalGetImage
    private fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onBlinkAndFaceDetected: (Boolean, Boolean) -> Unit
    ) {

        ProcessCameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        //cameraProviderFuture.addListener({
         //   val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()

            val faceDetectorOptions = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val faceDetector = FaceDetection.getClient(faceDetectorOptions)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImageProxy(faceDetector, imageProxy, context, onBlinkAndFaceDetected)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)

            preview.setSurfaceProvider(previewView.surfaceProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    @ExperimentalGetImage
    private fun processImageProxy(
        faceDetector: com.google.mlkit.vision.face.FaceDetector,
        imageProxy: ImageProxy,
        context: Context,
        onBlinkAndFaceDetected: (Boolean, Boolean) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f
                        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f
                        val eyeClosedThreshold = 0.1f

                        val blinkDetected = leftEyeOpenProbability < eyeClosedThreshold || rightEyeOpenProbability < eyeClosedThreshold
                        onBlinkAndFaceDetected(blinkDetected, true)
                    } else {
                        // Yüz algılanmadı
                        onBlinkAndFaceDetected(false, false)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

