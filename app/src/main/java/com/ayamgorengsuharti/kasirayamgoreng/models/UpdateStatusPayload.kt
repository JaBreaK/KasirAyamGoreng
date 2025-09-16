// File: models/UpdateStatusPayload.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

// Ini data yg kita KIRIM buat update
data class UpdateStatusPayload(
    @SerializedName("status_pembayaran")
    val status_pembayaran: String? = null, // Bikin nullable, siapa tahu kita cuma update salah satu

    @SerializedName("status_pesanan")
    val status_pesanan: String? = null
)