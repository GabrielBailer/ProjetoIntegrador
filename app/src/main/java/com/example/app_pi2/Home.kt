package com.example.app_pi2

import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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

        val spanCount = if (resources.getBoolean(R.bool.isTablet)) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
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
                        val cadastroEmailDialog = CadastroEmailRespFragment()
                        cadastroEmailDialog.show(supportFragmentManager, "CadastroEmailResp")
                    } else {
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

        // ⬇️ 1️⃣ Tratamento do deep link vindo da MainActivity
        tratarDeepLink()
    }

    private fun tratarDeepLink() {
        val oobCode = intent.getStringExtra("oobCode")
        val mode = intent.getStringExtra("mode")

        if (oobCode.isNullOrEmpty() || mode.isNullOrEmpty()) return

        // 🔥 Evita que o deep link seja processado novamente caso a Home seja recriada
        intent.removeExtra("oobCode")
        intent.removeExtra("mode")

        when (mode) {

            "verifyEmail" -> {
                // email verificado → abrir fragment de solicitar senha
                val frag = SolicitarSenhaRespFragment()
                frag.show(supportFragmentManager, "SolicitarSenhaResp")
            }

            "signIn" -> {
                // email link para login → criar senha direto
                val frag = CriarSenhaRespFragment()
                frag.show(supportFragmentManager, "CriarSenhaResp")
            }

            "resetPassword" -> {
                // redefinição de senha → mesma tela de criar senha
                val frag = CriarSenhaRespFragment()
                frag.show(supportFragmentManager, "CriarSenhaResp")
            }

            else -> {
                mostrarMensagem("Modo desconhecido: $mode")
            }
        }
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
