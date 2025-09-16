// File: viewmodel/PosViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderPayload
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderResponse
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class PosViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // --- Daftar Master & Terfilter ---
    private val _masterMenuList = MutableLiveData<List<MenuResponse.Produk>>() // Ini nyimpen SEMUA menu

    private val _filteredMenuList = MutableLiveData<List<MenuResponse.Produk>>() // Ini yg diliatin ke user
    val filteredMenuList: LiveData<List<MenuResponse.Produk>> = _filteredMenuList

    // --- Daftar Kategori ---
    private val _kategoriList = MutableLiveData<List<MenuResponse.Kategori>>() // Buat spinner filter
    val kategoriList: LiveData<List<MenuResponse.Kategori>> = _kategoriList

    // --- Daftar Metode Bayar ---
    private val _metodeList = MutableLiveData<List<MetodePembayaran>>()
    val metodeList: LiveData<List<MetodePembayaran>> = _metodeList

    // --- Status ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // --- Checkout ---
    private val _checkoutResult = MutableLiveData<Result<OrderResponse>>()
    val checkoutResult: LiveData<Result<OrderResponse>> = _checkoutResult
    // Panggil ini pas fragment dibuat
    fun loadInitialData() {
        fetchMenu() // Ambil menu
        fetchKategori() // Ambil kategori
        fetchMetodePembayaran() // Ambil metode bayar
    }

    private fun fetchMenu() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val menu = apiService.getAllMenu()
                _masterMenuList.value = menu // Simpen di master
                _filteredMenuList.value = menu // Awalnya, tampilin semua
            } catch (e: Exception) {
                _message.value = "Gagal load menu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchKategori() {
        viewModelScope.launch {
            try {
                // Pake fungsi API yg udah kita bikin dulu
                _kategoriList.value = apiService.getAllKategori()
            } catch (e: Exception) {
                _message.value = "Gagal load kategori: ${e.message}"
            }
        }
    }

    private fun fetchMetodePembayaran() {
        viewModelScope.launch {
            try {
                _metodeList.value = apiService.getMetodePembayaran()
            } catch (e: Exception) {
                _message.value = "Gagal load metode bayar: ${e.message}"
            }
        }
    }

    // --- FUNGSI BARU BUAT FILTER ---
    fun filterMenu(kategoriId: Int) {
        // Kalo ID-nya 0 (tombol "SEMUA")
        if (kategoriId == 0) {

            // GANTI BARIS INI:
            // _filteredMenuList.value = _masterMenuList.value (INI ERROR LAMA)

            // JADI INI:
            // Kalo master list-nya null, kasih list kosong aja
            _filteredMenuList.value = _masterMenuList.value ?: emptyList()
            return
        }

        // Kalo milih ID tertentu
        val filtered = _masterMenuList.value?.filter { produk ->
            produk.kategoriId == kategoriId
        }

        // GANTI BARIS INI:
        // _filteredMenuList.value = filtered (INI ERROR DI SCREENSHOT LO)

        // JADI INI:
        // Kalo hasil filter-nya null, kasih list kosong aja
        _filteredMenuList.value = filtered ?: emptyList()
    }

    // Fungsi Checkout (copy dari CartViewModel, tapi data customernya kita hardcode)
    fun checkout(
        payload: OrderPayload // Terima payload yg udah jadi
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
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