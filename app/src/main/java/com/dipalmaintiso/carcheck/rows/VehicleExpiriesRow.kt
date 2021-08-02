package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expiry
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.vehicle_expiry_row.view.*
import java.text.DateFormat
import java.time.Instant

class VehicleExpiriesRow (private val expiry: Expiry): Item<ViewHolder>(){
    var eid = expiry.eid
    override fun getLayout(): Int {
        return R.layout.vehicle_expiry_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.expiryNameTextView.text = expiry.expiryName
        val expiryDate = DateFormat.getDateInstance().format(expiry.expiryDate)
        val expiryMessage: String

        if (expiry.expiryDate > Instant.now().toEpochMilli()) {
            expiryMessage = "Expires on $expiryDate"
        }
        else {
            expiryMessage = "Expired on $expiryDate"
            val redColor = viewHolder.itemView.expiryDateTextView.resources.getColor(R.color.red)
            viewHolder.itemView.expiryDateTextView.setTextColor(redColor)
        }
        viewHolder.itemView.expiryDateTextView.text = expiryMessage
    }
}