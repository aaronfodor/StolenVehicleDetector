package com.arpadfodor.stolenvehicledetector.android.app.model

import java.text.SimpleDateFormat
import java.util.*

object DateHandler{

    private val timeZone = TimeZone.getTimeZone("UTC")
    private val calendar = Calendar.getInstance(timeZone)
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun stringToDate(dateString: String) : Date{
        return formatter.parse(dateString) ?: Date(0)
    }

    fun defaultDate() : Date{
        return Date(0)
    }

    fun dateToString(date: Date) : String{
        return formatter.format(date)
    }

    fun currentDateString() : String{
        return dateToString(calendar.time)
    }

}