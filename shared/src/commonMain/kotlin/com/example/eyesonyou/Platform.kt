package com.example.eyesonyou

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform