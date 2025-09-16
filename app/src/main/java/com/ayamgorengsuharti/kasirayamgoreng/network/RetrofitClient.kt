// Nama file: network/RetrofitClient.kt
package com.ayamgorengsuharti.kasirayamgoreng.network

// Pastiin import ini ada
import com.ayamgorengsuharti.kasirayamgoreng.network.ApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://www.ayamgorengsuharti.com/api/"

    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    private val gson = GsonBuilder()
        .serializeNulls() // Ini intinya
        .create()

    // VVVV MODIF BAGIAN INI VVVV
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        // Ganti GsonConverterFactory.create() jadi ini
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()


    // VVVVVV INI BAGIAN YANG ERROR VVVVVV
    // Pastiin baris ini ada, dan 'val' (bukan 'private val')
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }


}