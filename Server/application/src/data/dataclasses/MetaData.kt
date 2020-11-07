package com.arpadfodor.ktor.data.dataclasses

data class MetaData(
    var tableId: String,
    var dataSize: Int,
    var modificationTimeStampUTC: String
)