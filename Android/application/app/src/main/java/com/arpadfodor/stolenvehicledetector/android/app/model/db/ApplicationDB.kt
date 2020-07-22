package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

@Database(entities = [Vehicle::class, MetaData::class], version = 1, exportSchema = false)
abstract class ApplicationDB : RoomDatabase() {

    companion object {

        private const val APPLICATION_DB_NAME = "application_database"
        const val VEHICLE_TABLE_NAME = "vehicle_table"
        const val META_TABLE_NAME = "meta_table"

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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }

        }

    }

    abstract fun vehicleTable(): VehicleDAO
    abstract fun metaTable(): MetaDAO

}