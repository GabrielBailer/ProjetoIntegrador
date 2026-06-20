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
                   "int_black_aceno",
                   "int_black_aviso",
                   "int_black_baloes",
                   "int_black_banheiro",
                   "int_black_batatas_fritas",
                   "int_black_bebida",
                   "int_black_beijo_coracao",
                   "int_black_biscoito",
                   "int_black_bolo_aniversario",
                   "int_black_bravo",
                   "int_black_caderno",
                   "int_black_caneca",
                   "int_black_caneta",
                   "int_black_carrinho_bebe",
                   "int_black_carro",
                   "int_black_casa",
                   "int_black_celular",
                   "int_black_chorando",
                   "int_black_chorando_rir",
                   "int_black_chuvas_nuvens",
                   "int_black_chuveiro",
                   "int_black_computador",
                   "int_black_controle",
                   "int_black_coracao",
                   "int_black_crianca",
                   "int_black_culpado",
                   "int_black_despertador",
                   "int_black_dinheiro",
                   "int_black_doce",
                   "int_black_dor",
                   "int_black_estetoscopio",
                   "int_black_estrelas",
                   "int_black_feliz",
                   "int_black_floco_neve",
                   "int_black_flor",
                   "int_black_garrafa_agua",
                   "int_black_guarda_chuva",
                   "int_black_hamburger",
                   "int_black_homem",
                   "int_black_lapis",
                   "int_black_ligacao",
                   "int_black_livro",
                   "int_black_lua_estrelas",
                   "int_black_maca",
                   "int_black_mochila",
                   "int_black_mudo",
                   "int_black_mulher",
                   "int_black_musica",
                   "int_black_nao",
                   "int_black_nuvem",
                   "int_black_onibus",
                   "int_black_paixao",
                   "int_black_pao",
                   "int_black_pata",
                   "int_black_peixe",
                   "int_black_pergunta",
                   "int_black_peru",
                   "int_black_pincel",
                   "int_black_pipoca",
                   "int_black_pirulito",
                   "int_black_pizza",
                   "int_black_remedio",
                   "int_black_ruborizado",
                   "int_black_salada",
                   "int_black_sem_som",
                   "int_black_sem_visao",
                   "int_black_sim",
                   "int_black_sol",
                   "int_black_som",
                   "int_black_sono",
                   "int_black_sorvete",
                   "int_black_talher",
                   "int_black_tesoura",
                   "int_black_tonto",
                   "int_black_triste",
                   "int_black_vento",
                   "int_black_visao",
                   "int_black_xicara",
                   "int_papel_higienico"
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
