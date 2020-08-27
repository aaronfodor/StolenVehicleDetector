package com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import java.io.Serializable

@Entity(tableName = ApplicationDB.VEHICLE_TABLE_NAME)
data class DbUser(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "email")
    var email: String = "",
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "password")
    val password: String = "",
    @ColumnInfo(name = "hint")
    val hint: String = "",
    @ColumnInfo(name = "num_reports")
    var numReports: Int = 0
) : Serializable