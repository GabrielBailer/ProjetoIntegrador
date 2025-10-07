package com.example.app_pi2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.app_pi2.databinding.ActivityNovaInteracaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

            val id = UUID.randomUUID().toString()

            if (imageUri != null) {
                val storageRef = storage.reference.child("interacoes/$id.jpg")
                storageRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            salvarInteracao(id, titulo, descricao, uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao enviar imagem!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                salvarInteracao(id, titulo, descricao, null)
            }
        }
    }

    private fun salvarInteracao(id: String, titulo: String, descricao: String, imageUrl: String?) {
        val interacao = Interacao(
            id = id,
            titulo = titulo,
            descricao = descricao,
            imagem = imageUrl
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // Salvar localmente no Room
            dbLocal.interacaoDao().insert(interacao)

            // Salvar no Firestore
            val map = hashMapOf(
                "id" to interacao.id,
                "titulo" to interacao.titulo,
                "descricao" to interacao.descricao,
                "imagem" to (interacao.imagem ?: "")
            )

            firestore.collection("usuarios")
                .document(userId)
                .collection("interacoes")
                .document(id)
                .set(map)
                .addOnSuccessListener {
                    Toast.makeText(this@NovaInteracao, "Interação salva!", Toast.LENGTH_SHORT).show()
                    finish() // volta para a tela principal
                }
                .addOnFailureListener {
                    Toast.makeText(this@NovaInteracao, "Erro ao salvar no Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
                }
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
