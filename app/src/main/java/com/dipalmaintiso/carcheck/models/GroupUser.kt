
package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GroupUser(val uid: String, val administrator: Boolean) : Parcelable {
    constructor() : this("", false)
}