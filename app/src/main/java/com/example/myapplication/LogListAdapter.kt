package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.PointItemBinding
import com.example.myapplication.model.Event
import com.example.myapplication.model.Point

class LogListAdapter(context: Context) : RecyclerView.Adapter<LogListAdapter.VH>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    val list = arrayListOf<Point>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = PointItemBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val point = list[position]
        holder.binding.point = point
        holder.binding.root.isActivated = point.event != Event.NONE
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun add(point: Point) {
        list.add(0, point)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    class VH(val binding: PointItemBinding) : RecyclerView.ViewHolder(binding.root)
}