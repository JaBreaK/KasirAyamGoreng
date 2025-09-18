package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.OrderDetailActivity
import com.ayamgorengsuharti.kasirayamgoreng.adapter.OrderAdapter
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.OrderViewModel
import android.view.Menu // <-- TAMBAH
import android.view.MenuInflater // <-- TAMBAH
import androidx.appcompat.widget.SearchView // <-- TAMBAH

class OrderListFragment : Fragment(R.layout.fragment_order_list), SearchView.OnQueryTextListener {

    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // findViewById harus diawali view.
        rvOrders = view.findViewById(R.id.rv_orders)
        progressBar = view.findViewById(R.id.progress_bar_orders)

        setupRecyclerView()
        observeViewModel()
    }

    // biar datanya ke-refresh kalo kita balik ke halaman ini
    override fun onResume() {
        super.onResume()
        orderViewModel.fetchOrders() // Panggil API
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { orderId ->
            // ubah Intent(this, ...) -> Intent(requireContext(), ...)
            val intent = Intent(requireContext(), OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }

        rvOrders.layoutManager = LinearLayoutManager(requireContext())
        rvOrders.adapter = orderAdapter
    }

    private fun observeViewModel() {
        // observe with viewLifecycleOwner
        orderViewModel.orderList.observe(viewLifecycleOwner) { orders ->
            orderAdapter.updateData(orders)
        }

        orderViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        orderViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }
    // FUNGSI BARU 1: Buat nampilin Search Bar di Appbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_search_menu, menu) // Pake file XML yg kita bikin

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.isSubmitButtonEnabled = false // Nggak perlu tombol submit, kita cari live
        searchView?.setOnQueryTextListener(this) // Sambungin listener-nya ke Fragment ini

        super.onCreateOptionsMenu(menu, inflater)
    }

    // FUNGSI BARU 2: Implementasi listener-nya (yang tadi kita tambahin)
    override fun onQueryTextSubmit(query: String?): Boolean {
        orderViewModel.search(query.orEmpty()) // <-- TAMBAHIN "order"
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        orderViewModel.search(newText.orEmpty()) // <-- TAMBAHIN "order"
        return true
    }
}
