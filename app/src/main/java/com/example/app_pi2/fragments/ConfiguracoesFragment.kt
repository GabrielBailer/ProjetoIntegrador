package com.example.app_pi2.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.R
import com.example.app_pi2.databinding.FragmentConfirguracoesBinding
import com.example.app_pi2.utils.ModoResponsavelManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConfiguracoesDialogFragment : DialogFragment() {

    private var _binding: FragmentConfirguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura as abas
        configurarAbas()
        
        configurarListenerSenha()
        configurarModoResponsavel()
        configurarSwitch()
        configurarFechar()
    }

    private fun configurarAbas() {
        binding.tabAparencia.setOnClickListener { selecionarAba(true) }
        binding.tabSeguranca.setOnClickListener { selecionarAba(false) }
        
        // Inicializa na aba Aparência
        selecionarAba(true)
    }

    private fun selecionarAba(isAparencia: Boolean) {
        val context = requireContext()
        val corAtiva = ContextCompat.getColor(context, android.R.color.black)
        val corInativa = ContextCompat.getColor(context, android.R.color.darker_gray)

        if (isAparencia) {
            // Textos e Estilos
            binding.tabAparencia.setTextColor(corAtiva)
            binding.tabAparencia.setTypeface(null, Typeface.BOLD)
            binding.tabSeguranca.setTextColor(corInativa)
            binding.tabSeguranca.setTypeface(null, Typeface.NORMAL)

            // Visibilidade dos Cards
            binding.cardModoEscuro.visibility = View.VISIBLE
            binding.cardModoResponsavel.visibility = View.GONE

            // MUDANÇA DA DESCRIÇÃO
            binding.tvDescricaoConfiguracao.text = "O modo escuro reduz o brilho da tela e pode ajudar a diminuir o cansaço visual, especialmente em ambientes com pouca luz."

            // Indicador
            binding.indicadorTab.animate().x(binding.tabAparencia.x).setDuration(200)
        } else {
            // Textos e Estilos
            binding.tabSeguranca.setTextColor(corAtiva)
            binding.tabSeguranca.setTypeface(null, Typeface.BOLD)
            binding.tabAparencia.setTextColor(corInativa)
            binding.tabAparencia.setTypeface(null, Typeface.NORMAL)

            // Visibilidade dos Cards
            binding.cardModoEscuro.visibility = View.GONE
            binding.cardModoResponsavel.visibility = View.VISIBLE

            // MUDANÇA DA DESCRIÇÃO
            binding.tvDescricaoConfiguracao.text = "O modo responsável exige a senha do responsável para autorizar a inclusão de novas interações."

            // Indicador
            binding.indicadorTab.animate().x(binding.tabSeguranca.x).setDuration(200)
        }
    }

    private fun configurarListenerSenha() {
        parentFragmentManager.setFragmentResultListener("senha_result", this) { _, bundle ->
            val sucesso = bundle.getBoolean("sucesso")
            binding.switchResponsavel.setOnCheckedChangeListener(null)
            if (sucesso) {
                ModoResponsavelManager.setAtivo(requireContext(), true)
                binding.switchResponsavel.isChecked = true
            } else {
                binding.switchResponsavel.isChecked = false
            }
            configurarModoResponsavel()
        }
    }

    private fun configurarSwitch() {
        val prefs = requireContext().getSharedPreferences("config", 0)
        val isDark = prefs.getBoolean("dark_mode", false)

        binding.switchModoEscuro.isChecked = isDark
        atualizarTextoModo(isDark)

        binding.switchModoEscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            atualizarTextoModo(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun configurarModoResponsavel() {
        val ativo = ModoResponsavelManager.isAtivo(requireContext())
        binding.switchResponsavel.setOnCheckedChangeListener(null)
        binding.switchResponsavel.isChecked = ativo
        binding.switchResponsavel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) abrirValidacaoSenha() else ModoResponsavelManager.setAtivo(requireContext(), false)
        }
    }

    private fun abrirValidacaoSenha() {
        binding.switchResponsavel.isEnabled = false
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.switchResponsavel.isEnabled = true
                if (doc.getString("senha_responsavel") != null) {
                    SolicitarSenhaRespFragment().show(parentFragmentManager, "senha")
                } else {
                    CadastroEmailRespFragment().show(parentFragmentManager, "criarSenha")
                }
            }
            .addOnFailureListener {
                binding.switchResponsavel.isEnabled = true
                binding.switchResponsavel.isChecked = false
            }
    }

    private fun atualizarTextoModo(ativo: Boolean) {
        binding.tvStatusModo.text = if (ativo) "Ativado" else "Desativado"
    }

    private fun configurarFechar() {
        binding.btnFechar.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
