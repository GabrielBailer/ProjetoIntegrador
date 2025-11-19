package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ Inicializa App Check primeiro
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // 2️⃣ Espera o splash e então verifica usuário
        Handler(Looper.getMainLooper()).postDelayed({
            checkCurrentUser()
        }, SPLASH_TIME_OUT)
    }

    private fun checkCurrentUser() {
        val auth = FirebaseAuth.getInstance()
        val usuarioAtual = auth.currentUser

        val nextActivity = if (usuarioAtual != null) {
            Home::class.java
        } else {
            TelaLogin::class.java
        }

        startActivity(Intent(this@MainActivity, nextActivity))
        finish()
    }
}

