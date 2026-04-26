package com.example.app_pi2.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding
import com.example.app_pi2.ui.Home
import com.example.app_pi2.ui.TelaLogin
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkCurrentUser()
        }, SPLASH_TIME_OUT)
    }

    private fun checkCurrentUser() {
        val auth = FirebaseAuth.getInstance()
        val usuarioAtual = auth.currentUser

        val nextIntent = if (usuarioAtual != null) {
            Intent(this, Home::class.java)
        } else {
            Intent(this, TelaLogin::class.java)
        }

        startActivity(nextIntent)
        finish()
    }
}