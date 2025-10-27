package com.example.app_pi2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityNovaInteracaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NovaInteracao : AppCompatActivity() {

    private lateinit var binding: ActivityNovaInteracaoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dbLocal: AppDatabase

    private var nomeImagem: String? = null // Nome da imagem que será igual ao título digitado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaInteracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        dbLocal = AppDatabase.getInstance(applicationContext)

        setupCategorySpinner()
        setupTituloListener()

        binding.btnSalvarInteracao.setOnClickListener {
            val titulo = binding.etTituloInteracao.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = UUID.randomUUID().toString()
            salvarInteracaoNoFirestore(id, titulo, nomeImagem ?: titulo)
        }
    }

    private fun setupCategorySpinner() {
        val categorias = listOf("Comidas", "Sentimento", "Cuidados", "Educacional")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }

    /**
     * Monitora o campo de título e tenta automaticamente encontrar uma imagem drawable
     * com o mesmo nome do texto digitado.
     */
    private fun setupTituloListener() {
        binding.etTituloInteracao.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val tituloDigitado = text.toString().trim().lowercase(Locale.getDefault())
                if (tituloDigitado.isNotEmpty()) {
                    val resId = resources.getIdentifier(tituloDigitado, "drawable", packageName)
                    if (resId != 0) {
                        binding.imgInteracao.setImageResource(resId)
                        nomeImagem = tituloDigitado // define o nome da imagem igual ao título
                    } else {
                        binding.imgInteracao.setImageResource(R.drawable.ic_launcher_foreground)
                        nomeImagem = null
                    }
                } else {
                    binding.imgInteracao.setImageResource(R.drawable.ic_launcher_foreground)
                    nomeImagem = null
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

        val map = hashMapOf(
            "id" to id,
            "titulo" to titulo,
            "descricao" to categoria,
            "imagem" to imagemNome // salva o nome da imagem local
        )

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
}
