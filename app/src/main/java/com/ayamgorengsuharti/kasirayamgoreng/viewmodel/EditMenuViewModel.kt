// File: viewmodel/EditMenuViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse

import com.ayamgorengsuharti.kasirayamgoreng.models.ProdukDetail
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditMenuViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // Data Kategori (buat spinner)
    private val _kategoriList = MutableLiveData<List<MenuResponse.Kategori>>()
    val kategoriList: LiveData<List<MenuResponse.Kategori>> = _kategoriList

    // Data Menu Lama (buat ngisi form)
    private val _menuDetail = MutableLiveData<ProdukDetail>()
    val menuDetail: LiveData<ProdukDetail> = _menuDetail

    // Status
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateResult = MutableLiveData<Result<MenuResponse.Produk>>()
    val updateResult: LiveData<Result<MenuResponse.Produk>> = _updateResult

    // Ambil kategori (buat spinner)
    fun fetchKategori() {
        viewModelScope.launch {
            try {
                _kategoriList.value = apiService.getAllKategori()
            } catch (e: Exception) { /* biarin */ }
        }
    }

    // Ambil data menu lama (buat form)
    fun fetchMenuDetail(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _menuDetail.value = apiService.getMenuDetail(id)
            } catch (e: Exception) { /* handle error */ }
            finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi UPDATE
    fun updateMenu(
        context: Context,
        menuId: Int,
        newImageUri: Uri?, // Gambar baru (Boleh null kalo user nggak ganti)
        nama: String,
        deskripsi: String,
        harga: String,
        kategoriId: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Ubah String jadi RequestBody (sama kayak Add)
                val namaBody = nama.toRequestBody("text/plain".toMediaTypeOrNull())
                val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())
                val hargaBody = harga.toRequestBody("text/plain".toMediaTypeOrNull())
                val kategoriIdBody = kategoriId.toRequestBody("text/plain".toMediaTypeOrNull())

                // 2. Cek gambar: Kalo ada URI baru, proses. Kalo nggak, kirim null.
                val imagePart: MultipartBody.Part? = if (newImageUri != null) {
                    uriToMultipartBody(context, newImageUri, "gambar")
                } else {
                    null
                }

                // 3. Panggil API (updateMenu)
                val response = apiService.updateMenu(
                    menuId,
                    imagePart, // Kirim imagePart (bisa null)
                    namaBody,
                    deskripsiBody,
                    hargaBody,
                    kategoriIdBody
                )

                _updateResult.value = Result.success(response)

            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- COPY-PASTE FUNGSI HELPER AJAIB dari AddMenuViewModel ---
    private fun uriToMultipartBody(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val (fileName, fileType) = getFileNameAndMimeType(context, uri)
        val requestBody = stream.readBytes().toRequestBody(fileType?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    private fun getFileNameAndMimeType(context: Context, uri: Uri): Pair<String, String?> {
        var fileName = "temp_file"
        val mimeType = context.contentResolver.getType(uri)
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
            }
        }
        return Pair(fileName, mimeType)
    }
}