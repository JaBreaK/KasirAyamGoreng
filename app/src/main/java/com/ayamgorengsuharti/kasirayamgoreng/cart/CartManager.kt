// File: CartManager.kt
package com.ayamgorengsuharti.kasirayamgoreng

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse

// Pake 'object' (Singleton)
object CartManager {

    // 1. Kita bikin list-nya pake LiveData. Ini "sumber siaran"-nya.
    // Tipenya adalah MutableList dari CartItem, diawali list kosong
    private val _cartItemsLiveData = MutableLiveData<List<CartItem>>(emptyList())

    // Ini "channel TV"-nya, yang bisa ditonton sama Fragment/Activity
    val cartItemsLiveData: LiveData<List<CartItem>> = _cartItemsLiveData

    // 2. Fungsi nambah produk (logikanya di-upgrade)
    fun addProduk(produk: MenuResponse.Produk) {
        val currentList = _cartItemsLiveData.value.orEmpty().toMutableList()
        val existingItem = currentList.find { it.id == produk.id }

        if (existingItem != null) {
            existingItem.jumlah++
        } else {
            // Ubah Produk jadi CartItem
            val cartItem = CartItem(
                id = produk.id,
                nama_produk = produk.namaProduk,
                deskripsi = produk.deskripsi,
                harga = produk.harga,
                gambar_url = produk.gambarUrl,
                kategori_id = produk.kategoriId,
                kategori = produk.kategori,
                jumlah = 1
            )
            currentList.add(cartItem)
        }
        // "Siarkan" list yang baru!
        _cartItemsLiveData.value = currentList
    }

    // 3. FUNGSI BARU: Nambah Kuantitas (+)
    fun increaseQuantity(item: CartItem) {
        val currentList = _cartItemsLiveData.value.orEmpty().toMutableList()
        val itemDiList = currentList.find { it.id == item.id } ?: return

        itemDiList.jumlah++
        _cartItemsLiveData.value = currentList // Siarkan lagi
    }

    // 4. FUNGSI BARU: Ngurangin Kuantitas (-)
    fun decreaseQuantity(item: CartItem) {
        val currentList = _cartItemsLiveData.value.orEmpty().toMutableList()
        val itemDiList = currentList.find { it.id == item.id } ?: return

        if (itemDiList.jumlah > 1) {
            // Kalo jumlah masih di atas 1, kurangin aja
            itemDiList.jumlah--
        } else {
            // Kalo jumlahnya 1 terus dikurangin, hapus item-nya dari list
            currentList.remove(itemDiList)
        }
        _cartItemsLiveData.value = currentList // Siarkan lagi
    }

    // 5. Helper buat ngitung subtotal (cuma harga * jumlah)
    fun getSubtotal(): Int {
        return _cartItemsLiveData.value.orEmpty().sumOf { it.harga * it.jumlah }
    }

    // 6. Fungsi clear (tetep perlu)
    fun clearCart() {
        _cartItemsLiveData.value = emptyList() // Siarkan list kosong
    }
}