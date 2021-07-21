package com.dipalmaintiso.carcheck.utilities

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.dipalmaintiso.carcheck.models.GroupUser
import com.dipalmaintiso.carcheck.models.Vehicle
import com.google.firebase.database.*


const val DATABASE_URL = "https://carcheck-af4b2-default-rtdb.europe-west1.firebasedatabase.app/"
const val GROUP_ID = "GROUP_ID"
const val VEHICLE_ID = "VEHICLE_ID"
const val FAILURE = "FAILURE"

fun addUserToGroup(groupId: String?, userId: String, administrator: Boolean, context: Context, intent: Intent, ref: DatabaseReference?) {

    val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
    val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")

    groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                prepareForIntent("User already in group", context, intent)
            }
            else {
                val groupUser = GroupUser(userId, administrator)

                groupRef.setValue(groupUser)
                    .addOnSuccessListener {
                        userRef.child("/groups/$groupId/gid").setValue(groupId)
                            .addOnSuccessListener {
                                prepareForIntent("", context, intent)
                            }
                            .addOnFailureListener {
                                groupRef.removeValue()
                                ref?.removeValue()
                                prepareForIntent(it.message.toString(), context, intent)
                            }
                    }
                    .addOnFailureListener {
                        ref?.removeValue()
                        prepareForIntent(it.message.toString(), context, intent)
                    }
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    })
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

fun vehicleStatus(vehicle: Vehicle): String? {
    return "Free"
}

private fun prepareForIntent(message: String, context: Context, intent: Intent) {
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra(FAILURE, message)

    startActivity(context, intent, null)

}