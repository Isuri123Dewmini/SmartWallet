package com.sinixx.smartwallet

import java.util.Date
import java.text.SimpleDateFormat

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    val type: String // "income" or "expense"
)