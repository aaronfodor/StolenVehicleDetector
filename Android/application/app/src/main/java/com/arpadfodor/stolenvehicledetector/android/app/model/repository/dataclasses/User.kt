package com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses

data class User(
    var email: String = "",
    var name: String = "",
    val password: String = "",
    val hint: String = "",
    var numReports: Int = 0
)