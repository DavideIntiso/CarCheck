package com.dipalmaintiso.carcheck.vehiclemanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.utilities.EXPIRY_ID
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID

class VehicleExpiryActivity : AppCompatActivity() {

    var groupId: String? = null
    var vehicleId: String? = null
    var userId: String? = null
    var expiryId: String? = null
    var admin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_expiry)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)
        expiryId = intent.getStringExtra(EXPIRY_ID)

        if ("new" != expiryId && null != expiryId)
            fetchExpiryDataFromDatabase()
    }

    private fun fetchExpiryDataFromDatabase() {
    }
}