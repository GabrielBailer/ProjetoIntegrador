package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.app_pi2.databinding.ActivityHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val interacoesList = mutableListOf<Interacao>()
    private lateinit var adapter: InteracaoAdapter

    private lateinit var dbLocal: AppDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbLocal = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()

        firestore = FirebaseFirestore.getInstance()

        adapter = InteracaoAdapter(interacoesList) { position ->
            val interacao = interacoesList[position]
            Toast.makeText(this, "Clicou em: ${interacao.titulo}", Toast.LENGTH_SHORT).show()
        }


        lifecycleScope.launch {
            val local = withContext(Dispatchers.IO) { dbLocal.interacaoDao().getAll() }
            interacoesList.addAll(local)
            adapter.notifyDataSetChanged()
        }

        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, NovaInteracao::class.java)
            startActivity(intent)
        }

        binding.btnAdicionar.setOnClickListener {
            val id = UUID.randomUUID().toString()
            val novaInteracao = Interacao(
                id = id,
                titulo = "Nova Interação",
                descricao = "Descrição da interação..."
            )
            interacoesList.add(novaInteracao)
            adapter.notifyItemInserted(interacoesList.size - 1)

            lifecycleScope.launch(Dispatchers.IO) {
                dbLocal.interacaoDao().insert(novaInteracao)
            }

            firestore.collection("interacoes").document(id).set(novaInteracao)
        }
    }
}
