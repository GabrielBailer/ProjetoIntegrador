package com.example.app_pi2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_pi2.databinding.ActivityCrudBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Crud : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding
    private lateinit var adapter: UsuAdapter
    private val usuarioList = mutableListOf<Usuario>()
    private var selectedIndex: Int? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = UsuAdapter(usuarioList) { position ->
            val usuario = usuarioList[position]
            selectedIndex = position
            binding.etNomeResponsavel.setText(usuario.nomeResponsavel)
            binding.etNomeUsuario.setText(usuario.nomeUsuario)
            binding.etEndereco.setText(usuario.endereco)
            binding.etEmail.setText(usuario.email)
            binding.etSenha.setText(usuario.senha)
        }

        binding.recyclerViewUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsuarios.adapter = adapter

        binding.btnAdd.setOnClickListener { addUsuario() }
        binding.btnUpdate.setOnClickListener { updateUsuario() }
        binding.btnDelete.setOnClickListener { deleteUsuario() }
    }

    private fun addUsuario() {
        val email = binding.etEmail.text.toString()
        val senha = binding.etSenha.text.toString()
        if (email.isNotEmpty() && senha.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val usuario = Usuario(
                        nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                        nomeUsuario = binding.etNomeUsuario.text.toString(),
                        endereco = binding.etEndereco.text.toString(),
                        email = email,
                        senha = senha
                    )
                    db.collection("usuarios").document(uid).set(usuario)
                        .addOnSuccessListener {
                            usuarioList.add(usuario)
                            adapter.notifyItemInserted(usuarioList.size - 1)
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

    private fun updateUsuario() {
        selectedIndex?.let { index ->
            val uid = auth.currentUser?.uid ?: return
            val usuario = Usuario(
                nomeResponsavel = binding.etNomeResponsavel.text.toString(),
                nomeUsuario = binding.etNomeUsuario.text.toString(),
                endereco = binding.etEndereco.text.toString(),
                email = binding.etEmail.text.toString(),
                senha = binding.etSenha.text.toString()
            )
            db.collection("usuarios").document(uid).set(usuario)
                .addOnSuccessListener {
                    usuarioList[index] = usuario
                    adapter.notifyItemChanged(index)
                    limparCampos()
                    selectedIndex = null
                    Toast.makeText(this, "Usuário atualizado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteUsuario() {
        selectedIndex?.let { index ->
            val uid = auth.currentUser?.uid ?: return
            db.collection("usuarios").document(uid).delete()
                .addOnSuccessListener {
                    usuarioList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    limparCampos()
                    selectedIndex = null
                    Toast.makeText(this, "Usuário deletado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao deletar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun limparCampos() {
        binding.etNomeResponsavel.text.clear()
        binding.etNomeUsuario.text.clear()
        binding.etEndereco.text.clear()
        binding.etEmail.text.clear()
        binding.etSenha.text.clear()
    }
}
