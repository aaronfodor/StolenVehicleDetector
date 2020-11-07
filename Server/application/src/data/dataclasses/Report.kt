package com.arpadfodor.ktor.data.dataclasses

data class Report(
        val vehicleLicenseId: String,
        var reporterEmail: String,
        var latitude: Double,
        var longitude: Double,
        var message: String,
        override var key: String = vehicleLicenseId,
        override var version: Int = 1,
        override var validFromUTC: String = "",
        override var validToUTC: String = ""
) : IDataclass