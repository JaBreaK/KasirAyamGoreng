// File: fragment/MenuAdminFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.MenuAdminAdapter
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse
import com.ayamgorengsuharti.kasirayamgoreng.AddMenuActivity // IMPORT INI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.MenuAdminViewModel
import android.content.Intent // IMPORT INI
import com.ayamgorengsuharti.kasirayamgoreng.EditMenuActivity


class MenuAdminFragment : Fragment(R.layout.fragment_menu_admin) {

    private val viewModel: MenuAdminViewModel by viewModels()
    private lateinit var adapter: MenuAdminAdapter

    private lateinit var rvMenu: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddMenu: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMenu = view.findViewById(R.id.rv_menu_admin)
        progressBar = view.findViewById(R.id.progress_bar_menu_admin)
        fabAddMenu = view.findViewById(R.id.fab_add_menu) // VVVV TAMBAH INI VVVV

        setupRecyclerView()
        observeViewModel()

        // VVVV KASIH LISTENER BARU VVVV
        fabAddMenu.setOnClickListener {
            startActivity(Intent(requireContext(), AddMenuActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchMenu()
    }

    private fun setupRecyclerView() {
        // VVVV UPDATE CARA BIKIN ADAPTER VVVV
        adapter = MenuAdminAdapter(
            emptyList(),
            onEditClick = { produk ->
                // Kalo di-klik, buka EditActivity
                val intent = Intent(requireContext(), EditMenuActivity::class.java)
                intent.putExtra("MENU_ID", produk.id)
                startActivity(intent)
            },
            onDeleteClick = { produk ->
                // Panggil dialog konfirmasi delete
                showDeleteConfirmationDialog(produk)
            }
        )
        rvMenu.layoutManager = LinearLayoutManager(requireContext())
        rvMenu.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.menuList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    // Fungsi dialog (copy-paste dari KategoriFragment, ganti nama)
    private fun showDeleteConfirmationDialog(produk: MenuResponse.Produk) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Menu")
        builder.setMessage("Yakin mau hapus menu '${produk.namaProduk}'?")

        builder.setPositiveButton("Ya, Hapus") { dialog, _ ->
            viewModel.deleteMenu(produk.id)
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

}