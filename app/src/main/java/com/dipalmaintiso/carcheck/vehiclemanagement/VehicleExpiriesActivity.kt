package com.dipalmaintiso.carcheck.vehiclemanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expiry
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.rows.GroupUsersRow
import com.dipalmaintiso.carcheck.rows.GroupVehiclesRow
import com.dipalmaintiso.carcheck.rows.VehicleExpiryRow
import com.dipalmaintiso.carcheck.usermanagement.UserActivity
import com.dipalmaintiso.carcheck.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_group_vehicles.*
import kotlinx.android.synthetic.main.activity_vehicle_expiries.*
import java.util.ArrayList

class VehicleExpiriesActivity : AppCompatActivity() {
    val adapter = GroupAdapter<ViewHolder>()
    val vehiclesMap = ArrayList<String>()
    var groupId: String? = null
    var userId: String? = null
    var vehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_expiries)
        vehicleExpiriesRecyclerView.adapter = adapter
        var itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        vehicleExpiriesRecyclerView.addItemDecoration(itemDecoration)

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

        displayExpiries()

        verifyUserAdministrator()

    }

    private fun displayExpiries() {
        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles")

        groupRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                vehiclesMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                vehiclesMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun verifyUserAdministrator() {
        userId = FirebaseAuth.getInstance().uid
        val intent = Intent(this, VehicleExpiryActivity::class.java)

        intent.putExtra(GROUP_ID, groupId)
        intent.putExtra(VEHICLE_ID, vehicleId)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.child("users/$userId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if (admin) {
                    adapter.setOnItemClickListener { item, view ->
                        val vehicleExpiryRow = item as VehicleExpiryRow
                        intent.putExtra(EXPIRY_ID, vehicleExpiryRow.eid)

                        startActivity(intent)
                    }

                    addExpiryFloatingActionButton.isEnabled = true
                    addExpiryFloatingActionButton.isVisible = true
                    addExpiryFloatingActionButton.setOnClickListener {
                        intent.putExtra(EXPIRY_ID, "new")

                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun refreshRecyclerView(){
        vehiclesMap.forEach() {
            val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$it/expiries")
            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val expiry = dataSnapshot.getValue(Expiry::class.java)!!
                        adapter.add(VehicleExpiryRow(expiry, groupId, vehicleId))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
        vehiclesMap.clear()
        adapter.notifyDataSetChanged()
    }
}