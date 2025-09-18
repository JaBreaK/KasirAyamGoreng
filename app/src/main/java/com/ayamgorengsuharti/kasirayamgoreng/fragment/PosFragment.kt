// File: fragment/PosFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.CartManager
import com.ayamgorengsuharti.kasirayamgoreng.OrderDetailActivity
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.CartAdapter
import com.ayamgorengsuharti.kasirayamgoreng.adapter.PosMenuAdapter
import com.ayamgorengsuharti.kasirayamgoreng.models.CartItem
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.models.OrderPayload
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.PosViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.SearchView

class PosFragment : Fragment(R.layout.fragment_pos), SearchView.OnQueryTextListener {

    private val viewModel: PosViewModel by viewModels()
    private val PPN_RATE = 0.11 // PPN 11%

    private lateinit var searchViewPos: SearchView

    // --- Views ---
    // Panel Kiri
    private lateinit var spinnerKategoriFilter: Spinner
    private lateinit var rvMenuGrid: RecyclerView

    // Panel Kanan (Struk)
    private lateinit var rvCartPos: RecyclerView
    private lateinit var tvPosSubtotal: TextView
    private lateinit var tvPosPpn: TextView
    private lateinit var tvTotalPos: TextView
    private lateinit var etPosNama: TextInputEditText
    private lateinit var etPosCatatan: TextInputEditText
    private lateinit var rgPosMetode: RadioGroup
    private lateinit var btnBayarPos: Button

    // Global
    private lateinit var progressBar: ProgressBar

    // --- Adapters ---
    private lateinit var posMenuAdapter: PosMenuAdapter
    private lateinit var cartAdapter: CartAdapter
    private lateinit var layoutCashPayment: LinearLayout
    private lateinit var etJumlahBayar: TextInputEditText
    private lateinit var tvKembalian: TextView


    // --- Data Holders ---
    private var metodeList: List<MetodePembayaran> = emptyList()
    private var kategoriList: List<MenuResponse.Kategori> = emptyList()
    private var currentTotal: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cari Views (Panel Kiri)
        spinnerKategoriFilter = view.findViewById(R.id.spinner_kategori_filter)
        rvMenuGrid = view.findViewById(R.id.rv_menu_grid)
        searchViewPos = view.findViewById(R.id.search_view_pos)

        // Cari Views (Panel Kanan)
        rvCartPos = view.findViewById(R.id.rv_cart_pos)
        tvPosSubtotal = view.findViewById(R.id.tv_pos_subtotal)
        tvPosPpn = view.findViewById(R.id.tv_pos_ppn)
        tvTotalPos = view.findViewById(R.id.tv_total_pos)
        etPosNama = view.findViewById(R.id.et_pos_nama_pelanggan)
        etPosCatatan = view.findViewById(R.id.et_pos_catatan)
        rgPosMetode = view.findViewById(R.id.rg_pos_metode)
        btnBayarPos = view.findViewById(R.id.btn_bayar_pos)

        // Global
        progressBar = view.findViewById(R.id.progress_bar_pos)
        layoutCashPayment = view.findViewById(R.id.layout_cash_payment)
        etJumlahBayar = view.findViewById(R.id.et_jumlah_bayar)
        tvKembalian = view.findViewById(R.id.tv_kembalian)

        setupAdapters()
        setupListeners()
        observeViewModel()

        viewModel.loadInitialData()
    }

    private fun setupAdapters() {
        // Adapter Grid Menu (Kiri)
        posMenuAdapter = PosMenuAdapter(emptyList()) { produk ->
            CartManager.addProduk(produk) // Kalo diklik, langsung tambah ke keranjang
        }
        // Pastiin Grid Manager-nya sesuai (misal 3 kolom)
        rvMenuGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        rvMenuGrid.adapter = posMenuAdapter

        // Adapter List Keranjang (Kanan) - Pake adapter baru yg ada steppernya
        cartAdapter = CartAdapter(
            emptyList(),
            onIncrease = { cartItem -> CartManager.increaseQuantity(cartItem) },
            onDecrease = { cartItem -> CartManager.decreaseQuantity(cartItem) }
        )
        rvCartPos.layoutManager = LinearLayoutManager(requireContext())
        rvCartPos.adapter = cartAdapter
        rvCartPos.isNestedScrollingEnabled = false // Biar nggak rebutan scroll
    }

    private fun setupListeners() {
        // Listener tombol Bayar
        btnBayarPos.setOnClickListener {
            handleBayar()
        }

        // Listener Filter Kategori

        spinnerKategoriFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (kategoriList.isNotEmpty()) {
                    val selectedKategoriId = kategoriList[position].id
                    // PANGGIL FUNGSI BARU VM: filterByCategory
                    viewModel.filterByCategory(selectedKategoriId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 1. Listener buat RadioGroup (Dengerin pindah metode bayar)
        rgPosMetode.setOnCheckedChangeListener { group, checkedId ->
            // Kita cari nama metode yg di-check
            val selectedMetode = metodeList.find { it.id == checkedId }
            if (selectedMetode?.nama_metode == "Cash") {
                // KALO "CASH", TAMPILIN FORM KEMBALIAN
                layoutCashPayment.visibility = View.VISIBLE
                calculateChange() // Langsung hitung (mungkin isinya 0)
            } else {
                // KALO BUKAN "CASH", SEMBUNYIIN
                layoutCashPayment.visibility = View.GONE
                etJumlahBayar.setText("") // Kosongin lagi
                tvKembalian.text = "" // Kosongin lagi
            }
        }

        // 2. Listener buat EditText (Dengerin kasir ngetik jumlah uang)
        etJumlahBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tiap kali ketikan berubah, panggil fungsi hitung kembalian
                calculateChange()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        searchViewPos.setOnQueryTextListener(this)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        // Observe Menu Terfilter -> kirim ke Adapter Kiri
        viewModel.filteredMenuList.observe(viewLifecycleOwner) { menu ->
            posMenuAdapter.updateData(menu ?: emptyList())
        }

        // Observe Kategori -> kirim ke Spinner Kiri
        viewModel.kategoriList.observe(viewLifecycleOwner) { list ->
            val kategoriWithAll = mutableListOf(MenuResponse.Kategori(0, "SEMUA KATEGORI")) // (Sesuaikan Tipe Kategori lo)
            kategoriWithAll.addAll(list)
            this.kategoriList = kategoriWithAll.toList()

            val namaKategori = this.kategoriList.map { it.namaKategori }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, namaKategori)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKategoriFilter.adapter = adapter
        }

        // Observe Metode Bayar -> kirim ke RadioGroup Kanan
        viewModel.metodeList.observe(viewLifecycleOwner) { list ->
            this.metodeList = list
            rgPosMetode.removeAllViews()

            list.forEach { metode ->
                val radioButton = RadioButton(requireContext())
                radioButton.text = metode.nama_metode
                radioButton.id = metode.id // ID RadioButton = ID dari API
                rgPosMetode.addView(radioButton)
            }
            if (list.isNotEmpty()) {
                rgPosMetode.check(list[0].id) // Auto-cek yg pertama
            }
        }

        // Observe Hasil Checkout
        viewModel.checkoutResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { orderResponse ->
                Toast.makeText(requireContext(), "Order Berhasil Dibuat! ID: ${orderResponse.id}", Toast.LENGTH_SHORT).show()
                CartManager.clearCart() // Kosongin keranjang

                // Reset form
                etPosNama.setText("") // Balikin ke default
                etPosCatatan.setText("")

                // Lempar ke detail order
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", orderResponse.id)
                startActivity(intent)

                // Bilang ke ViewModel "Bro, event sukses-nya udah kita pake, lupain gih"
                viewModel.clearCheckoutResult()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Checkout GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // OBSERVER UTAMA: Nontonin Keranjang Belanja
        CartManager.cartItemsLiveData.observe(viewLifecycleOwner) { cartList ->
            // Tiap keranjang berubah (+ atau -), update UI Kanan
            updateCartUi(cartList)
        }
    }

    // Fungsi update UI Kanan (Struk)
    private fun updateCartUi(cartList: List<CartItem>) {
        cartAdapter.updateData(cartList) // Update list-nya

        // Hitung semua harga
        val subtotal = cartList.sumOf { it.harga * it.jumlah }
        val ppn = (subtotal * PPN_RATE).toInt()
        val total = subtotal + ppn

        this.currentTotal = total

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        // Set ke 3 TextView berbeda
        tvPosSubtotal.text = formatter.format(subtotal)
        tvPosPpn.text = formatter.format(ppn)
        tvTotalPos.text = formatter.format(total)

        btnBayarPos.isEnabled = cartList.isNotEmpty()

        // Otomatis hitung ulang kembalian kalo cart berubah
        calculateChange()
    }

    // Fungsi Bayar (sekarang ngambil data dari form)
    private fun handleBayar() {
        val items = CartManager.cartItemsLiveData.value.orEmpty()
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data dari form
        val nama = etPosNama.text.toString().trim().ifEmpty { "Pelanggan Kasir" }
        val catatan = etPosCatatan.text.toString().trim()
        val selectedMetodeId = rgPosMetode.checkedRadioButtonId

        if (selectedMetodeId == -1) {
            Toast.makeText(requireContext(), "Metode bayar gagal di-load", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung total akhir
        val subtotal = CartManager.getSubtotal()
        val totalAkhir = (subtotal * (1 + PPN_RATE)).toInt()

        val payload = OrderPayload(
            cartItems = items,
            total_harga = totalAkhir,
            metode_pembayaran_id = selectedMetodeId,
            nama_pelanggan = nama, // <-- Pake data form
            nomor_wa = "0000", // <-- Hardcode aja buat POS
            tipe_pesanan = "OFFLINE", // Pasti OFFLINE kalo via POS
            catatan_pelanggan = catatan // <-- Pake data form
        )

        viewModel.checkout(payload)
    }

    // FUNGSI BARU BUAT NGITUNG KEMBALIAN
    private fun calculateChange() {
        // Cuma jalanin kalo form-nya keliatan
        if (layoutCashPayment.visibility == View.VISIBLE) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0

            try {
                // Ambil uang yg dibayar, ubah ke Angka (Int)
                val uangDiterima = etJumlahBayar.text.toString().toIntOrNull() ?: 0

                // Ambil total belanja (yg udah kita simpen)
                val totalBelanja = this.currentTotal

                if (uangDiterima >= totalBelanja) {
                    val kembalian = uangDiterima - totalBelanja
                    tvKembalian.text = formatter.format(kembalian)
                } else {
                    // Kalo uangnya kurang
                    tvKembalian.text = "Uang Kurang"
                }
            } catch (e: Exception) {
                tvKembalian.text = "Error"
            }
        }
    }
    override fun onQueryTextSubmit(query: String?): Boolean {
        // Nggak perlu ngapa2in, kita udah handle live di onQueryTextChange
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Tiap kali ketikan berubah, panggil fungsi searchMenu di VM
        viewModel.searchMenu(newText.orEmpty())
        return true
    }
}