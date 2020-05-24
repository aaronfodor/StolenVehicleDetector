package com.arpadfodor.android.stolencardetector.model.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StolenVehicle::class], version = 1, exportSchema = false)
abstract class StolenVehicleDB : RoomDatabase() {
    abstract fun dbInteraction(): StolenVehicleDAO
}