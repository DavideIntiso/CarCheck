package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Vehicle(val vid: String, val gid: String?, val make: String, val model: String, val plate: String, val type: String, val seats: Int): Parcelable{
    constructor() : this("", "", "", "", "", "", 0)
}