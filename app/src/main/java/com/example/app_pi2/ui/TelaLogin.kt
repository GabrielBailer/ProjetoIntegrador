package com.example.app_pi2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.ui.Home
import com.example.app_pi2.ui.NovoCadastro
import com.example.app_pi2.ui.RecuperarSenha
import com.example.app_pi2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class TelaLogin : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        configurarTela()
    }

    private fun configurarTela() {
        binding.btnAcessar.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val senha = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Falha no login: ${e.localizedMessage ?: "Verifique suas credenciais"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        binding.tvCriarContaTexto.setOnClickListener {
            startActivity(Intent(this, NovoCadastro::class.java))
        }

        binding.textEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenha::class.java))
        }
    }
}