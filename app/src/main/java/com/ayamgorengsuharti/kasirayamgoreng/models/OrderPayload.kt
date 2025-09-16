// Nama file: models/OrderPayload.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

// Ini adalah class untuk SATU ITEM di dalem keranjang
// Isinya gabungan dari 'Produk' + 'jumlah'
data class CartItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_produk")
    val nama_produk: String,

    @SerializedName("deskripsi")
    val deskripsi: String,

    @SerializedName("harga")
    val harga: Int,

    @SerializedName("gambar_url")
    val gambar_url: String,

    @SerializedName("kategori_id")
    val kategori_id: Int,

    @SerializedName("kategori")
    val kategori: MenuResponse.Kategori, // Ini pake Kategori class dari MenuResponse.kt

    @SerializedName("jumlah")
    var jumlah: Int // Pake 'var' biar bisa diubah-ubah
)

// Ini adalah class "bungkusan" utamanya yg dikirim ke API
// VVVV UPDATE CLASS INI VVVV
data class OrderPayload(
    @SerializedName("cartItems")
    val cartItems: List<CartItem>,

    @SerializedName("total_harga")
    val total_harga: Int,

    @SerializedName("metode_pembayaran_id")
    val metode_pembayaran_id: Int,

    @SerializedName("nama_pelanggan")
    val nama_pelanggan: String,

    @SerializedName("nomor_wa")
    val nomor_wa: String,

    // VVVV TAMBAHIN 2 BARIS INI VVVV
    @SerializedName("tipe_pesanan")
    val tipe_pesanan: String, // "OFFLINE"

    @SerializedName("catatan_pelanggan")
    val catatan_pelanggan: String // "Catatan dari kasir"
)