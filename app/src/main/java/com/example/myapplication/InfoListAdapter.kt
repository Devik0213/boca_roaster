package com.example.myapplication

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.RoatingInfoBinding
import com.example.myapplication.databinding.RoatingInfoEditableBinding
import java.text.SimpleDateFormat

class InfoListAdapter(context: Context, dateFormat: SimpleDateFormat) :
    RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val EDITABLE = 1000
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    val list =
        arrayListOf(Info.Date(""), Info.BeanWeight(""), Info.Name(""), Info.RoastedBeanWeight(""))


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RoatingInfoEditableBinding.inflate(inflater, parent, false).takeIf {
            viewType == EDITABLE
        } ?: RoatingInfoBinding.inflate(inflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return EDITABLE.takeIf {
            list[position].isEditable
        } ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = list[position]
        holder.binding.apply {
            if (this is RoatingInfoBinding) {
                label.text = info.label
                value.text = info.value
            } else if (this is RoatingInfoEditableBinding) {
                label.text = info.label
                label.text = info.value
                value.inputType = InputType.TYPE_CLASS_TEXT.takeIf {
                    info is Info.Name
                } ?: InputType.TYPE_CLASS_NUMBER
                value.addTextChangedListener {
                    info.value = it.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}