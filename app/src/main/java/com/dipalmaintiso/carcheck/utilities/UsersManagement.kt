package com.dipalmaintiso.carcheck.utilities

import com.dipalmaintiso.carcheck.models.GroupUser
import com.google.firebase.database.FirebaseDatabase

const val DATABASE_URL = "https://carcheck-af4b2-default-rtdb.europe-west1.firebasedatabase.app/"
const val GROUP_ID = "GROUP_ID"
const val VEHICLE_ID = "VEHICLE_ID"

fun addUserToGroup(groupId: String, userId: String, administrator: Boolean) : String {

    var failure = ""
    val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
    val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")

    val groupUser = GroupUser(userId, administrator)

    ref.setValue(groupUser)
        .addOnSuccessListener {
            userRef.child("/groups/$groupId/gid").setValue(groupId)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                    ref.removeValue()
                    failure = it.message!!
                }
        }
        .addOnFailureListener {
            failure = it.message!!
        }
    return failure
}

fun addVehicleToGroup(groupId: String?, vehicleId: String) : String {

    var failure = ""
    val vehicleRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vehicleId")
    val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles/$vehicleId")

    ref.child("vid").setValue(vehicleId)
        .addOnSuccessListener {
        }
        .addOnFailureListener {
            vehicleRef.removeValue()
            failure = it.message!!
        }
    return failure
}