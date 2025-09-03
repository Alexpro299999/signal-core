package com.x_xsan.signalcore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CONTACT_LIST = stringSetPreferencesKey("priority_contacts")
        val BATTERY_OPTIMIZATION_REQUESTED = booleanPreferencesKey("battery_optimization_requested")
    }

    val priorityContacts: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CONTACT_LIST]?.toList() ?: emptyList()
        }

    val batteryOptimizationRequested: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BATTERY_OPTIMIZATION_REQUESTED] ?: false
        }

    suspend fun setBatteryOptimizationRequested() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BATTERY_OPTIMIZATION_REQUESTED] = true
        }
    }

    suspend fun addContact(newContact: String) {
        context.dataStore.edit { preferences ->
            val currentContacts = preferences[PreferencesKeys.CONTACT_LIST] ?: setOf()
            preferences[PreferencesKeys.CONTACT_LIST] = currentContacts + newContact
        }
    }
}