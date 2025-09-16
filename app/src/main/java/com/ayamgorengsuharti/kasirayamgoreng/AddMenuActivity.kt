// File: AddMenuActivity.kt
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

import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.AddMenuViewModel
import com.google.android.material.textfield.TextInputEditText

class AddMenuActivity : AppCompatActivity() {

    private val viewModel: AddMenuViewModel by viewModels()

    // Deklarasi Views
    private lateinit var btnPilihGambar: Button
    private lateinit var imgPreview: ImageView
    private lateinit var etNama: TextInputEditText
    private lateinit var etDeskripsi: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar

    // Variabel buat nyimpen alamat gambar yg dipilih
    private var selectedImageUri: Uri? = null

    // Variabel buat nyimpen list kategori
    private var kategoriList: List<MenuResponse.Kategori> = emptyList()

    // --- INI CARA BARU BUAT MILIH GAMBAR ---
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent() // Ini kontrak standar buat milih file
    ) { uri: Uri? ->
        // Ini kode yg jalan pas user SELESAI milih gambar
        if (uri != null) {
            selectedImageUri = uri
            imgPreview.setImageURI(uri) // Tampilkan preview
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)

        // Setup Appbar
        title = "Tambah Menu Baru"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views
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

        // Ambil data kategori buat spinner
        viewModel.fetchKategori()
    }

    private fun setupListeners() {
        // Pas tombol "Pilih Gambar" diklik
        btnPilihGambar.setOnClickListener {
            // Buka galeri
            imagePickerLauncher.launch("image/*") // "image/*" artinya cuma nampilin gambar
        }

        // Pas tombol "Simpan" diklik
        btnSimpan.setOnClickListener {
            handleSimpan()
        }
    }

    private fun observeViewModel() {
        // Observe Kategori buat Spinner
        viewModel.kategoriList.observe(this) { list ->
            this.kategoriList = list
            val namaKategori = list.map { it.namaKategori }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaKategori)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKategori.adapter = adapter
        }

        // Observe status loading
        viewModel.isLoading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
            btnSimpan.isEnabled = !it
        }

        // Observe hasil upload
        viewModel.uploadResult.observe(this) { result ->
            result.onSuccess { produkBaru ->
                Toast.makeText(this, "BERHASIL! Menu '${produkBaru.namaProduk}' ditambahkan!", Toast.LENGTH_LONG).show()
                finish() // Tutup halaman
            }.onFailure {
                Toast.makeText(this, "UPLOAD GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleSimpan() {
        // Validasi data
        val nama = etNama.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val harga = etHarga.text.toString().trim()

        if (selectedImageUri == null) {
            Toast.makeText(this, "Gambar tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }
        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }
        if (deskripsi.isEmpty()) {
            etDeskripsi.error = "Deskripsi tidak boleh kosong"
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            return
        }
        if (kategoriList.isEmpty()) {
            Toast.makeText(this, "Kategori gagal di-load", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil ID kategori dari spinner
        val selectedKategoriId = kategoriList[spinnerKategori.selectedItemPosition].id

        // Panggil ViewModel buat UPLOAD
        viewModel.createMenu(
            this,
            selectedImageUri!!,
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