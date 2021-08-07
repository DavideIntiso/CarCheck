package com.dipalmaintiso.carcheck.rows

import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.group_users_row.view.*
import kotlinx.android.synthetic.main.group_vehicles_row.view.*

class GroupVehiclesRow(private val vehicle: Vehicle): Item<ViewHolder>(){
    val vid = vehicle.vid
    val gid = vehicle.gid
    override fun getLayout(): Int {
        return R.layout.group_vehicles_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val makeAndModel = vehicle.make + " " + vehicle.model

        viewHolder.itemView.makeAndModelTextView.text = makeAndModel
        viewHolder.itemView.plateNumberTextView.text = vehicle.plate

        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$gid/vehicles/$vid")
        val vehicleRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vid")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.child("status").getValue(String::class.java)!!
                viewHolder.itemView.vehicleStatusTextView.text = "$status "

                vehicleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val vehicleImageUrl = dataSnapshot.child("vehicleImageUrl").getValue(String::class.java)!!

                        if (vehicleImageUrl != "") {
                            val targetImageView = viewHolder.itemView.vehiclePictureCircleImageView
                            Picasso.get().load(vehicleImageUrl).into(targetImageView)
                        }
                        else {
                            when (vehicle.type) {
                                "Car" ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.car)
                                "Bus" ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.bus)
                                "Caravan" ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.caravan)
                                "Truck" ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.truck)
                                "Motorcycle" ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.motorcycle)
                                else ->
                                    viewHolder.itemView.vehiclePictureCircleImageView.setImageResource(R.drawable.car)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}