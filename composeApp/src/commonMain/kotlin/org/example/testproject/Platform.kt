package org.example.testproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun isAndroid(): Boolean