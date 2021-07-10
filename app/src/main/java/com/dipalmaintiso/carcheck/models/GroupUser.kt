
package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GroupUser(val uid: String, val username: String, val profileImageUrl: String, val administrator: Boolean) : Parcelable {
    constructor() : this("", "", "", false)
}
/*
package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GroupUser(val administrator: Boolean,
                     val gUid: String,
                     val gUsername: String,
                     val gProfileImageUrl: String
): User(gUid, gUsername, gProfileImageUrl) {
    constructor() : this(false, "", "", "")
} */