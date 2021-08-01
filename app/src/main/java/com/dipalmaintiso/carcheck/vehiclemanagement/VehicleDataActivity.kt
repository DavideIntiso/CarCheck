package com.dipalmaintiso.carcheck.vehiclemanagement

import android.content.Intent
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
import com.dipalmaintiso.carcheck.views.GroupVehiclesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_vehicle_data.*


var groupId: String? = null
var vehicleId: String? = null
var userId: String? = null
var admin: Boolean = false

class VehicleDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_data)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)

        verifyUserAdministratorAndDisplay()

        if ("new" != vehicleId && null != vehicleId)
            fetchVehicleDataFromDatabase()
    }

    private fun fetchVehicleDataFromDatabase() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vehicleId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val type = dataSnapshot.child("type").getValue(String::class.java)!!

                typeTextViewVehicleData.text = type

                for (i in 0 until typeSpinnerVehicleData.count) {
                    val spinnerValue = typeSpinnerVehicleData.getItemAtPosition(i)
                    if (spinnerValue.toString() == type) {
                        typeSpinnerVehicleData.setSelection(i)
                        break
                    }
                }

                makeEditTextVehicleData.setText(dataSnapshot.child("make").getValue(String::class.java)!!)
                modelEditTextVehicleData.setText(dataSnapshot.child("model").getValue(String::class.java)!!)
                plateEditTextVehicleData.setText(dataSnapshot.child("plate").getValue(String::class.java)!!)
                seatsEditTextVehicleData.setText(dataSnapshot.child("seats").getValue(Int::class.java)!!.toString())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun saveVehicleToFirebaseDatabase(intent: Intent) {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles")

        if ("new" == vehicleId) {
            vehicleId = ref.push().key
        }

        if (null == vehicleId || "" == vehicleId) {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
        }

        else {
            val make = makeEditTextVehicleData.text.toString()
            val model = modelEditTextVehicleData.text.toString()
            val plate = plateEditTextVehicleData.text.toString()
            val type = typeSpinnerVehicleData.selectedItem.toString()
            val seats = seatsEditTextVehicleData.text.toString().toInt()

            val vehicle = Vehicle(vehicleId!!, groupId, make, model, plate, type, seats)

            ref.child(vehicleId!!).setValue(vehicle)
                .addOnSuccessListener {
                    addVehicleToGroup(groupId, vehicleId!!, applicationContext, intent, ref.child(vehicleId!!))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun verifyUserAdministratorAndDisplay() {
        userId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                // If an admin is creating a new vehicle
                if ("new" == vehicleId && admin) {
                    val intent = Intent(applicationContext, GroupVehiclesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(GROUP_ID, groupId)

                    editVehicleData(intent)
                }
                // If an admin is viewing an existing vehicle (and therefore can modify it)
                else if ("new" != vehicleId && "" != vehicleId && null != vehicleId && admin) {
                    val intent = Intent(applicationContext, VehicleDataActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(GROUP_ID, groupId)
                    intent.putExtra(VEHICLE_ID, vehicleId)

                    makeVehicleEditableAndDeletable(intent)
                }
                // If a non-admin user is viewing an existing vehicle
                else if ("new" != vehicleId && "" != vehicleId && null != vehicleId && !admin) {
                    enableSpinner(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun editVehicleData(intent: Intent) {
        makeVehicleDataEditable()

        editButtonVehicleData.isEnabled = false
        editButtonVehicleData.isVisible = false
        deleteButtonVehicleData.isEnabled = false
        deleteButtonVehicleData.isVisible = false
        cancelButtonVehicleData.isEnabled = true
        cancelButtonVehicleData.isVisible = true
        saveButtonVehicleData.isEnabled = true
        saveButtonVehicleData.isVisible = true

        cancelButtonVehicleData.setOnClickListener {
            startActivity(intent)
        }

        saveButtonVehicleData.setOnClickListener {
            saveVehicleToFirebaseDatabase(intent)
        }
    }

    private fun makeVehicleEditableAndDeletable(intent: Intent) {
        enableSpinner(false)

        cancelButtonVehicleData.isEnabled = false
        cancelButtonVehicleData.isVisible = false
        saveButtonVehicleData.isEnabled = false
        saveButtonVehicleData.isVisible = false
        editButtonVehicleData.isEnabled = true
        editButtonVehicleData.isVisible = true
        deleteButtonVehicleData.isEnabled = true
        deleteButtonVehicleData.isVisible = true

        editButtonVehicleData.setOnClickListener {
            editVehicleData(intent)
        }

        deleteButtonVehicleData.setOnClickListener {
            showWarning()
        }
    }

    private fun makeVehicleDataEditable() {
        enableSpinner(true)

        makeEditTextVehicleData.isEnabled = true
        modelEditTextVehicleData.isEnabled = true
        plateEditTextVehicleData.isEnabled = true
        seatsEditTextVehicleData.isEnabled = true
    }

    private fun showWarning() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Do you really want to delete this vehicle?")

        builder.setPositiveButton("Delete") { dialog, which ->
            deleteVehicle()

            val intent = Intent(applicationContext, GroupVehiclesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(GROUP_ID, groupId)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun deleteVehicle() {
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId").removeValue()
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vehicleId").removeValue()
    }

    private fun enableSpinner(flag: Boolean) {
        if (flag) {
            typeSpinnerVehicleData.isEnabled = true
            typeSpinnerVehicleData.isVisible = true
            typeTextViewVehicleData.isEnabled = false
            typeTextViewVehicleData.isVisible = false
        }
        else {
            typeSpinnerVehicleData.isEnabled = false
            typeSpinnerVehicleData.isVisible = false
            typeTextViewVehicleData.isEnabled = true
            typeTextViewVehicleData.isVisible = true
        }
    }
}