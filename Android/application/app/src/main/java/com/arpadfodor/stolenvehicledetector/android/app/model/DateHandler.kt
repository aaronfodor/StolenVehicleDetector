package com.arpadfodor.stolenvehicledetector.android.app.model

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object DateHandler{

    private val timeZone = TimeZone.getTimeZone("UTC")
    private val calendar = Calendar.getInstance(timeZone)
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    private val defaultDate = Date(0)
    private val defaultDateString = "1970-01-01 00:00:00"

    fun stringToDate(dateString: String) : Date{

        var date = defaultDate

        try{
            date = formatter.parse(dateString) ?: defaultDate
        }
        catch (e: Exception){}
        finally{
            return date
        }

    }

    fun defaultDate() : Date{
        return defaultDate
    }

    fun defaultDateString() : String{
        return defaultDateString
    }

    fun dateToString(date: Date) : String{
        return formatter.format(date) ?: ""
    }

    fun currentDateString() : String{
        return dateToString(calendar.time)
    }

}