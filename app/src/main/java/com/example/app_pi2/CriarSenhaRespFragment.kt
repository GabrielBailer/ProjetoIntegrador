package com.example.app_pi2.ui.seguranca

import android.content.Context
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

class CriarSenhaRespFragment : DialogFragment() {

    private lateinit var editSenha: EditText
    private lateinit var editConfirmarSenha: EditText
    private lateinit var btnCadastrar: Button

    private val functions by lazy { FirebaseFunctions.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_criar_senha_resp, container, false)

        editSenha = view.findViewById(R.id.edit_nova_senha)
        editConfirmarSenha = view.findViewById(R.id.edit_confirmar_senha)
        btnCadastrar = view.findViewById(R.id.btn_salvar_senha)

        btnCadastrar.setOnClickListener {
            val senha = editSenha.text.toString().trim()
            val confirmarSenha = editConfirmarSenha.text.toString().trim()

            when {
                senha.isEmpty() || confirmarSenha.isEmpty() ->
                    mostrarMensagem("Preencha todos os campos")

                senha != confirmarSenha ->
                    mostrarMensagem("As senhas não coincidem")

                senha.length < 6 ->
                    mostrarMensagem("A senha deve ter no mínimo 6 caracteres")

                else -> salvarSenhaNoServidor(senha)
            }
        }

        return view
    }

    private fun salvarSenhaNoServidor(senha: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            mostrarMensagem("Erro interno: UID do usuário não encontrado.")
            return
        }

        val data = hashMapOf(
            "uid" to uid,
            "password" to senha
        )

        functions
            .getHttpsCallable("saveResponsiblePassword")
            .call(data)
            .addOnSuccessListener {
                mostrarMensagem("Senha cadastrada com sucesso!")
                abrirNovaInteracao()
                dismiss()
            }
            .addOnFailureListener {
                mostrarMensagem("Erro ao salvar senha: ${it.localizedMessage}")
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
