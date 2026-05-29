package com.example.app_pi2.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_pi2.R
import com.example.app_pi2.data.model.Interacao
import com.example.app_pi2.databinding.ItemInteracaoBinding

class InteracaoAdapter(
    private val onItemClick: (Interacao) -> Unit,
    private val onLongClick: (Interacao) -> Unit
) : ListAdapter<Interacao, InteracaoAdapter.InteracaoViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class InteracaoViewHolder(
        val binding: ItemInteracaoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InteracaoViewHolder {

        val binding = ItemInteracaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return InteracaoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: InteracaoViewHolder,
        position: Int
    ) {

        val interacao = getItem(position)

        holder.binding.tvTitulo.text = interacao.titulo

        holder.binding.root.setOnClickListener {
            onItemClick(interacao)
        }

        holder.binding.root.setOnLongClickListener {
            onLongClick(interacao)
            true
        }

        val imagem = interacao.imagem
        if (!imagem.isNullOrEmpty()) {
            if (imagem.startsWith("http")) {
                Glide.with(holder.itemView.context)
                    .load(imagem)
                    .into(holder.binding.ivIcone)
            } else {
                val context = holder.itemView.context
                val resId = context.resources.getIdentifier(
                    imagem,
                    "drawable",
                    context.packageName
                )

                if (resId != 0) {
                    holder.binding.ivIcone.setImageResource(resId)
                } else {
                    holder.binding.ivIcone.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        } else {
            holder.binding.ivIcone.setImageResource(R.drawable.ic_launcher_foreground)
        }

        val background = holder.binding.layoutCard.background

        if (background is GradientDrawable) {
            background.mutate()

            background.setColor(interacao.corInt)
        }
    }

}