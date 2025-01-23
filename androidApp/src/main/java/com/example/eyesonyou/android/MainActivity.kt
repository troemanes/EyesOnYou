package com.example.eyesonyou.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.FaceDetector
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.eyesonyou.Greeting

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {

                GameScreen()

                }
        }
    }
}


@Composable
fun GameScreen() {
    var gameTime by remember { mutableStateOf(0) }
    var blinkCount by remember { mutableStateOf(0) }
    var isGameActive by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isGameActive = true
        } else {
            Toast.makeText(context, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
        }
    }

    // Otomatik izin kontrolü
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            isGameActive = true
        }
    }

    // Oyun zamanlayıcısı
    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while(isGameActive) {
                delay(1000)
                gameTime++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rakip Kamera Bölümü
        OpponentCamera()

        Spacer(Modifier.height(10.dp))

        // Oyun Durumu Bilgileri
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Geçen Zaman: $gameTime saniye",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Kırpma: $blinkCount",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        // Kullanıcı Kamera Bölümü
        UserCamera(
            onBlinkDetected = { blinkCount++ },
            isGameActive = isGameActive
        )

        // Oyun Kontrol Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { isGameActive = true }) {
                Text("Başlat")
            }
            Button(onClick = { isGameActive = false }) {
                Text("Durdur")
            }
        }
    }
}

@Composable
fun OpponentCamera() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Rakip Bekleniyor...", color = Color.White)
    }
}

@Composable
fun UserCamera(
    onBlinkDetected: () -> Unit,
    isGameActive: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var blinkDetected by remember { mutableStateOf(false) }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            coroutineScope.launch {
                val cameraProvider = ProcessCameraProvider.getInstance(context).await()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView?.surfaceProvider)


                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val faceDetectorOptions = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build()

                val faceDetector = FaceDetection.getClient(faceDetectorOptions)

                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    val leftEyeOpen = face.leftEyeOpenProbability ?: 1f
                                    val rightEyeOpen = face.rightEyeOpenProbability ?: 1f
                                    val eyeClosedThreshold = 0.1f

                                    val currentBlinkDetected =
                                        leftEyeOpen < eyeClosedThreshold ||
                                                rightEyeOpen < eyeClosedThreshold

                                    if (currentBlinkDetected && !blinkDetected) {
                                        onBlinkDetected()
                                        blinkDetected = true
                                    } else if (!currentBlinkDetected) {
                                        blinkDetected = false
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraX", "Kamera başlatma hatası", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        onDispose {
            // Gerekirse temizleme işlemleri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            }
        )
        Text("Kullanıcı Kamerası", color = Color.White)
    }
}
@ExperimentalGetImage
private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onBlinkAndFaceDetected: (Boolean, Boolean) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

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