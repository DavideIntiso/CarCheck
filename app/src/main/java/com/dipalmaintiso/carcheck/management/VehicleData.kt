package com.dipalmaintiso.carcheck.management

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID

var groupId: String? = null
var vehicleId: String? = null

class VehicleData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_data)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)
    }
}