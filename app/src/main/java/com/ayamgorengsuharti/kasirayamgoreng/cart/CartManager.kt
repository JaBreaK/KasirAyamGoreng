// Nama file: CartManager.kt
package com.ayamgorengsuharti.kasirayamgoreng.cart

import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse


// Pake 'object' biar jadi Singleton (cuma ada 1 di seluruh app)
object CartManager {

    // List buat nyimpen barang
    val cartItems = mutableListOf<CartItem>()

    // Fungsi buat nambahin produk ke keranjang
    fun addProduk(produk: MenuResponse.Produk) {
        // Cek dulu produknya udah ada di keranjang atau belum
        val existingItem = cartItems.find { it.id == produk.id }

        if (existingItem != null) {
            // Kalo udah ada, tambahin jumlahnya aja
            existingItem.jumlah++
        } else {
            // Kalo belum ada, "ubah" Produk jadi CartItem
            val cartItem = CartItem(
                id = produk.id,
                nama_produk = produk.namaProduk,
                deskripsi = produk.deskripsi,
                harga = produk.harga,
                gambar_url = produk.gambarUrl,
                kategori_id = produk.kategoriId,
                kategori = produk.kategori,
                jumlah = 1 // Awalnya 1
            )
            cartItems.add(cartItem) // Masukin ke list
        }
    }

    // Fungsi buat ngitung total harga
    fun getTotalHarga(): Int {
        return cartItems.sumOf { it.harga * it.jumlah }
    }

    // Fungsi buat ngosongin keranjang (setelah checkout)
    fun clearCart() {
        cartItems.clear()
    }
}