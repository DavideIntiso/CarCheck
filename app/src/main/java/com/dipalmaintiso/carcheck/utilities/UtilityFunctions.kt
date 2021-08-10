package com.dipalmaintiso.carcheck.utilities

import com.google.firebase.database.FirebaseDatabase

const val DATABASE_URL = "https://carcheck-af4b2-default-rtdb.europe-west1.firebasedatabase.app/"
const val GROUP_ID = "GROUP_ID"
const val VEHICLE_ID = "VEHICLE_ID"
const val USER_ID = "USER_ID"
const val EXPIRY_ID = "EXPIRY_ID"
const val EXPENSE_ID = "EXPENSE_ID"
const val FAILURE = "FAILURE"


fun removeUserFromGroup(groupId: String?, userId: String?) {
    FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId").removeValue()
    FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId/groups/$groupId").removeValue()
}

