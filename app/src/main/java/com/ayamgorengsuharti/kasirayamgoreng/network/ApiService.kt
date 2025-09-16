// File: network/ApiService.kt
package com.ayamgorengsuharti.kasirayamgoreng.network

import com.ayamgorengsuharti.kasirayamgoreng.models.KategoriPayload
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderPayload
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderResponse

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.ayamgorengsuharti.kasirayamgoreng.models.UpdateStatusPayload // IMPORT INI
import retrofit2.http.PATCH // IMPORT INI

import com.ayamgorengsuharti.kasirayamgoreng.models.DeleteResponse // IMPORT INI
import com.ayamgorengsuharti.kasirayamgoreng.models.ProdukDetail
import retrofit2.http.DELETE // IMPORT INI
import retrofit2.http.PUT // IMPORT INI

import okhttp3.MultipartBody // <-- PENTING
import okhttp3.RequestBody // <-- PENTING
import retrofit2.http.Multipart // <-- PENTING
import retrofit2.http.Part // <-- PENTING
interface ApiService {

    @GET("menu")
    suspend fun getAllMenu(): List<MenuResponse.Produk>

    // VVVV TAMBAHIN FUNGSI INI VVVV
    @POST("orders")
    suspend fun createOrder(
        @Body payload: OrderPayload
    ): OrderResponse

    @GET("metode-pembayaran")
    suspend fun getMetodePembayaran(): List<MetodePembayaran>

    @GET("orders")
    suspend fun getAllOrders(): List<Order> // Dapetin SEMUA order

    @GET("orders/{id}") // Pake {id} buat parameter dinamis
    suspend fun getOrderDetail(
        @Path("id") orderId: Int // @Path("id") bakal gantiin {id}
    ): Order // Response-nya 1 object Order aja

    @PATCH("orders/{id}")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body payload: UpdateStatusPayload
    ): Order // Response-nya adalah data Order yg udah ke-update

    @GET("kategori")
    suspend fun getAllKategori(): List<MenuResponse.Kategori>

    @POST("kategori")
    suspend fun createKategori(
        @Body payload: KategoriPayload
    ): MenuResponse.Kategori // Response-nya adalah kategori yg baru dibuat
    @PUT("kategori/{id}")
    suspend fun updateKategori(
        @Path("id") id: Int,
        @Body payload: KategoriPayload // Kita pake payload yg sama kayak POST
    ): MenuResponse.Kategori // Response-nya adalah kategori yg udah di-update

    @DELETE("kategori/{id}")
    suspend fun deleteKategori(
        @Path("id") id: Int
    ): DeleteResponse // Response-nya message doang

    @DELETE("menu/{id}")
    suspend fun deleteMenu(
        @Path("id") id: Int
    ): DeleteResponse // Pake model response yg sama kayak delete kategori

    @Multipart // Tandain ini sebagai request Multipart (file + data)
    @POST("menu")
    suspend fun createMenu(
        // Ini buat file gambarnya
        @Part gambar: MultipartBody.Part,

        // Ini buat data teks-nya, harus dibungkus RequestBody
        @Part("nama_produk") nama_produk: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("kategori_id") kategori_id: RequestBody

    ): MenuResponse.Produk // Response-nya adalah produk yg baru dibuat

    // 1. Buat ambil data lama
    @GET("menu/{id}")
    suspend fun getMenuDetail(
        @Path("id") id: Int
    ): ProdukDetail // Pake model baru

    // 2. Buat update data (mirip createMenu, tapi pake PUT)
    @Multipart
    @PUT("menu/{id}")
    suspend fun updateMenu(
        @Path("id") id: Int,

        // Bikin gambar jadi Nullable. Kalo user nggak milih gambar baru, kita kirim null
        @Part gambar: MultipartBody.Part?,

        @Part("nama_produk") nama_produk: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("kategori_id") kategori_id: RequestBody
    ): MenuResponse.Produk // Response-nya adalah produk yg udah di-update

    // VVVV TAMBAHIN FUNGSI INI VVVV
    @Multipart
    @PUT("metode-pembayaran/{id}")
    suspend fun updateMetodePembayaran(
        @Path("id") id: Int,

        // Data Teks
        @Part("nama_metode") nama_metode: RequestBody,
        @Part("nomor_rekening") nomor_rekening: RequestBody,
        @Part("nama_rekening") nama_rekening: RequestBody,

        // Gambar (Opsional, boleh null kalo nggak ganti)
        @Part gambar_qris: MultipartBody.Part?

    ): MetodePembayaran // Response-nya adalah data yg udah di-update

    @Multipart
    @POST("konfirmasi-pembayaran/{orderId}")
    suspend fun uploadBuktiPembayaran(
        @Path("orderId") orderId: Int,
        @Part bukti: MultipartBody.Part // Nama part-nya "bukti"
    ): DeleteResponse
}