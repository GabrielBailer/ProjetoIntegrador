package com.example.app_pi2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.databinding.FragmentConfirguracoesBinding
import com.example.app_pi2.dialog.configurarAbasConfiguracao
import com.example.app_pi2.utils.ModoResponsavelManager
import com.example.app_pi2.utils.FalaAutomaticaManager
import com.example.app_pi2.utils.ThemeManager

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

        configurarAbasConfiguracao(binding.root)

        configurarSwitches()
    }

    private fun configurarSwitches() {
        // Switch Modo Escuro
        val isDarkMode = ThemeManager.isModoEscuro(requireContext())

        binding.switchModoEscuro.isChecked = isDarkMode
        binding.tvStatusModo.text =
            if (isDarkMode) "Ativado" else "Desativado"

        binding.switchModoEscuro.setOnCheckedChangeListener { _, isChecked ->

            ThemeManager.salvarModoEscuro(requireContext(), isChecked)

            binding.tvStatusModo.text =
                if (isChecked) "Ativado" else "Desativado"

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Switch Modo Responsável
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

        // --- CORREÇÃO: Switch Fala Automática (trocado de "Altomatica" para "Automatica") ---
        val falaAutomaticaAtiva =
            FalaAutomaticaManager.isAtivo(requireContext())

        binding.switchFalaAutomatica.isChecked =
            falaAutomaticaAtiva

        binding.tvFalaAutomatica.text =
            if (falaAutomaticaAtiva) "Ativado" else "Desativado"

        binding.switchFalaAutomatica.setOnCheckedChangeListener { _, isChecked ->

            FalaAutomaticaManager.setAtivo(
                requireContext(),
                isChecked
            )

            binding.tvFalaAutomatica.text =
                if (isChecked) "Ativado" else "Desativado"
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