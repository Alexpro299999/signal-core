package com.x_xsan.signalcore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CONTACT_LIST = stringSetPreferencesKey("priority_contacts")
    }

    val priorityContacts: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CONTACT_LIST]?.toList() ?: emptyList()
        }

    suspend fun addContact(newContact: String) {
        context.dataStore.edit { preferences ->
            val currentContacts = preferences[PreferencesKeys.CONTACT_LIST] ?: setOf()
            preferences[PreferencesKeys.CONTACT_LIST] = currentContacts + newContact
        }
    }
}