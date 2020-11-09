package com.arpadfodor.stolenvehicledetector.data.dataclasses

data class Log(
        val id: Int,
        val timestampUTC: String,
        val type: LogType,
        val summary: String,
        val message: String
)