package com.x_xsan.signalcore

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainScreenState(
    val contactNameInput: String = "",
    val contacts: List<String> = emptyList(),
    val hasDndPermission: Boolean = false,
    val hasNotificationListenerPermission: Boolean = false,
    val isLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.priorityContacts.collect { contactsFromRepo ->
                _uiState.update { it.copy(contacts = contactsFromRepo) }
            }
        }
    }

    fun updatePermissionsState() {
        val dnd = notificationManager.isNotificationPolicyAccessGranted
        val packageName = getApplication<Application>().packageName
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(getApplication())
        val notification = listeners.contains(packageName)

        Log.d("MainViewModel", "Updating permissions. DND: $dnd, Listener: $notification")

        _uiState.update {
            it.copy(
                hasDndPermission = dnd,
                hasNotificationListenerPermission = notification,
                isLoading = false
            )
        }
    }

    fun onContactNameChange(newName: String) {
        _uiState.update { it.copy(contactNameInput = newName) }
    }

    fun onAddContactClick() {
        val newContact = _uiState.value.contactNameInput
        if (newContact.isBlank()) { return }
        viewModelScope.launch {
            userPreferencesRepository.addContact(newContact)
            _uiState.update { it.copy(contactNameInput = "") }
        }
    }
}