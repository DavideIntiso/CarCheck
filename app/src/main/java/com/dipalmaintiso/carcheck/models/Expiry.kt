package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Expiry(val eid: String?, val expiryName: String, val expiryDate: Long): Parcelable{
    constructor() : this("", "", 0L)
}