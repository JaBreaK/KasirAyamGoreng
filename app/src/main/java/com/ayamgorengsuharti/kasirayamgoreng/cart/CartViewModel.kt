// File: CartViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderPayload
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderResponse
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
class CartViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // LiveData buat status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData buat status checkout (sukses/gagal)
    private val _checkoutResult = MutableLiveData<Result<OrderResponse>>()
    val checkoutResult: LiveData<Result<OrderResponse>> = _checkoutResult
    private val _metodeList = MutableLiveData<List<MetodePembayaran>>()
    val metodeList: LiveData<List<MetodePembayaran>> = _metodeList


    fun fetchMetodePembayaran() {
        viewModelScope.launch {
            try {
                val response = apiService.getMetodePembayaran()
                _metodeList.value = response
            } catch (e: Exception) {
                // Kalo gagal, kita bisa kirim list kosong atau handle error
                _metodeList.value = emptyList()
            }
        }
    }

    // GANTI JADI SATU FUNGSI INI AJA:
    fun checkout(payload: OrderPayload) { // <-- PARAMETERNYA CUMA 1 OBJEK
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Payload udah jadi dari Activity, tinggal lempar ke API
                val response = apiService.createOrder(payload)
                _checkoutResult.value = Result.success(response)
            } catch (e: Exception) {
                _checkoutResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}