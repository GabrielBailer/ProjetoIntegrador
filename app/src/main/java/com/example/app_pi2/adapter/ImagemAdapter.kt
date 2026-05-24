package com.example.app_pi2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.app_pi2.R
import com.example.app_pi2.databinding.ItemImagemBinding

class ImagemAdapter(
    private val imagens: List<String>,
    private val onImagemSelecionada: (String) -> Unit
) : RecyclerView.Adapter<ImagemAdapter.ImagemViewHolder>() {

    private var imagemSelecionada: String? = null

    inner class ImagemViewHolder(val binding: ItemImagemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagemViewHolder {
        val binding = ItemImagemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImagemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagemViewHolder, position: Int) {
        val nomeImagem = imagens[position]
        val context = holder.itemView.context

        val resId = context.resources.getIdentifier(
            nomeImagem,
            "drawable",
            context.packageName
        )

        holder.binding.imgOpcao.setImageResource(resId)

        val selecionada = nomeImagem == imagemSelecionada

        if (selecionada) {
            holder.binding.cardImagem.strokeWidth = 6
            holder.binding.cardImagem.strokeColor = ContextCompat.getColor(
                context,
                R.color.primary
            )
            holder.binding.cardImagem.cardElevation = 6f
        } else {
            holder.binding.cardImagem.strokeWidth = 1
            holder.binding.cardImagem.strokeColor = ContextCompat.getColor(
                context,
                android.R.color.darker_gray
            )
            holder.binding.cardImagem.cardElevation = 3f
        }

        holder.binding.root.setOnClickListener {
            val imagemAnterior = imagemSelecionada
            imagemSelecionada = nomeImagem

            imagemAnterior?.let { anterior ->
                val posicaoAnterior = imagens.indexOf(anterior)
                if (posicaoAnterior != -1) {
                    notifyItemChanged(posicaoAnterior)
                }
            }

            notifyItemChanged(position)
            onImagemSelecionada(nomeImagem)
        }
    }

    override fun getItemCount() = imagens.size
}