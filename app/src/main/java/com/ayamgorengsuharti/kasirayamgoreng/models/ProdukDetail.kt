// File: models/ProdukDetail.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

// Ini mirip kayak Produk, tapi flat
data class ProdukDetail(
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
    val kategori_id: Int
)