package com.example.app_pi2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.databinding.FragmentConfirguracoesBinding

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

        binding.tabAparencia.setOnClickListener {
            configurarAparencia()
        }

        binding.tabSeguranca.setOnClickListener {
            configurarSeguranca()
        }
    }

    private fun configurarAparencia() {
        binding.tabAparencia.alpha = 1f
        binding.tabSeguranca.alpha = 0.5f
        binding.cardModoEscuro.visibility = View.VISIBLE
        binding.cardModoResponsavel.visibility = View.GONE
    }

    private fun configurarSeguranca() {
        binding.tabAparencia.alpha = 0.5f
        binding.tabSeguranca.alpha = 1f
        binding.cardModoEscuro.visibility = View.GONE
        binding.cardModoResponsavel.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
