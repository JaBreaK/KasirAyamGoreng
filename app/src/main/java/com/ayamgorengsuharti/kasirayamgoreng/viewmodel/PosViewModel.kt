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
import java.util.Locale // IMPORT INI

class PosViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // Data Master (Backup)
    private val _masterMenuList = MutableLiveData<List<MenuResponse.Produk>>()

    // Data yang diliatin ke UI
    private val _filteredMenuList = MutableLiveData<List<MenuResponse.Produk>>()
    val filteredMenuList: LiveData<List<MenuResponse.Produk>> = _filteredMenuList

    // Data Kategori & Metode Bayar
    private val _kategoriList = MutableLiveData<List<MenuResponse.Kategori>>()
    val kategoriList: LiveData<List<MenuResponse.Kategori>> = _kategoriList
    private val _metodeList = MutableLiveData<List<MetodePembayaran>>()
    val metodeList: LiveData<List<MetodePembayaran>> = _metodeList

    // Variabel buat nyimpen kondisi filter terakhir
    private var currentQuery: String = ""
    private var currentCategoryId: Int = 0 // 0 = "SEMUA"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // --- Checkout ---
    private val _checkoutResult = MutableLiveData<Result<OrderResponse>>()
    val checkoutResult: LiveData<Result<OrderResponse>> = _checkoutResult

    // ... (Status LiveData: isLoading, message, checkoutResult biarin aja) ...



    fun loadInitialData() {
        fetchMenu()
        fetchKategori()
        fetchMetodePembayaran()
    }

    private fun fetchMenu() {
        (isLoading as MutableLiveData).value = true
        viewModelScope.launch {
            try {
                val menu = apiService.getAllMenu()
                _masterMenuList.value = menu
                applyFilters() // Panggil filter (awalannya kosong, jadi nampilin semua)
            } catch (e: Exception) {
                (message as MutableLiveData).value = "Gagal load menu: ${e.message}"
            } finally {
                (isLoading as MutableLiveData).value = false
            }
        }
    }

    private fun fetchKategori() {
        viewModelScope.launch {
            try {
                _kategoriList.value = apiService.getAllKategori()
            } catch (e: Exception) {
                (message as MutableLiveData).value = "Gagal load kategori: ${e.message}"
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

    // --- LOGIKA FILTER BARU (DIGABUNG) ---

    // Dipanggil dari Spinner
    fun filterByCategory(kategoriId: Int) {
        currentCategoryId = kategoriId // Simpen kondisi baru
        applyFilters() // Jalanin filter gabungan
    }

    // Dipanggil dari Search Bar
    fun searchMenu(query: String) {
        currentQuery = query // Simpen kondisi baru
        applyFilters() // Jalanin filter gabungan
    }

    // INI MESIN FILTER GABUNGAN-nya
    private fun applyFilters() {
        var filteredList = _masterMenuList.value ?: emptyList()

        // 1. Filter dulu pake Search Bar (Teks)
        if (currentQuery.isNotBlank()) {
            filteredList = filteredList.filter { produk ->
                produk.namaProduk.lowercase(Locale.ROOT).contains(currentQuery.lowercase(Locale.ROOT))
            }
        }

        // 2. Hasil filter teks, kita saring LAGI pake Kategori
        if (currentCategoryId != 0) { // 0 artinya "SEMUA KATEGORI"
            filteredList = filteredList.filter { produk ->
                produk.kategoriId == currentCategoryId
            }
        }

        // 3. Kirim hasil akhir ke UI
        _filteredMenuList.value = filteredList
    }


    // --- Fungsi Checkout ---
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
    fun clearCheckoutResult() {
        _checkoutResult.value = null // Set datanya balik jadi null (netral)
    }
}