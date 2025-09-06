package com.x_xsan.signalcore

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_settings",
    indices = [Index(value = ["name"], unique = true)]
)
data class ContactSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val soundResourceName: String,
    val volume: Float,
    val durationSec: Int,
    val flashlightSpeedMs: Int
)