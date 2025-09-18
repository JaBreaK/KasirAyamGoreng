package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // INI YANG DILIATIN KE UI (TETEP SAMA)
    private val _orderList = MutableLiveData<List<Order>>()
    val orderList: LiveData<List<Order>> = _orderList

    // VVVV TAMBAHIN INI VVVV
    // INI TEMPAT KITA NYIMPEN DATA ASLI DARI API (BACKUP)
    private var masterOrderList: List<Order> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchOrders() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getAllOrders().sortedByDescending { it.id }

                // VVVV UPDATE LOGIKA INI VVVV
                // 1. Simpen data asli ke backup
                masterOrderList = response
                // 2. Tampilkan data ke UI
                _orderList.value = response

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // VVVV TAMBAHIN FUNGSI BARU INI (MESIN PENCARIANNYA) VVVV
    fun search(query: String) {
        // Kalo search bar-nya kosong, balikin semua data dari master
        if (query.isBlank()) {
            _orderList.value = masterOrderList
            return
        }

        // Kalo ada ketikan, saring (filter) MASTER LIST-nya
        val filteredList = masterOrderList.filter { order ->
            // Cek 3 field (ID, Nama, WA)
            val matchesId = order.id.toString().contains(query, ignoreCase = true)
            val matchesName = order.nama_pelanggan?.contains(query, ignoreCase = true) == true
            val matchesWa = order.nomor_wa?.contains(query, ignoreCase = true) == true

            // Kalo salah satu aja cocok, tampilin
            matchesId || matchesName || matchesWa
        }

        // Tampilkan HASIL SARINGAN ke UI
        _orderList.value = filteredList
    }
}