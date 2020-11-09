package com.arpadfodor.stolenvehicledetector.model

import com.google.gson.GsonBuilder
import com.arpadfodor.stolenvehicledetector.data.dataclasses.RawVehicle
import com.arpadfodor.stolenvehicledetector.data.dataclasses.Vehicle
import com.google.gson.Gson
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

object DataTransformer{

    fun transformRawData(rawVehiclesList: MutableList<RawVehicle>) : MutableList<Vehicle> {

        val vehicles = mutableListOf<Vehicle>()

        rawVehiclesList.forEachIndexed { index, rawVehicle ->
            val licenseId = rawVehicle.name.replace("Rendszám: ", "")
            val type = rawVehicle.type.replace("Jármű fajta: ", "")
            val manufacturer =rawVehicle.manufacturer.replace("Gyártmány: ", "")
            val color = rawVehicle.color.replace("Szín: ", "")
            val vehicle = Vehicle(licenseId, type, manufacturer, color)
            vehicles.add(vehicle)
        }

        return vehicles

    }

    fun objectToJsonString(source: Any) : String{
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(source)
        return jsonString
    }

    fun stringToDate(dateString: String) : Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return sdf.parse(dateString)
    }

    fun <T>jsonToType(jsonContent: String, typeToken: Type) : T?{
        try{
            return Gson().fromJson<T>(jsonContent, typeToken)
        }
        catch(e: Exception){
            e.printStackTrace()
        }
        return null
    }

}