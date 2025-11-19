package com.example.app_pi2

import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.app_pi2.databinding.ActivityHomeBinding
import com.example.app_pi2.ui.seguranca.CriarSenhaRespFragment
import com.example.app_pi2.ui.seguranca.SolicitarSenhaRespFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        dbLocal = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()

        firestore = FirebaseFirestore.getInstance()

        adapter = InteracaoAdapter(interacoesList) { position ->
            val interacao = interacoesList[position]
            val dialog = InteracaoDialogFragment.newInstance(interacao.titulo, interacao.imagem)
            dialog.show(supportFragmentManager, "InteracaoDialog")
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 🔄 Sincronização Firestore → Room → RecyclerView
        if (userId != null) {
            firestore.collection("usuarios")
                .document(userId!!)
                .collection("interacoes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val firestoreInteracoes = snapshot.documents
                                .mapNotNull { it.toObject(Interacao::class.java) }

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

        // ➕ Botão Adicionar Interação
        binding.btnAdicionar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val quantidade = dbLocal.interacaoDao().countInteracoes()

                withContext(Dispatchers.Main) {
                    if (quantidade == 0) {
                        // Nenhuma interação → iniciar fluxo do responsável
                        val cadastroEmailDialog = CadastroEmailRespFragment()
                        cadastroEmailDialog.show(supportFragmentManager, "CadastroEmailResp")
                    } else {
                        // Já existem interações → pedir senha cadastrada
                        val solicitarSenhaDialog = SolicitarSenhaRespFragment()
                        solicitarSenhaDialog.show(supportFragmentManager, "SolicitarSenhaResp")
                    }
                }
            }
        }

        // ⚙️ Botão Configurações
        binding.btnConfiguracoes.setOnClickListener {
            startActivity(Intent(this, Configuracoes::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        val emailLink = intent.data?.toString() ?: return
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("responsavel_email", null) ?: return

        val auth = FirebaseAuth.getInstance()

        if (auth.isSignInWithEmailLink(emailLink)) {
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mostrarMensagem("Link validado! Cadastre a senha do responsável.")
                        // Abrir CriarSenhaRespFragment direto
                        val criarSenhaDialog = CriarSenhaRespFragment()
                        criarSenhaDialog.show(supportFragmentManager, "CriarSenhaResp")
                    } else {
                        mostrarMensagem("Erro ao validar link: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
