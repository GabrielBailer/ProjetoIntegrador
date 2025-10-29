package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        binding.btnAcessar.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val senha = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, Home::class.java))
                        finish(
                        )
                    } else {
                        Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.textEsqueciSenha.setOnClickListener {
            val intent = Intent(this, RecuperarSenha::class.java)
            startActivity(intent)
        }

        binding.btnCriarConta.setOnClickListener {
            val intent = Intent(this, NovoCadastro::class.java)
            startActivity(intent)
        }
    }
}