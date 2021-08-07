package com.dipalmaintiso.carcheck.vehiclemanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_vehicle_location.*

class VehicleLocationActivity : AppCompatActivity() {

    var groupId: String? = null
    var vehicleId: String? = null

    private var position = LatLng(44.403121, 8.958580)

    var markerOptions: MarkerOptions = MarkerOptions().position(position)

    lateinit var marker : Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_location)

        groupId = intent.getStringExtra(GROUP_ID)
        vehicleId = intent.getStringExtra(VEHICLE_ID)

        with(mapView) {
            onCreate(null)
            getMapAsync{
                MapsInitializer.initialize(applicationContext)
                setMapLocation(it)
            }
        }

        updatePositionButton.setOnClickListener {
            saveVehicleLocationToFirebaseDatabase()
        }
    }

    private fun saveVehicleLocationToFirebaseDatabase() {
        val selectedPosition = marker.position

        val longitude = selectedPosition.longitude
        val latitude = selectedPosition.latitude

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId")

        ref.child("longitude").setValue(longitude)
            .addOnSuccessListener {
                ref.child("latitude").setValue(latitude)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setMapLocation(map : GoogleMap) {
        with(map) {
            mapType = GoogleMap.MAP_TYPE_NORMAL

            val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("latitude").exists() && dataSnapshot.child("longitude").exists()) {
                        val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)
                        position = LatLng(latitude!!, longitude!!)
                    }
                    markerOptions.position(position)
                    marker = addMarker(markerOptions)

                    moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
                }
            })

            setOnMapClickListener {
                if(::marker.isInitialized){
                    marker.remove()
                }
                markerOptions.position(it)
                marker = addMarker(markerOptions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}