// File: adapter/KategoriAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // IMPORT INI
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse


// VVVV UPDATE CONSTRUCTOR-NYA VVVV
class KategoriAdapter(
    private var kategoriList: List<MenuResponse.Kategori>,
    private val onEditClick: (MenuResponse.Kategori) -> Unit,    // Fungsi buat kirim sinyal Edit
    private val onDeleteClick: (MenuResponse.Kategori) -> Unit // Fungsi buat kirim sinyal Delete
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    // VVVV UPDATE VIEWHOLDER-NYA VVVV
    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_kategori_name)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_kategori)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_kategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.tvName.text = kategori.namaKategori // Ingat, pake camelCase

        // VVVV KASIH LISTENER KE TOMBOL VVVV
        holder.btnEdit.setOnClickListener {
            onEditClick(kategori) // Kirim sinyal edit + datanya
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(kategori) // Kirim sinyal delete + datanya
        }
    }

    override fun getItemCount(): Int = kategoriList.size

    fun updateData(newList: List<MenuResponse.Kategori>) {
        kategoriList = newList
        notifyDataSetChanged()
    }
}