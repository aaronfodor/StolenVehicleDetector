package com.arpadfodor.ktor.model.dataclasses

data class User(
    var email: String,
    var name: String,
    var password: String,
    var hint: String,
    var active: Boolean,
    var permissions:  MutableList<PermissionType>
)