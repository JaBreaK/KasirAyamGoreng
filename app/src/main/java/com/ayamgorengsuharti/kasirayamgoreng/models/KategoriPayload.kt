// File: models/KategoriPayload.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

data class KategoriPayload(
    @SerializedName("nama_kategori")
    val nama_kategori: String
)