// File: models/Order.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

// Ini adalah satu object orderan di dalam list
data class Order(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_pelanggan")
    val nama_pelanggan: String?,

    @SerializedName("total_harga")
    val total_harga: Int,

    @SerializedName("status_pembayaran")
    val status_pembayaran: String,

    @SerializedName("status_pesanan")
    val status_pesanan: String,

    @SerializedName("tipe_pesanan")
    val tipe_pesanan: String,

    @SerializedName("waktu_order")
    val waktu_order: String, // Ini String dulu, gampang

    // Ini adalah list produk yg dipesan
    @SerializedName("orderitems")
    val orderItems: List<OrderItem>,

    @SerializedName("pembayaran")
val pembayaran: List<PembayaranItem>?

    // Kita cuekin dulu 'pembayaran' biar gampang
)

// Ini adalah satu item di dalam 'orderItems'
data class OrderItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("jumlah")
    val jumlah: Int,

    // Di dalem order item ada info produknya
    @SerializedName("produk")
    val produk: OrderProduk
)

// Ini adalah data produk di dalem 'orderItems'
// Kita bikin baru, jangan pake 'Produk' yg lama
// biar gampang dan nggak bentrok
data class OrderProduk(
    @SerializedName("nama_produk")
    val nama_produk: String,

    @SerializedName("harga")
    val harga: Int
)

data class PembayaranItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("bukti_pembayaran_url")
    val bukti_pembayaran_url: String?, // <-- INI YANG KITA CARI

    @SerializedName("metodepembayaran")
    val metodepembayaran: MetodeDetail? // Info metode yg dipake
)

// Info metode di dalem "pembayaran"
data class MetodeDetail(
    @SerializedName("id")
    val id: Int, // <-- INI YANG KITA PAKE BUAT VALIDASI (1 atau 2)

    @SerializedName("nama_metode")
    val nama_metode: String
)