package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiUser(
    val email: String,
    var password: String,
    var name: String,
    val hint: String,
    var active: Boolean,
    var numReports: Int,
    var permissions:  MutableList<Int>,
    var key: String = email,
    var version: Int = 1,
    var validFromUTC: String = "",
    var validToUTC: String = ""
)