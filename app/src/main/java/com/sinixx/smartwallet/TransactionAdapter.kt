package com.sinixx.smartwallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    var transactionList: List<Transaction>,
    private val context: Context,
    private val onDeleteClickListener: (Transaction) -> Unit,
    private val onEditClickListener: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.transactionDescription)
        val amount: TextView = itemView.findViewById(R.id.transactionAmount)
        val date: TextView = itemView.findViewById(R.id.transactionDate)
        val editButton: ImageButton  = itemView.findViewById(R.id.btnEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
        val transactionType: TextView = itemView.findViewById(R.id.transactionCategory)
    }

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = transactionList[position]
        holder.description.text = currentItem.title
        holder.amount.text = currentItem.amount.toString()
        holder.date.text = currentItem.date
        holder.transactionType.text = currentItem.type

        holder.editButton.setOnClickListener {
            onEditClickListener(currentItem)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(currentItem)
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Transaction")
        builder.setMessage("Are you sure you want to delete this transaction?" +
                "\n\nDescription: ${transaction.title}" +
                "\nType: ${transaction.type}" +
                "\nAmount: ${transaction.amount}" +
                "\nDate: ${transaction.date}")

        builder.setPositiveButton("Delete") { dialog, _ ->
            onDeleteClickListener(transaction)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun updateData (newTransactionList: List<Transaction>) {
        transactionList = newTransactionList
        notifyDataSetChanged()
    }

}
