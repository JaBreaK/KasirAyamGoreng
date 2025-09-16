// File: fragment/KategoriFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.KategoriAdapter
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.KategoriViewModel
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AlertDialog
import android.widget.EditText

class KategoriFragment : Fragment(R.layout.fragment_kategori) {

    private val viewModel: KategoriViewModel by viewModels()
    private lateinit var adapter: KategoriAdapter

    private lateinit var rvKategori: RecyclerView
    private lateinit var etKategoriName: TextInputEditText
    private lateinit var btnAdd: Button
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvKategori = view.findViewById(R.id.rv_kategori)
        etKategoriName = view.findViewById(R.id.et_kategori_name)
        btnAdd = view.findViewById(R.id.btn_add_kategori)
        progressBar = view.findViewById(R.id.progress_bar_kategori)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // VVVV MODIF INI VVVV
        // Kita isi adapternya pake lambda yg kita siapin
        adapter = KategoriAdapter(
            emptyList(),
            onEditClick = { kategori ->
                // Panggil fungsi dialog edit
                showEditDialog(kategori)
            },
            onDeleteClick = { kategori ->
                // Panggil fungsi dialog delete
                showDeleteConfirmationDialog(kategori)
            }
        )
        rvKategori.layoutManager = LinearLayoutManager(requireContext())
        rvKategori.adapter = adapter
    }

    private fun setupListeners() {
        btnAdd.setOnClickListener {
            val nama = etKategoriName.text.toString().trim()
            if (nama.isEmpty()) {
                etKategoriName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            // Panggil ViewModel
            viewModel.createKategori(nama)
            etKategoriName.text = null // Kosongin field
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.kategoriList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    private fun showEditDialog(kategori: MenuResponse.Kategori) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Kategori")

        // Bikin EditText buat di dalem dialog
        val input = EditText(requireContext())
        input.setText(kategori.namaKategori) // Isi pake nama lama
        builder.setView(input)

        // Tombol "Simpan"
        builder.setPositiveButton("Simpan") { dialog, _ ->
            val namaBaru = input.text.toString().trim()
            if (namaBaru.isNotEmpty()) {
                viewModel.updateKategori(kategori.id, namaBaru)
            } else {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // Tombol "Batal"
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showDeleteConfirmationDialog(kategori: MenuResponse.Kategori) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Kategori")
        builder.setMessage("Yakin mau hapus kategori '${kategori.namaKategori}'?")

        // Tombol "Ya, Hapus"
        builder.setPositiveButton("Ya, Hapus") { dialog, _ ->
            viewModel.deleteKategori(kategori.id)
            dialog.dismiss()
        }

        // Tombol "Batal"
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}