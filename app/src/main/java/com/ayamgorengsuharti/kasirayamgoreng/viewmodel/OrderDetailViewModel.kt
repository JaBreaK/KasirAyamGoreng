package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import com.ayamgorengsuharti.kasirayamgoreng.models.UpdateStatusPayload
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Context // IMPORT INI
import android.net.Uri // IMPORT INI
import android.provider.OpenableColumns // IMPORT INI
import com.ayamgorengsuharti.kasirayamgoreng.models.DeleteResponse // IMPORT INI
import okhttp3.MediaType.Companion.toMediaTypeOrNull // IMPORT INI
import okhttp3.MultipartBody // IMPORT INI
import okhttp3.RequestBody.Companion.toRequestBody // IMPORT INI

class OrderDetailViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _orderDetail = MutableLiveData<Order>()
    val orderDetail: LiveData<Order> = _orderDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    private val _updateResult = MutableLiveData<Result<Order>>()
    val updateResult: LiveData<Result<Order>> = _updateResult
    private val _uploadBuktiResult = MutableLiveData<Result<DeleteResponse>>()
    val uploadBuktiResult: LiveData<Result<DeleteResponse>> = _uploadBuktiResult

    // Fungsi buat ngambil detail 1 order
    fun fetchOrderDetail(orderId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getOrderDetail(orderId)
                _orderDetail.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateOrderStatus(orderId: Int, statusBayar: String, statusPesanan: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Buat payload-nya
                val payload = UpdateStatusPayload(
                    status_pembayaran = statusBayar,
                    status_pesanan = statusPesanan
                )
                // Panggil API
                val response = apiService.updateOrderStatus(orderId, payload)

                // Kirim hasil sukses
                _updateResult.value = Result.success(response)

                // Update juga data detail order yg lagi diliat
                _orderDetail.value = response

            } catch (e: Exception) {
                // Kirim hasil gagal
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun uploadBukti(context: Context, orderId: Int, imageUri: Uri) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Pake helper ajaib
                val imagePart = uriToMultipartBody(context, imageUri, "bukti")
                if (imagePart == null) {
                    _uploadBuktiResult.value = Result.failure(Exception("Gagal proses gambar bukti"))
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.uploadBuktiPembayaran(orderId, imagePart)
                _uploadBuktiResult.value = Result.success(response)

                // Kalo sukses upload, otomatis refresh detail order-nya
                // biar URL bukti bayarnya ke-update
                fetchOrderDetail(orderId)

            } catch (e: Exception) {
                _uploadBuktiResult.value = Result.failure(e)
                _isLoading.value = false
            }
        }
    }


    // VVVV COPY-PASTE 2 FUNGSI HELPER AJAIB DARI VIEWMODEL LAIN VVVV

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