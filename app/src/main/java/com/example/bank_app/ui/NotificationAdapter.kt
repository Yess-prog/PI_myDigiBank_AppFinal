package com.example.bank_app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R
import com.example.bank_app.databinding.ItemNotificationBinding

data class Notif(val iconRes: Int, val text: String)

class NotificationAdapter(private val list: List<Notif>) :
    RecyclerView.Adapter<NotificationAdapter.VH>() {

    class VH(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = list[position]
        holder.binding.ivIcon.setImageResource(n.iconRes)
        holder.binding.tvText.text = n.text
    }

    override fun getItemCount() = list.size
}