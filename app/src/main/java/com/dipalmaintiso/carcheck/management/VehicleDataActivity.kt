package com.dipalmaintiso.carcheck.management

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.dipalmaintiso.carcheck.utilities.addVehicleToGroup
import com.dipalmaintiso.carcheck.views.GroupUsersActivity
import com.dipalmaintiso.carcheck.views.GroupVehiclesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_vehicle_data.*
import java.util.*

var groupId: String? = null
var vehicleId: String? = null
var userId: String? = null
var admin: Boolean = false
var bg: Drawable? = null

class VehicleDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_data)
        spinner.isEnabled = false
        bg = spinner.background
        spinner.background = null

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)

        verifyUserAdministratorAndDisplay()
    }

    private fun makeVehicleDataEditable() {
        spinner.isEnabled = true
        spinner.background = bg

        makeEditTextVehicleData.isEnabled = true
        modelEditTextVehicleData.isEnabled = true
        plateEditTextVehicleData.isEnabled = true
        seatsEditTextVehicleData.isEnabled = true

        cancelButtonVehicleData.isEnabled = true
        cancelButtonVehicleData.isVisible = true
        saveButtonVehicleData.isEnabled = true
        saveButtonVehicleData.isVisible = true

        cancelButtonVehicleData.setOnClickListener {
            val intent = Intent(this, GroupVehiclesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(GROUP_ID, groupId)
            startActivity(intent)
        }

        saveButtonVehicleData.setOnClickListener {
            saveVehicleToFirebaseDatabase()
        }
    }

    private fun saveVehicleToFirebaseDatabase() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles")

        val vid = ref.push().key

        if (null == vid) {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
        }

        else {
            val make = makeEditTextVehicleData.text.toString()
            val model = modelEditTextVehicleData.text.toString()
            val plate = plateEditTextVehicleData.text.toString()
            val type = spinner.selectedItem.toString()
            val seats = seatsEditTextVehicleData.text.toString().toInt()

            var vehicle = Vehicle(vid, groupId, make, model, plate, type, seats)

            ref.child(vid).setValue(vehicle)
                .addOnSuccessListener {
                    val intent = Intent(applicationContext, GroupVehiclesActivity::class.java)
                    intent.putExtra(GROUP_ID, groupId)

                    addVehicleToGroup(groupId, vid, applicationContext, intent, ref.child(vid))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun verifyUserAdministratorAndDisplay() {
        userId = FirebaseAuth.getInstance().uid
        verifyUserLoggedIn()

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if ("new" == vehicleId && admin) {
                    makeVehicleDataEditable()
                }
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