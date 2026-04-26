package com.example.app_pi2.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.app_pi2.data.local.data.AppDatabase
import com.example.app_pi2.data.model.Interacao
import com.example.app_pi2.databinding.FragmentInteracaoDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InteracaoDialogFragment : DialogFragment() {

    private var _binding: FragmentInteracaoDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var dbLocal: AppDatabase

    private var interacaoId: String? = null
    private var interacao: Interacao? = null
    private var nomeImagemSelecionada: String? = null
    private var corSelecionada: String = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interacaoId = arguments?.getString("interacao_id")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val largura = (resources.displayMetrics.widthPixels * 0.92).toInt()
            val altura = (resources.displayMetrics.heightPixels * 0.85).toInt()
            setLayout(largura, altura)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInteracaoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        dbLocal = AppDatabase.getInstance(requireContext())

        aplicarCores()
        buscarInteracao()

        binding.btnSalvar.setOnClickListener {
            salvarEdicao()
        }

        binding.imgInteracao.setOnClickListener {
            SelecionarImagemFragment()
                .show(childFragmentManager, "selecionar_imagem")
        }

        binding.btnVoltar.setOnClickListener {
            dismiss()
        }

        binding.btnExcluir.setOnClickListener {
            deletarInteracao()
        }

        // Ouvinte para receber a imagem do SelecionarImagemFragment
        childFragmentManager.setFragmentResultListener(
            "imagem_request",
            this
        ) { _, bundle ->
            val nomeImagem = bundle.getString("imagem") ?: return@setFragmentResultListener
            nomeImagemSelecionada = nomeImagem
            
            val resId = resources.getIdentifier(
                nomeImagem,
                "drawable",
                requireContext().packageName
            )

            if (resId != 0) {
                binding.imgInteracao.setImageResource(resId)
            }
        }
    }

    private fun buscarInteracao() {
        val id = interacaoId ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val item = dbLocal.interacaoDao().getById(id)
            withContext(Dispatchers.Main) {
                interacao = item
                preencherDados()
            }
        }
    }

    private fun preencherDados() {
        interacao?.let { item ->
            binding.etTitulo.setText(item.titulo)
            nomeImagemSelecionada = item.imagem
            
            val resId = resources.getIdentifier(item.imagem, "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.imgInteracao.setImageResource(resId)
            }

            corSelecionada = item.cor.uppercase()
            when (corSelecionada) {
                "#FFC9C9" -> binding.corVermelho.isChecked = true
                "#FFD8B2" -> binding.corLaranja.isChecked = true
                "#FFF3C4" -> binding.corAmarelo.isChecked = true
                "#D4F5DD" -> binding.corVerde.isChecked = true
                "#CFE8FF" -> binding.corAzul.isChecked = true
                "#E3D4FF" -> binding.corRoxo.isChecked = true
            }
        }
    }

    private fun salvarEdicao() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val titulo = binding.etTitulo.text.toString().trim()
        if (titulo.isEmpty()) {
            binding.etTitulo.error = "Digite um título"
            return
        }

        val novaCor = when (binding.rgCores.checkedRadioButtonId) {
            binding.corVermelho.id -> "#FFC9C9"
            binding.corLaranja.id -> "#FFD8B2"
            binding.corAmarelo.id -> "#FFF3C4"
            binding.corVerde.id -> "#D4F5DD"
            binding.corAzul.id -> "#CFE8FF"
            binding.corRoxo.id -> "#E3D4FF"
            else -> interacao?.cor ?: "#FFFFFF"
        }

        val imagem = nomeImagemSelecionada ?: interacao?.imagem ?: "int_feliz"

        val atualizada = interacao?.copy(
            titulo = titulo,
            imagem = imagem,
            cor = novaCor
        ) ?: return

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(atualizada.id)
            .set(atualizada)
            .addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    dbLocal.interacaoDao().update(atualizada)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Atualizado!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun aplicarCores() {
        pintar(binding.corVermelho, "#FF3B30")
        pintar(binding.corLaranja, "#FF9500")
        pintar(binding.corAmarelo, "#FFCC00")
        pintar(binding.corVerde, "#34C759")
        pintar(binding.corAzul, "#007AFF")
        pintar(binding.corRoxo, "#AF52DE")
    }

    private fun pintar(view: View, cor: String) {
        val drawable = view.background.mutate() as? GradientDrawable
        drawable?.setColor(Color.parseColor(cor))
    }

    private fun deletarInteracao() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val item = interacao
        if (userId.isNullOrBlank() || item == null) return

        firestore.collection("usuarios")
            .document(userId)
            .collection("interacoes")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    dbLocal.interacaoDao().delete(item)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Interação excluída!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
    }

    companion object {
        fun newInstance(interacaoId: String): InteracaoDialogFragment {
            val fragment = InteracaoDialogFragment()
            val args = Bundle()
            args.putString("interacao_id", interacaoId)
            fragment.arguments = args
            return fragment
        }
    }
}
