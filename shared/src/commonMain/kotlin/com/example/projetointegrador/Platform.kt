package com.example.projetointegrador

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform