package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.function.DoubleToLongFunction

@Parcelize
class Vehicle(val vid: String, val gid: String, val make: String, val model: String, val plate: String, val type: String, val seats: Int, val capacity: Double): Parcelable{
    constructor() : this("", "", "", "", "", "", 0, 0.0)
}