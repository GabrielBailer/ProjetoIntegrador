package com.example.app_pi2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.ui.seguranca.CriarSenhaRespFragment



class ValidarCodigoRespFragment : DialogFragment() {

    private lateinit var edtCodigo: EditText
    private lateinit var btnValidar: Button



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_validar_codigo_resp, container, false)

        edtCodigo = view.findViewById(R.id.edit_codigo_verificacao)
        btnValidar = view.findViewById(R.id.btn_validar_codigo)

        btnValidar.setOnClickListener {
            val codigo = edtCodigo.text.toString().trim()

            if (codigo.isEmpty()) {
                mostrarMensagem("Digite o código recebido no e-mail")
            } else {
                validarCodigo(codigo)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun validarCodigo(codigo: String) {

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val codigoSalvo = prefs.getString("responsavel_codigo", null)

        if (codigoSalvo == null) {
            mostrarMensagem("Erro interno: código não encontrado.")
            return
        }

        if (codigo != codigoSalvo) {
            mostrarMensagem("Código incorreto")
            return
        }

        // se passou, ok:
        mostrarMensagem("Código verificado com sucesso!")
        abrirCadastroSenha()
        dismiss()
    }

    private fun abrirCadastroSenha() {
        val dialogSenha = CriarSenhaRespFragment()
        dialogSenha.show(parentFragmentManager, "CriarSenhaRespFragment")
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
