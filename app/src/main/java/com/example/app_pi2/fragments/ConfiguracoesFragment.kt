package com.example.app_pi2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.databinding.FragmentConfirguracoesBinding
import com.example.app_pi2.dialog.configurarAbasConfiguracao
import com.example.app_pi2.utils.ModoResponsavelManager

class ConfiguracoesDialogFragment : DialogFragment() {

    private var _binding: FragmentConfirguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFechar.setOnClickListener {
            dismiss()
        }

        // Chama a SUA função que controla as abas e a linha indicadora
        configurarAbasConfiguracao(binding.root)

        // Configura a lógica de ligar/desligar os botões
        configurarSwitches()
    }

    private fun configurarSwitches() {
        // --- 1. Lógica do Switch Modo Escuro ---
        binding.switchModoEscuro.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvStatusModo.text = "Ativado"
            } else {
                binding.tvStatusModo.text = "Desativado"
            }
        }

        // --- 2. Lógica do Switch Modo Responsável ---
        val isResponsavelAtivo = ModoResponsavelManager.isAtivo(requireContext())
        binding.switchResponsavel.isChecked = isResponsavelAtivo

        binding.switchResponsavel.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.isChecked = false
                abrirValidacaoDeSenha()
            } else {
                ModoResponsavelManager.setAtivo(requireContext(), false)
                Toast.makeText(requireContext(), "Modo Responsável desativado", Toast.LENGTH_SHORT).show()
            }
        }

        parentFragmentManager.setFragmentResultListener("senha_result", viewLifecycleOwner) { _, bundle ->
            val sucesso = bundle.getBoolean("sucesso", false)
            if (sucesso) {
                binding.switchResponsavel.setOnCheckedChangeListener(null)
                binding.switchResponsavel.isChecked = true
                configurarListenerResponsavel()

                ModoResponsavelManager.setAtivo(requireContext(), true)
                Toast.makeText(requireContext(), "Modo Responsável ativado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarListenerResponsavel() {
        binding.switchResponsavel.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                ModoResponsavelManager.setAtivo(requireContext(), false)
                Toast.makeText(requireContext(), "Modo Responsável desativado", Toast.LENGTH_SHORT).show()
            } else {
                buttonView.isChecked = false
                abrirValidacaoDeSenha()
            }
        }
    }

    private fun abrirValidacaoDeSenha() {
        val fragmentSenha = SolicitarSenhaRespFragment()
        fragmentSenha.show(parentFragmentManager, "SolicitarSenhaResp")
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog?.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}