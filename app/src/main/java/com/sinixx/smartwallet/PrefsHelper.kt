package com.sinixx.smartwallet

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

class PrefsHelper() {
    private val prefs = SmartWalletApp.context().getSharedPreferences("transactions", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTransactions(transactions: List<Transaction>) {
        prefs.edit().putString("transactions", gson.toJson(transactions)).apply()
    }

    fun getTransactions(): List<Transaction> {
       return SharedPreferencesHelper().getAllTransactions()
    }

    fun getCategorySummary(monthYear: String): Map<String, Double> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // match your stored format
        val outputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        return getTransactions()
            .filter {
                try {
                    val parsedDate = inputFormat.parse(it.date) // 🟢 convert String -> Date
                    parsedDate != null &&
                            outputFormat.format(parsedDate) == monthYear &&
                            it.type.equals("expense", ignoreCase = true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }


    fun getTotalExpenses(monthYear: String): Double {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        // Debug prints
        println("Month-Year: $monthYear")
        println("Transactions: ${getTransactions()}")

        return getTransactions()
            .filter { txn ->
                txn.type.equals("expense", ignoreCase = true)// && formatter.format(txn.date) == monthYear
            }
            .sumOf { it.amount }
    }
}