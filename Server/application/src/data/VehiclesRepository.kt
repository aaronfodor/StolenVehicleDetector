package com.arpadfodor.stolenvehicledetector.data

import com.arpadfodor.stolenvehicledetector.data.dataclasses.Vehicle
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<Vehicle>>() {}.type

class VehiclesRepository(name: String, metaDAO: MetaDAO) : DataclassDAO<Vehicle>(name, typeToken, metaDAO){

    fun addOrUpdate(items: List<Vehicle>): Int{

        val areSameItems: (Vehicle, Vehicle) -> Boolean = { record, item ->
            (record.licenseId == item.licenseId)
        }

        val isUpdateNeeded: (Vehicle, Vehicle) -> Boolean = { record, item ->
            (record.color != item.color || record.manufacturer != item.manufacturer || record.type != item.type)
        }

        val updateRecordBasedOnItem: (Vehicle, Vehicle) -> Unit = { record, item ->
            record.type = item.type
            record.manufacturer = item.manufacturer
            record.color = item.color
        }

        return super.addOrUpdate(items, areSameItems = areSameItems, isUpdateNeeded = isUpdateNeeded,
                updateRecordBasedOnItem = updateRecordBasedOnItem)
    }

}