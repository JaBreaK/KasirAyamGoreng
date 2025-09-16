// File: viewmodel/MenuAdminViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class MenuAdminViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // Pake model <Produk> yg udah kita punya
    private val _menuList = MutableLiveData<List<MenuResponse.Produk>>()
    val menuList: LiveData<List<MenuResponse.Produk>> = _menuList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        fetchMenu() // Langsung panggil pas dibuat
    }

    fun fetchMenu() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Pake fungsi yg udah ada
                val response = apiService.getAllMenu()
                _menuList.value = response
            } catch (e: Exception) {
                _message.value = "Gagal fetch menu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi baru buat delete
    fun deleteMenu(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                apiService.deleteMenu(id)
                _message.value = "Menu berhasil dihapus!"
                fetchMenu() // Refresh list
            } catch (e: Exception) {
                _message.value = "Gagal hapus menu: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}