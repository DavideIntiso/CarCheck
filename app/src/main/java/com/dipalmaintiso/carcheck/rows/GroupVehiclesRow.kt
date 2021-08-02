package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Vehicle
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.group_vehicles_row.view.*

class GroupVehiclesRow(private val vehicle: Vehicle): Item<ViewHolder>(){
    val vid = vehicle.vid
    override fun getLayout(): Int {
        return R.layout.group_vehicles_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val makeAndModel = vehicle.make + " " + vehicle.model

        viewHolder.itemView.makeAndModelTextView.text = makeAndModel
        viewHolder.itemView.plateNumberTextView.text = vehicle.plate

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