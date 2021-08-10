package com.dipalmaintiso.carcheck.vehiclemanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expense
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.EXPENSE_ID
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_vehicle_expense.*
import java.time.Instant
import java.util.*

class VehicleExpenseActivity : AppCompatActivity() {

    var groupId: String? = null
    private var vehicleId: String? = null
    var userId: String? = null
    private var expenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_expense)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)
        expenseId = intent.getStringExtra(EXPENSE_ID)

        if ("new" != expenseId && null != expenseId)
            fetchExpenseDataFromDatabase()

        makeExpenseDataEditable()
    }

    private fun fetchExpenseDataFromDatabase() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId/expenses/$expenseId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val expense = dataSnapshot.getValue(Expense::class.java)!!

                expenseNameEditTextVehicleExpense.setText(expense.expenseName)
                expenseValueEditTextVehicleExpense.setText(expense.expenseValue.toString())
                expenseNotesEditTextVehicleExpense.setText(expense.expenseNotes)
                expenseDateCalendarViewVehicleExpense.date = expense.expenseDate
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun makeExpenseDataEditable() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId/expenses")

        if ("new" == expenseId) {
            expenseId = ref.push().key
        }

        if (null == vehicleId || "" == vehicleId) {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
        }

        deleteButtonVehicleExpense.setOnClickListener {
            ref.child(expenseId!!).removeValue()

            finish()
        }

        var expenseDate = 0L

        expenseDateCalendarViewVehicleExpense.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val c: Calendar = Calendar.getInstance()
            c.set(year, month, dayOfMonth)
            expenseDate = c.timeInMillis
        }

        saveButtonVehicleExpense.setOnClickListener {
            val expenseName = expenseNameEditTextVehicleExpense.text.toString()
            val expenseValueText = expenseValueEditTextVehicleExpense.text.toString()
            val expenseNotes = expenseNotesEditTextVehicleExpense.text.toString()

            if (expenseName.isBlank() || expenseValueText.isBlank())
                Toast.makeText(this, "Please enter a name and a value for the expense.", Toast.LENGTH_LONG).show()

            else {
                val expenseValue = expenseValueText.toDouble()
                if (expenseDate == 0L)
                    expenseDate = Instant.now().toEpochMilli()

                val expense = Expense(expenseId, expenseName, expenseValue, expenseNotes, expenseDate)

                ref.child(expenseId!!).setValue(expense)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}