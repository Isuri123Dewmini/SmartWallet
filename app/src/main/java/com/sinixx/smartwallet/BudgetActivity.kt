package com.sinixx.smartwallet

import android.os.Bundle

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sinixx.smartwallet.SharedPreferencesHelper.BudgetPrefsHelper
import com.sinixx.smartwallet.databinding.ActivityBudgetBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetBinding
    private lateinit var budgetPrefs: BudgetPrefsHelper
    private lateinit var transactionPrefs: PrefsHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var spinnerCurrency: android.widget.Spinner
    private val currencyPrefs by lazy { getSharedPreferences("prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        budgetPrefs = BudgetPrefsHelper(this)
        transactionPrefs = PrefsHelper()

        setupRecyclerView()
        setupBudgetInput()
        updateBudgetDisplay()
        spinnerCurrency = binding.root.findViewById(R.id.spinnerCurrency)
        setupCurrencySpinner()
    }

    private fun setupRecyclerView() = with(binding.rvCategories) {
        layoutManager = LinearLayoutManager(this@BudgetActivity)
        adapter = CategoryAdapter().also { categoryAdapter = it }
    }

    private fun setupBudgetInput() = binding.btnSaveBudget.setOnClickListener {
        binding.etBudget.text.toString().takeIf { it.isNotBlank() }?.let { input ->
            input.toDoubleOrNull()?.let { budget ->
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                budgetPrefs.setBudget(currentMonth, budget)
                updateBudgetDisplay()
            } ?: Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("LKR", "USD", "EUR", "INR")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
        val savedCurrency = currencyPrefs.getString("currency", "LKR")
        spinnerCurrency.setSelection(currencies.indexOf(savedCurrency))
        spinnerCurrency.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                currencyPrefs.edit().putString("currency", currencies[position]).apply()
                updateBudgetDisplay()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
    }

    private fun getCurrencySymbol(): String {
        return when (currencyPrefs.getString("currency", "LKR")) {
            "USD" -> "$"
            "EUR" -> "€"
            "INR" -> "₹"
            else -> "Rs"
        }
    }

    private fun updateBudgetDisplay() {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val budget = budgetPrefs.getBudget(currentMonth)
        val expenses = transactionPrefs.getTotalExpenses(currentMonth)
        val progress = if (budget > 0) (expenses / budget * 100).toInt() else 0
        val currency = getCurrencySymbol()

        // Update progress bar
        binding.progressBudget.apply {
            setProgressCompat(progress.coerceAtMost(100), true)
            setIndicatorColor(ContextCompat.getColor(this@BudgetActivity, if (progress >= 90) R.color.red else R.color.green))
        }

        // Update text displays
        binding.tvBudgetPercentage.text = expenses.toString()
        binding.tvSpentAmount.text = "Spent: $currency${"%.2f".format(expenses)}"
        binding.tvBudgetAmount.text = "Budget: $currency${"%.2f".format(budget)}"

        // Show warning if spending nears/exceeds budget
        if (budget > 0 && progress >= 90) {
            val alertMsg = if (progress >= 100) {
                // Show alert dialog when budget is exceeded
                android.app.AlertDialog.Builder(this@BudgetActivity)
                    .setTitle("Budget Exceeded")
                    .setMessage("Warning: You have exceeded your budget!")
                    .setPositiveButton("OK", null)
                    .show()
                com.sinixx.smartwallet.NotificationUtils.showBudgetAlert(this@BudgetActivity, "You have exceeded your budget!")
                "Warning: You have exceeded your budget!"
            } else {
                com.sinixx.smartwallet.NotificationUtils.showBudgetAlert(this@BudgetActivity, "You are nearing your budget limit!")
                "Warning: You are nearing your budget limit!"
            }
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(this@BudgetActivity, R.color.red))
            binding.tvBudgetWarning.text = alertMsg
            binding.tvBudgetWarning.visibility = android.view.View.VISIBLE
        } else {
            binding.tvBudgetWarning.visibility = android.view.View.GONE
        }

        // Update category list
        val categorySummary: List<Pair<String, Double>> = transactionPrefs.getCategorySummary(currentMonth)
            .orEmpty()
            .toList()
        categoryAdapter.submitList(categorySummary)
    }

    override fun onResume() {
        super.onResume()
        updateBudgetDisplay()
    }
}

