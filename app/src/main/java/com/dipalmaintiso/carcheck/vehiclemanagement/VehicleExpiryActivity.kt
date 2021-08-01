package com.dipalmaintiso.carcheck.vehiclemanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expiry
import com.dipalmaintiso.carcheck.utilities.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_vehicle_data.*
import kotlinx.android.synthetic.main.activity_vehicle_expiry.*
import kotlinx.android.synthetic.main.vehicle_expiry_row.*

class VehicleExpiryActivity : AppCompatActivity() {

    var groupId: String? = null
    var vehicleId: String? = null
    var userId: String? = null
    var expiryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_expiry)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)
        expiryId = intent.getStringExtra(EXPIRY_ID)

        if ("new" != expiryId && null != expiryId)
            fetchExpiryDataFromDatabase()

        makeExpiryDataEditable()
    }

    private fun fetchExpiryDataFromDatabase() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId/expiries/$expiryId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val expiry = dataSnapshot.getValue(Expiry::class.java)!!

                expiryNameEditTextVehicleExpiry.setText(expiry.expiryName)
                expiryDateCalendarViewVehicleExpiry.date = expiry.expiryDate
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun makeExpiryDataEditable() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId/expiries")

        if ("new" == expiryId) {
            expiryId = ref.push().key
        }

        if (null == vehicleId || "" == vehicleId) {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
        }

        deleteButtonVehicleExpiry.setOnClickListener {
            ref.child(expiryId!!).removeValue()

            val intent = Intent(this, VehicleExpiriesActivity::class.java)

            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(VEHICLE_ID, vehicleId)

            startActivity(intent)
        }

        saveButtonVehicleExpiry.setOnClickListener {
            val expiryName = expiryNameEditTextVehicleExpiry.text.toString()
            val expiryDate = expiryDateCalendarViewVehicleExpiry.date

            val expiry = Expiry(expiryId, expiryName, expiryDate)

            ref.child(expiryId!!).setValue(expiry)
                .addOnSuccessListener {
                    val intent = Intent(this, VehicleExpiriesActivity::class.java)

                    intent.putExtra(GROUP_ID, groupId)
                    intent.putExtra(VEHICLE_ID, vehicleId)

                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}