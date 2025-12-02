package com.example.app_pi2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_pi2.databinding.ActivityNovaInteracaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NovaInteracao : AppCompatActivity(), SelecionarImagemFragment.OnImagemSelecionadaListener {

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
        dbLocal = AppDatabase.getInstance(applicationContext)

        categorias()
        tituloListener()
        aplicarCoresNosCirculos()

        binding.btnSalvarInteracao.setOnClickListener {
            val titulo = binding.etTituloInteracao.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            corSelecionada = when (binding.rgCores.checkedRadioButtonId) {
                binding.corVermelho.id -> "#FF3B30"
                binding.corLaranja.id -> "#FF9500"
                binding.corAmarelo.id -> "#FFCC00"
                binding.corVerde.id -> "#34C759"
                binding.corAzul.id -> "#007AFF"
                binding.corRoxo.id -> "#AF52DE"
                else -> "#FFFFFF"
            }

            val id = UUID.randomUUID().toString()
            val imagemParaSalvar = nomeImagemSelecionada ?: "alm_cafe_1"
            salvarInteracaoNoFirestore(id, titulo, imagemParaSalvar)
        }

        binding.imgInteracao.setOnClickListener {
            val fragment = SelecionarImagemFragment()
            fragment.show(supportFragmentManager, "selecionar_imagem")
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

    private fun categorias() {
        val categorias = listOf("Comidas", "Sentimento", "Cuidados", "Educacional")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(android.graphics.Color.BLACK)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }

    private fun tituloListener() {
        binding.etTituloInteracao.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun salvarInteracaoNoFirestore(id: String, titulo: String, imagemNome: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val imagemLimpa = imagemNome.substringBeforeLast('.')

        val map = hashMapOf(
            "id" to id,
            "titulo" to titulo,
            "descricao" to categoria,
            "imagem" to imagemLimpa,
            "cor" to corSelecionada
        )

        val interacao = Interacao(
            id = id,
            titulo = titulo,
            descricao = categoria,
            imagem = imagemLimpa,
            cor = corSelecionada
        )

        runOnUiThread {
            Toast.makeText(this@NovaInteracao, "Interação salva!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@NovaInteracao, Home::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(id)
            .set(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Interação salva!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onImagemSelecionada(nomeImagem: String) {
        nomeImagemSelecionada = nomeImagem
        val resId = resources.getIdentifier(nomeImagem, "drawable", packageName)
        if (resId != 0) {
            binding.imgInteracao.setImageResource(resId)
        }
    }
}
