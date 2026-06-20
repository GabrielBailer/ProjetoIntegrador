package com.example.app_pi2.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.app_pi2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions

class CadastroEmailRespFragment : DialogFragment() {

    private lateinit var editEmailResponsavel: EditText
    private lateinit var btnEnviar: Button

    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    private var tipo: TipoDeFluxo = TipoDeFluxo.CADASTRO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val view = inflater.inflate(R.layout.fragment_cadastro_email_resp, container, false)

        editEmailResponsavel = view.findViewById(R.id.edit_email_alternativo)
        btnEnviar = view.findViewById(R.id.btn_enviar_codigo)

        btnEnviar.setOnClickListener {
            val emailResponsavel = editEmailResponsavel.text.toString().trim()
            val emailUser = auth.currentUser?.email ?: ""

            when {
                emailResponsavel.isEmpty() ->
                    mostrarMensagem("Digite o e-mail")

                !Patterns.EMAIL_ADDRESS.matcher(emailResponsavel).matches() ->
                    mostrarMensagem("Digite um e-mail válido")

                tipo == TipoDeFluxo.CADASTRO && emailResponsavel == emailUser ->
                    mostrarMensagem("Use um e-mail diferente do da conta")

                else -> enviarCodigoResponsavel(emailResponsavel)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        // 1. Padronizado para 90% da tela igual aos outros modais
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()

        dialog?.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 2. Aqui está a linha mágica da transparência para os cantos aparecerem!
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun enviarCodigoResponsavel(emailDestino: String) {
        val data = hashMapOf(
            "email" to emailDestino
        )

        functions
            .getHttpsCallable("enviarCodigo")
            .call(data)
            .addOnSuccessListener {
                mostrarMensagem("Código enviado para: $emailDestino")

                dismiss()

                val fragment = ValidarCodigoRespFragment()

                val bundle = Bundle()
                bundle.putString("email", emailDestino)

                // ✅ CORRETO
                bundle.putString("flow", tipo.name)

                fragment.arguments = bundle
                fragment.show(parentFragmentManager, "ValidarCodigoResp")
            }
            .addOnFailureListener { e ->
                mostrarMensagem("Erro ao enviar código: ${e.localizedMessage}")
            }
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}