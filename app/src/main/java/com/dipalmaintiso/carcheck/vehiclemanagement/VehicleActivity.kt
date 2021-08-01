package com.dipalmaintiso.carcheck.vehiclemanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.rows.GroupVehiclesRow
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_vehicle.*

class VehicleActivity : AppCompatActivity() {

    var groupId: String? = null
    var vehicleId: String? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle)

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

        vehicleDataCardViewVehicleActivity.setOnClickListener {
            val intent = Intent(this, VehicleDataActivity::class.java)

            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(VEHICLE_ID, vehicleId)

            startActivity(intent)
        }

        vehicleExpiriesCardViewVehicleActivity.setOnClickListener {
            val intent = Intent(this, VehicleExpiriesActivity::class.java)

            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(VEHICLE_ID, vehicleId)

            startActivity(intent)
        }
    }
}