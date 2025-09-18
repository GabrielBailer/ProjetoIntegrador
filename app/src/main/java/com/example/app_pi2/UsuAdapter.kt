package com.example.app_pi2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_pi2.databinding.ItemUsuarioBinding

class UsuAdapter(
    private val usuarios: MutableList<Usuario>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<UsuAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.binding.tvNomeUsuario.text = usuario.nomeUsuario
        holder.binding.tvNomeResponsavel.text = usuario.nomeResponsavel
    }

    override fun getItemCount() = usuarios.size
}
