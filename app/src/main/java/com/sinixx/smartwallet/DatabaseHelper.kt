package com.sinixx.smartwallet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "FinanceTracker.db"
        const val DATABASE_VERSION = 1
    }

    // Table and column names
    object TransactionEntry {
        const val TABLE_NAME = "transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_TYPE = "type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES = """
            CREATE TABLE ${TransactionEntry.TABLE_NAME} (
                ${TransactionEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TransactionEntry.COLUMN_TITLE} TEXT NOT NULL,
                ${TransactionEntry.COLUMN_AMOUNT} REAL NOT NULL,
                ${TransactionEntry.COLUMN_CATEGORY} TEXT NOT NULL,
                ${TransactionEntry.COLUMN_DATE} TEXT NOT NULL,
                ${TransactionEntry.COLUMN_TYPE} TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${TransactionEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun addTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TransactionEntry.COLUMN_TITLE, transaction.title)
            put(TransactionEntry.COLUMN_AMOUNT, transaction.amount)
            put(TransactionEntry.COLUMN_CATEGORY, transaction.category)
            put(TransactionEntry.COLUMN_DATE, transaction.date)
            put(TransactionEntry.COLUMN_TYPE, transaction.type)
        }
        return db.insert(TransactionEntry.TABLE_NAME, null, values)
    }

    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TransactionEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(TransactionEntry.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(TransactionEntry.COLUMN_TITLE))
                val amount = getDouble(getColumnIndexOrThrow(TransactionEntry.COLUMN_AMOUNT))
                val category = getString(getColumnIndexOrThrow(TransactionEntry.COLUMN_CATEGORY))
                val date = getString(getColumnIndexOrThrow(TransactionEntry.COLUMN_DATE))
                val type = getString(getColumnIndexOrThrow(TransactionEntry.COLUMN_TYPE))

                transactions.add(
                    Transaction(
                        id = id,
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        type = type
                    )
                )
            }
        }
        cursor.close()
        return transactions
    }

    fun deleteTransaction(id: Int): Int {
        val db = writableDatabase
        println("Transaction with ID: $id deleted")
        return db.delete(
            TransactionEntry.TABLE_NAME,
            "${TransactionEntry.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun updateTransaction(transaction: Transaction): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TransactionEntry.COLUMN_TITLE, transaction.title)
            put(TransactionEntry.COLUMN_AMOUNT, transaction.amount)
            put(TransactionEntry.COLUMN_CATEGORY, transaction.category)
            put(TransactionEntry.COLUMN_DATE, transaction.date)
            put(TransactionEntry.COLUMN_TYPE, transaction.type)
        }
        return db.update(
            TransactionEntry.TABLE_NAME,
            values,
            "${TransactionEntry.COLUMN_ID} = ?",
            arrayOf(transaction.id.toString())
        )
    }

    fun getTransactionById(id: Int): Transaction? {
        val db = readableDatabase
        val cursor = db.query(
            TransactionEntry.TABLE_NAME,
            null,
            "${TransactionEntry.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_TITLE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_AMOUNT))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_CATEGORY))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_DATE))
            val type = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_TYPE))

            Transaction(
                id = id,
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type
            )
        } else {
            null
        }.also {
            cursor.close()
        }
    }
}