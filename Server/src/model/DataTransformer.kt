package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.MetaData
import com.arpadfodor.ktor.model.dataclasses.StolenVehicle
import com.arpadfodor.ktor.model.dataclasses.StolenVehicles
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

object DataTransformer{

    fun transformRawData(stolenVehiclesList: MutableList<StolenVehicle>) : StolenVehicles{

        for(element in stolenVehiclesList){
            element.name = element.name.replace("Rendszám: ", "")
            element.type = element.type.replace("Jármű fajta: ", "")
            element.manufacturer = element.manufacturer.replace("Gyártmány: ", "")
            element.color = element.color.replace("Szín: ", "")
        }

        val utcTime = DataUtils.currentTimeUTC()
        val metadata = MetaData(stolenVehiclesList.size, utcTime)

        return StolenVehicles(stolenVehiclesList, metadata)

    }

    fun transformObjectToString(source: Any) : String{
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(source)
        return jsonString
    }

    fun stringToDate(dateString: String) : Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return sdf.parse(dateString)
    }

}