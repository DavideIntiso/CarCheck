package com.dipalmaintiso.carcheck.management

import com.dipalmaintiso.carcheck.models.GroupUser
import com.google.firebase.database.*

const val DATABASE_URL = "https://carcheck-af4b2-default-rtdb.europe-west1.firebasedatabase.app/"

fun addUserToGroup(groupId: String, userId: String, administrator: Boolean) : String {

    var success = ""
    val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
    val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")

    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var username = dataSnapshot.child("username").getValue(String::class.java)!!
            var profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String::class.java)!!

            val groupUser = GroupUser(userId, username, profileImageUrl, administrator)

            ref.setValue(groupUser)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                    success = it.message!!
                }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })
    return success
}