// File: adapter/MetodeBayarAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran

class MetodeBayarAdapter(
    private var metodeList: List<MetodePembayaran>,
    private val onEditClick: (MetodePembayaran) -> Unit
) : RecyclerView.Adapter<MetodeBayarAdapter.MetodeViewHolder>() {

    inner class MetodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaMetode: TextView = itemView.findViewById(R.id.tv_nama_metode)
        val tvNamaRekening: TextView = itemView.findViewById(R.id.tv_nama_rekening)
        val tvNomorRekening: TextView = itemView.findViewById(R.id.tv_nomor_rekening)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_metode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metode_bayar, parent, false)
        return MetodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetodeViewHolder, position: Int) {
        val metode = metodeList[position]

        holder.tvNamaMetode.text = metode.nama_metode
        holder.tvNamaRekening.text = metode.nama_rekening
        holder.tvNomorRekening.text = metode.nomor_rekening

        // Kalo nomor rekening kosong (kayak QRIS atau Cash), sembunyiin text-nya
        holder.tvNomorRekening.visibility = if (metode.nomor_rekening.isNullOrEmpty()) View.GONE else View.VISIBLE

        holder.btnEdit.setOnClickListener {
            onEditClick(metode) // Kirim sinyal edit + semua datanya
        }
    }

    override fun getItemCount(): Int = metodeList.size

    fun updateData(newList: List<MetodePembayaran>) {
        metodeList = newList
        notifyDataSetChanged()
    }
}