package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Vehicle
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class GroupVehiclesRow(private val groupName: Vehicle): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.group_vehicles_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // viewHolder.itemView.groupNameTextView.text = groupName
    }
}