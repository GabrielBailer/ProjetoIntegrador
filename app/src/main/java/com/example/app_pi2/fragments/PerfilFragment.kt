package com.example.app_pi2.fragments

import android.content.Intent
import com.example.app_pi2.ui.TelaLogin
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilDialogFragment : DialogFragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var modoEdicao = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        carregarDados()
        configurarBotoes()
    }

    private fun configurarBotoes() {
        binding.btnCancelar.setOnClickListener {
            if (modoEdicao) {
                alternarModo(false)
            } else {
                dismiss()
            }
        }

        binding.btnAcao.setOnClickListener {
            if (modoEdicao) {
                salvarPerfil()
            } else {
                alternarModo(true)
            }
        }

        binding.tvSair.setOnClickListener {

            auth.signOut()
            dismiss()
            val intent = Intent(requireActivity(), TelaLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    private fun alternarModo(editar: Boolean) {
        modoEdicao = editar

        if (editar) {
            binding.tvNome.visibility = View.GONE
            binding.tvEmail.visibility = View.GONE
            binding.tvContato.visibility = View.GONE

            binding.etNome.visibility = View.VISIBLE
            binding.etEmail.visibility = View.VISIBLE
            binding.etContato.visibility = View.VISIBLE

            binding.btnAcao.text = "Salvar"
            binding.btnAcao.setTextColor(Color.parseColor("#35393C"))
            binding.btnAcao.setBackgroundColor(Color.parseColor("#A4D8FF"))
        } else {
            binding.tvNome.visibility = View.VISIBLE
            binding.tvEmail.visibility = View.VISIBLE
            binding.tvContato.visibility = View.VISIBLE

            binding.etNome.visibility = View.GONE
            binding.etEmail.visibility = View.GONE
            binding.etContato.visibility = View.GONE

            binding.btnAcao.text = "Editar"
            binding.btnAcao.setTextColor(Color.parseColor("#A4D8FF"))
            binding.btnAcao.setBackgroundColor(Color.parseColor("#35393C"))
        }
    }

    private fun carregarDados() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nome = doc.getString("nomeUsuario").orEmpty()
                val email = doc.getString("email").orEmpty()
                val contato = doc.getString("numeroContato").orEmpty()

                binding.tvNome.text = nome
                binding.tvEmail.text = email
                binding.tvContato.text = contato

                binding.etNome.setText(nome)
                binding.etEmail.setText(email)
                binding.etContato.setText(contato)
            }
    }

    private fun salvarPerfil() {
        val uid = auth.currentUser?.uid ?: return

        val dados = hashMapOf(
            "nomeUsuario" to binding.etNome.text.toString(),
            "email" to binding.etEmail.text.toString(),
            "numeroContato" to binding.etContato.text.toString()
        )

        db.collection("usuarios")
            .document(uid)
            .update(dados as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Salvo!", Toast.LENGTH_SHORT).show()
                alternarModo(false)
                carregarDados()
            }
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