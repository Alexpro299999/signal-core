package com.x_xsan.signalcore

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactSettingsDao {
    @Query("SELECT * FROM contact_settings ORDER BY name ASC")
    fun getAll(): Flow<List<ContactSettings>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactSettings)

    @Update
    suspend fun update(contact: ContactSettings)

    @Delete
    suspend fun delete(contact: ContactSettings)
}