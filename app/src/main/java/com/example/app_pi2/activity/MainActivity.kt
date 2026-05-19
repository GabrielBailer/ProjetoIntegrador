package com.example.app_pi2.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding
import com.example.app_pi2.ui.Home
import com.example.app_pi2.ui.TelaLogin
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(SPLASH_TIME_OUT)
            checkCurrentUser()
        }
    }

    private fun checkCurrentUser() {

        val nextIntent = if (auth.currentUser != null) {
            Intent(this, Home::class.java)
        } else {
            Intent(this, TelaLogin::class.java)
        }

        startActivity(nextIntent)
        finish()
    }
}