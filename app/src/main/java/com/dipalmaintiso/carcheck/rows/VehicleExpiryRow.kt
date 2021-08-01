package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_groups_row.view.*

class VehicleExpiryRow (private val groupName: String, groupId: String): Item<ViewHolder>(){
    var gid = groupId
    override fun getLayout(): Int {
        return R.layout.user_groups_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.groupNameTextView.text = groupName
    }
}