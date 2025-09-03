package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFalaComigo.setOnClickListener {
            startActivity(Intent(this, Tela_Login::class.java))
        }

        binding.btnAppAlarm.setOnClickListener {
            startActivity(Intent(this, Tela_Login::class.java))
        }
    }
}