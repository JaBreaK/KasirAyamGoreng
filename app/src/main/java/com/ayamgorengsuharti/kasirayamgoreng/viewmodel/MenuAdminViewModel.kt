// File: viewmodel/MenuAdminViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse

import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale // Pastiin import ini ada

class MenuAdminViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // INI YANG DILIATIN KE UI (HASIL FILTER)
    private val _menuList = MutableLiveData<List<MenuResponse.Produk>>()
    val menuList: LiveData<List<MenuResponse.Produk>> = _menuList

    // INI BACKUP DATA ASLI DARI API
    private var masterMenuList: List<MenuResponse.Produk> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        fetchMenu()
    }

    fun fetchMenu() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getAllMenu()
                masterMenuList = response // Simpen ke backup
                _menuList.value = response  // Tampilkan ke UI
            } catch (e: Exception) {
                _message.value = "Gagal fetch menu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi delete (tetep sama)
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

    // VVVV FUNGSI BARU BUAT SEARCH VVVV
    fun searchMenu(query: String) {
        // Kalo search bar kosong, balikin full list
        if (query.isBlank()) {
            _menuList.value = masterMenuList
            return
        }

        // Kalo ada ketikan, saring MASTER LIST-nya
        val filteredList = masterMenuList.filter { produk ->
            // Filter cuma berdasarkan nama produk
            produk.namaProduk.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
        }

        // Tampilkan HASIL SARINGAN ke UI
        _menuList.value = filteredList
    }
}