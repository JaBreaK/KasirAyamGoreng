// File: OrderViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.orderlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _orderList = MutableLiveData<List<Order>>()
    val orderList: LiveData<List<Order>> = _orderList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchOrders() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Panggil API
                val response = apiService.getAllOrders()
                // Kita urutin dari yg paling baru (ID paling besar)
                _orderList.value = response.sortedByDescending { it.id }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}