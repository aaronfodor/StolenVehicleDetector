package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiMetaData(
    var tableId: String,
    var dataSize: Int,
    var modificationTimeStampUTC: String
)