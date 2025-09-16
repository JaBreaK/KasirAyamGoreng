package com.ayamgorengsuharti.kasirayamgoreng.adapter
// Nama file: adapter/MenuAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
// Ganti 'com.ayamgorengsuharti.kasirayamgoreng' kalo package lo beda
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import android.widget.Toast
import com.ayamgorengsuharti.kasirayamgoreng.CartManager
//               INI PENTING VVVVVVVVVVVVVVVVVVVVVVVVVVVV
class MenuAdapter(private var menuList: List<MenuResponse.Produk>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() { // <--- INI JUGA PENTING BANGET

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu: ImageView = itemView.findViewById(R.id.img_menu)
        val tvNama: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tv_deskripsi)
        val tvHarga: TextView = itemView.findViewById(R.id.tv_harga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val produk = menuList[position]

        holder.tvNama.text = produk.namaProduk
        holder.tvDeskripsi.text = produk.deskripsi
        holder.tvHarga.text = "Rp ${produk.harga}"

        Glide.with(holder.itemView.context)
            .load(produk.gambarUrl)
            .into(holder.imgMenu)

        // VVVV TAMBAHIN INI VVVV
        holder.itemView.setOnClickListener {
            // Panggil fungsi yg kita bikin tadi
            CartManager.addProduk(produk)

            // Kasih feedback ke user
            Toast.makeText(
                holder.itemView.context,
                "${produk.namaProduk} ditambahkan ke keranjang",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = menuList.size

    // FUNGSI INI PASTI LO LUPA TAMBAHIN VVVVVVVVVVVVVVV
    fun updateData(newList: List<MenuResponse.Produk>) {
        menuList = newList
        notifyDataSetChanged()
    }
}