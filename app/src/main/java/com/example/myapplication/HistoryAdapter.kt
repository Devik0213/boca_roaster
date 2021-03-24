package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.HistoryItemBinding
import com.example.myapplication.helper.Formatter
import com.example.myapplication.room.RoastInfo

class HistoryAdapter(private val layoutInflater: LayoutInflater, val click: (RoastInfo) -> Unit) :
    RecyclerView.Adapter<ViewHolder>() {
    var list = arrayListOf<RoastInfo>()

    fun setData(newList: List<RoastInfo>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = list[position]
        holder.binding.apply {
            this as HistoryItemBinding
            root.setOnClickListener {
                click(history)
            }
            date.text = Formatter.shortDateFormat.format(history.timeId)
            name.text = history.beanName
            weight.text =
                "${history.roastedWeight} (${Math.round(history.roastedWeight / history.beanWeight.toFloat() * 10) / 10f}%)"
            time.text = Formatter.processTime(history.processTime)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
