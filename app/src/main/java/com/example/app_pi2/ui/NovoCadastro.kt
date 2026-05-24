package com.example.app_pi2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // Import necessário para o gesto de voltar
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityCriarContaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class NovoCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCriarContaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnAdd.setOnClickListener { addUsuario() }
        binding.btnLimpar.setOnClickListener { limparCampos() }

        // --- Intercepta o gesto de "Voltar" do celular para ir à Tela de Login ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@NovoCadastro, TelaLogin::class.java))
                finish()
            }
        })
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

        binding.btnAdd.isEnabled = false

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    binding.btnAdd.isEnabled = true
                    Toast.makeText(this, "Erro ao obter usuário cadastrado.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val usuario = hashMapOf(
                    "nomeUsuario" to nome,
                    "numeroContato" to contato,
                    "email" to email
                )

                db.collection("usuarios")
                    .document(uid)
                    .set(usuario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Home::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnAdd.isEnabled = true
                        Toast.makeText(
                            this,
                            "Erro ao salvar dados: ${e.localizedMessage ?: "tente novamente"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                binding.btnAdd.isEnabled = true

                val errorMessage = when (exception) {
                    is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
                    else -> "Falha no cadastro: ${exception.localizedMessage ?: "tente novamente"}"
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun limparCampos() {
        binding.etNomeUsuario.text.clear()
        binding.etNumeroContato.text.clear()
        binding.etEmail.text.clear()
        binding.etSenha.text.clear()
    }
}