enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
     //   maven { url = uri("https://webrtc.org/maven") } // WebRTC için gerekli

    }
}

rootProject.name = "EyesOnYou"
include(":androidApp")
include(":shared")