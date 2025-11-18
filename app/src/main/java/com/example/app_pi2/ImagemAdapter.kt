package com.example.app_pi2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_pi2.databinding.ItemImagemBinding

class ImagemAdapter(
    private val imagens: List<String>,
    private val onImagemSelecionada: (String) -> Unit
) : RecyclerView.Adapter<ImagemAdapter.ImagemViewHolder>() {

    private var imagemSelecionada: String? = null

    inner class ImagemViewHolder(val binding: ItemImagemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagemViewHolder {
        val binding = ItemImagemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagemViewHolder, position: Int) {
        val nomeImagem = imagens[position]
        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(nomeImagem, "drawable", context.packageName)

        holder.binding.imgOpcao.setImageResource(resId)

        holder.binding.root.setOnClickListener {
            imagemSelecionada = nomeImagem
            onImagemSelecionada(nomeImagem)
        }
    }

    override fun getItemCount() = imagens.size
}
