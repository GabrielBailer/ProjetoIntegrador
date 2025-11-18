package com.example.app_pi2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.app_pi2.databinding.FragmentSelecionarImagemBinding

class SelecionarImagemFragment : DialogFragment() {

    private lateinit var binding: FragmentSelecionarImagemBinding
    private lateinit var adapter: ImagemAdapter
    private var listener: OnImagemSelecionadaListener? = null
    private var imagemSelecionada: String? = null

    interface OnImagemSelecionadaListener {
        fun onImagemSelecionada(nomeImagem: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnImagemSelecionadaListener) {
            listener = context
        } else {
            throw RuntimeException("$context deve implementar OnImagemSelecionadaListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelecionarImagemBinding.inflate(inflater, container, false)

        val imagensDisponiveis = listOf(
            "alm_cafe_1",
            "alm_cha_1",
            "alm_donut_1",
            "alm_fome_1",
            "alm_hamburguer_1",
            "alm_pizza_1",
            "alm_refrigerante_1",
            "esc_calculadora_1",
            "esc_geografia_1",
            "esc_numeros_1",
            "hig_banheiro_1",
            "hig_banho_1",
            "hig_papel_higienico_1",
            "sen_apatico_1",
            "sen_bravo_1",
            "sen_cansaco_1",
            "sen_chateado_1",
            "sen_chorar_1",
            "sen_felicidade_1",
            "sen_piscar_1",
            "sen_tranquilidade_1",
            "sen_tristeza_1"
        )

        adapter = ImagemAdapter(imagensDisponiveis) { nome ->
            imagemSelecionada = nome
        }

        binding.recyclerImagens.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerImagens.adapter = adapter

        binding.btnConfirmarImagem.setOnClickListener {
            if (imagemSelecionada != null) {
                listener?.onImagemSelecionada(imagemSelecionada!!)
                parentFragmentManager.popBackStack()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Selecione uma imagem!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}
