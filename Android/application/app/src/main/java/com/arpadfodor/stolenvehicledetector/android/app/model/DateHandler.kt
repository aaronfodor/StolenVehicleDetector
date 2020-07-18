package com.arpadfodor.stolenvehicledetector.android.app.model

import java.text.SimpleDateFormat
import java.util.*

object DateHandler{

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun stringToDate(dateString: String) : Date{
        return formatter.parse(dateString) ?: Date(0)
    }

    fun dateToString(date: Date) : String{
        return formatter.format(date)
    }

    fun defaultDate() : Date{
        return Date(0)
    }

}