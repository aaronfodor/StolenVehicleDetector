package com.arpadfodor.ktor.model.db

import com.arpadfodor.ktor.model.dataclasses.Vehicle
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<Vehicle>>() {}.type

class VehiclesDAO(name: String,  metaDAO: MetaDAO?) : DAO<Vehicle>(name, typeToken, metaDAO)