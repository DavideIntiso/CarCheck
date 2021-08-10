package com.dipalmaintiso.carcheck.vehiclemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expense
import com.dipalmaintiso.carcheck.models.Expiry
import com.dipalmaintiso.carcheck.rows.VehicleExpensesRow
import com.dipalmaintiso.carcheck.rows.VehicleExpiriesRow
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.EXPENSE_ID
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_vehicle_expenses.*
import kotlinx.android.synthetic.main.activity_vehicle_expiries.*

class VehicleExpensesActivity : AppCompatActivity() {
    val adapter = GroupAdapter<ViewHolder>()
    var groupId: String? = null
    var userId: String? = null
    var vehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_expenses)
        vehicleExpensesRecyclerView.adapter = adapter
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        vehicleExpensesRecyclerView.addItemDecoration(itemDecoration)

        userId = FirebaseAuth.getInstance().uid
        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)

        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupName = dataSnapshot.child("groupName").getValue(String::class.java)!!

                val vehicleRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vehicleId")
                vehicleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val plate = dataSnapshot.child("plate").getValue(String::class.java)!!

                        supportActionBar?.title = "$groupName - $plate"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        supportActionBar?.title = "$groupName - Vehicle"
                        Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "Vehicle"
                Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        displayExpenses()
        verifyUserAdministrator()
    }

    private fun verifyUserAdministrator() {
        userId = FirebaseAuth.getInstance().uid
        val intent = Intent(this, VehicleExpenseActivity::class.java)

        intent.putExtra(GROUP_ID, groupId)
        intent.putExtra(VEHICLE_ID, vehicleId)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.child("users/$userId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if (admin) {
                    adapter.setOnItemClickListener { item, _ ->
                        val vehicleExpensesRow = item as VehicleExpensesRow
                        intent.putExtra(EXPENSE_ID, vehicleExpensesRow.eid)

                        startActivity(intent)
                    }

                    addExpenseFloatingActionButton.isEnabled = true
                    addExpenseFloatingActionButton.isVisible = true
                    addExpenseFloatingActionButton.setOnClickListener {
                        intent.putExtra(EXPENSE_ID, "new")

                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun displayExpenses() {
        adapter.clear()
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId/expenses")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (child in dataSnapshot.children) {
                        val expense = child.getValue(Expense::class.java)!!
                        adapter.add(VehicleExpensesRow(expense))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}