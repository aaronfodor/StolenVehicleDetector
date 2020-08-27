package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiUser(
    var email: String,
    var name: String,
    var password: String,
    var hint: String,
    var numReports: Int
)