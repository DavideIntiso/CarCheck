package com.dipalmaintiso.carcheck.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Expense(val eid: String?, val expenseName: String, val expenseValue: Double, val expenseNotes: String, val expenseDate: Long): Parcelable{
    constructor() : this("", "",0.0,"", 0L)
}