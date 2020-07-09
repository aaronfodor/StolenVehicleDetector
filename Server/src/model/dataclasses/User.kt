package com.arpadfodor.ktor.model.dataclasses

data class User(
    var name: String,
    var password: String,
    var types:  MutableList<UserType>
)