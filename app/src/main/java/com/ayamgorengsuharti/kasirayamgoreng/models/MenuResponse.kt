package com.ayamgorengsuharti.kasirayamgoreng.models
import com.google.gson.annotations.SerializedName

class MenuResponse {
    data class Produk(
        @SerializedName("id")
        val id: Int,

        @SerializedName("nama_produk")
        val namaProduk: String,

        @SerializedName("deskripsi")
        val deskripsi: String,

        @SerializedName("harga")
        val harga: Int,

        @SerializedName("gambar_url")
        val gambarUrl: String,

        @SerializedName("kategori_id")
        val kategoriId: Int,

        @SerializedName("kategori")
        val kategori: Kategori
    )

    // Ini mewakili object 'kategori' yang ada di dalem 'Produk'
    data class Kategori(
        @SerializedName("id")
        val id: Int,

        @SerializedName("nama_kategori")
        val namaKategori: String
    )
}