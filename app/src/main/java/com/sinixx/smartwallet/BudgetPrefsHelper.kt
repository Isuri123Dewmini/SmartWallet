package com.sinixx.smartwallet

import android.content.Context

class BudgetPrefsHelper(context: Context) {
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    fun setBudget(monthYear: String, amount: Double) {
        prefs.edit().putFloat(monthYear, amount.toFloat()).apply()
    }

    fun getBudget(monthYear: String): Double {
        return prefs.getFloat(monthYear, 0f).toDouble()
    }

    fun getAllBudgets(): Map<String, Double> {
        return prefs.all.mapValues { it.value.toString().toDouble() }
    }
}