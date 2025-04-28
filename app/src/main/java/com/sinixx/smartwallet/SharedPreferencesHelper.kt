package com.sinixx.smartwallet

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper() {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SmartWalletApp.context())

    fun createTransaction(transaction: Transaction) {
        val nextId = sharedPreferences.getInt("next_transaction_id", 1)
        val newTransaction = transaction.copy(id = nextId)

        sharedPreferences.edit {
            putString("transaction_${nextId}_title", newTransaction.title)
            putFloat("transaction_${nextId}_amount", newTransaction.amount.toFloat())
            putString("transaction_${nextId}_category", newTransaction.category)
            putString("transaction_${nextId}_date", newTransaction.date)
            putString("transaction_${nextId}_type", newTransaction.type)
            val transactionIds = getTransactionIds().toMutableSet()
            transactionIds.add(nextId.toString())
            putStringSet("transaction_ids", transactionIds)
            putInt("next_transaction_id", nextId + 1)
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val transactionIds = getTransactionIds()
        return transactionIds.mapNotNull { idStr ->
            val id = idStr.toIntOrNull() ?: return@mapNotNull null
            val title = sharedPreferences.getString("transaction_${id}_title", "") ?: ""
            val amount = sharedPreferences.getFloat("transaction_${id}_amount", 0f).toDouble()
            val category = sharedPreferences.getString("transaction_${id}_category", "") ?: ""
            val date = sharedPreferences.getString("transaction_${id}_date", "") ?: ""
            val type = sharedPreferences.getString("transaction_${id}_type", "") ?: ""
            Transaction(id, title, amount, category, date, type)
        }
    }

    fun clearAllTransactions() {
        val transactionIds = getTransactionIds()
        sharedPreferences.edit {
            transactionIds.forEach { idStr ->
                val id = idStr.toIntOrNull() ?: return@forEach
                remove("transaction_${id}_title")
                remove("transaction_${id}_amount")
                remove("transaction_${id}_category")
                remove("transaction_${id}_date")
                remove("transaction_${id}_type")
            }
            remove("transaction_ids")
            putInt("next_transaction_id", 1)
        }
    }

    fun editTransaction(transaction: Transaction) {
        val id = transaction.id
        if (id == 0) return
        val transactionIds = getTransactionIds()
        if (!transactionIds.contains(id.toString())) return

        sharedPreferences.edit {
            putString("transaction_${id}_title", transaction.title)
            putFloat("transaction_${id}_amount", transaction.amount.toFloat())
            putString("transaction_${id}_category", transaction.category)
            putString("transaction_${id}_date", transaction.date)
            putString("transaction_${id}_type", transaction.type)
        }
    }

    fun deleteTransaction(id: Int) {
        val transactionIds = getTransactionIds().toMutableSet()
        if (transactionIds.remove(id.toString())) {
            sharedPreferences.edit {
                putStringSet("transaction_ids", transactionIds)
                remove("transaction_${id}_title")
                remove("transaction_${id}_amount")
                remove("transaction_${id}_category")
                remove("transaction_${id}_date")
                remove("transaction_${id}_type")
            }
        }
    }

    fun getTransactionById(id: Int): Transaction? {
        val title = sharedPreferences.getString("transaction_${id}_title", null)
        val amount = sharedPreferences.getFloat("transaction_${id}_amount", 0f).toDouble()
        val category = sharedPreferences.getString("transaction_${id}_category", null)
        val date = sharedPreferences.getString("transaction_${id}_date", null)
        val type = sharedPreferences.getString("transaction_${id}_type", null)

        return if (title != null && category != null && date != null && type != null) {
            Transaction(id, title, amount, category, date, type)
        } else {
            null
        }
    }

    fun getCategorySummary(monthYear: String): Map<String, Double> {
        val sharedPrefs: SharedPreferences = sharedPreferences
        val json = sharedPrefs.getString("transaction_list", null) ?: return emptyMap()

        val transactions = Gson().fromJson<List<Transaction>>(json, object : TypeToken<List<Transaction>>() {}.type)
            ?: return emptyMap()

        return transactions
            .asSequence()
            .filter { transaction ->
                val isExpense = transaction.type == "expense"
                val sameMonth = transaction.date.startsWith(monthYear)
                isExpense && sameMonth
            }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }

    class BudgetPrefsHelper(context: Context) {
        private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

        fun setBudget(monthYear: String, amount: Double) {
            prefs.edit().putFloat(monthYear, amount.toFloat()).apply()
        }

        fun getBudget(monthYear: String): Double {
            return prefs.getFloat(monthYear, 0f).toDouble()
        }

        // Optional: Get all budgets
        fun getAllBudgets(): Map<String, Double> {
            return prefs.all.mapValues { it.value.toString().toDouble() }
        }
    }

    private fun getTransactionIds(): Set<String> {
        return sharedPreferences.getStringSet("transaction_ids", emptySet()) ?: emptySet()
    }

    fun saveTransactions(transactions: List<Transaction>) {
        clearAllTransactions()
        transactions.forEach { createTransaction(it) }
    }
}