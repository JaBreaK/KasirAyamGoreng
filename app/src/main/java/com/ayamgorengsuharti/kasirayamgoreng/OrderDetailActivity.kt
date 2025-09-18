// File: OrderDetailActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
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
import com.google.android.material.appbar.MaterialToolbar

import java.text.SimpleDateFormat

import java.util.TimeZone

class OrderDetailActivity : AppCompatActivity() {

    private val viewModel: OrderDetailViewModel by viewModels()
    private var orderId: Int = -1

    // Definisikan semua Views
    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerWa: TextView
    private lateinit var tvOrderTime: TextView
    private lateinit var tvTipePesanan: TextView
    private lateinit var tvStatusBayar: TextView
    private lateinit var tvStatusPesanan: TextView
    private lateinit var layoutAlasanBatal: LinearLayout
    private lateinit var tvKeteranganBatal: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var detailAdapter: OrderDetailAdapter
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvPaymentTime: TextView
    private lateinit var layoutCatatanPelanggan: LinearLayout
    private lateinit var tvCustomerNote: TextView

    private lateinit var progressBar: ProgressBar
    private lateinit var loadingoverlay: FrameLayout
    private lateinit var spinnerStatusBayar: Spinner
    private lateinit var spinnerStatusPesanan: Spinner
    private lateinit var btnUpdateStatus: Button


    private val listStatusBayar = listOf("LUNAS", "BATAL")
    private val listStatusPesanan = listOf("SEDANG_DIMASAK", "SIAP_DIAMBIL", "SELESAI")

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

        // VVVV TAMBAHIN BLOK INI VVVV
        // 1. Cari Toolbar baru kita
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_order_detail)
        // 2. Setel sebagai ActionBar RESMI
        setSupportActionBar(toolbar)

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

        findViewsById()

        setupRecyclerView()
        setupSpinners()
        setupListeners() // Kita modif ini
        observeViewModel() // Kita modif ini

        // Panggil API
        viewModel.fetchOrderDetail(orderId)
    }
    // Fungsi baru biar onCreate rapi
    private fun findViewsById() {
        tvCustomerName = findViewById(R.id.tv_detail_customer_name)
        tvCustomerWa = findViewById(R.id.tv_detail_customer_wa)
        tvOrderTime = findViewById(R.id.tv_detail_order_time)
        tvTipePesanan = findViewById(R.id.tv_detail_tipe_pesanan)
        tvStatusBayar = findViewById(R.id.tv_detail_status_bayar)
        tvStatusPesanan = findViewById(R.id.tv_detail_status_pesanan)
        layoutAlasanBatal = findViewById(R.id.layout_alasan_batal)
        tvKeteranganBatal = findViewById(R.id.tv_detail_keterangan_batal)
        rvItems = findViewById(R.id.rv_order_items_detail)
        tvTotalPrice = findViewById(R.id.tv_detail_total_price)
        tvPaymentMethod = findViewById(R.id.tv_detail_payment_method)
        tvPaymentTime = findViewById(R.id.tv_detail_payment_time)
        layoutCatatanPelanggan = findViewById(R.id.layout_catatan_pelanggan)
        tvCustomerNote = findViewById(R.id.tv_detail_customer_note)
        layoutBuktiBayar = findViewById(R.id.layout_bukti_bayar)
        btnLihatBukti = findViewById(R.id.btn_lihat_bukti)
        btnUploadBukti = findViewById(R.id.btn_upload_bukti)
        imgBuktiPreview = findViewById(R.id.img_bukti_preview)
        spinnerStatusBayar = findViewById(R.id.spinner_status_bayar)
        spinnerStatusPesanan = findViewById(R.id.spinner_status_pesanan)
        btnUpdateStatus = findViewById(R.id.btn_update_status)
        progressBar = findViewById(R.id.progress_bar_detail)
        loadingoverlay = findViewById(R.id.loading_overlay)
    }

    private fun setupRecyclerView() {
        detailAdapter = OrderDetailAdapter(emptyList())
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.isNestedScrollingEnabled = false
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
            loadingoverlay.visibility = if (it) View.VISIBLE else View.GONE
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

            }.onFailure {
                Toast.makeText(this, "Upload GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateUi(order: Order) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        tvCustomerName.text = order.nama_pelanggan ?: "N/A"
        tvCustomerWa.text = order.nomor_wa ?: "N/A"
        tvTipePesanan.text = order.tipe_pesanan
        tvOrderTime.text = formatWaktu(order.waktu_order) // Pake helper format // Kita tampilkan tipe pesanan di sini
        tvTotalPrice.text = formatter.format(order.total_harga)


        // Set Status Bayar
        tvStatusBayar.text = order.status_pembayaran
        when (order.status_pembayaran) {
            "LUNAS" -> tvStatusBayar.setBackgroundColor(Color.parseColor("#C8E6C9"))
            "BELUM_BAYAR" -> tvStatusBayar.setBackgroundColor(Color.parseColor("#FFCDD2"))
            "BATAL" -> tvStatusBayar.setBackgroundColor(Color.parseColor("#BDBDBD"))
            else -> tvStatusBayar.setBackgroundColor(Color.parseColor("#FFECB3"))
        }

        // Set Status Pesanan
        tvStatusPesanan.text = order.status_pesanan
        if (order.status_pembayaran == "BATAL" && !order.keterangan_batal.isNullOrBlank()) {
            layoutAlasanBatal.visibility = View.VISIBLE
            tvKeteranganBatal.text = order.keterangan_batal
        } else {
            layoutAlasanBatal.visibility = View.GONE
        }

        // 2. Tampilkan Catatan Pelanggan
        if (!order.catatan_pelanggan.isNullOrBlank()) {
            layoutCatatanPelanggan.visibility = View.VISIBLE
            tvCustomerNote.text = order.catatan_pelanggan
        } else {
            layoutCatatanPelanggan.visibility = View.GONE
        }
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
        if (paymentInfo != null) {
            tvPaymentMethod.text = paymentInfo.metodepembayaran?.nama_metode ?: "N/A"
            tvPaymentTime.text = formatWaktu(paymentInfo.waktu_bayar) // Format waktunya

            val metodeId = paymentInfo.metodepembayaran?.id
            // Tampilkan tombol HANYA kalo QRIS (1) atau Transfer (2)
            if (metodeId == 1 || metodeId == 2) {
                layoutBuktiBayar.visibility = View.VISIBLE
                currentBuktiUrl = paymentInfo.bukti_pembayaran_url

                if (!currentBuktiUrl.isNullOrEmpty()) {
                    btnLihatBukti.visibility = View.VISIBLE
                } else {
                    btnLihatBukti.visibility = View.GONE
                    imgBuktiPreview.visibility = View.GONE
                }
            } else {
                // Sembunyiin kalo metodenya "Cash" (ID 3)
                layoutBuktiBayar.visibility = View.GONE
            }
        } else {
            // Kalo data pembayaran null (harusnya nggak mungkin kalo udah bayar)
            tvPaymentMethod.text = "N/A"
            tvPaymentTime.text = "N/A"
            layoutBuktiBayar.visibility = View.GONE
        }
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

    // Helper buat format waktu (COPY-PASTE DARI ORDER ADAPTER)
    private fun formatWaktu(waktuString: String?): String {
        if (waktuString.isNullOrBlank()) {
            return "N/A"
        }
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(waktuString)
            outputFormat.format(date)
        } catch (e: Exception) {
            waktuString // Kalo gagal format, balikin aslinya
        }
    }

    // Fungsi buat tombol kembali di Appbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}