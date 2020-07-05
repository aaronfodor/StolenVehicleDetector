package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StolenVehicleDAO {

    @Query("SELECT * FROM ${ApplicationDB.STOLEN_VEHICLES_TABLE_NAME}")
    fun getAll(): List<StolenVehicle>

    @Query("SELECT * FROM ${ApplicationDB.STOLEN_VEHICLES_TABLE_NAME} WHERE license_id=:id ")
    fun getByLicenseId(id: String): List<StolenVehicle>

    @Query("DELETE FROM ${ApplicationDB.STOLEN_VEHICLES_TABLE_NAME}")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg stolen_vehicle_data: StolenVehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stolen_vehicle_data_list: List<StolenVehicle>)

}