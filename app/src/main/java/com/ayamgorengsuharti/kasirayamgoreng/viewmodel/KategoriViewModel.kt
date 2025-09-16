// File: viewmodel/KategoriViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.ayamgorengsuharti.kasirayamgoreng.models.KategoriPayload
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class KategoriViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _kategoriList = MutableLiveData<List<MenuResponse.Kategori>>()
    val kategoriList: LiveData<List<MenuResponse.Kategori>> = _kategoriList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        fetchKategori() // Langsung panggil pas ViewModel dibuat
    }

    fun fetchKategori() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getAllKategori()
                _kategoriList.value = response
            } catch (e: Exception) {
                _message.value = "Gagal fetch: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createKategori(namaKategori: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val payload = KategoriPayload(nama_kategori = namaKategori)
                apiService.createKategori(payload)
                _message.value = "Kategori '${namaKategori}' berhasil ditambahkan!"
                // Setelah sukses, refresh list-nya
                fetchKategori()
            } catch (e: Exception) {
                _message.value = "Gagal nambah: ${e.message}"
                _isLoading.value = false // Hanya set false kalo gagal, kalo sukses udah dihandle fetchKategori()
            }
        }
    }

    fun updateKategori(id: Int, namaKategoriBaru: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val payload = KategoriPayload(nama_kategori = namaKategoriBaru)
                apiService.updateKategori(id, payload)
                _message.value = "Kategori berhasil di-update!"
                fetchKategori() // Refresh list
            } catch (e: Exception) {
                _message.value = "Gagal update: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteKategori(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                apiService.deleteKategori(id)
                _message.value = "Kategori berhasil dihapus!"
                fetchKategori() // Refresh list
            } catch (e: Exception) {
                _message.value = "Gagal hapus: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Nanti kita tambahin fun updateKategori() dan deleteKategori() di sini
}