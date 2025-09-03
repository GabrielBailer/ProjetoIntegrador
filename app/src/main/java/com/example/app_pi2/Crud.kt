package com.example.app_pi2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityCrudBinding
import com.example.app_pi2.Usuario // Added import

class Crud : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding
    private val usuarioList = mutableListOf<Usuario>()
    private lateinit var adapter: ArrayAdapter<Usuario>
    private var selectedIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usuarioList)
        binding.listView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            try {
                val usuario = Usuario(
                    id = binding.etId.text.toString().toInt(),
                    nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                    nomeUsuario = binding.etNomeUsuario.text.toString(),
                    nomeProfessor = binding.etNomeProfessor.text.toString(),
                    endereco = binding.etEndereco.text.toString(),
                    email = binding.etEmail.text.toString(),
                    senha = binding.etSenha.text.toString()
                )

                usuarioList.add(usuario)
                adapter.notifyDataSetChanged()
                limparCampos()

            } catch (e: Exception) {
                Toast.makeText(this, "Preencha os campos corretamente!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val usuario = usuarioList[position]
            selectedIndex = position

            binding.etId.setText(usuario.id.toString())
            binding.etNomeResponsavel.setText(usuario.nomeResponsavel)
            binding.etNomeUsuario.setText(usuario.nomeUsuario)
            binding.etNomeProfessor.setText(usuario.nomeProfessor)
            binding.etEndereco.setText(usuario.endereco)
            binding.etEmail.setText(usuario.email)
            binding.etSenha.setText(usuario.senha)
        }

        binding.btnUpdate.setOnClickListener {
            selectedIndex?.let {
                val usuario = Usuario(
                    id = binding.etId.text.toString().toInt(),
                    nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                    nomeUsuario = binding.etNomeUsuario.text.toString(),
                    nomeProfessor = binding.etNomeProfessor.text.toString(),
                    endereco = binding.etEndereco.text.toString(),
                    email = binding.etEmail.text.toString(),
                    senha = binding.etSenha.text.toString()
                )
                usuarioList[it] = usuario
                adapter.notifyDataSetChanged()
                limparCampos()
                selectedIndex = null
            }
        }

        binding.btnDelete.setOnClickListener {
            selectedIndex?.let {
                usuarioList.removeAt(it)
                adapter.notifyDataSetChanged()
                limparCampos()
                selectedIndex = null
            }
        }
    }

    private fun limparCampos() {
        binding.etId.text.clear()
        binding.etNomeResponsavel.text.clear()
        binding.etNomeUsuario.text.clear()
        binding.etNomeProfessor.text.clear()
        binding.etEndereco.text.clear()
        binding.etEmail.text.clear()
        binding.etSenha.text.clear()
    }
}
