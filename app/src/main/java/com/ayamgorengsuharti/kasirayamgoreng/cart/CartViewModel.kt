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

    fun checkout(namaPelanggan: String, nomorWa: String, catatan: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Ambil data dari CartManager
                val items = CartManager.cartItems
                val total = CartManager.getTotalHarga()

                // 2. Buat Payload (DENGAN DATA BARU)
                val payload = OrderPayload(
                    cartItems = items,
                    total_harga = total,
                    metode_pembayaran_id = 1,
                    nama_pelanggan = namaPelanggan,
                    nomor_wa = nomorWa,

                    // VVVV TAMBAHIN INI VVVV
                    tipe_pesanan = "OFFLINE", // Hardcode sesuai permintaan lo
                    catatan_pelanggan = catatan // Ambil dari parameter
                )

                // 3. Panggil API
                val response = apiService.createOrder(payload)

                // 4. Kasih kabar kalo sukses
                _checkoutResult.value = Result.success(response)

                // 5. Kosongin keranjang
                CartManager.clearCart()

            } catch (e: Exception) {
                // 6. Kasih kabar kalo gagal
                _checkoutResult.value = Result.failure(e)
            } finally {
                // 7. Berhenti loading
                _isLoading.value = false
            }
        }
    }
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

    // VVVV UPDATE FUNGSI CHECKOUT INI VVVV
    fun checkout(
        namaPelanggan: String,
        nomorWa: String,
        catatan: String,
        metodeId: Int // <--- TAMBAH PARAMETER INI
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val items = CartManager.cartItems
                val total = CartManager.getTotalHarga()

                val payload = OrderPayload(
                    cartItems = items,
                    total_harga = total,
                    // VVVV GANTI INI VVVV
                    metode_pembayaran_id = metodeId, // <-- PAKE PARAMETER
                    nama_pelanggan = namaPelanggan,
                    nomor_wa = nomorWa,
                    tipe_pesanan = "OFFLINE",
                    catatan_pelanggan = catatan
                )

                val response = apiService.createOrder(payload)
                _checkoutResult.value = Result.success(response)
                CartManager.clearCart()

            } catch (e: Exception) {
                _checkoutResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}