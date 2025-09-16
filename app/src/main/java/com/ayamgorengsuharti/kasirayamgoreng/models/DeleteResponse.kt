// File: models/DeleteResponse.kt
package com.ayamgorengsuharti.kasirayamgoreng.models

import com.google.gson.annotations.SerializedName

data class DeleteResponse(
    @SerializedName("message")
    val message: String
)