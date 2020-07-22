package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData

@Dao
interface MetaDAO {

    @Query("SELECT * FROM ${ApplicationDB.META_TABLE_NAME}")
    fun getAll(): List<MetaData>?

    @Query("SELECT * FROM ${ApplicationDB.META_TABLE_NAME} WHERE id=:key ")
    fun getByKey(key: String): MetaData?

    @Query("DELETE FROM ${ApplicationDB.META_TABLE_NAME} WHERE id=:key ")
    fun deleteByKey(key: String)

    @Query("DELETE FROM ${ApplicationDB.META_TABLE_NAME}")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg meta: MetaData)

}