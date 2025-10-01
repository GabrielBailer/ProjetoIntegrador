package com.example.app_pi2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_pi2.databinding.ItemInteracaoBinding

class InteracaoAdapter(
    private val interacoes: MutableList<Interacao>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<InteracaoAdapter.InteracaoViewHolder>() {

    inner class InteracaoViewHolder(val binding: ItemInteracaoBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InteracaoViewHolder {
        val binding = ItemInteracaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InteracaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InteracaoViewHolder, position: Int) {
        val interacao = interacoes[position]
        holder.binding.tvTitulo.text = interacao.titulo
        holder.binding.tvDescricao.text = interacao.descricao
    }

    override fun getItemCount() = interacoes.size
}
