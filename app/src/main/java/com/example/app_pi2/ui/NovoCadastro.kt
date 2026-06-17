package com.example.app_pi2.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityCriarContaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.example.app_pi2.utils.InteracoesPadrao
import java.time.LocalDate
import java.time.Period
import java.util.Calendar

class NovoCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCriarContaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etDataNascimento.setOnClickListener { configIdade() }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnAdd.setOnClickListener { addUsuario() }
        binding.btnLimpar.setOnClickListener { limparCampos() }

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
        val dataNascimento = binding.etDataNascimento.text.toString().trim()
        val maiorDeIdade = calcularMaioridade(dataNascimento)

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

                val dataNascimento = binding.etDataNascimento.text.toString().trim()

                if (dataNascimento.isEmpty()) {
                    Toast.makeText(this, "Informe a data de nascimento", Toast.LENGTH_SHORT).show()
                }

                val partes = dataNascimento.split("/")

                val dia = partes[0].toInt()
                val mes = partes[1].toInt()
                val ano = partes[2].toInt()

                val idade = calcularIdade(
                    dia = dia,
                    mes = mes,
                    ano = ano
                )

                val maiorDeIdade = idade >= 18

                val usuario = hashMapOf(
                    "nomeUsuario" to nome,
                    "numeroContato" to contato,
                    "email" to email,
                    "dataNascimento" to dataNascimento,
                    "maiorDeIdade" to maiorDeIdade,
                    "responsavelConfigurado" to false
                )

                db.collection("usuarios")
                    .document(uid)
                    .set(usuario)
                    .addOnSuccessListener {
                        criarInteracoesPadrao(uid)

                        Toast.makeText(
                            this,
                            "Usuário cadastrado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()

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

    private fun criarInteracoesPadrao(uid: String) {

        val ref = db.collection("usuarios")
            .document(uid)
            .collection("interacoes")

        ref.get().addOnSuccessListener { snapshot ->

            if (!snapshot.isEmpty) {
                return@addOnSuccessListener
            }

            val interacoes = InteracoesPadrao.criarLista()

            interacoes.forEach { interacao ->

                ref.document(interacao.id)
                    .set(interacao)
            }
        }
    }

    private fun calcularMaioridade(data: String): Boolean {

        val partes = data.split("/")

        if (partes.size != 3) return false

        val dia = partes[0].toInt()
        val mes = partes[1].toInt()
        val ano = partes[2].toInt()

        val nascimento = java.time.LocalDate.of(
            ano,
            mes,
            dia
        )

        return nascimento.plusYears(18)
            .isBefore(java.time.LocalDate.now()) ||
                nascimento.plusYears(18)
                    .isEqual(java.time.LocalDate.now())
    }

    @SuppressLint("DefaultLocale")
    private fun configIdade(){
        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->

                val data =
                    String.format(
                        "%02d/%02d/%04d",
                        dia,
                        mes + 1,
                        ano
                    )

                binding.etDataNascimento.setText(data)

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun calcularIdade(
        dia: Int,
        mes: Int,
        ano: Int
    ): Int {

        val nascimento = LocalDate.of(
            ano,
            mes + 1,
            dia
        )

        return Period.between(
            nascimento,
            LocalDate.now()
        ).years
    }
}