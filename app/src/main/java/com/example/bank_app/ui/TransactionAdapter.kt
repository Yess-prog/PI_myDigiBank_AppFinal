package com.example.bank_app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R

class TransactionAdapter(private val list: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.VH>() {

    data class Transaction(val label: String, val amount: Double)

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLabel: TextView = itemView.findViewById(R.id.transactionTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.transactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val t = list[pos]
        h.tvLabel.text = t.label
        h.tvAmount.text = if (t.amount > 0) "+${t.amount} TND" else "${t.amount} TND"
        h.tvAmount.setTextColor(if (t.amount > 0)
            ContextCompat.getColor(h.itemView.context, R.color.accent)
        else Color.RED)
    }

    override fun getItemCount() = list.size
}