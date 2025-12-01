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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import kotlin.random.Random

class CadastroEmailRespFragment : DialogFragment() {

    private lateinit var editEmailResponsavel: EditText
    private lateinit var btnEnviar: Button
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
                    mostrarMensagem("Digite o e-mail do responsável")

                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailResponsavel).matches() ->
                    mostrarMensagem("Digite um e-mail válido")

                emailResponsavel == emailUser ->
                    mostrarMensagem("Use um e-mail diferente do da conta do usuário")

                else -> enviarCodigoResponsavel(emailResponsavel)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun enviarCodigoResponsavel(emailDestino: String) {

        // ✔ Gerar código de 6 dígitos
        val codigo = Random.nextInt(100000, 999999).toString()

        // ✔ Salvar no SharedPreferences para ser verificado no próximo fragment
        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
            putString("responsavel_email", emailDestino)
            putString("responsavel_codigo", codigo)
        }

        // -------------------------
        // ✔ Criar documento na collection "mail"
        // -------------------------
        val mailData = hashMapOf(
            "to" to listOf(emailDestino),
            "message" to hashMapOf(
                "subject" to "Código de verificação",
                "text" to "Seu código é: $codigo"
            )
        )

        db.collection("mail")
            .add(mailData)
            .addOnSuccessListener {
                mostrarMensagem("Código enviado para: $emailDestino")

                dismiss()

                val dialogValidar = ValidarCodigoRespFragment()
                dialogValidar.show(parentFragmentManager, "ValidarCodigoRespFragment")
            }
            .addOnFailureListener { e ->
                val erro = e.localizedMessage ?: "Erro desconhecido"
                mostrarMensagem("Falha ao enviar e-mail: $erro")
            }
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}
