package com.example.app_pi2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityConfiguracoesBinding
import com.google.firebase.auth.FirebaseAuth

class Configuracoes : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, TelaLogin::class.java)
            startActivity(intent)
        }

        // Aqui você pode adicionar a lógica para os botões da sua tela, por exemplo:
        // binding.btnUsuario.setOnClickListener {
        //     coisas
        // }
    }
}
