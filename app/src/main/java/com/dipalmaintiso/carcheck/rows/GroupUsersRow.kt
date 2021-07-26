package com.dipalmaintiso.carcheck.rows

import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.GroupUser
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.group_users_row.view.*


class GroupUsersRow(private val groupUser: GroupUser): Item<ViewHolder>(){
    val uid = groupUser.uid
    override fun getLayout(): Int {
        return R.layout.group_users_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        var userId = groupUser.uid
        var admin = groupUser.administrator
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.child("username").getValue(String::class.java)!!
                val profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String::class.java)!!

                viewHolder.itemView.usernameTextView.text = username

                val targetImageView = viewHolder.itemView.userPictureCircleImageView
                Picasso.get().load(profileImageUrl).into(targetImageView)

                if (admin)
                    viewHolder.itemView.userRoleTextView.text = "Administrator"
                else
                    viewHolder.itemView.userRoleTextView.text = "Participant"
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}