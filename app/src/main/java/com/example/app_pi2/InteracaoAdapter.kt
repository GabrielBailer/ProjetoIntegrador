package com.example.app_pi2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_pi2.databinding.ItemInteracaoBinding

class InteracaoAdapter(
    private val interacoes: MutableList<Interacao>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<InteracaoAdapter.InteracaoViewHolder>() {

    inner class InteracaoViewHolder(val binding: ItemInteracaoBinding)
        : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(getAbsoluteAdapterPosition())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InteracaoViewHolder {
        val binding = ItemInteracaoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return InteracaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InteracaoViewHolder, position: Int) {
        val interacao = interacoes[position]
        holder.binding.tvTitulo.text = interacao.titulo

        // Carregar imagem local ou do Firebase
        val imagem = interacao.imagem
        if (!imagem.isNullOrEmpty()) {
            if (imagem.startsWith("http")) {
                Glide.with(holder.itemView.context)
                    .load(imagem)
                    .into(holder.binding.imgMiniatura)
            } else {
                val context = holder.itemView.context
                val resId = context.resources.getIdentifier(imagem, "drawable", context.packageName)
                if (resId != 0) holder.binding.imgMiniatura.setImageResource(resId)
                else holder.binding.imgMiniatura.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.binding.imgMiniatura.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    override fun getItemCount() = interacoes.size
}
