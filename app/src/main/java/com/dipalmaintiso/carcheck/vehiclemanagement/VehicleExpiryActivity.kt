package com.dipalmaintiso.carcheck.vehiclemanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expiry
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.EXPIRY_ID
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_vehicle_expiry.*
import java.time.Instant
import java.util.*

class VehicleExpiryActivity : AppCompatActivity() {

    var groupId: String? = null
    private var vehicleId: String? = null
    var userId: String? = null
    private var expiryId: String? = null

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

            finish()
        }

        var expiryDate = 0L

        expiryDateCalendarViewVehicleExpiry.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val c: Calendar = Calendar.getInstance()
            c.set(year, month, dayOfMonth)
            expiryDate = c.timeInMillis
        }

        saveButtonVehicleExpiry.setOnClickListener {
            val expiryName = expiryNameEditTextVehicleExpiry.text.toString()

            if (expiryName.isBlank())
                Toast.makeText(this, "Please enter a name for the expiry.", Toast.LENGTH_LONG).show()

            else {
                if (expiryDate == 0L)
                    expiryDate = Instant.now().toEpochMilli()

                val expiry = Expiry(expiryId, expiryName, expiryDate)

                ref.child(expiryId!!).setValue(expiry)
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