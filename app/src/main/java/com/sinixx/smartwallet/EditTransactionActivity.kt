package com.sinixx.smartwallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var et_id : EditText
    private lateinit var et_amount: EditText
    private lateinit var et_description: EditText
    private lateinit var spinner_type: Spinner
    private lateinit var spinner_category: Spinner
    private lateinit var et_date: EditText
    private lateinit var btn_apply_transaction: Button
    private lateinit var btn_delete_transaction: Button
    private lateinit var btn_revert_edit: Button
    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_transaction)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpUI()
        setListners()
    }

    private fun setUpUI() {
        et_id = findViewById<EditText>(R.id.et_id)
        et_amount = findViewById<EditText>(R.id.et_amount)
        et_description = findViewById<EditText>(R.id.et_description)
        spinner_type = findViewById<Spinner>(R.id.spinner_type)
        spinner_category = findViewById(R.id.spinner_category)
        et_date = findViewById(R.id.et_date)
        btn_apply_transaction = findViewById<Button>(R.id.btnApply)
        btn_delete_transaction = findViewById<Button>(R.id.btnDeleteOnEdit)
        btn_revert_edit = findViewById<Button>(R.id.btnRevert)
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Other")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_category.adapter = adapter
        val types = arrayOf("Income", "Expense")
        val typeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_type.adapter = typeAdapter
        et_date.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                et_date.setText(dateStr)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
        val transactionid = intent.getIntExtra("transaction_id", -1)
        if (transactionid != -1) {
            transaction = SharedPreferencesHelper().getTransactionById(transactionid)!!
            et_id.setText(transaction.id.toString())
            et_amount.setText(transaction.amount.toString())
            et_description.setText(transaction.title)
            // Set spinner_type selection using typeAdapter and transaction.type
            spinner_type.setSelection(typeAdapter.getPosition(transaction.type))
            spinner_category.setSelection(adapter.getPosition(transaction.category))
            et_date.setText(transaction.date)
        } else {
            finish()
        }
    }

    private fun setListners() {
        btn_apply_transaction.setOnClickListener {
            val id = et_id.text.toString()
            val amount = et_amount.text.toString()
            val description = et_description.text.toString()
            val type = spinner_type.selectedItem.toString()
            val category = spinner_category.selectedItem?.toString() ?: "Other"
            val date = et_date.text.toString()
            if (id.isNotEmpty() && amount.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty()) {
                val transaction = Transaction(
                    id = id.toInt(),
                    title = description,
                    amount = amount.toDouble(),
                    category = category,
                    date = date,
                    type = type
                )
                SharedPreferencesHelper().editTransaction(transaction)
            }
            finish()
        }
        btn_delete_transaction.setOnClickListener {
            val id = et_id.text.toString()
            if (id.isNotEmpty()) {
                SharedPreferencesHelper().deleteTransaction(id.toInt())
            }
            finish()
        }
        btn_revert_edit.setOnClickListener {
            finish()
        }
    }
}


