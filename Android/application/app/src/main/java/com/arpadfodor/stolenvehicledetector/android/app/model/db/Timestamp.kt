package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = ApplicationDB.TIMESTAMPS_TABLE_NAME)
data class Timestamp(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "timestamp_utc")
    val timestampUTC: String = ""
) : Serializable