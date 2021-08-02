package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Expiry
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.vehicle_expiry_row.view.*
import java.text.DateFormat
import java.time.Instant
import java.util.*
import kotlin.coroutines.coroutineContext

class VehicleExpiriesRow (private val expiry: Expiry, groupId: String?, vehicleId: String?): Item<ViewHolder>(){
    var eid = expiry.eid
    var gid = groupId
    var vid = vehicleId
    override fun getLayout(): Int {
        return R.layout.vehicle_expiry_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.expiryNameTextView.text = expiry.expiryName
        var expiryDate = DateFormat.getDateInstance().format(expiry.expiryDate)
        var expiryMessage = ""

        if (expiry.expiryDate > Instant.now().toEpochMilli()) {
            expiryMessage = "Expires on $expiryDate"
        }
        else {
            expiryMessage = "Expired on $expiryDate"
            var redColor = viewHolder.itemView.expiryDateTextView.resources.getColor(R.color.red)
            viewHolder.itemView.expiryDateTextView.setTextColor(redColor)
        }
        viewHolder.itemView.expiryDateTextView.text = expiryMessage
    }
}