// File: fragment/MenuFragment.kt
package com.ayamgorengsuharti.kasirayamgoreng.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.ayamgorengsuharti.kasirayamgoreng.CartActivity
import com.ayamgorengsuharti.kasirayamgoreng.MainViewModel
import com.ayamgorengsuharti.kasirayamgoreng.R
import com.ayamgorengsuharti.kasirayamgoreng.adapter.MenuAdapter

// 1. Ganti jadi : Fragment(R.layout.fragment_menu)
// Ini cara modern, otomatis ngurusin layout. Kita nggak perlu "onCreateView"
class MenuFragment : Fragment(R.layout.fragment_menu) {

    // Semua variabel kelas pindah ke sini
    private lateinit var rvMenu: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabCart: LottieAnimationView

    // ViewModel tetep sama, gampang
    private val mainViewModel: MainViewModel by viewModels()
    private val menuAdapter = MenuAdapter(emptyList())

    // 2. Semua logika dari "onCreate" pindah ke "onViewCreated"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. PENTING: Semua findViewById WAJIB diawali "view."
        rvMenu = view.findViewById(R.id.rv_menu)
        progressBar = view.findViewById(R.id.progress_bar)
        fabCart = view.findViewById(R.id.fab_cart)

        setupRecyclerView()
        observeViewModel()

        // Panggil API
        mainViewModel.fetchMenu()

        fabCart.setOnClickListener {
            // 4. PENTING: Konteks "this" ganti jadi "requireContext()"
            startActivity(Intent(requireContext(), CartActivity::class.java))
        }
    }

    // Fungsi-fungsi helper ini sama persis, tinggal copy-paste
    private fun setupRecyclerView() {
        rvMenu.layoutManager = LinearLayoutManager(requireContext()) // Konteks ganti
        rvMenu.adapter = menuAdapter
    }

    private fun observeViewModel() {
        // "this" di observe ganti jadi "viewLifecycleOwner"
        mainViewModel.menuList.observe(viewLifecycleOwner) { menu ->
            menuAdapter.updateData(menu)
        }

        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        mainViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }
}