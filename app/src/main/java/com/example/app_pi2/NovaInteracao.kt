package com.example.app_pi2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
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


    private var nomeImagem: String? = null // Nome da imagem que será igual ao título digitado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaInteracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        dbLocal = AppDatabase.getInstance(applicationContext)

        categorias()
        tituloListener()

        binding.btnSalvarInteracao.setOnClickListener {
            val titulo = binding.etTituloInteracao.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = UUID.randomUUID().toString()
            val imagemParaSalvar = nomeImagemSelecionada ?: "imagem_padrao"
            salvarInteracaoNoFirestore(id, titulo, imagemParaSalvar)
        }

        binding.imgInteracao.setOnClickListener {
            val fragment = SelecionarImagemFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment) // ou container da sua Activity
                .addToBackStack(null)
                .commit()
        }

        binding.imgInteracao.setOnClickListener {
            val fragment = SelecionarImagemFragment()
            fragment.show(supportFragmentManager, "selecionar_imagem")
        }


    }

    private fun categorias() {
        val categorias = listOf("Comidas", "Sentimento", "Cuidados", "Educacional")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as android.widget.TextView).setTextColor(android.graphics.Color.BLACK)
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
                // Não tentamos mais adivinhar a imagem pelo título
                // Apenas mantemos a imagem atual (a que foi escolhida no fragment)
                if (nomeImagemSelecionada != null) {
                    val resId = resources.getIdentifier(nomeImagemSelecionada, "drawable", packageName)
                    if (resId != 0) {
                        binding.imgInteracao.setImageResource(resId)
                    } else {
                        binding.imgInteracao.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                } else {
                    binding.imgInteracao.setImageResource(R.drawable.ic_launcher_foreground)
                }
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

        val imagemLimpa = imagemNome?.substringBeforeLast(".")

        val map = hashMapOf(
            "id" to id,
            "titulo" to titulo,
            "descricao" to categoria,
            "imagem" to imagemLimpa
        )

        val interacao = Interacao(
            id = id,
            titulo = titulo,
            descricao = categoria,
            imagem = imagemLimpa
        )

        lifecycleScope.launch(Dispatchers.IO){
            dbLocal.interacaoDao().insert(interacao)
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(id)
            .set(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Interação salva!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onImagemSelecionada(nomeImagem: String) {
        nomeImagemSelecionada = nomeImagem
        val resId = resources.getIdentifier(nomeImagem, "drawable", packageName)
        binding.imgInteracao.setImageResource(resId)
    }
}
