plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
//    id("com.google.gms.google-services") // Firebase Plugin doğru format

}

android {
    namespace = "com.example.eyesonyou.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.eyesonyou.android"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    implementation(libs.vision.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    debugImplementation(libs.compose.ui.tooling)
    implementation (libs.google.services) // Firebase Plugin

    // Firebase
    implementation(libs.firebase.bom) // BOM ile sürüm yönetimi
  //  implementation(libs.firebase.auth.ktx)
  //  implementation(libs.firebase.firestore.ktx)
      // WebRTC
  //     implementation(libs.google.webrtc)
       // ML Kit Face Detection
     //  implementation(libs.face.detection)
       // CameraX


    implementation(libs.androidx.camera.camera2)
       implementation(libs.androidx.camera.lifecycle)
       implementation(libs.camera.view)
    //implementation ("org.webrtc:google-webrtc:1.0.32006")

    // ML Kit Face Detection kütüphanesi
    implementation ("com.google.mlkit:face-detection:16.1.7")
   // implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")


}