package com.example.app_pi2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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


        binding.btnSelecionarImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.btnSalvarInteracao.setOnClickListener {
            val titulo = binding.etNomeInteracao.text.toString()
            val descricao = binding.etDescricaoInteracao.text.toString()

            if (titulo.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Mostra um feedback de progresso (opcional, mas recomendado)
            binding.btnSalvarInteracao.isEnabled = false
            binding.btnSalvarInteracao.text = "Salvando..."

            val id = UUID.randomUUID().toString()

            if (imageUri != null) {
                val storageRef = storage.reference.child("interacoes/$id.jpg")
                storageRef.putFile(imageUri!!).addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            salvarInteracaoNoFirestore(id, titulo, descricao, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnSalvarInteracao.isEnabled = true
                        binding.btnSalvarInteracao.text = "Salvar"
                    }
            } else {
                salvarInteracaoNoFirestore(id, titulo, descricao, null)
            }
        }
    }


    private fun salvarInteracaoNoFirestore(id: String, titulo: String, descricao: String, imageUrl: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            binding.btnSalvarInteracao.isEnabled = true
            binding.btnSalvarInteracao.text = "Salvar"
            return
        }

        val map = hashMapOf(
            "id" to id,
            "titulo" to titulo,
            "descricao" to descricao,
            "imagem" to (imageUrl ?: "")
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

