package com.dipalmaintiso.carcheck.management

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.dipalmaintiso.carcheck.models.GroupUser
import com.dipalmaintiso.carcheck.models.User
import com.dipalmaintiso.carcheck.views.UserGroupsActivity
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_registration.*

const val DATABASE_URL = "https://carcheck-af4b2-default-rtdb.europe-west1.firebasedatabase.app/"

fun addUserToGroup(groupId: String, userId: String, administrator: Boolean) {

    val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
    var username = ""
    var profileImageUrl = ""

    userRef.addListenerForSingleValueEvent(object : ValueEventListener {

        override fun onCancelled(error: DatabaseError) {
            println(error!!.message)
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java)
            username = user!!.username
            profileImageUrl = user!!.profileImageUrl
        }
    })

    val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")
    /*
    var username: String = ""
    var profileImageUrl: String = ""
    usernameRef.get().addOnSuccessListener {
         username = it.value.toString()
    }.addOnFailureListener{
        Log.e("firebase", "Error getting data", it)
    }
    profileImageUrlRef.get().addOnSuccessListener {
        profileImageUrl = it.value.toString()
    }.addOnFailureListener{
        Log.e("firebase", "Error getting data", it)
    }

     */
    val groupUser = GroupUser(userId, username, profileImageUrl, administrator)

    ref.setValue(groupUser)
        .addOnSuccessListener {
            Log.d("ciao", "si")
        }
        .addOnFailureListener {
            Log.d("ciao", "no")
        }
}