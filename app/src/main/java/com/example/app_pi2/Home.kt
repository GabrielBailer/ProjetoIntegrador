package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.app_pi2.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbLocal: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private val interacoesList = mutableListOf<Interacao>()
    private lateinit var adapter: InteracaoAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Room e Firestore
        dbLocal = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").fallbackToDestructiveMigration().build()
        firestore = FirebaseFirestore.getInstance()

        // Configura RecyclerView
        adapter = InteracaoAdapter(interacoesList) { position ->
            val interacao = interacoesList[position]
            Toast.makeText(this, "Clicou em: ${interacao.titulo}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Sincroniza Firestore -> Room -> RecyclerView
        if (userId != null) {
            firestore.collection("usuarios")
                .document(userId!!)
                .collection("interacoes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val firestoreInteracoes = snapshot.documents.mapNotNull { it.toObject(Interacao::class.java) }

                            // Limpa Room e salva tudo novo
                            dbLocal.interacaoDao().clearAll()
                            dbLocal.interacaoDao().insertAll(firestoreInteracoes)

                            // Atualiza lista do RecyclerView no thread principal
                            withContext(Dispatchers.Main) {
                                interacoesList.clear()
                                interacoesList.addAll(firestoreInteracoes)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
        }

        //adicionar nova interação
        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, NovaInteracao::class.java)
            startActivity(intent)
        }
    }
}
