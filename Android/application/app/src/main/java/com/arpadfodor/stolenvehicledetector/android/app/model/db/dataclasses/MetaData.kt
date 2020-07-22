package com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import java.io.Serializable

@Entity(tableName = ApplicationDB.META_TABLE_NAME)
data class MetaData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var Id: String = "",
    @ColumnInfo(name = "data_size")
    var dataSize: Int = 0,
    @ColumnInfo(name = "timestamp_utc")
    var modificationTimestampUTC: String = DateHandler.dateToString(DateHandler.defaultDate())
) : Serializable