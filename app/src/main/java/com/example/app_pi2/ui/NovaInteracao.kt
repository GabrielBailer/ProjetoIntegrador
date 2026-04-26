package com.example.app_pi2.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_pi2.fragments.SelecionarImagemFragment
import com.example.app_pi2.data.local.data.AppDatabase
import com.example.app_pi2.data.model.Interacao
import com.example.app_pi2.databinding.ActivityNovaInteracaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class NovaInteracao : AppCompatActivity() {

    private lateinit var binding: ActivityNovaInteracaoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dbLocal: AppDatabase

    private var nomeImagemSelecionada: String? = null
    private var corSelecionada: String = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaInteracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        dbLocal = AppDatabase.Companion.getInstance(applicationContext)

        aplicarCoresNosCirculos()

        binding.btnSalvarInteracao.setOnClickListener {
            salvarInteracao()
        }

        binding.imgInteracao.setOnClickListener {
            SelecionarImagemFragment()
                .show(supportFragmentManager, "selecionar_imagem")
        }

        supportFragmentManager.setFragmentResultListener(
            "imagem_request",
            this
        ) { _, bundle ->

            val nomeImagem = bundle.getString("imagem") ?: return@setFragmentResultListener

            nomeImagemSelecionada = nomeImagem

            val resId = resources.getIdentifier(nomeImagem, "drawable", packageName)
            if (resId != 0) {
                binding.imgInteracao.setImageResource(resId)
            }
        }
    }

    private fun salvarInteracao() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val titulo = binding.etTituloInteracao.text.toString().trim()
        if (titulo.isEmpty()) {
            binding.etTituloInteracao.error = "Digite um título"
            binding.etTituloInteracao.requestFocus()
            return
        }

        corSelecionada = when (binding.rgCores.checkedRadioButtonId) {
            binding.corVermelho.id -> "#FFC9C9"
            binding.corLaranja.id -> "#FFD8B2"
            binding.corAmarelo.id -> "#FFF3C4"
            binding.corVerde.id -> "#D4F5DD"
            binding.corAzul.id -> "#CFE8FF"
            binding.corRoxo.id -> "#E3D4FF"
            else -> "#FFFFFF"
        }

        val imagem = (nomeImagemSelecionada ?: "alm_cafe_1").substringBeforeLast(".")
        val id = UUID.randomUUID().toString()

        val interacao = Interacao(
            id = id,
            titulo = titulo,
            descricao = "",
            imagem = imagem,
            cor = corSelecionada
        )

        binding.btnSalvarInteracao.isEnabled = false

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(id)
            .set(interacao)
            .addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    dbLocal.interacaoDao().insert(interacao)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@NovaInteracao,
                            "Interação salva!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@NovaInteracao, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
                binding.btnSalvarInteracao.isEnabled = true
                Toast.makeText(
                    this,
                    "Erro ao salvar: ${e.localizedMessage ?: "tente novamente"}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun aplicarCoresNosCirculos() {
        pintar(binding.corVermelho, "#FF3B30")
        pintar(binding.corLaranja, "#FF9500")
        pintar(binding.corAmarelo, "#FFCC00")
        pintar(binding.corVerde, "#34C759")
        pintar(binding.corAzul, "#007AFF")
        pintar(binding.corRoxo, "#AF52DE")
    }

    private fun pintar(view: View, cor: String) {
        val drawable = view.background.mutate() as? GradientDrawable
        drawable?.setColor(Color.parseColor(cor))
    }

    fun onImagemSelecionada(nomeImagem: String) {
        nomeImagemSelecionada = nomeImagem
        val resId = resources.getIdentifier(nomeImagem, "drawable", packageName)
        if (resId != 0) {
            binding.imgInteracao.setImageResource(resId)
        }
    }
}