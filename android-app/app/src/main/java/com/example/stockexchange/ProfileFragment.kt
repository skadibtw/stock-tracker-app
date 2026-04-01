package com.example.stockexchange

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stockexchange.data.StockRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val symbols = listOf(
        "SBER",
        "GAZP",
        "LKOH",
        "YNDX",
        "ROSN",
        "VTBR",
        "NVTK",
        "MGNT"
    )

    private val stockRepository = StockRepository()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = arguments?.getString("username") ?: "Иван Петров"

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvBalance = view.findViewById<TextView>(R.id.tvBalance)
        val tvStockCount = view.findViewById<TextView>(R.id.tvStockCount)
        val tvRegDate = view.findViewById<TextView>(R.id.tvRegDate)
        val switchDarkTheme = view.findViewById<SwitchMaterial>(R.id.switchDarkTheme)

        switchDarkTheme.isChecked = ThemePrefs.isDarkEnabled(requireContext())
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            ThemePrefs.setDarkEnabled(requireContext(), isChecked)
        }
        val btnRefreshStats = view.findViewById<Button>(R.id.btnRefreshStats)

        tvUserName.text = username
        tvUserEmail.text = "$username@example.com".lowercase()
        tvBalance.text = "10 000 ₽"
        tvStockCount.text = "5"
        tvRegDate.text = "01.01.2024"


        btnRefreshStats.setOnClickListener {
            refreshStatistics(tvBalance, tvStockCount)
            updateBottomNavQuotes()
        }

        // Обновляем label в bottom nav при открытии профиля.
        updateBottomNavQuotes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun refreshStatistics(tvBalance: TextView, tvStockCount: TextView) {
        val newBalance = (-5000..50000).random()
        val newCount = (1..20).random()

        tvBalance.text = "$newBalance ₽"
        tvStockCount.text = newCount.toString()

        Toast.makeText(requireContext(), "Статистика обновлена", Toast.LENGTH_SHORT).show()
    }

    private fun updateBottomNavQuotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            val quotes = runCatching { stockRepository.fetchQuotes(symbols) }
                .getOrElse { throwable ->
                    // requireContext() может упасть, если фрагмент уже отсоединен при пересоздании по смене темы.
                    context?.let {
                        Toast.makeText(it, "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show()
                    }
                    emptyList()
                }

            val top = quotes.maxByOrNull { it.changePct }
            if (top == null) return@launch

            // requireActivity() может упасть, если фрагмент уже не attached.
            val bottomNav: BottomNavigationView =
                activity?.findViewById(R.id.bottomNavigation) ?: return@launch

            val titleProfile = "Профиль"
            val titleMyStat = "Статистика"
            val titleStockList = "Акции"

            bottomNav.menu.findItem(R.id.nav_profile)?.title = titleProfile
            bottomNav.menu.findItem(R.id.nav_statistics)?.title = titleMyStat
            bottomNav.menu.findItem(R.id.nav_stock_list)?.title = titleStockList
        }
    }

    private fun formatRub(value: Double): String {
        return String.format(Locale.US, "%.2f ₽", value)
    }

    private fun formatPct(value: Double): String {
        return String.format(Locale.US, "%+.2f%%", value)
    }
}