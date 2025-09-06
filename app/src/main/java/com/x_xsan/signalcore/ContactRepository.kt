package com.x_xsan.signalcore

import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactSettingsDao: ContactSettingsDao) {
    val allContacts: Flow<List<ContactSettings>> = contactSettingsDao.getAll()

    suspend fun insert(contact: ContactSettings) {
        contactSettingsDao.insert(contact)
    }

    suspend fun update(contact: ContactSettings) {
        contactSettingsDao.update(contact)
    }

    suspend fun delete(contact: ContactSettings) {
        contactSettingsDao.delete(contact)
    }
}