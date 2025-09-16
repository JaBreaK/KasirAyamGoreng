// File: OrderDetailActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.adapter.OrderDetailAdapter
import com.ayamgorengsuharti.kasirayamgoreng.models.Order
import java.text.NumberFormat
import java.util.Locale
import android.widget.ArrayAdapter // IMPORT INI
import android.widget.Button // IMPORT INI
import android.widget.Spinner // IMPORT INI
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.OrderDetailViewModel
import android.content.Intent // IMPORT INI
import android.net.Uri // IMPORT INI
import android.widget.ImageView // IMPORT INI
import android.widget.LinearLayout // IMPORT INI
import androidx.activity.result.contract.ActivityResultContracts // IMPORT INI
import com.bumptech.glide.Glide


class OrderDetailActivity : AppCompatActivity() {

    private val viewModel: OrderDetailViewModel by viewModels()
    private var orderId: Int = -1

    // Definisikan semua Views
    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerWa: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvStatusBayar: TextView
    private lateinit var tvStatusPesanan: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerStatusBayar: Spinner
    private lateinit var spinnerStatusPesanan: Spinner
    private lateinit var btnUpdateStatus: Button

    private lateinit var detailAdapter: OrderDetailAdapter
    private val listStatusBayar = listOf("BELUM_BAYAR", "MENUNGGU_KONFIRMASI", "LUNAS", "BATAL")
    private val listStatusPesanan = listOf("PESANAN_DITERIMA", "SEDANG_DIMASAK", "SIAP_DIAMBIL", "SELESAI")

    // VVVV TAMBAH VIEWS BARU VVVV
    private lateinit var layoutBuktiBayar: LinearLayout
    private lateinit var btnLihatBukti: Button
    private lateinit var btnUploadBukti: Button
    private lateinit var imgBuktiPreview: ImageView

    // Variabel buat nampung URL bukti yg ada
    private var currentBuktiUrl: String? = null

    // Launcher buat milih gambar bukti (COPY-PASTE DARI ACTIVITY LAIN)
    private val buktiPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Kalo user milih gambar, langsung panggil ViewModel buat upload
            viewModel.uploadBukti(this, orderId, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Ambil ID dari Intent
        orderId = intent.getIntExtra("ORDER_ID", -1)
        if (orderId == -1) {
            Toast.makeText(this, "Error: ID Order tidak valid", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup Appbar
        title = "Detail Order #$orderId"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views
        tvCustomerName = findViewById(R.id.tv_detail_customer_name)
        tvCustomerWa = findViewById(R.id.tv_detail_customer_wa)
        tvTotalPrice = findViewById(R.id.tv_detail_total_price)
        tvStatusBayar = findViewById(R.id.tv_detail_status_bayar)
        tvStatusPesanan = findViewById(R.id.tv_detail_status_pesanan)
        rvItems = findViewById(R.id.rv_order_items_detail)
        progressBar = findViewById(R.id.progress_bar_detail)
        spinnerStatusBayar = findViewById(R.id.spinner_status_bayar)
        spinnerStatusPesanan = findViewById(R.id.spinner_status_pesanan)
        btnUpdateStatus = findViewById(R.id.btn_update_status)
        layoutBuktiBayar = findViewById(R.id.layout_bukti_bayar)
        btnLihatBukti = findViewById(R.id.btn_lihat_bukti)
        btnUploadBukti = findViewById(R.id.btn_upload_bukti)
        imgBuktiPreview = findViewById(R.id.img_bukti_preview)

        setupRecyclerView()
        setupSpinners()
        setupListeners() // Kita modif ini
        observeViewModel() // Kita modif ini

        // Panggil API
        viewModel.fetchOrderDetail(orderId)
    }

    private fun setupRecyclerView() {
        detailAdapter = OrderDetailAdapter(emptyList())
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = detailAdapter
    }

    // VVVV TAMBAHIN FUNGSI INI VVVV
    private fun setupSpinners() {
        // Setup Spinner Status Bayar
        val adapterBayar = ArrayAdapter(this, android.R.layout.simple_spinner_item, listStatusBayar)
        adapterBayar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatusBayar.adapter = adapterBayar

        // Setup Spinner Status Pesanan
        val adapterPesanan = ArrayAdapter(this, android.R.layout.simple_spinner_item, listStatusPesanan)
        adapterPesanan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatusPesanan.adapter = adapterPesanan
    }

    // VVVV TAMBAHIN FUNGSI INI VVVV
    private fun setupListeners() {
        btnUpdateStatus.setOnClickListener {
            // Ambil status yg dipilih dari spinner
            val newStatusBayar = spinnerStatusBayar.selectedItem as String
            val newStatusPesanan = spinnerStatusPesanan.selectedItem as String

            // Panggil ViewModel
            viewModel.updateOrderStatus(orderId, newStatusBayar, newStatusPesanan)
        }
        // Tombol buat upload gambar BARU
        btnUploadBukti.setOnClickListener {
            buktiPickerLauncher.launch("image/*")
        }

        // Tombol buat LIAT gambar LAMA
        btnLihatBukti.setOnClickListener {
            if (currentBuktiUrl != null) {
                // Tampilkan di ImageView di bawah
                Glide.with(this).load(currentBuktiUrl).into(imgBuktiPreview)
                imgBuktiPreview.visibility = View.VISIBLE
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
        }

        // Ini bagian utama
        viewModel.orderDetail.observe(this) { order ->
            populateUi(order)
        }
        viewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Status berhasil di-update!", Toast.LENGTH_SHORT).show()
                // UI-nya otomatis ke-refresh karena _orderDetail juga di-update
            }.onFailure {
                Toast.makeText(this, "Gagal update: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
        // VVVV TAMBAH OBSERVER BARU VVVV
        viewModel.uploadBuktiResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Upload bukti berhasil!", Toast.LENGTH_SHORT).show()
                // ViewModel otomatis nge-refresh, jadi biarin aja
                imgBuktiPreview.visibility = View.GONE // Sembunyiin preview lama
            }.onFailure {
                Toast.makeText(this, "Upload GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateUi(order: Order) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        tvCustomerName.text = order.nama_pelanggan ?: "N/A"
        tvCustomerWa.text = order.tipe_pesanan // Kita tampilkan tipe pesanan di sini
        tvTotalPrice.text = formatter.format(order.total_harga)

        // Set Status Bayar
        tvStatusBayar.text = order.status_pembayaran
        when (order.status_pembayaran) {
            "LUNAS" -> tvStatusBayar.setBackgroundColor(Color.parseColor("#C8E6C9"))
            "BELUM_BAYAR" -> tvStatusBayar.setBackgroundColor(Color.parseColor("#FFCDD2"))
            else -> tvStatusBayar.setBackgroundColor(Color.parseColor("#FFECB3"))
        }

        // Set Status Pesanan
        tvStatusPesanan.text = order.status_pesanan
        when (order.status_pesanan) {
            "SELESAI" -> tvStatusPesanan.setBackgroundColor(Color.parseColor("#B2EBF2"))
            "SIAP_DIAMBIL" -> tvStatusPesanan.setBackgroundColor(Color.parseColor("#C8E6C9"))
            else -> tvStatusPesanan.setBackgroundColor(Color.parseColor("#FFECB3"))
        }

        // Update adapter RecyclerView-nya
        detailAdapter.updateData(order.orderItems)
        val posBayar = listStatusBayar.indexOf(order.status_pembayaran)
        if (posBayar != -1) {
            spinnerStatusBayar.setSelection(posBayar)
        }

        val posPesanan = listStatusPesanan.indexOf(order.status_pesanan)
        if (posPesanan != -1) {
            spinnerStatusPesanan.setSelection(posPesanan)
        }

        // Ambil data pembayaran pertama (asumsi cuma 1)
        val paymentInfo = order.pembayaran?.firstOrNull()
        val metodeId = paymentInfo?.metodepembayaran?.id

        // Cek apakah metode bayarnya 1 (QRIS) atau 2 (Transfer)
        if (metodeId == 1 || metodeId == 2) {
            // Kalo iya, tampilin seluruh bagian upload/lihat bukti
            layoutBuktiBayar.visibility = View.VISIBLE

            // Simpan URL bukti yg sekarang
            currentBuktiUrl = paymentInfo?.bukti_pembayaran_url

            // Cek: kalo URL-nya ADA, tampilin tombol "Lihat Bukti"
            if (!currentBuktiUrl.isNullOrEmpty()) {
                btnLihatBukti.visibility = View.VISIBLE
            } else {
                // Kalo NGGAK ADA, sembunyiin tombol "Lihat Bukti"
                btnLihatBukti.visibility = View.GONE
                imgBuktiPreview.visibility = View.GONE // Sembunyiin preview juga
            }
        } else {
            // Kalo metodenya 3 (Cash) atau lainnya, sembunyiin semua bagian bukti
            layoutBuktiBayar.visibility = View.GONE
        }
    }

    // Fungsi buat tombol kembali di Appbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}