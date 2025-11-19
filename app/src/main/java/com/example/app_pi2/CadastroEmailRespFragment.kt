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
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class CadastroEmailRespFragment : DialogFragment() {

    private lateinit var editEmailResponsavel: EditText
    private lateinit var btnEnviar: Button
    private val auth = FirebaseAuth.getInstance()

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

                emailResponsavel == emailUser ->
                    mostrarMensagem("Use um e-mail diferente do da conta")

                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailResponsavel).matches() ->
                    mostrarMensagem("Digite um e-mail válido")

                else -> enviarLinkEmail(emailResponsavel)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun enviarLinkEmail(emailDestino: String) {
        // Salva email em SharedPreferences para uso futuro
        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("responsavel_email", emailDestino)
            .apply()

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://app-pi2.firebaseapp.com/verificar") // link de redirecionamento
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                requireContext().packageName,
                true, // instalar app se não tiver
                null
            )
            .build()

        auth.sendSignInLinkToEmail(emailDestino, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mostrarMensagem("Link enviado para $emailDestino! Abra o app pelo link recebido.")
                    dismiss()
                } else {
                    mostrarMensagem("Erro ao enviar link: ${task.exception?.message}")
                }
            }
    }

    private fun mostrarMensagem(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
