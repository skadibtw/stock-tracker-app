package com.example.stockexchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockexchange.databinding.FragmentStockListBinding
import com.example.stockexchange.data.StockQuote
import com.example.stockexchange.data.StockRepository
import java.util.Locale
import kotlinx.coroutines.launch

class StockListFragment : Fragment() {

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stockRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = StockAdapter(emptyList()) { stock ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StockChartFragment.newInstance(stock))
                .addToBackStack(null)
                .commit()
        }
        binding.stockRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            val quotes = runCatching { stockRepository.fetchQuotes(symbols) }
                .getOrElse {
                    Toast.makeText(requireContext(), "Ошибка загрузки курсов", Toast.LENGTH_SHORT)
                        .show()
                    emptyList()
                }

            val newStocks = quotes.map { it.toStock() }
            if (newStocks.isEmpty()) {
                Toast.makeText(requireContext(), "Курсы не найдены", Toast.LENGTH_SHORT).show()
            }
            adapter.updateItems(newStocks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun StockQuote.toStock(): Stock {
        val priceText = String.format(Locale.US, "%.2f ₽", price)
        val changeText = String.format(Locale.US, "%+.2f ₽ (%+.2f%%)", changeAbs, changePct)
        return Stock(
            symbol = symbol,
            name = name,
            price = priceText,
            change = changeText
        )
    }

    private class StockAdapter(
        private var items: List<Stock>,
        private val onItemClick: (Stock) -> Unit
    ) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

        fun updateItems(newItems: List<Stock>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_stock, parent, false)
            return StockViewHolder(view)
        }

        override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
            holder.bind(items[position], onItemClick)
        }

        override fun getItemCount(): Int = items.size

        class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val symbolTextView: TextView = itemView.findViewById(R.id.symbolTextView)
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
            private val changeTextView: TextView = itemView.findViewById(R.id.changeTextView)

            fun bind(stock: Stock, onItemClick: (Stock) -> Unit) {
                symbolTextView.text = stock.symbol
                nameTextView.text = stock.name
                priceTextView.text = stock.price
                changeTextView.text = stock.change

                changeTextView.setTextColor(
                    when {
                        stock.change.startsWith("+") -> ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
                        stock.change.startsWith("-") -> ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                        else -> ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
                    }
                )

                itemView.setOnClickListener { onItemClick(stock) }
            }
        }
    }
}