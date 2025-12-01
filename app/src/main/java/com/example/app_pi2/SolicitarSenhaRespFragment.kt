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
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult

class SolicitarSenhaRespFragment : DialogFragment() {

    private lateinit var editSenha: EditText
    private lateinit var btnConfirmar: Button
    private val functions = FirebaseFunctions.getInstance()

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

    private fun validarSenha(senhaDigitada: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            mostrarMensagem("Erro interno: UID do usuário não encontrado.")
            return
        }

        mostrarMensagem("Validando senha...")

        val payload = mapOf(
            "uid" to uid,
            "password" to senhaDigitada
        )

        functions
            .getHttpsCallable("verifyResponsiblePassword")
            .call(payload)
            .addOnSuccessListener { result: HttpsCallableResult ->
                val data = result.data
                val ok = (data as? Map<*, *>)?.get("success") as? Boolean ?: false

                if (ok) {
                    mostrarMensagem("Senha correta!")
                    abrirNovaInteracao()
                    dismiss()
                } else {
                    mostrarMensagem("Senha incorreta")
                }
            }
            .addOnFailureListener { e ->
                mostrarMensagem("Erro ao validar senha: ${e.localizedMessage}")
            }
    }

    private fun abrirNovaInteracao() {
        val intent = Intent(requireContext(), NovaInteracao::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
