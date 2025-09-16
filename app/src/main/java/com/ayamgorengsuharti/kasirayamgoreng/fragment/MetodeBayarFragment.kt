// File: fragment/MetodeBayarFragment.kt
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
import com.ayamgorengsuharti.kasirayamgoreng.EditMetodeBayarActivity // Bakal merah dulu
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.MetodeBayarAdapter
import com.ayamgorengsuharti.kasirayamgoreng.viewmodel.MetodeBayarViewModel

class MetodeBayarFragment : Fragment(R.layout.fragment_metode_bayar) {

    private val viewModel: MetodeBayarViewModel by viewModels()
    private lateinit var adapter: MetodeBayarAdapter

    private lateinit var rvMetode: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMetode = view.findViewById(R.id.rv_metode_bayar)
        progressBar = view.findViewById(R.id.progress_bar_metode)

        setupRecyclerView()
        observeViewModel()
    }

    // Pake onResume biar list-nya refresh pas kita balik dari halaman Edit
    override fun onResume() {
        super.onResume()
        viewModel.fetchMetode()
    }

    private fun setupRecyclerView() {
        adapter = MetodeBayarAdapter(emptyList()) { metode ->
            // Kalo item diklik
            val intent = Intent(requireContext(), EditMetodeBayarActivity::class.java)
            // Kirim SEMUA data metode-nya ke activity baru
            // Ini bisa karena model-nya udah : Serializable (Langkah 0)
            intent.putExtra("DATA_METODE", metode)
            startActivity(intent)
        }
        rvMetode.layoutManager = LinearLayoutManager(requireContext())
        rvMetode.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.metodeList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }
}