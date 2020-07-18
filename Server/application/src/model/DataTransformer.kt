package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.GsonBuilder
import model.dataclasses.RawVehicle
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

}