// File: fragment/MenuAdminFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.os.Bundle
import android.view.Menu // <--- IMPORT
import android.view.MenuInflater // <--- IMPORT
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView // <--- IMPORT
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.MenuAdminAdapter

import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.MenuAdminViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton // (Import FAB lo yg lama)
import android.content.Intent // (Import Intent lo yg lama)
import com.ayamgorengsuharti.kasirayamgoreng.AddMenuActivity // (Import AddMenuActivity lo yg lama)
import com.ayamgorengsuharti.kasirayamgoreng.EditMenuActivity
import com.ayamgorengsuharti.kasirayamgoreng.models.MenuResponse


// VVVV UPDATE DEFINISI CLASS-NYA VVVV
class MenuAdminFragment : Fragment(R.layout.fragment_menu_admin), SearchView.OnQueryTextListener {

    private val viewModel: MenuAdminViewModel by viewModels()
    private lateinit var adapter: MenuAdminAdapter

    private lateinit var rvMenu: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddMenu: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true) // <-- WAJIB: Bilang ke Activity kalo kita punya menu

        rvMenu = view.findViewById(R.id.rv_menu_admin)
        progressBar = view.findViewById(R.id.progress_bar_menu_admin)
        fabAddMenu = view.findViewById(R.id.fab_add_menu)

        setupRecyclerView()
        observeViewModel()

        fabAddMenu.setOnClickListener {
            startActivity(Intent(requireContext(), AddMenuActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMenu()
    }

    private fun setupRecyclerView() {
        adapter = MenuAdminAdapter(
            emptyList(),
            onEditClick = { produk ->
                // (Logika edit lo yg lama)
                val intent = Intent(requireContext(), EditMenuActivity::class.java)
                intent.putExtra("MENU_ID", produk.id)
                startActivity(intent)
            },
            onDeleteClick = { produk ->
                // (Logika delete lo yg lama)
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

        // Observer ini sekarang otomatis nampilin hasil filter
        viewModel.menuList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

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

    // VVVV TAMBAHIN 2 FUNGSI BARU INI (COPY-PASTE DARI OrderListFragment) VVVV

    // Fungsi buat nampilin Search Bar di Appbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_search_menu, menu) // Pake ulang file XML yg sama

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.isSubmitButtonEnabled = false
        searchView?.setOnQueryTextListener(this) // Sambungin ke Fragment ini

        super.onCreateOptionsMenu(menu, inflater)
    }

    // Fungsi listener buat nanganin ketikan
    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchMenu(query.orEmpty()) // Panggil VM
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchMenu(newText.orEmpty()) // Panggil VM
        return true
    }
}