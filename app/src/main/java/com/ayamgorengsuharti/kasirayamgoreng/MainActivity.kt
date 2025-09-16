// File: MainActivity.kt
package com.ayamgorengsuharti.kasirayamgoreng

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- INI SEMUA KODE SETUP NAVIGASI ---

        // 1. Cari semua View penting
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        navView = findViewById(R.id.nav_view)

        // 2. Pasang Toolbar sebagai Appbar resmi
        setSupportActionBar(toolbar)

        // 3. Ambil NavController (Si Pengatur "Kamar") dari Host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 4. Definisikan Halaman "Level Atas" (yg ada di sidebar)
        // Ini biar tombol "Burger" (garis tiga) muncul di halaman ini
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_menu, R.id.nav_orders, R.id.nav_pos // <-- ID dari menu lo
            ), drawerLayout
        )

        // 5. Sambungin NavController ke Appbar (Biar judul & tombol kembali otomatis)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 6. Sambungin NavController ke Sidebar (Biar sidebar bisa diklik)
        navView.setupWithNavController(navController)
    }

    // Fungsi ini wajib ada biar tombol "Kembali" di Appbar (kalo bukan di home) berfungsi
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}