package com.sinixx.smartwallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CategoryAnalysisViewModel : ViewModel() {

    private val _categoryAnalysis = MutableLiveData<CategoryAnalysisActivity>()
    val categoryAnalysis: LiveData<CategoryAnalysisActivity> get() = _categoryAnalysis

    private val transactions = PrefsHelper().getTransactions()

    fun setTransactions(transactions: List<Transaction>) {
        _categoryAnalysis.value = CategoryAnalysisActivity()
    }

    fun getTopCategories(limit: Int) {
        val topCategories = _categoryAnalysis.value?.getTopCategories(limit)
        // Handle updating UI with top categories if needed
    }
}

