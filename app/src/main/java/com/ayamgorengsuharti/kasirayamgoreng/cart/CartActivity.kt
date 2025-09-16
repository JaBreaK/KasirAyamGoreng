// File: CartActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.adapter.CartAdapter
import com.ayamgorengsuharti.kasirayamgoreng.cart.CartViewModel
import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderPayload
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private val cartViewModel: CartViewModel by viewModels() // Tetep pake ini buat checkout

    // Views
    private lateinit var rvCart: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvPpn: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var etNama: EditText
    private lateinit var etNomorWa: EditText
    private lateinit var etCatatan: EditText
    private lateinit var rgMetodeBayar: RadioGroup
    private lateinit var btnCheckout: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var cartAdapter: CartAdapter
    private var metodeList: List<MetodePembayaran> = emptyList()
    private val PPN_RATE = 0.11 // PPN 11%

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        title = "Keranjang Belanja"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views
        setupViews()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Ambil data metode bayar (buat RadioGroup)
        cartViewModel.fetchMetodePembayaran()
    }

    private fun setupViews() {
        rvCart = findViewById(R.id.rv_cart)
        tvSubtotal = findViewById(R.id.tv_subtotal)
        tvPpn = findViewById(R.id.tv_ppn)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        etNama = findViewById(R.id.et_nama_pelanggan)
        etNomorWa = findViewById(R.id.et_nomor_wa)
        etCatatan = findViewById(R.id.et_catatan)
        rgMetodeBayar = findViewById(R.id.rg_metode_bayar)
        btnCheckout = findViewById(R.id.btn_checkout)
        progressBar = findViewById(R.id.progress_bar_cart)
    }

    private fun setupRecyclerView() {
        // Buat adapter DENGAN sinyal baru
        cartAdapter = CartAdapter(
            emptyList(),
            onIncrease = { cartItem ->
                CartManager.increaseQuantity(cartItem)
            },
            onDecrease = { cartItem ->
                CartManager.decreaseQuantity(cartItem)
            }
        )
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = cartAdapter
    }

    private fun setupListeners() {
        btnCheckout.setOnClickListener {
            handleCheckout()
        }
    }

    private fun observeViewModel() {
        // "Nonton" CartManager! Ini bagian reaktif-nya
        CartManager.cartItemsLiveData.observe(this) { cartList ->
            // Tiap kali data di CartManager berubah (di-tambahin/kurangin)
            // 1. Update list di adapter
            cartAdapter.updateData(cartList)
            // 2. Update semua harga
            updateTotals(cartList)
        }

        // Observer buat loading (sama kayak lama)
        cartViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnCheckout.isEnabled = !isLoading
        }

        // Observer buat hasil checkout (sama kayak lama)
        cartViewModel.checkoutResult.observe(this) { result ->
            result.onSuccess { orderResponse ->
                Toast.makeText(this, "Checkout Sukses! Order ID: ${orderResponse.id}", Toast.LENGTH_SHORT).show()
                CartManager.clearCart() // JANGAN LUPA CLEAR CART

                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", orderResponse.id)
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(this, "Checkout Gagal: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observer baru buat ngisi RadioGroup
        cartViewModel.metodeList.observe(this) { list ->
            this.metodeList = list
            rgMetodeBayar.removeAllViews() // Bersihin dulu

            list.forEach { metode ->
                // Bikin RadioButton baru buat tiap metode
                val radioButton = RadioButton(this)
                radioButton.text = metode.nama_metode
                radioButton.id = metode.id // Kita set ID RadioButton = ID metode dari API
                rgMetodeBayar.addView(radioButton)
            }
            // Otomatis cek tombol pertama
            if (list.isNotEmpty()) {
                rgMetodeBayar.check(list[0].id)
            }
        }
    }

    // FUNGSI BARU: Buat ngitung harga
    private fun updateTotals(cartList: List<CartItem>) {
        val subtotal = cartList.sumOf { it.harga * it.jumlah }
        val ppn = (subtotal * PPN_RATE).toInt() // Kita buletin aja
        val total = subtotal + ppn

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0 // Biar nggak ada koma

        tvSubtotal.text = formatter.format(subtotal)
        tvPpn.text = formatter.format(ppn)
        tvTotalPrice.text = formatter.format(total)

        // Tombol checkout mati kalo keranjang kosong
        btnCheckout.isEnabled = cartList.isNotEmpty()
    }

    private fun handleCheckout() {
        // Validasi form (sama kayak lama)
        val nama = etNama.text.toString().trim()
        val noWa = etNomorWa.text.toString().trim()
        val catatan = etCatatan.text.toString().trim()

        if (nama.isEmpty() || noWa.isEmpty()) {
            Toast.makeText(this, "Nama dan Nomor WA wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil ID dari RadioGroup
        val selectedMetodeId = rgMetodeBayar.checkedRadioButtonId
        if (selectedMetodeId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val items = CartManager.cartItemsLiveData.value.orEmpty()
        val total = (CartManager.getSubtotal() * (1 + PPN_RATE)).toInt()

        // Buat Payload
        val payload = OrderPayload(
            cartItems = items,
            total_harga = total, // Kirim total SETELAH PPN
            metode_pembayaran_id = selectedMetodeId,
            nama_pelanggan = nama,
            nomor_wa = noWa,
            tipe_pesanan = "OFFLINE", // Ini tetep, karena ini app kasir
            catatan_pelanggan = catatan
        )

        // Lempar ke ViewModel
        cartViewModel.checkout(payload)
    }

    // Buat tombol kembali di Appbar (sama kayak lama)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}