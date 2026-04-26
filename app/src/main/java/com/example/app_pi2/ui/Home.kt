package com.example.app_pi2.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.app_pi2.R
import com.example.app_pi2.adapter.InteracaoAdapter
import com.example.app_pi2.data.local.data.AppDatabase
import com.example.app_pi2.data.model.Interacao
import com.example.app_pi2.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.speech.tts.TextToSpeech
import com.example.app_pi2.fragments.ConfiguracoesDialogFragment
import com.example.app_pi2.fragments.PerfilDialogFragment
import com.example.app_pi2.utils.ModoResponsavelManager
import com.example.app_pi2.fragments.InteracaoDialogFragment
import java.util.Locale

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbLocal: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private var listenerRegistration: ListenerRegistration? = null
    private val interacoesList = mutableListOf<Interacao>()
    private val listaOriginal = mutableListOf<Interacao>()
    private lateinit var adapter: InteracaoAdapter
    private lateinit var tts: TextToSpeech



    // 🔥 CONTROLE DE ESTADO
    private var temInteracaoSelecionada = false

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                view.paddingTop + systemBars.top,
                view.paddingRight,
                view.paddingBottom + systemBars.bottom
            )

            insets
        }

        if (userId.isNullOrBlank()) {
            startActivity(Intent(this, TelaLogin::class.java))
            finish()
            return
        }

        dbLocal = AppDatabase.getInstance(applicationContext)
        firestore = FirebaseFirestore.getInstance()

        configurarRecyclerView()
        configurarBusca()
        configurarBotoes()

        // 🔥 Estado inicial
        atualizarEstadoBotaoFalar()

        carregarInteracoesLocais()
        observarInteracoesRemotas()
    }

    private fun configurarRecyclerView() {
        adapter = InteracaoAdapter(interacoesList) { position ->
            val interacao = interacoesList.getOrNull(position) ?: return@InteracaoAdapter

            binding.tvFraseMontada.text = interacao.titulo

            // 🔥 ATUALIZA ESTADO
            temInteracaoSelecionada = true
            atualizarEstadoBotaoFalar()
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val resultado = tts.setLanguage(Locale("pt", "BR"))

                if (resultado == TextToSpeech.LANG_MISSING_DATA ||
                    resultado == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Toast.makeText(this, "Idioma não suportado.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Erro ao iniciar TTS.", Toast.LENGTH_SHORT).show()
            }
        }

        val spanCount = if (resources.getBoolean(R.bool.isTablet)) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        binding.recyclerView.adapter = adapter
    }

    private fun configurarBusca() {
        val searchPlate = binding.searchView.findViewById<View>(
            androidx.appcompat.R.id.search_plate
        )
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarInteracoes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarInteracoes(newText)
                return true
            }
        })
    }

    private fun configurarBotoes() {

        binding.btnPerfil.setOnClickListener {
            PerfilDialogFragment().show(supportFragmentManager, "perfil")
        }

        binding.btnConfiguracoes.setOnClickListener {
            ConfiguracoesDialogFragment().show(supportFragmentManager, "config")
        }

        binding.btnFalar.setOnClickListener {

            if (!temInteracaoSelecionada && ModoResponsavelManager.isAtivo(this)) {
                startActivity(Intent(this, NovaInteracao::class.java))
            } else if(!temInteracaoSelecionada) {
                tts.speak(
                    "Ative modo de responsável para criar interações",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }else{

                val texto = binding.tvFraseMontada.text.toString().trim()

                tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)

                binding.tvFraseMontada.text = ""
                temInteracaoSelecionada = false
                atualizarEstadoBotaoFalar()
            }
        }

        binding.btnEditar.setOnClickListener {
            if (!ModoResponsavelManager.isAtivo(this)) {
                Toast.makeText(this, "Ative o modo responsável", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val texto = binding.tvFraseMontada.text.toString()

            val interacao = interacoesList.find { it.titulo == texto }

            if (interacao != null) {
                InteracaoDialogFragment.newInstance(interacao.id)
                    .show(supportFragmentManager, "editar")
            } else {
                Toast.makeText(this, "Selecione uma interação", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDeletar.setOnClickListener {

            if (!ModoResponsavelManager.isAtivo(this)) {
                Toast.makeText(this, "Ative o modo responsável", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.tvFraseMontada.text = ""
            temInteracaoSelecionada = false
            atualizarEstadoBotaoFalar()

        }
    }

    private fun aplicarModoResponsavel() {

        val ativo = ModoResponsavelManager.isAtivo(this)

        binding.btnEditar.isEnabled = ativo
        binding.btnDeletar.isEnabled = ativo

        // visual (opcional mas MUITO melhor)
        val alpha = if (ativo) 1f else 0.4f

        binding.btnEditar.alpha = alpha
        binding.btnDeletar.alpha = alpha
    }

    private fun atualizarEstadoBotaoFalar() {
        if (temInteracaoSelecionada) {
            binding.btnFalar.setBackgroundColor(Color.parseColor("#A4D8FF"))
            binding.btnFalar.text = "Falar"
        } else {
            binding.btnFalar.setBackgroundColor(Color.parseColor("#A9A9B2"))
            binding.btnFalar.text = "Criar interação"
        }
    }

    private fun carregarInteracoesLocais() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = dbLocal.interacaoDao().getAll()

            withContext(Dispatchers.Main) {
                atualizarLista(lista)
            }
        }
    }

    private fun observarInteracoesRemotas() {
        val uid = userId ?: return

        listenerRegistration = firestore.collection("usuarios")
            .document(uid)
            .collection("interacoes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                lifecycleScope.launch(Dispatchers.IO) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Interacao::class.java)
                    }

                    dbLocal.interacaoDao().clearAll()
                    dbLocal.interacaoDao().insertAll(lista)

                    withContext(Dispatchers.Main) {
                        atualizarLista(lista)
                    }
                }
            }
    }

    private fun atualizarLista(lista: List<Interacao>) {
        listaOriginal.clear()
        listaOriginal.addAll(lista)

        interacoesList.clear()
        interacoesList.addAll(lista)
        adapter.notifyDataSetChanged()

        binding.tvNaoEncontrado.visibility =
            if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filtrarInteracoes(texto: String?) {
        val resultado = if (texto.isNullOrBlank()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.titulo.contains(texto, ignoreCase = true)
            }
        }

        interacoesList.clear()
        interacoesList.addAll(resultado)
        adapter.notifyDataSetChanged()

        binding.tvNaoEncontrado.visibility =
            if (resultado.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {

        listenerRegistration?.remove()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        aplicarModoResponsavel()
    }


}