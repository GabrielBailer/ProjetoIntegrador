package com.example.app_pi2.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.app_pi2.adapter.ImagemAdapter
import com.example.app_pi2.databinding.FragmentSelecionarImagemBinding

class SelecionarImagemFragment : DialogFragment() {

    private var _binding: FragmentSelecionarImagemBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ImagemAdapter
    private var imagemSelecionada: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelecionarImagemBinding.inflate(inflater, container, false)

               val imagensDisponiveis = listOf(
            "int_baloes",
            "int_batatas_fritas",
            "int_bebida",
            "int_coracao",
            "int_biscoito",
            "int_bolo_aniversario",
            "int_bravo",
            "int_caderno",
            "int_caneca",
            "int_carrinho_bebe",
            "int_carro",
            "int_casa",
            "int_celular",
            "int_chorando",
            "int_chorando_rir",
            "int_chuvas_nuvens",
            "int_computador",
            "int_controle",
            "int_coracao",
            "int_crianca",
            "int_culpado",
            "int_despertador",
            "int_dinheiro",
            "int_doce",
            "int_estetoscopio",
            "int_estrelas",
            "int_feliz",
            "int_floco_neve",
            "int_flor",
            "int_garrafa_agua",
            "int_guarda_chuva",
            "int_hamburger",
            "int_homem",
            "int_livro",
            "int_lua_estrelas",
            "int_maca",
            "int_mochila",
            "int_mulher",
            "int_musica",
            "int_nuvem",
            "int_onibus",
            "int_paixao",
            "int_pao",
            "int_pata",
            "int_peixe",
            "int_peru",
            "int_pincel",
            "int_pipoca",
            "int_pirulito",
            "int_pizza",
            "int_remedio",
            "int_ruborizado",
            "int_salada",
            "int_sol",
            "int_sorvete",
            "int_talher",
            "int_tonto",
            "int_triste",
            "int_vento",
            "int_xicara",
        )

        adapter = ImagemAdapter(imagensDisponiveis) { nome ->
            imagemSelecionada = nome
        }

        binding.recyclerImagens.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerImagens.adapter = adapter

        binding.btnConfirmarImagem.setOnClickListener {
            val imagem = imagemSelecionada

            if (imagem != null) {

                parentFragmentManager.setFragmentResult(
                    "imagem_request",
                    Bundle().apply {
                        putString("imagem", imagem)
                    }
                )

                dismiss()

            } else {
                Toast.makeText(requireContext(), "Selecione uma imagem!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
