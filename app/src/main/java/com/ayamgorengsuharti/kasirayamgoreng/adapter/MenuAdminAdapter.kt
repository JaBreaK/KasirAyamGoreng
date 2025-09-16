// File: adapter/MenuAdminAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse

import java.text.NumberFormat
import java.util.Locale

// Pake pola listener kayak KategoriAdapter
class MenuAdminAdapter(
    private var menuList: List<MenuResponse.Produk>,
    private val onEditClick: (MenuResponse.Produk) -> Unit,
    private val onDeleteClick: (MenuResponse.Produk) -> Unit
) : RecyclerView.Adapter<MenuAdminAdapter.MenuAdminViewHolder>() {

    // VVVV UPDATE VIEWHOLDER VVVV
    inner class MenuAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu: ImageView = itemView.findViewById(R.id.img_menu)
        val tvNama: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val tvHarga: TextView = itemView.findViewById(R.id.tv_harga)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_menu) // <--- TAMBAH INI
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuAdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_admin, parent, false)
        return MenuAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuAdminViewHolder, position: Int) {
        val produk = menuList[position]

        holder.tvNama.text = produk.namaProduk // Ingat, camelCase

        // Format harga biar cantik
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.tvHarga.text = formatter.format(produk.harga)

        Glide.with(holder.itemView.context)
            .load(produk.gambarUrl)
            .into(holder.imgMenu)

        // VVVV KASIH LISTENER BARU VVVV
        holder.btnEdit.setOnClickListener {
            onEditClick(produk)
        }

        // Kasih sinyal delete
        holder.btnDelete.setOnClickListener {
            onDeleteClick(produk)
        }
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newList: List<MenuResponse.Produk>) {
        menuList = newList
        notifyDataSetChanged()
    }
}