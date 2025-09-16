// File: adapter/CartAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem

// Adapter ini sekarang butuh 2 "sinyal" baru
class CartAdapter(
    private var cartList: List<CartItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder-nya di-update
    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_cart_item_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_cart_item_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_cart_item_quantity)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease_qty)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase_qty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.tvName.text = item.nama_produk
        holder.tvPrice.text = "Rp ${item.harga}"
        holder.tvQuantity.text = item.jumlah.toString() // Tampilkan jumlah

        // Kasih sinyal kalo tombol + diklik
        holder.btnIncrease.setOnClickListener {
            onIncrease(item)
        }

        // Kasih sinyal kalo tombol - diklik
        holder.btnDecrease.setOnClickListener {
            onDecrease(item)
        }
    }

    override fun getItemCount(): Int = cartList.size

    fun updateData(newList: List<CartItem>) {
        cartList = newList
        notifyDataSetChanged() // Tetep pake ini buat refresh
    }
}