package com.example.app_pi2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.R
import com.google.firebase.functions.FirebaseFunctions

class ValidarCodigoRespFragment : DialogFragment() {

    private lateinit var edtCodigo: EditText
    private lateinit var btnValidar: Button

    private val functions = FirebaseFunctions.getInstance()

    private var email: String = ""
    private var tipo: TipoDeFluxo = TipoDeFluxo.CADASTRO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = arguments?.getString("email") ?: ""

        tipo = try {
            TipoDeFluxo.valueOf(arguments?.getString("flow") ?: "CADASTRO")
        } catch (e: Exception) {
            TipoDeFluxo.CADASTRO
        }
    }

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

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun validarCodigo(codigo: String) {

        val data = hashMapOf(
            "email" to email,
            "codigo" to codigo
        )

        functions
            .getHttpsCallable("validarCodigo")
            .call(data)
            .addOnSuccessListener {

                mostrarMensagem("Código verificado com sucesso!")

                dismiss()

                abrirCriarSenha()

            }
            .addOnFailureListener {

                mostrarMensagem("Código inválido ou expirado")

            }
    }

    private fun abrirCriarSenha() {

        val dialogSenha = CriarSenhaRespFragment()

        val bundle = Bundle()
        bundle.putString("email", email)

        dialogSenha.arguments = bundle

        dialogSenha.show(parentFragmentManager, "CriarSenhaRespFragment")
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}