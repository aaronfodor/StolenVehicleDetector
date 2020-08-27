package com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import java.io.Serializable

@Entity(tableName = ApplicationDB.VEHICLE_TABLE_NAME)
data class DbVehicle(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "license_id")
    val licenseId: String = "",
    @ColumnInfo(name = "type")
    val type: String = "",
    @ColumnInfo(name = "manufacturer")
    val manufacturer: String = "",
    @ColumnInfo(name = "color")
    val color: String = ""
) : Serializable