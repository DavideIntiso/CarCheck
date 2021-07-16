package com.dipalmaintiso.carcheck.management

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.UserGroupsRow
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_vehicle_data.*

var groupId: String? = null
var vehicleId: String? = null
var userId: String? = null
var admin: Boolean = false

class VehicleDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_data)
        spinner.isEnabled = false
        spinner.background = null

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)

        verifyUserAdministrator()

        if ("" == vehicleId && admin) {
            makeVehicleDataEditable()
        }
    }

    private fun makeVehicleDataEditable() {
        spinner.isEnabled = false
    }

    private fun verifyUserAdministrator() {
        userId = FirebaseAuth.getInstance().uid
        verifyUserLoggedIn()

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun verifyUserLoggedIn(){
        if (userId == null){
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}