package com.arpadfodor.ktor.model

import java.lang.Exception

object ValidatorService{

    fun isUtcTimeStampValid(timeStamp: String) : Boolean{
        return (isValidUtcTimeStamp(timeStamp) && isTimeStampAfterNow(timeStamp))
    }

    private fun isValidUtcTimeStamp(timeStamp: String) : Boolean{

        if(timeStamp.length != 19){
            return false
        }

        try {
            val year = timeStamp.substring(0, 4).toInt()
            if(!isBetween(year, 2010, 9999)){ return false }
            val month = timeStamp.substring(5, 7).toInt()
            if(!isBetween(month, 1, 12)){ return false }
            val day = timeStamp.substring(8, 10).toInt()
            if(!isBetween(day, 1, 31)){ return false }
            val hour = timeStamp.substring(11, 13).toInt()
            if(!isBetween(hour, 0, 23)){ return false }
            val minute = timeStamp.substring(14, 16).toInt()
            if(!isBetween(minute, 0, 59)){ return false }
            val second = timeStamp.substring(17, 19).toInt()
            if(!isBetween(second, 0, 59)){ return false }
        }
        catch (e: Exception){
            return false
        }

        return true

    }

    private fun isTimeStampAfterNow(timeStamp: String) : Boolean{
        val stamp = DataTransformer.stringToDate(timeStamp)
        val now = DataTransformer.stringToDate(DataUtils.currentTimeUTC())
        return stamp.before(now)
    }

    fun isLatitudeValid(latitude: Double) : Boolean{
        return isBetween(latitude, -90.0, 90.0)
    }

    fun isLongitudeValid(longitude: Double) : Boolean{
        return isBetween(longitude, -180.0, 180.0)
    }

    private fun <T : Comparable<T>> isBetween(value: T, min: T, max: T) : Boolean{
        return((min <= value) && (value <= max))
    }

}