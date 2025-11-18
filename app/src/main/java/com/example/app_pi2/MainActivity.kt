package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val usuarioAtual = auth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({

            if (usuarioAtual != null) {
                val intent = Intent(this@MainActivity, Home::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@MainActivity, TelaLogin::class.java)
                startActivity(intent)
            }
            finish()
        }, SPLASH_TIME_OUT)
    }
}
