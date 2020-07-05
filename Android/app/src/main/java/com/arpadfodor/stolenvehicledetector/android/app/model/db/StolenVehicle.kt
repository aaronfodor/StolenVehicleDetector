package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = ApplicationDB.STOLEN_VEHICLES_TABLE_NAME)
data class StolenVehicle(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "license_id")
    val licenseId: String = "",
    @ColumnInfo(name = "vehicle_type")
    val vehicleType: String = "",
    @ColumnInfo(name = "manufacturer")
    val manufacturer: String = "",
    @ColumnInfo(name = "color")
    val color: String = ""
) : Serializable