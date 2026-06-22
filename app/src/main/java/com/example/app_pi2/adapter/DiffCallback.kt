package com.example.app_pi2.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.app_pi2.data.model.Interacao

class DiffCallback : DiffUtil.ItemCallback<Interacao>() {

    override fun areItemsTheSame(
        oldItem: Interacao,
        newItem: Interacao
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Interacao,
        newItem: Interacao
    ): Boolean {
        return oldItem == newItem
    }
}