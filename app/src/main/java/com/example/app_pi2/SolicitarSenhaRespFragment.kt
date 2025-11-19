package com.example.app_pi2.ui.seguranca

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.NovaInteracao
import com.example.app_pi2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SolicitarSenhaRespFragment : DialogFragment() {

    private lateinit var editSenha: EditText
    private lateinit var btnConfirmar: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_solicitar_senha, container, false)

        editSenha = view.findViewById(R.id.edit_senha_interna)
        btnConfirmar = view.findViewById(R.id.btn_confirmar_senha)

        btnConfirmar.setOnClickListener {
            val senhaDigitada = editSenha.text.toString().trim()
            if (senhaDigitada.isEmpty()) {
                mostrarMensagem("Digite a senha")
            } else {
                validarSenha(senhaDigitada)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun validarSenha(senhaDigitada: String) {
        val userId = auth.currentUser?.uid ?: run {
            mostrarMensagem("Usuário não autenticado")
            return
        }

        firestore.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val senhaCadastrada = doc.getString("senhaResponsavel")
                if (senhaCadastrada == senhaDigitada) {
                    mostrarMensagem("Senha correta!")
                    abrirNovaInteracao()
                    dismiss()
                } else {
                    mostrarMensagem("Senha incorreta")
                }
            }
            .addOnFailureListener {
                mostrarMensagem("Erro ao validar senha: ${it.message}")
            }
    }

    private fun abrirNovaInteracao() {
        val intent = Intent(requireContext(), NovaInteracao::class.java)
        startActivity(intent)
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
