package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Group(val gid: String, val groupName: String, val creatorId: String): Parcelable{
    constructor() : this("", "", "")
}