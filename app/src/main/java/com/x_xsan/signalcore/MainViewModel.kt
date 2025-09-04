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
    val hasBatteryOptimizationPermission: Boolean = true,
    val isLoading: Boolean = true,
    val isEditDialogOpen: Boolean = false,
    val contactToEdit: String = "",
    val editContactInput: String = ""
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
        viewModelScope.launch {
            userPreferencesRepository.batteryOptimizationRequested.collect { isRequested ->
                _uiState.update { it.copy(hasBatteryOptimizationPermission = isRequested) }
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

    fun onBatteryOptimizationDismissed() {
        viewModelScope.launch {
            userPreferencesRepository.setBatteryOptimizationRequested()
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

    fun onRemoveContactClick(contactToRemove: String) {
        viewModelScope.launch {
            userPreferencesRepository.removeContact(contactToRemove)
        }
    }

    fun onEditContactClick(contact: String) {
        _uiState.update {
            it.copy(
                isEditDialogOpen = true,
                contactToEdit = contact,
                editContactInput = contact
            )
        }
    }

    fun onEditContactNameChange(newName: String) {
        _uiState.update { it.copy(editContactInput = newName) }
    }

    fun onEditDialogConfirm() {
        val oldName = _uiState.value.contactToEdit
        val newName = _uiState.value.editContactInput

        if (newName.isBlank() || oldName == newName) {
            onEditDialogDismiss()
            return
        }

        viewModelScope.launch {
            userPreferencesRepository.updateContact(oldName, newName)
            onEditDialogDismiss()
        }
    }

    fun onEditDialogDismiss() {
        _uiState.update {
            it.copy(
                isEditDialogOpen = false,
                contactToEdit = "",
                editContactInput = ""
            )
        }
    }

}