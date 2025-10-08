package com.example.app_pi2

import android.content.Intent
import android.os.Bundle

import android.speech.tts.TextToSpeech
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


class Home : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbLocal: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private val interacoesList = mutableListOf<Interacao>()
    private lateinit var adapter: InteracaoAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        dbLocal = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").fallbackToDestructiveMigration().build()
        firestore = FirebaseFirestore.getInstance()

        adapter = InteracaoAdapter(interacoesList) { position ->
            val interacao = interacoesList[position]
            if (!interacao.descricao.isNullOrEmpty()) {
                tts.speak(interacao.descricao, TextToSpeech.QUEUE_FLUSH, null, "")
            } else {
                Toast.makeText(this, "Descrição vazia, não há o que ler.", Toast.LENGTH_SHORT).show()
            }

        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        if (userId != null) {
            firestore.collection("usuarios")
                .document(userId!!)
                .collection("interacoes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val firestoreInteracoes = snapshot.documents.mapNotNull { it.toObject(Interacao::class.java) }
                            dbLocal.interacaoDao().clearAll()
                            dbLocal.interacaoDao().insertAll(firestoreInteracoes)


                            withContext(Dispatchers.Main) {
                                interacoesList.clear()
                                interacoesList.addAll(firestoreInteracoes)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
        }


        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, NovaInteracao::class.java)
            startActivity(intent)
        }


        binding.btnConfiguracoes.setOnClickListener {
        val intent = Intent(this, Configuracoes::class.java)
           startActivity(intent)
       }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Idioma Português (BR) não suportado.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Falha na inicialização do Text-to-Speech.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
