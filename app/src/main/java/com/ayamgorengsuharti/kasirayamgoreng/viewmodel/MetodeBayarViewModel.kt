// File: viewmodel/MetodeBayarViewModel.kt
package com.ayamgorengsuharti.kasirayamgoreng.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.network.RetrofitClient
import kotlinx.coroutines.launch

class MetodeBayarViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _metodeList = MutableLiveData<List<MetodePembayaran>>()
    val metodeList: LiveData<List<MetodePembayaran>> = _metodeList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        fetchMetode()
    }

    fun fetchMetode() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Pake fungsi yg udah kita bikin buat CartActivity
                _metodeList.value = apiService.getMetodePembayaran()
            } catch (e: Exception) {
                _message.value = "Gagal fetch: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi update-nya nanti kita bikin di ViewModel-nya halaman EDIT
}