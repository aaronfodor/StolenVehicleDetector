package com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import java.io.Serializable

@Entity(tableName = ApplicationDB.REPORT_TABLE_NAME)
data class DbReport(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val Id: Int = 0,
    @ColumnInfo(name = "vehicle")
    val Vehicle: String = "",
    @ColumnInfo(name = "reporter")
    val Reporter: String = "",
    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,
    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,
    @ColumnInfo(name = "message")
    val message: String = "",
    @ColumnInfo(name = "timestamp_utc")
    val timestampUTC: String = DateHandler.dateToString(DateHandler.defaultDate())
) : Serializable