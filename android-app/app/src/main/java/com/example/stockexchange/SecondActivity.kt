package com.example.stockexchange

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stockexchange.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    private companion object {
        const val KEY_SELECTED_NAV = "selected_nav"
        const val KEY_USERNAME = "username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePrefs.applySaved(this)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = savedInstanceState?.getString(KEY_USERNAME) ?: intent.getStringExtra("username")

        setupBottomNavigation(binding.bottomNavigation, username)

        binding.bottomNavigation.selectedItemId =
            savedInstanceState?.getInt(KEY_SELECTED_NAV, R.id.nav_profile) ?: R.id.nav_profile
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_NAV, binding.bottomNavigation.selectedItemId)
        outState.putString(KEY_USERNAME, (intent.getStringExtra("username") ?: ""))
    }

    private fun openProfileFragment(username: String?) {
        val fragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                if (!username.isNullOrEmpty()) {
                    putString("username", username)
                }
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupBottomNavigation(bottomNav: BottomNavigationView, username: String?) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    openProfileFragment(username)
                    true
                }
                R.id.nav_statistics -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, StatisticsFragment())
                        .commit()
                    true
                }
                R.id.nav_stock_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, StockListFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

}
