// File: CartActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.adapter.CartAdapter
import java.text.NumberFormat
import java.util.Locale
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.ayamgorengsuharti.kasirayamgoreng.OrderDetailActivity
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran

class CartActivity : AppCompatActivity() {

    // Panggil ViewModel
    private val cartViewModel: CartViewModel by viewModels()

    // Siapin Views
    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var etNama: EditText
    private lateinit var etNomorWa: EditText
    private lateinit var etCatatan: EditText
    private lateinit var spinnerMetode: Spinner
    private lateinit var cartAdapter: CartAdapter
    private var metodeList: List<MetodePembayaran> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        title = "Keranjang Belanja"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views ke ID
        rvCart = findViewById(R.id.rv_cart)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        btnCheckout = findViewById(R.id.btn_checkout)
        progressBar = findViewById(R.id.progress_bar_cart)

        // VVVV TAMBAHIN INI VVVV
        etNama = findViewById(R.id.et_nama_pelanggan)
        etNomorWa = findViewById(R.id.et_nomor_wa)
        etCatatan = findViewById(R.id.et_catatan)
        spinnerMetode = findViewById(R.id.spinner_metode)

        setupRecyclerView()
        setupListeners() // Modif fungsi ini
        observeViewModel()
        cartViewModel.fetchMetodePembayaran()
    }

    // PENTING: Pake onResume()
    // Biar tiap kali kita balik ke halaman ini, datanya refresh
    override fun onResume() {
        super.onResume()
        loadCartData()
    }
    override fun onSupportNavigateUp(): Boolean {
        // Ini cara modern buat handle tombol kembali di Appbar
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        // Buat adapter dengan list kosong dulu
        cartAdapter = CartAdapter(emptyList())
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = cartAdapter
    }

    private fun loadCartData() {
        // Ambil data terbaru dari CartManager
        val items = CartManager.cartItems
        cartAdapter.updateData(items) // Update adapternya

        // Update total harga
        val total = CartManager.getTotalHarga()
        // Format harga biar cantik (misal: Rp 10.000)
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalPrice.text = "Total: ${formatter.format(total)}"

        // Kalo keranjang kosong, tombol checkout-nya mati
        btnCheckout.isEnabled = items.isNotEmpty()
    }

    private fun setupListeners() {
        btnCheckout.setOnClickListener {
            // 1. Ambil teks dari EditText
            val nama = etNama.text.toString().trim()
            val noWa = etNomorWa.text.toString().trim()
            val catatan = etCatatan.text.toString().trim() // VVVV TAMBAHIN INI VVVV

            // 2. Validasi simpel (cuma nama & no WA)
            if (nama.isEmpty()) {
                etNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (noWa.isEmpty()) {
                etNomorWa.error = "Nomor WA tidak boleh kosong"
                return@setOnClickListener
            }
            if (metodeList.isEmpty()) {
                Toast.makeText(this, "Metode pembayaran belum ter-load", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil posisi item yg dipilih di spinner
            val selectedPosition = spinnerMetode.selectedItemPosition
            // Ambil ID-nya dari list yg kita simpen
            val selectedMetodeId = metodeList[selectedPosition].id

            // 3. Panggil fungsi checkout DENGAN parameter catatan
            cartViewModel.checkout(nama, noWa, catatan, selectedMetodeId)
        }
    }

    private fun observeViewModel() {
        // Liatin status loading
        cartViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnCheckout.isEnabled = !isLoading // Matiin tombol pas loading
        }

        // Liatin hasil checkout
        cartViewModel.checkoutResult.observe(this) { result ->
            result.onSuccess { orderResponse ->
                // Kalo sukses
                Toast.makeText(
                    this,
                    "Checkout Sukses! Order ID: ${orderResponse.id}",
                    Toast.LENGTH_SHORT // Bikin Toast-nya singkat aja
                ).show()

                // LANGSUNG BUKA DETAIL ORDER
                val newOrderId = orderResponse.id
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", newOrderId)
                startActivity(intent)

                finish() // Tutup halaman keranjang

            }.onFailure { error ->
                // Kalo gagal
                Toast.makeText(
                    this,
                    "Checkout Gagal: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        cartViewModel.metodeList.observe(this) { list ->
            // 1. Simpen list-nya ke variabel global di Activity
            this.metodeList = list

            // 2. Ambil NAMA-nya aja buat ditampilin di spinner
            val namaMetodeList = list.map { it.nama_metode }

            // 3. Bikin adapter buat spinner
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, // Layout bawaan Android
                namaMetodeList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // 4. Pasang adapter-nya ke spinner
            spinnerMetode.adapter = adapter
        }
    }
}