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
import com.example.app_pi2.utils.AuthManager
import com.example.app_pi2.utils.FalaAutomaticaManager
import java.util.Locale
import androidx.core.content.ContextCompat
import com.example.app_pi2.fragments.CriarSenhaRespFragment
import com.example.app_pi2.fragments.TipoDeFluxo

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbLocal: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private var listenerRegistration: ListenerRegistration? = null
    private val listaOriginal = mutableListOf<Interacao>()
    private lateinit var adapter: InteracaoAdapter
    val userId = AuthManager.getUserId()
    private lateinit var tts: TextToSpeech
    private var interacaoSelecionada: Interacao? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialPaddingTop = binding.root.paddingTop
        val initialPaddingBottom = binding.root.paddingBottom
        val initialPaddingLeft = binding.root.paddingLeft
        val initialPaddingRight = binding.root.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                initialPaddingLeft,
                initialPaddingTop + systemBars.top,
                initialPaddingRight,
                initialPaddingBottom + systemBars.bottom
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
        configurarTTS()
        configurarBusca()
        configurarBotoes()
        verificarCadastroResponsavel()

        supportFragmentManager.setFragmentResultListener(
            "visual-update",
            this
        ) { _, _ ->
            atualizarEstadoBotaoFalar()
        }

        atualizarEstadoBotaoFalar()

        carregarInteracoesLocais()
        observarInteracoesRemotas()
    }

    private fun configurarRecyclerView() {

        adapter = InteracaoAdapter (

            onItemClick = { interacao ->

                val falaAutomatica = FalaAutomaticaManager.isAtivo(this)

                if (falaAutomatica) {
                    val texto = interacao.titulo.trim()
                    tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)

                    // CORREÇÃO AQUI: Agora ele também atualiza o visual (borda)
                    // e mantém a última interação falada em destaque.
                    interacaoSelecionada = interacao
                    adapter.idSelecionado = interacao.id
                    adapter.notifyDataSetChanged()
                    atualizarEstadoBotaoFalar()

                } else {
                    // Lógica para selecionar ou desmarcar a interação clicada
                    if (interacaoSelecionada?.id == interacao.id) {
                        interacaoSelecionada = null
                        adapter.idSelecionado = null
                    } else {
                        interacaoSelecionada = interacao
                        adapter.idSelecionado = interacao.id
                    }

                    adapter.notifyDataSetChanged() // Atualiza a tela para mostrar a borda
                    atualizarEstadoBotaoFalar()
                }
            },

            onLongClick = { interacao ->

                if (!ModoResponsavelManager.isAtivo(this)) {
                    return@InteracaoAdapter
                }

                InteracaoDialogFragment
                    .newInstance(interacao.id)
                    .show(supportFragmentManager, "editar")
            }
        )

        val spanCount = if (resources.getBoolean(R.bool.isTablet)) 3 else 2

        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun configurarTTS() {

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder()
                    .setLanguage("pt")
                    .setRegion("BR")
                    .build()

                val resultado = tts.setLanguage(locale)

                if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Idioma não suportado.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Erro ao iniciar TTS.", Toast.LENGTH_SHORT).show()
            }
        }
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
            if (supportFragmentManager.findFragmentByTag("perfil") == null) {
                PerfilDialogFragment().show(supportFragmentManager, "perfil")
            }
        }

        binding.btnConfiguracoes.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("config") == null) {
                ConfiguracoesDialogFragment().show(supportFragmentManager, "config")
            }
        }

        binding.btnFalar.setOnClickListener {
            if (interacaoSelecionada == null) {
                if (ModoResponsavelManager.isAtivo(this)) {
                    startActivity(Intent(this, NovaInteracao::class.java))
                } else {
                    tts.speak(
                        "Ative modo de responsável para criar interações",
                        TextToSpeech.QUEUE_FLUSH, null, null
                    )
                }
            } else {
                val texto = interacaoSelecionada?.titulo?.trim().orEmpty()
                tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)

                interacaoSelecionada = null
                adapter.idSelecionado = null
                adapter.notifyDataSetChanged()
                atualizarEstadoBotaoFalar()
            }
        }

        binding.btnEditar.setOnClickListener {
            if (!ModoResponsavelManager.isAtivo(this)) {
                Toast.makeText(this, "Ative o modo responsável", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val interacao = interacaoSelecionada

            if (interacao != null) {
                InteracaoDialogFragment
                    .newInstance(interacao.id)
                    .show(supportFragmentManager, "editar")
            } else {
                Toast.makeText(this, "Selecione uma interação", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDeletar.setOnClickListener {
            interacaoSelecionada = null
            adapter.idSelecionado = null
            adapter.notifyDataSetChanged()
            atualizarEstadoBotaoFalar()
        }
    }

    private fun atualizarEstadoBotaoFalar() {
        val modoResponsavelAtivo = ModoResponsavelManager.isAtivo(this)
        val modoFala = FalaAutomaticaManager.isAtivo(this)

        val visibilidadeOn = View.VISIBLE
        val visibilidadeOff = View.GONE

        if(modoResponsavelAtivo && modoFala){
            binding.btnFalar.visibility = visibilidadeOn
            binding.btnEditar.visibility = visibilidadeOff
            binding.btnDeletar.visibility = visibilidadeOff
        } else {
            if(modoResponsavelAtivo && !modoFala){
                binding.btnFalar.visibility = visibilidadeOn
                binding.btnEditar.visibility = visibilidadeOn
                binding.btnDeletar.visibility = visibilidadeOn
            } else {
                if(!modoResponsavelAtivo && modoFala){
                    binding.btnFalar.visibility = visibilidadeOff
                    binding.btnEditar.visibility = visibilidadeOff
                    binding.btnDeletar.visibility = visibilidadeOff
                }else{
                    binding.btnFalar.visibility = visibilidadeOn
                    binding.btnEditar.visibility = visibilidadeOff
                    binding.btnDeletar.visibility = visibilidadeOn
                    binding.btnFalar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
                    binding.btnFalar.setTextColor(ContextCompat.getColor(this, R.color.on_primary))
                    binding.btnFalar.iconTint = ContextCompat.getColorStateList(this, R.color.on_primary)
                    binding.btnFalar.text = "Falar"
                }
            }
        }

        if (interacaoSelecionada != null) {
            binding.btnFalar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
            binding.btnFalar.setTextColor(ContextCompat.getColor(this, R.color.on_primary))
            binding.btnFalar.iconTint = ContextCompat.getColorStateList(this, R.color.on_primary)
            binding.btnFalar.text = "Falar"

            if (modoResponsavelAtivo) {
                binding.btnEditar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.on_primary)
                binding.btnEditar.iconTint = ContextCompat.getColorStateList(this, R.color.primary)

                binding.btnDeletar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.on_primary)
                binding.btnDeletar.iconTint = ContextCompat.getColorStateList(this, R.color.primary)
            } else {
                binding.btnEditar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.bg_secondary)
                binding.btnEditar.iconTint = ContextCompat.getColorStateList(this, R.color.text_secondary)

                binding.btnDeletar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.on_primary)
                binding.btnDeletar.iconTint = ContextCompat.getColorStateList(this, R.color.primary)
            }

        } else {
            if (modoResponsavelAtivo) {
                binding.btnFalar.setTextColor(ContextCompat.getColor(this, R.color.primary))
                binding.btnFalar.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.on_primary)
                binding.btnFalar.text = "Criar interação"
                binding.btnFalar.iconTint = ContextCompat.getColorStateList(this, R.color.primary)

                binding.btnEditar.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.bg_secondary)
                binding.btnEditar.iconTint =
                    ContextCompat.getColorStateList(this, R.color.text_secondary)

                binding.btnDeletar.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.bg_secondary)
                binding.btnDeletar.iconTint =
                    ContextCompat.getColorStateList(this, R.color.text_secondary)
            } else {
                binding.btnDeletar.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.bg_secondary)
                binding.btnDeletar.iconTint =
                    ContextCompat.getColorStateList(this, R.color.text_secondary)
            }
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

                    dbLocal.interacaoDao().upsertAll(lista)

                    withContext(Dispatchers.Main) {
                        atualizarLista(lista)
                    }
                }
            }
    }

    private fun atualizarLista(lista: List<Interacao>) {
        listaOriginal.clear()
        listaOriginal.addAll(lista)

        val queryAtual = binding.searchView.query?.toString()
        filtrarInteracoes(queryAtual)
    }

    private fun filtrarInteracoes(texto: String?) {
        val resultado = if (texto.isNullOrBlank()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.titulo.contains(texto, ignoreCase = true)
            }
        }
        adapter.submitList(resultado.toList())
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
        atualizarEstadoBotaoFalar()

        if (listaOriginal.isNotEmpty()) {
            val queryAtual = binding.searchView.query?.toString()
            filtrarInteracoes(queryAtual)
        }
    }

    private fun verificarCadastroResponsavel() {
        val uid = userId ?: return

        firestore.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val maiorDeIdade = doc.getBoolean("maiorDeIdade") ?: false
                val responsavelConfigurado = doc.getBoolean("responsavelConfigurado") ?: false

                if (maiorDeIdade && !responsavelConfigurado) {
                    cadSenhaResp()
                }
            }
    }

    private fun cadSenhaResp() {
        if (supportFragmentManager.findFragmentByTag("CriarSenhaResp") == null) {
            val fragment = CriarSenhaRespFragment()
            val bundle = Bundle()
            bundle.putString("flow", TipoDeFluxo.CADASTRO.name)
            fragment.arguments = bundle
            fragment.isCancelable = false
            fragment.show(supportFragmentManager, "CriarSenhaResp")
        }
    }
}