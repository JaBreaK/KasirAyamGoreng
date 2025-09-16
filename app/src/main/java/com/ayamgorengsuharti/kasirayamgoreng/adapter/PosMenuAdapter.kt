// File: adapter/PosMenuAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import java.text.NumberFormat
import java.util.Locale

class PosMenuAdapter(
    private var menuList: List<MenuResponse.Produk>,
    private val onItemClick: (MenuResponse.Produk) -> Unit // Sinyal kalo item diklik
) : RecyclerView.Adapter<PosMenuAdapter.PosViewHolder>() {

    inner class PosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgItem: ImageView = itemView.findViewById(R.id.img_pos_item)
        val tvName: TextView = itemView.findViewById(R.id.tv_pos_item_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_pos_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pos_menu, parent, false)
        return PosViewHolder(view)
    }

    override fun onBindViewHolder(holder: PosViewHolder, position: Int) {
        val produk = menuList[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvName.text = produk.namaProduk
        holder.tvPrice.text = formatter.format(produk.harga)

        Glide.with(holder.itemView.context)
            .load(produk.gambarUrl)
            .into(holder.imgItem)

        holder.itemView.setOnClickListener {
            onItemClick(produk) // Kirim sinyal!
        }
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newList: List<MenuResponse.Produk>) {
        menuList = newList
        notifyDataSetChanged()
    }
}