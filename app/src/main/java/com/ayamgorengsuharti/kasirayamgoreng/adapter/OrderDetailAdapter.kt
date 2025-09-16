// File: adapter/OrderDetailAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderItem
import java.text.NumberFormat
import java.util.Locale

class OrderDetailAdapter(private var itemList: List<OrderItem>) :
    RecyclerView.Adapter<OrderDetailAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvQuantity.text = "${item.jumlah}x"
        holder.tvName.text = item.produk.nama_produk
        holder.tvPrice.text = formatter.format(item.produk.harga * item.jumlah)
    }

    override fun getItemCount(): Int = itemList.size

    fun updateData(newList: List<OrderItem>) {
        itemList = newList
        notifyDataSetChanged()
    }
}