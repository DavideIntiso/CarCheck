package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.Instant
import java.util.*

@Parcelize
class Expiry(val eid: String, val expiryName: String, val expiryDate: Date): Parcelable{
    constructor() : this("", "", Date.from(Instant.now()))
}