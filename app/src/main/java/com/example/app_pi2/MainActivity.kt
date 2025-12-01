package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // AppCheck debug
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // Deep link
        val deepLink = intent?.data
        val oobCode = deepLink?.getQueryParameter("oobCode")
        val mode = deepLink?.getQueryParameter("mode")

        val firebaseLink = intent?.dataString

        Handler(Looper.getMainLooper()).postDelayed({
            checkCurrentUser(oobCode, mode, firebaseLink)
        }, SPLASH_TIME_OUT)
    }

    private fun checkCurrentUser(oobCode: String?, mode: String?, firebaseLink: String?) {
        val auth = FirebaseAuth.getInstance()
        val usuarioAtual = auth.currentUser

        val nextIntent = if (usuarioAtual != null) {
            Intent(this, Home::class.java)
        } else {
            Intent(this, TelaLogin::class.java)
        }

        // Passar dados do deep link para a próxima tela
        if (!oobCode.isNullOrEmpty()) {
            nextIntent.putExtra("oobCode", oobCode)
            nextIntent.putExtra("mode", mode)
            nextIntent.putExtra("link", firebaseLink)
        }

        startActivity(nextIntent)
        finish()
    }
}
