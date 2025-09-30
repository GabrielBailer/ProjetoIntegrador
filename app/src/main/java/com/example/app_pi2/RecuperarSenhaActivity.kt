package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnVoltar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        editEmail = findViewById(R.id.edit_email)
        btnEnviar = findViewById(R.id.btn_enviar)
        btnVoltar = findViewById(R.id.btn_voltar)
        auth = FirebaseAuth.getInstance()

        btnEnviar.setOnClickListener {
            enviarEmailRecuperacao()
        }

        btnVoltar.setOnClickListener {
            voltarParaLogin()
        }
    }

    private fun enviarEmailRecuperacao() {
        val email = editEmail.text.toString().trim()

        if (email.isEmpty()) {
            mostrarMensagem("Por favor, digite seu email")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarMensagem("Digite um email válido")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mostrarMensagem("Email de recuperação enviado para: $email")
                } else {
                    mostrarMensagem("Erro: ${task.exception?.message}")
                }
            }
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, Tela_Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}