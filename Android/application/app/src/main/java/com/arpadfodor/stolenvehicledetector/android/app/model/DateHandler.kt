package com.arpadfodor.stolenvehicledetector.android.app.model

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object DateHandler{

    private const val formatterPattern = "yyyy-MM-dd HH:mm:ss"
    private val formatter = SimpleDateFormat(formatterPattern, Locale.ENGLISH)
    private val defaultDate = Date(0)
    private const val defaultDateString = "1970-01-01 00:00:00"

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

    fun currentTimeUTC() : String {
        return DateTimeFormatter
            .ofPattern(formatterPattern)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

}