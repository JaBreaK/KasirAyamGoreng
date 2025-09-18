// File: EditMetodeBayarActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ayamgorengsuharti.kasirayamgoreng.models.MetodePembayaran
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.EditMetodeViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.appbar.MaterialToolbar
class EditMetodeBayarActivity : AppCompatActivity() {

    private val viewModel: EditMetodeViewModel by viewModels()
    private var metodeData: MetodePembayaran? = null

    // Views
    private lateinit var etNamaMetode: TextInputEditText
    private lateinit var etNamaRekening: TextInputEditText
    private lateinit var etNomorRekening: TextInputEditText
    private lateinit var btnPilihQris: Button
    private lateinit var imgQrisPreview: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null // Gambar BARU (kalo ada)

    // Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgQrisPreview.setImageURI(uri) // Tampilkan preview gambar BARU
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_metode_bayar)

        // VVVV TAMBAHIN BLOK INI VVVV
        // 1. Cari Toolbar baru kita
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_edit_metode_bayar)
        // 2. Setel sebagai ActionBar RESMI
        setSupportActionBar(toolbar)


        // 1. Ambil DATA LAMA dari Intent
        metodeData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DATA_METODE", MetodePembayaran::class.java)
        } else {
            intent.getSerializableExtra("DATA_METODE") as? MetodePembayaran
        }

        if (metodeData == null) {
            Toast.makeText(this, "Data Metode tidak valid", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup Appbar
        title = "Edit: ${metodeData!!.nama_metode}"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hubungin Views
        etNamaMetode = findViewById(R.id.et_nama_metode)
        etNamaRekening = findViewById(R.id.et_nama_rekening)
        etNomorRekening = findViewById(R.id.et_nomor_rekening)
        btnPilihQris = findViewById(R.id.btn_pilih_qris)
        imgQrisPreview = findViewById(R.id.img_qris_preview)
        btnSimpan = findViewById(R.id.btn_simpan_metode)
        progressBar = findViewById(R.id.progress_bar_edit_metode)

        // 2. Isi form pake data lama
        populateUi(metodeData!!)

        setupListeners()
        observeViewModel()
    }

    private fun populateUi(data: MetodePembayaran) {
        etNamaMetode.setText(data.nama_metode)
        etNamaRekening.setText(data.nama_rekening)
        etNomorRekening.setText(data.nomor_rekening)

        // Load gambar QRIS LAMA pake Glide (kalo ada)
        if (!data.gambar_qris_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(data.gambar_qris_url)
                .into(imgQrisPreview)
        }
    }

    private fun setupListeners() {
        btnPilihQris.setOnClickListener {
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

        // Observe Hasil Update
        viewModel.updateResult.observe(this) { result ->
            result.onSuccess { metodeUpdate ->
                Toast.makeText(this, "BERHASIL! Metode '${metodeUpdate.nama_metode}' di-update!", Toast.LENGTH_LONG).show()
                finish() // Tutup halaman
            }.onFailure {
                Toast.makeText(this, "UPDATE GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleUpdate() {
        val namaMetode = etNamaMetode.text.toString().trim()
        // Kirim string kosong kalo null/kosong, API lo nerima ini
        val namaRek = etNamaRekening.text.toString().trim()
        val nomRek = etNomorRekening.text.toString().trim()

        if (namaMetode.isEmpty()) {
            etNamaMetode.error = "Nama metode wajib diisi"
            return
        }

        // Panggil ViewModel buat UPDATE
        viewModel.updateMetode(
            this,
            metodeData!!.id,
            selectedImageUri, // Kirim URI baru (bisa null kalo nggak ganti)
            namaMetode,
            namaRek,
            nomRek
        )
    }

    // Buat tombol kembali di Appbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}