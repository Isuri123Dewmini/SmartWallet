package com.sinixx.smartwallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var et_amount: EditText;
    private lateinit var et_description: EditText;
    private lateinit var spinner_type: android.widget.Spinner;
    private lateinit var btn_add_transaction: Button;
    private lateinit var spHelper: SharedPreferencesHelper
    private lateinit var spinner_category: Spinner
    private lateinit var et_date: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpUI()
        setupCategorySpinner()
        setupDatePicker()
        btnAddTransaction()
    }

    private fun setUpUI() {
        et_amount = findViewById(R.id.et_amount)
        et_description = findViewById(R.id.et_description)
        spinner_type = findViewById(R.id.spinner_type)
        btn_add_transaction = findViewById(R.id.btnApply)
        spinner_category = findViewById(R.id.spinner_category)
        et_date = findViewById(R.id.et_date)
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Other")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_category.adapter = adapter
    }

    private fun setupDatePicker() {
        et_date.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                et_date.setText(dateStr)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
    }

    private fun btnAddTransaction() {
        btn_add_transaction.setOnClickListener {
            val amount = et_amount.text.toString()
            val description = et_description.text.toString()
            val type = spinner_type.selectedItem.toString()
            val category = spinner_category.selectedItem?.toString() ?: "Other"
            val date = et_date.text.toString()
            if (amount.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty()) {
                val transaction = Transaction(
                    id = 0, // This will be set by the database
                    title = description,
                    amount = amount.toDouble(),
                    category = category,
                    date = date,
                    type = type
                )
                spHelper = SharedPreferencesHelper()
                spHelper.createTransaction(transaction)
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}