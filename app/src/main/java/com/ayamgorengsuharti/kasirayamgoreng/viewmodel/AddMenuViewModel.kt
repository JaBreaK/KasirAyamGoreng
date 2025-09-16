// File: viewmodel/AddMenuViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse

import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddMenuViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // --- Untuk Spinner Kategori ---
    private val _kategoriList = MutableLiveData<List<MenuResponse.Kategori>>()
    val kategoriList: LiveData<List<MenuResponse.Kategori>> = _kategoriList

    // --- Untuk Status Upload ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResult = MutableLiveData<Result<MenuResponse.Produk>>()
    val uploadResult: LiveData<Result<MenuResponse.Produk>> = _uploadResult

    // Panggil ini pas halaman dibuka
    fun fetchKategori() {
        viewModelScope.launch {
            try {
                _kategoriList.value = apiService.getAllKategori()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Fungsi utama buat UPLOAD
    fun createMenu(
        context: Context, // Kita butuh context buat baca file
        imageUri: Uri,
        nama: String,
        deskripsi: String,
        harga: String,
        kategoriId: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Ubah String jadi RequestBody
                val namaBody = nama.toRequestBody("text/plain".toMediaTypeOrNull())
                val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())
                val hargaBody = harga.toRequestBody("text/plain".toMediaTypeOrNull())
                val kategoriIdBody = kategoriId.toRequestBody("text/plain".toMediaTypeOrNull())

                // 2. Ubah URI Gambar jadi MultipartBody.Part (Pake fungsi helper)
                val imagePart = uriToMultipartBody(context, imageUri, "gambar")

                if (imagePart == null) {
                    _uploadResult.value = Result.failure(Exception("Gagal memproses gambar"))
                    _isLoading.value = false
                    return@launch
                }

                // 3. Panggil API
                val response = apiService.createMenu(
                    imagePart,
                    namaBody,
                    deskripsiBody,
                    hargaBody,
                    kategoriIdBody
                )

                _uploadResult.value = Result.success(response)

            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- FUNGSI HELPER AJAIB ---
    // Buat ubah Uri (alamat file) jadi file yg bisa dikirim Retrofit
    private fun uriToMultipartBody(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        // 1. Dapatkan InputStream dari Uri
        val stream = context.contentResolver.openInputStream(uri) ?: return null

        // 2. Dapatkan nama file & tipe file
        val (fileName, fileType) = getFileNameAndMimeType(context, uri)

        // 3. Buat RequestBody dari stream
        val requestBody = stream.readBytes().toRequestBody(
            fileType?.toMediaTypeOrNull()
        )

        // 4. Buat MultipartBody.Part
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    // Helper buat dapet nama file & tipe MIME
    private fun getFileNameAndMimeType(context: Context, uri: Uri): Pair<String, String?> {
        var fileName = "temp_file"
        val mimeType = context.contentResolver.getType(uri)

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return Pair(fileName, mimeType)
    }
}