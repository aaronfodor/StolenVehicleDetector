package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StolenVehicle::class, Timestamp::class], version = 1, exportSchema = false)
abstract class ApplicationDB : RoomDatabase() {

    companion object {

        private const val APPLICATION_DB_NAME = "application_database"
        const val STOLEN_VEHICLES_TABLE_NAME = "stolen_vehicles_table"
        const val TIMESTAMPS_TABLE_NAME = "timestamps_table"

        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ApplicationDB? = null

        fun getDatabase(context: Context): ApplicationDB {

            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApplicationDB::class.java,
                    APPLICATION_DB_NAME
                ).build()
                INSTANCE = instance
                return instance
            }

        }

    }

    abstract fun stolenVehicleTable(): StolenVehicleDAO
    abstract fun timestampTable(): TimestampDAO

}