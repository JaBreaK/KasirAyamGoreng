// File: fragment/PosFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.content.Intent
import android.widget.AdapterView
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
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
import java.text.NumberFormat
import java.util.Locale

class PosFragment : Fragment(R.layout.fragment_pos) {

    private val viewModel: PosViewModel by viewModels()

    // Views
    private lateinit var rvMenuGrid: RecyclerView
    private lateinit var rvCartPos: RecyclerView
    private lateinit var tvTotalPos: TextView
    private lateinit var spinnerMetodePos: Spinner
    private lateinit var btnBayarPos: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerKategoriFilter: Spinner

    // Adapters
    private lateinit var posMenuAdapter: PosMenuAdapter
    private lateinit var cartAdapter: CartAdapter // Kita pake ulang CartAdapter lama

    // Data holder
    private var metodeList: List<MetodePembayaran> = emptyList()
    private var kategoriList: List<MenuResponse.Kategori> = emptyList() // (Sesuaikan import Kategori lo)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cari Views
        rvMenuGrid = view.findViewById(R.id.rv_menu_grid)
        rvCartPos = view.findViewById(R.id.rv_cart_pos)
        tvTotalPos = view.findViewById(R.id.tv_total_pos)
        spinnerMetodePos = view.findViewById(R.id.spinner_metode_pos)
        btnBayarPos = view.findViewById(R.id.btn_bayar_pos)
        progressBar = view.findViewById(R.id.progress_bar_pos)
        spinnerKategoriFilter = view.findViewById(R.id.spinner_kategori_filter)

        setupAdapters()
        setupListeners()
        observeViewModel()

        // Panggil data awal
        viewModel.loadInitialData()
    }

    // Kita udah nggak perlu onResume() buat update UI,
    // karena LiveData observer udah nanganin itu secara otomatis.

    private fun setupAdapters() {
        // 1. Adapter Grid Menu (Kiri)
        posMenuAdapter = PosMenuAdapter(emptyList()) { produk ->
            // Kalo menu diklik, kita cuma manggil CartManager. Udah.
            CartManager.addProduk(produk)
            // NGGAK PERLU manggil updateCartUi() manual lagi di sini
        }
        rvMenuGrid.layoutManager = GridLayoutManager(requireContext(), 3) // 3 Kolom
        rvMenuGrid.adapter = posMenuAdapter

        // 2. Adapter List Keranjang (Kanan)
        // INI FIX #1: PAKE CONSTRUCTOR ADAPTER YG BARU (DENGAN LAMBDA)
        cartAdapter = CartAdapter(
            emptyList(),
            onIncrease = { cartItem ->
                CartManager.increaseQuantity(cartItem) // Perintah tambah
            },
            onDecrease = { cartItem ->
                CartManager.decreaseQuantity(cartItem) // Perintah kurang
            }
        )
        rvCartPos.layoutManager = LinearLayoutManager(requireContext())
        rvCartPos.adapter = cartAdapter
    }

    private fun setupListeners() {
        btnBayarPos.setOnClickListener {
            handleBayar()
        }

        spinnerKategoriFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (kategoriList.isNotEmpty()) {
                    val selectedKategoriId = kategoriList[position].id
                    viewModel.filterMenu(selectedKategoriId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        // Kalo data menu TERFILTER dateng, update adapter grid kiri
        viewModel.filteredMenuList.observe(viewLifecycleOwner) { menu ->
            posMenuAdapter.updateData(menu ?: emptyList())
        }

        // Kalo data kategori dateng, update spinner filter
        viewModel.kategoriList.observe(viewLifecycleOwner) { list ->
            // Bikin list baru, tambahin "SEMUA KATEGORI" di paling atas
            val kategoriWithAll = mutableListOf(MenuResponse.Kategori(0, "SEMUA KATEGORI")) // (Sesuaikan nama class Kategori lo)
            kategoriWithAll.addAll(list)

            this.kategoriList = kategoriWithAll.toList()

            val namaKategori = this.kategoriList.map { it.namaKategori } // Pake camelCase
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                namaKategori
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKategoriFilter.adapter = adapter
        }

        // Kalo data metode bayar dateng, update spinner bayar
        viewModel.metodeList.observe(viewLifecycleOwner) { list ->
            this.metodeList = list
            val namaMetodeList = list.map { it.nama_metode }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                namaMetodeList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMetodePos.adapter = adapter
        }

        // Kalo checkout berhasil
        viewModel.checkoutResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { orderResponse ->
                Toast.makeText(requireContext(), "Order Berhasil Dibuat! ID: ${orderResponse.id}", Toast.LENGTH_SHORT).show()
                CartManager.clearCart() // Kosongin keranjang
                // Nggak perlu updateCartUi() manual, LiveData udah ngurusin

                // VVVV INI KODE YANG HILANG VVVV
                // Langsung lempar ke detail order sesuai permintaan lo
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", orderResponse.id)
                startActivity(intent)
            }
        }

        // INI FIX #4 (REAKTIF): "Nontonin" CartManager
        CartManager.cartItemsLiveData.observe(viewLifecycleOwner) { cartList ->
            // Tiap kali keranjang berubah (ditambah/dikurang),
            // panggil fungsi update total
            updateCartUi(cartList)
        }
    }

    // INI FIX #2: FUNGSI UPDATE UI YANG BARU
    private fun updateCartUi(cartList: List<CartItem>) {
        cartAdapter.updateData(cartList) // Update list keranjangnya

        // HITUNG TOTAL (TERMASUK PPN) BIAR SAMA KAYAK CartActivity
        val subtotal = CartManager.getSubtotal() // Pake helper baru dari CartManager
        val ppn = (subtotal * 0.11).toInt() // Asumsi PPN 11%
        val total = subtotal + ppn

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        tvTotalPos.text = formatter.format(total) // Tampilkan total akhir

        btnBayarPos.isEnabled = cartList.isNotEmpty()
    }

    // INI FIX #3: FUNGSI BAYAR YANG BARU (PAKE TOTAL AKHIR + PPN)
    private fun handleBayar() {
        val items = CartManager.cartItemsLiveData.value.orEmpty() // Ambil data dari LiveData
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung total akhir (termasuk PPN) buat dikirim ke API
        val subtotal = CartManager.getSubtotal()
        val totalAkhir = (subtotal * 1.11).toInt() // Total + 11% PPN

        if (metodeList.isEmpty()) {
            Toast.makeText(requireContext(), "Metode bayar belum ke-load", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedMetodeId = metodeList[spinnerMetodePos.selectedItemPosition].id

        // Buat Payload
        val payload = OrderPayload(
            cartItems = items,
            total_harga = totalAkhir, // KIRIM TOTAL AKHIR (UDAH + PPN)
            metode_pembayaran_id = selectedMetodeId,
            nama_pelanggan = "Pelanggan Kasir",
            nomor_wa = "0000",
            tipe_pesanan = "OFFLINE",
            catatan_pelanggan = "Pembelian via POS Kasir"
        )

        // Lempar ke ViewModel
        viewModel.checkout(payload)
    }
}