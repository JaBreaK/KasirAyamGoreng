// File: EditMenuActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.bumptech.glide.Glide // IMPORT GLIDE
import com.google.android.material.appbar.MaterialToolbar
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.EditMenuViewModel
import com.google.android.material.textfield.TextInputEditText

class EditMenuActivity : AppCompatActivity() {

    private val viewModel: EditMenuViewModel by viewModels()
    private var menuId: Int = -1

    // Views
    private lateinit var btnPilihGambar: Button
    private lateinit var imgPreview: ImageView
    private lateinit var etNama: TextInputEditText
    private lateinit var etDeskripsi: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null // Gambar BARU (kalo ada)
    private var kategoriList: List<MenuResponse.Kategori> = emptyList()

    // Image Picker Launcher (sama kayak Add)
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgPreview.setImageURI(uri) // Tampilkan preview gambar BARU
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_menu) // Pake layout edit


        // VVVV TAMBAHIN BLOK INI VVVV
        // 1. Cari Toolbar baru kita
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_edit_menu)
        // 2. Setel sebagai ActionBar RESMI
        setSupportActionBar(toolbar)

        // 1. Ambil ID menu dari Intent
        menuId = intent.getIntExtra("MENU_ID", -1)
        if (menuId == -1) {
            Toast.makeText(this, "ID Menu tidak valid", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup Appbar
        title = "Edit Menu #$menuId"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views (Sama kayak Add)
        btnPilihGambar = findViewById(R.id.btn_pilih_gambar)
        imgPreview = findViewById(R.id.img_preview)
        etNama = findViewById(R.id.et_nama_produk)
        etDeskripsi = findViewById(R.id.et_deskripsi)
        etHarga = findViewById(R.id.et_harga)
        spinnerKategori = findViewById(R.id.spinner_kategori_menu)
        btnSimpan = findViewById(R.id.btn_simpan_menu)
        progressBar = findViewById(R.id.progress_bar_add_menu)

        setupListeners()
        observeViewModel()

        // 2. Panggil API (Ambil data kategori DAN data menu lama)
        viewModel.fetchKategori()
        viewModel.fetchMenuDetail(menuId)
    }

    private fun setupListeners() {
        btnPilihGambar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSimpan.setOnClickListener {
            handleUpdate()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
            btnSimpan.isEnabled = !it
        }

        // Observe Kategori buat Spinner
        viewModel.kategoriList.observe(this) { list ->
            this.kategoriList = list
            val namaKategori = list.map { it.namaKategori }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaKategori)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKategori.adapter = adapter

            // Cek kalo data detail udah dateng, baru set spinner-nya
            viewModel.menuDetail.value?.let { setSpinnerSelection(it.kategori_id) }
        }

        // Observe Data Menu Lama
        viewModel.menuDetail.observe(this) { detail ->
            // Isi semua form pake data lama
            etNama.setText(detail.nama_produk)
            etDeskripsi.setText(detail.deskripsi)
            etHarga.setText(detail.harga.toString())

            // Load gambar LAMA pake Glide
            Glide.with(this).load(detail.gambar_url).into(imgPreview)

            // Cek kalo list kategori udah dateng, baru set spinner-nya
            if (kategoriList.isNotEmpty()) {
                setSpinnerSelection(detail.kategori_id)
            }
        }

        // Observe Hasil Update
        viewModel.updateResult.observe(this) { result ->
            result.onSuccess { produkUpdate ->
                Toast.makeText(this, "BERHASIL! Menu '${produkUpdate.namaProduk}' di-update!", Toast.LENGTH_LONG).show()
                finish() // Tutup halaman
            }.onFailure {
                Toast.makeText(this, "UPDATE GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Helper buat nyocokin kategori_id dgn posisi di spinner
    private fun setSpinnerSelection(kategoriIdLama: Int) {
        val position = kategoriList.indexOfFirst { it.id == kategoriIdLama }
        if (position != -1) {
            spinnerKategori.setSelection(position)
        }
    }

    private fun handleUpdate() {
        // Validasi data (sama kayak Add)
        val nama = etNama.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val harga = etHarga.text.toString().trim()

        // Validasi form... (lo bisa copy-paste dari AddMenuActivity)
        if (nama.isEmpty() || deskripsi.isEmpty() || harga.isEmpty() || kategoriList.isEmpty()) {
            Toast.makeText(this, "Semua data teks wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }
        // Kalo gambar nggak diisi (selectedImageUri == null), gpp, kita kirim null

        val selectedKategoriId = kategoriList[spinnerKategori.selectedItemPosition].id

        // Panggil ViewModel buat UPDATE
        viewModel.updateMenu(
            this,
            menuId,
            selectedImageUri, // Kirim URI baru (bisa null kalo nggak ganti)
            nama,
            deskripsi,
            harga,
            selectedKategoriId.toString()
        )
    }

    // Buat tombol kembali di Appbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}