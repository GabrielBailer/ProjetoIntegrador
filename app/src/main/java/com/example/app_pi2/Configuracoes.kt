package com.example.app_pi2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityConfiguracoesBinding

class Configuracoes : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aqui você pode adicionar a lógica para os botões da sua tela, por exemplo:
        // binding.btnUsuario.setOnClickListener {
        //     // Ação para o botão de usuário
        // }
    }
}
