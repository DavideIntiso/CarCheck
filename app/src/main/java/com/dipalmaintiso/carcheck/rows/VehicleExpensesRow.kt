package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expense
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.vehicle_expenses_row.view.*
import java.text.DateFormat
import java.time.Instant

class VehicleExpensesRow (private val expense: Expense): Item<ViewHolder>(){
    var eid = expense.eid
    override fun getLayout(): Int {
        return R.layout.vehicle_expenses_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.expenseNameTextView.text = expense.expenseName
        val expenseDate = DateFormat.getDateInstance().format(expense.expenseDate)
        val expenseValue = expense.expenseValue.toString()
        val expenseMessage = "€$expenseValue, on $expenseDate"

        viewHolder.itemView.expenseMessageTextView.text = expenseMessage
    }
}