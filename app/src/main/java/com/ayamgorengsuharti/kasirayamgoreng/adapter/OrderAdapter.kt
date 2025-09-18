// File: adapter/OrderAdapter.kt
package com.ayamgorengsuharti.kasirayamgoreng.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class OrderAdapter(
    private var orderList: List<Order>,
    private val onItemClick: (Int) -> Unit // <--- TAMBAH INI
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdCustomer: TextView = itemView.findViewById(R.id.tv_order_id_customer)
        val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        val tvItems: TextView = itemView.findViewById(R.id.tv_order_items)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        val tvStatusBayar: TextView = itemView.findViewById(R.id.tv_order_status_bayar)
        val tvStatusPesanan: TextView = itemView.findViewById(R.id.tv_order_status_pesanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        // Set ID dan Nama
        holder.tvIdCustomer.text = "Order #${order.id} - ${order.nama_pelanggan ?: "N/A"}"

        // VVVV TAMBAHIN BLOK INI VVVV
        // Format Waktu Order
        try {
            // Ini format input dari API (Contoh: 2025-09-14T11:17:49.000Z)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Penting, karena "Z" itu UTC

            // Ini format output yg kita mau (Contoh: 14 Sep 2025, 11:17)
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            val date = inputFormat.parse(order.waktu_order) // Ubah String jadi object Date
            holder.tvOrderTime.text = outputFormat.format(date) // Format ulang

        } catch (e: Exception) {
            // Kalo gagal format, tampilin aja apa adanya
            holder.tvOrderTime.text = order.waktu_order
        }
        // ^^^^ SAMPAI SINI ^^^^

        // Format Total Harga
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.tvTotal.text = "Total: ${formatter.format(order.total_harga)}"

        // Set Status Bayar (kasih warna)
        holder.tvStatusBayar.text = order.status_pembayaran
        when (order.status_pembayaran) {
            "LUNAS" -> holder.tvStatusBayar.setBackgroundColor(Color.parseColor("#C8E6C9"))
            "BELUM_BAYAR" -> holder.tvStatusBayar.setBackgroundColor(Color.parseColor("#FFCDD2"))
            else -> holder.tvStatusBayar.setBackgroundColor(Color.parseColor("#FFECB3"))
        }

        // Set Status Pesanan
        holder.tvStatusPesanan.text = order.status_pesanan
        when (order.status_pesanan) {
            "SELESAI" -> holder.tvStatusPesanan.setBackgroundColor(Color.parseColor("#B2EBF2"))
            "SIAP_DIAMBIL" -> holder.tvStatusPesanan.setBackgroundColor(Color.parseColor("#C8E6C9"))
            else -> holder.tvStatusPesanan.setBackgroundColor(Color.parseColor("#FFECB3"))
        }

        // Bikin rangkuman item
        val itemsSummary = order.orderItems.joinToString(separator = "\n") { item ->
            "• ${item.jumlah}x ${item.produk.nama_produk}"
        }
        holder.tvItems.text = itemsSummary

        holder.itemView.setOnClickListener {
            // Panggil fungsi lambda-nya, kirim ID orderan
            onItemClick(order.id)
        }
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }
}