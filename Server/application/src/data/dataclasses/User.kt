package com.arpadfodor.stolenvehicledetector.data.dataclasses

data class User(
        val email: String,
        var password: String,
        var name: String,
        var hint: String,
        var active: Boolean,
        var numReports: Int,
        var permissions:  MutableList<PermissionType>,
        override var key: String = email,
        override var version: Int = 1,
        override var validFromUTC: String = "",
        override var validToUTC: String = ""
) : IDataclass