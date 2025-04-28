package com.sinixx.smartwallet

import android.icu.text.DecimalFormat
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sinixx.smartwallet.databinding.ActivityCategoryAnalysisBinding
import java.util.Date

class CategoryAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAnalysisBinding
    private val viewModel: CategoryAnalysisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transactions = SharedPreferencesHelper().getAllTransactions()

        // Set transactions to ViewModel
        viewModel.setTransactions(transactions)

        // Observe changes in category analysis data
        viewModel.categoryAnalysis.observe(this) { categoryAnalysisActivity ->
            // Update UI when data changes
            updateUI()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = CategoryAdapter()
        binding.rvTopCategories.layoutManager = LinearLayoutManager(this)
        binding.rvTopCategories.adapter = adapter

        updateUI()
    }

    private fun updateUI() {
        viewModel.categoryAnalysis.value?.let { categoryAnalysis ->
            val topCategories = categoryAnalysis.getTopCategories(5)
            val totalExpense = categoryAnalysis.calculateCategoryTotals().values.sum()

            // Set total expense
            binding.tvTotalExpense.text = "Total Expenses: ${formatCurrency(totalExpense)}"

            // Update top categories RecyclerView
            (binding.rvTopCategories.adapter as CategoryAdapter).submitList(topCategories)

            // Update category percentage breakdown
            val breakdown = categoryAnalysis.getPercentageBreakdown()
            binding.tvPercentageBreakdown.text = breakdown.entries.joinToString("\n") {
                "${it.key}: ${formatPercentage(it.value)}%"
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return DecimalFormat("$#,##0.00").format(amount)
    }
    private fun formatPercentage(percentage: Double): String {
        return DecimalFormat("0.00").format(percentage)
    }

    fun getTopCategories(limit: Int): List<Pair<String, Double>> {
        val transactions = SharedPreferencesHelper().getAllTransactions()
        return transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
    }

    fun getPercentageBreakdown(): Map<String, Double> {
        val transactions = SharedPreferencesHelper().getAllTransactions()
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        return transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .mapValues { (category, total) -> (total / totalExpense) * 100 }
    }

    fun calculateCategoryTotals(): Map<String, Double> {
        val transactions = SharedPreferencesHelper().getAllTransactions()
        return transactions
            .filter { it.type.lowercase() == "expense" }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }

}
