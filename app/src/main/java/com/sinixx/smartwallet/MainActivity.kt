package com.sinixx.smartwallet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton as GFloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var rView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var spHelper: SharedPreferencesHelper
    private lateinit var editTransactionLauncher: ActivityResultLauncher<Intent>
    private lateinit var addTransactionLauncher: ActivityResultLauncher<Intent>
    private lateinit var btnAdd: GFloatingActionButton
    private lateinit var btnBActivity : GFloatingActionButton
    private lateinit var btnExportData: android.widget.Button
    private lateinit var btnImportData: android.widget.Button
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            exportTransactionsToUri(uri)
        }
    }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            importTransactionsFromUri(uri)
        }
    }

    init {
        editTransactionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Handle result here if needed */
        }

        addTransactionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                loadTransactions()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize shared preferences helper
        spHelper = SharedPreferencesHelper()
        // Setup views
        btnAdd = findViewById(R.id.btnAdd)
        btnBActivity = findViewById(R.id.btnBActivity)
        rView = findViewById(R.id.rView)
        btnExportData = findViewById(R.id.btnExportData)
        btnImportData = findViewById(R.id.btnImportData)
        // Setup RecyclerView
        setupRecyclerView()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)       }

        btnBActivity.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }
        btnExportData.setOnClickListener {
            exportLauncher.launch("SmartWalletBackup_${System.currentTimeMillis()}.json")
        }
        btnImportData.setOnClickListener {
            importLauncher.launch("application/json")
        }
        scheduleDailyReminder()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            transactionList = spHelper.getAllTransactions(),
            context = this,
            onDeleteClickListener = { transaction -> delTransaction(transaction.id) },
            onEditClickListener = { transaction -> editTransaction(transaction.id) }
        )
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter = adapter
    }

    private fun editTransaction(id: Int) {
        val intent = Intent(this, EditTransactionActivity::class.java)
        intent.putExtra("transaction_id", id)
        editTransactionLauncher.launch(intent)
    }

    private fun delTransaction(id: Int) {
        spHelper.deleteTransaction(id)
        loadTransactions()
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun loadTransactions() {
        val transactions = spHelper.getAllTransactions()
        adapter.updateData(transactions)
    }

    private fun exportTransactionsToUri(uri: android.net.Uri) {
        val transactions = spHelper.getAllTransactions()
        val json = com.google.gson.Gson().toJson(transactions)
        try {
            contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            Toast.makeText(this, "Export successful!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importTransactionsFromUri(uri: android.net.Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (!json.isNullOrEmpty()) {
                val type = object : com.google.gson.reflect.TypeToken<List<Transaction>>() {}.type
                val transactions: List<Transaction> = com.google.gson.Gson().fromJson(json, type)
                // Overwrite all transactions with imported ones
                spHelper.saveTransactions(transactions)
                loadTransactions()
                Toast.makeText(this, "Import successful!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    companion object {
        const val EDIT_REQUEST_CODE = 1001
    }
}