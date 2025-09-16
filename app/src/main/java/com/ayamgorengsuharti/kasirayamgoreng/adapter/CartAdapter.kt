// File: adapter/CartAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem

// Adapter ini nerima List<CartItem>
class CartAdapter(private var cartList: List<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Bikin ViewHolder
    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgItem: ImageView = itemView.findViewById(R.id.img_cart_item)
        val tvName: TextView = itemView.findViewById(R.id.tv_cart_item_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_cart_item_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_cart_item_quantity)
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
        holder.tvQuantity.text = "x ${item.jumlah}" // Tampilin jumlah

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .into(holder.imgItem)
    }

    override fun getItemCount(): Int = cartList.size

    // Nanti kita pake ini buat refresh list
    fun updateData(newList: List<CartItem>) {
        cartList = newList
        notifyDataSetChanged()
    }
}