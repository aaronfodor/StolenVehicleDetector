package com.arpadfodor.stolenvehicledetector.data

import com.arpadfodor.stolenvehicledetector.communication.StatusCodes
import com.arpadfodor.stolenvehicledetector.model.DataUtils
import com.arpadfodor.stolenvehicledetector.data.dataclasses.IDataclass
import com.arpadfodor.stolenvehicledetector.data.dataclasses.MetaData
import java.lang.reflect.Type

open class DataclassDAO<T : IDataclass>(name: String, typeToken: Type, metaDAO: MetaDAO) : CacheDAO<T>(name, typeToken){

    private val metaKey = name
    private val metaDAO = metaDAO

    val historyDAO = DAO<T>("${name}_history", typeToken)

    init {
        initialize()
        if(historyDAO.read().isEmpty()){
            historyDAO.write(records)
        }
        log("Initialized.")
    }

    fun add(items: List<T>, areSameItems: (T, T) -> Boolean): Int{

        val validItems = mutableListOf<T>()

        for(item in items){
            val itemFound = false
            for(record in records){
                if(areSameItems(record, item)){
                    break
                }
            }
            if(!itemFound){
                records.add(item)
                validItems.add(item)
            }
        }

        historyDAO.add(items)
        val result = write(records)
        log("Add result: $result ")
        return result
    }

    fun update(items: List<T>, areSameItems: (T, T) -> Boolean, isUpdateNeeded: (record:T,item:T) -> Boolean,
               updateRecordBasedOnItem: (record:T,item:T) -> Unit): Int{

        val validItems = mutableListOf<T>()

        for(item in items){

            for(record in records){

                if(areSameItems(record, item)){
                    if(isUpdateNeeded(record, item)){
                        updateRecordBasedOnItem(record, item)

                        record.version = record.version + 1
                        record.validFromUTC = item.validFromUTC
                        item.version = record.version
                        item.validFromUTC = item.validFromUTC

                        validItems.add(item)
                    }
                    break
                }

            }

        }

        historyDAO.add(validItems)
        val result = write(records)
        log("Update result: $result")
        return result
    }

    fun addOrUpdate(items: List<T>, areSameItems: (T, T) -> Boolean, isUpdateNeeded: (record:T,item:T) -> Boolean,
                    updateRecordBasedOnItem: (record:T,item:T) -> Unit): Int{

        val validItems = mutableListOf<T>()

        for(item in items){

            var itemFound = false
            for(record in records){

                if(areSameItems(record, item)){
                    itemFound = true
                    if(isUpdateNeeded(record, item)){
                        updateRecordBasedOnItem(record, item)

                        record.version = record.version + 1
                        record.validFromUTC = item.validFromUTC
                        item.version = record.version
                        item.validFromUTC = item.validFromUTC

                        validItems.add(item)
                    }
                    break
                }

            }

            if(!itemFound){
                records.add(item)
                validItems.add(item)
            }

        }

        historyDAO.add(validItems)
        val result = write(records)
        log("Add or update result: $result")
        return result
    }

    open fun delete(key: String, currentTimeUTC: String): Int{
        records.find { it.key == key } ?: return StatusCodes.NOT_FOUND

        records.retainAll { it.key != key }
        val result = super.write(records)

        val historyContent = historyDAO.read()
        historyContent.forEach { historyItem ->
            if(!records.any { it.key == historyItem.key }){
                if(historyItem.validToUTC == ""){
                    historyItem.validToUTC = currentTimeUTC
                }

            }
        }
        historyDAO.write(historyContent)
        log("Delete with key $key. Result: $result")
        return result
    }

    override fun write(itemsToWrite: List<T>): Int {
        val writeResult = super.write(itemsToWrite)
        if(writeResult == StatusCodes.SUCCESS){
            updateMeta()
        }
        log("Write result: $writeResult")
        return writeResult
    }

    private fun updateMeta(){
        val dataSize = records.size
        val modificationTimestampUTC = DataUtils.currentTimeUTC()
        metaDAO.updateMeta(MetaData(metaKey, dataSize, modificationTimestampUTC))
        log("Meta updated: size=$dataSize, timestamp=$modificationTimestampUTC")
    }

}