package com.example.app_pi2

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


import com.example.app_pi2.databinding.ActivityNovaInteracaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class NovaInteracao : AppCompatActivity() {

    private lateinit var binding: ActivityNovaInteracaoBinding
    private var imageUri: Uri? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var dbLocal: AppDatabase


    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaInteracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        dbLocal = AppDatabase.getInstance(applicationContext) // singleton recomendado

        setupImageSpinner()
        setupCategorySpinner()

        binding.btnSalvarInteracao.setOnClickListener {
            val titulo = binding.etNomeInteracao.text.toString()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Preencha o nome da interação!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSalvarInteracao.isEnabled = false
            binding.btnSalvarInteracao.text = "Salvando..."

            val id = UUID.randomUUID().toString()

            if (imageUri != null) {
                val storageRef = storage.reference.child("interacoes/$id.jpg")

                val uploadTask = if (ContentResolver.SCHEME_ANDROID_RESOURCE == imageUri!!.scheme) {
                    val resourceId = imageUri!!.lastPathSegment!!.toInt()
                    val stream = resources.openRawResource(resourceId)
                    storageRef.putStream(stream)
                } else {
                    storageRef.putFile(imageUri!!)
                }

                uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            salvarInteracaoNoFirestore(id, titulo, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnSalvarInteracao.isEnabled = true
                        binding.btnSalvarInteracao.text = "Salvar"
                    }
            } else {
                salvarInteracaoNoFirestore(id, titulo, null)
            }
        }
    }

    private fun setupCategorySpinner() {
        val categorias = listOf("Comidas", "Sentimento", "Cuidados", "Educacional")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }

    private fun setupImageSpinner() {
        val imageNames = listOf(
            "Selecione uma imagem", "cha", "cafe", "fome", "banho", "donut", "felicidade", "pizza",
            "bravo2", "medico", "tristeza", "apatico", "bocejar", "numeros", "banheiro",
            "chateado", "chorando", "piscando", "remedios", "geografia", "tranquilidade",
            "hamburguer", "calculadora", "refrigerante", "papel_higienico"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, imageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerImagem.adapter = adapter

        binding.spinnerImagem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val resourceName = imageNames[position]
                    val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                    if (resourceId != 0) {
                        binding.imgInteracao.setImageResource(resourceId)
                        imageUri = Uri.parse("android.resource://$packageName/$resourceId")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun salvarInteracaoNoFirestore(id: String, titulo: String, imageUrl: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            binding.btnSalvarInteracao.isEnabled = true
            binding.btnSalvarInteracao.text = "Salvar"
            return
        }

        val categoria = binding.spinnerCategoria.selectedItem.toString()

        val map = hashMapOf(
            "id" to id,
            "titulo" to titulo,
            "imagem" to (imageUrl ?: ""),
            "categoria" to categoria
        )

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(id)
            .set(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Interação salva!", Toast.LENGTH_SHORT).show()
                finish() // Volta para a tela Home, que irá atualizar a lista
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar no Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSalvarInteracao.isEnabled = true
                binding.btnSalvarInteracao.text = "Salvar"
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imgInteracao.setImageURI(imageUri)
        }
    }

}
