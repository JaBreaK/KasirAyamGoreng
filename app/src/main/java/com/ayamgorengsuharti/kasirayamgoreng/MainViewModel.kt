package com.ayamgorengsuharti.kasirayamgoreng
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient

import kotlinx.coroutines.launch

    // ViewModel ini butuh ApiService buat kerja
    class MainViewModel : ViewModel() {

        // Ambil ApiService dari RetrofitClient
        private val apiService = RetrofitClient.apiService

        // Data menu (Pake MutableLiveData biar bisa diubah)
        // Ini yang "dilihat" sama Activity
        private val _menuList = MutableLiveData<List<MenuResponse.Produk>>()
        val menuList: LiveData<List<MenuResponse.Produk>> = _menuList

        // Data status loading
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        // Data error
        private val _errorMessage = MutableLiveData<String>()
        val errorMessage: LiveData<String> = _errorMessage

        // Fungsi buat manggil API
        fun fetchMenu() {
            _isLoading.value = true // Mulai loading

            // Pake viewModelScope biar aman (lifecycle-aware)
            viewModelScope.launch {
                try {
                    // Panggil fungsi di ApiService
                    val response = apiService.getAllMenu()
                    _menuList.value = response // Sukses, update datanya
                } catch (e: Exception) {
                    // Gagal
                    _errorMessage.value = e.message
                } finally {
                    _isLoading.value = false // Selesai loading
                }
            }
        }
    }
