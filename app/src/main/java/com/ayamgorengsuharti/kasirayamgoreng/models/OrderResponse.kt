// File: models/OrderResponse.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

// Ini data yg kita dapet SETELAH checkout berhasil
data class OrderResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("total_harga")
    val total_harga: Int,

    @SerializedName("status_pembayaran")
    val status_pembayaran: String,

    @SerializedName("status_pesanan")
    val status_pesanan: String,

    @SerializedName("nama_pelanggan")
    val nama_pelanggan: String? // Kita bikin nullable (boleh null)
)