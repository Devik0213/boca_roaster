package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.PointItemBinding
import com.example.myapplication.model.RPoint

class ListAdapter(context: Context) : RecyclerView.Adapter<ListAdapter.VH>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    val list = arrayListOf<RPoint>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = PointItemBinding.inflate(inflater)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val point = list[position]
        holder.binding.point = point
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun add(rPoint: RPoint) {
        list.add(rPoint)
        notifyDataSetChanged()
    }

    fun clear(){
        list.clear()
        notifyDataSetChanged()
    }
    class VH(val binding: PointItemBinding) : RecyclerView.ViewHolder(binding.root)
}