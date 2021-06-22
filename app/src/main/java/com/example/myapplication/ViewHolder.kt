package com.example.myapplication

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class ViewHolder(
    val binding: ViewDataBinding,
    click: (Int) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener { click(adapterPosition) }
    }
}