package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        editEmail = findViewById(R.id.edit_email)
        btnEnviar = findViewById(R.id.btn_enviar)
        btnVoltar = findViewById(R.id.btn_voltar)

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

        if (verificarEmailNoBanco(email)) {
            mostrarMensagem("Email de recuperação enviado para: $email")
        } else {
            mostrarMensagem("Email não encontrado no sistema")
        }
    }

    private fun verificarEmailNoBanco(email: String): Boolean {
        val emailsCadastrados = listOf("admin@email.com", "usuario@email.com")
        return emailsCadastrados.contains(email)
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, Tela_Login::class.java)
        startActivity(intent)
        finish() // Fecha esta tela
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}