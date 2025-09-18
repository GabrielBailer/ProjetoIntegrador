package com.example.app_pi2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ActivityCrudBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Crud : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding
    private lateinit var adapter: ArrayAdapter<Usuario>
    private val usuarioList = mutableListOf<Usuario>()
    private var selectedIndex: Int? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usuarioList)
        binding.listView.adapter = adapter

        // Adicionar usuário
        binding.btnAdd.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val senha = binding.etSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                // 1. Cria usuário no Authentication
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                            val usuario = Usuario(
                                id = binding.etId.text.toString().toInt(),
                                nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                                nomeUsuario = binding.etNomeUsuario.text.toString(),
                                nomeProfessor = binding.etNomeProfessor.text.toString(),
                                endereco = binding.etEndereco.text.toString(),
                                email = email,
                                senha = senha
                            )

                            // 2. Salva no Firestore
                            db.collection("usuarios").document(uid).set(usuario)
                                .addOnSuccessListener {
                                    usuarioList.add(usuario)
                                    adapter.notifyDataSetChanged()
                                    limparCampos()
                                    Toast.makeText(this, "Usuário cadastrado no Firebase!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha email e senha!", Toast.LENGTH_SHORT).show()
            }
        }

        // Selecionar usuário da lista
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

// Atualizar usuário no Firestore
        binding.btnUpdate.setOnClickListener {
            selectedIndex?.let { index ->       // renomeei 'it' para 'index'
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val usuario = Usuario(
                        id = binding.etId.text.toString().toInt(),
                        nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                        nomeUsuario = binding.etNomeUsuario.text.toString(),
                        nomeProfessor = binding.etNomeProfessor.text.toString(),
                        endereco = binding.etEndereco.text.toString(),
                        email = binding.etEmail.text.toString(),
                        senha = binding.etSenha.text.toString()
                    )

                    db.collection("usuarios").document(uid).set(usuario)
                        .addOnSuccessListener {
                            usuarioList[index] = usuario     // usa 'index' do let
                            adapter.notifyDataSetChanged()
                            limparCampos()
                            selectedIndex = null
                            Toast.makeText(this, "Usuário atualizado!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

// Deletar usuário no Firestore
        binding.btnDelete.setOnClickListener {
            selectedIndex?.let { index ->       // renomeei 'it' para 'index'
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("usuarios").document(uid).delete()
                        .addOnSuccessListener {
                            usuarioList.removeAt(index)   // usa 'index' do let
                            adapter.notifyDataSetChanged()
                            limparCampos()
                            selectedIndex = null
                            Toast.makeText(this, "Usuário deletado!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao deletar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
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
