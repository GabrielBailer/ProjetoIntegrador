package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityNovoCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class NovoCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityNovoCadastroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnAdd.setOnClickListener { addUsuario() }

        binding.btnLimpar.setOnClickListener { limparCampos() }

        binding.btnVoltar.setOnClickListener {
            val intent = Intent(this, TelaLogin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addUsuario() {
        val nome = binding.etNomeUsuario.text.toString().trim()
        val contato = binding.etNumeroContato.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        if (nome.isEmpty() || contato.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Formato de e-mail inválido"
            binding.etEmail.requestFocus()
            return
        }

        if (senha.length < 6) {
            binding.etSenha.error = "A senha deve ter pelo menos 6 caracteres"
            binding.etSenha.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid ?: return@addOnCompleteListener
                val usuario = Usuario(
                    nomeUsuario = nome,
                    numeroContato = contato,
                    email = email,
                    senha = senha
                )
                db.collection("usuarios").document(uid).set(usuario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, Home::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                val exception = task.exception
                val errorMessage = when (exception) {
                    is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
                    else -> "Falha no cadastro: ${exception?.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun limparCampos() {
        binding.etNomeUsuario.text.clear()
        binding.etNumeroContato.text.clear()
        binding.etEmail.text.clear()
        binding.etSenha.text.clear()
    }
}
