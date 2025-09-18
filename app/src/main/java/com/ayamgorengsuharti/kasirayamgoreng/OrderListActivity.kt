package com.ayamgorengsuharti.kasirayamgoreng

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayamgorengsuharti.kasirayamgoreng.adapter.OrderAdapter
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.OrderViewModel

class OrderListActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        title = "Daftar Order"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvOrders = findViewById(R.id.rv_orders)
        progressBar = findViewById(R.id.progress_bar_orders)

        setupRecyclerView()
        observeViewModel()
    }

    // Pake onResume() biar datanya ke-refresh kalo kita balik ke halaman ini
    override fun onResume() {
        super.onResume()
        orderViewModel.fetchOrders() // Panggil API
    }
    override fun onSupportNavigateUp(): Boolean {
        // Ini cara modern buat handle tombol kembali di Appbar
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        // VVVV MODIF INI VVVV
        // Buat adapternya di sini, sambil masukin lambda-nya
        orderAdapter = OrderAdapter(emptyList()) { orderId ->
            // Ini kode yg bakal dijalanin pas item diklik
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", orderId) // Kirim ID-nya
            startActivity(intent)
        }

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter
    }

    private fun observeViewModel() {
        orderViewModel.orderList.observe(this) { orders ->
            orderAdapter.updateData(orders)
        }

        orderViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        orderViewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }
}