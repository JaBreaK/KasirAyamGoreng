// File: models/MetodePembayaran.kt
package com.ayamgorengsuharti.kasirayamgoreng.models
import java.io.Serializable
import com.google.gson.annotations.SerializedName

data class MetodePembayaran(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_metode")
    val nama_metode: String,

    // Kalo lo belum nambahin field lain, TAMBAHIN SEKARANG
    @SerializedName("is_active")
val is_active: Boolean,

@SerializedName("nomor_rekening")
val nomor_rekening: String?,

@SerializedName("nama_rekening")
val nama_rekening: String?,

@SerializedName("gambar_qris_url")
val gambar_qris_url: String?

) : java.io.Serializable // <--- TAMBAH INI (PENTING BANGET)