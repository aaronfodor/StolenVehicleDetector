package com.arpadfodor.android.stolenvehicledetector.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StolenVehicleDAO {

    @Query("SELECT * FROM StolenVehicle")
    fun getAll(): List<StolenVehicle>

    @Query("SELECT * FROM StolenVehicle WHERE license_id=:id ")
    fun getByLicenseId(id: String): List<StolenVehicle>

    @Query("DELETE FROM StolenVehicle")
    fun deleteAll()

    @Insert
    fun insert(vararg stolen_vehicle_data: StolenVehicle)

}