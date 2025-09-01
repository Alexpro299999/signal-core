package com.x_xsan.signalcore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.priorityContacts.collect { contactsFromRepo ->
                _uiState.update { it.copy(contacts = contactsFromRepo) }
            }
        }
    }

    fun onContactNameChange(newName: String) {
        _uiState.update { it.copy(contactNameInput = newName) }
    }

    fun onAddContactClick() {
        val newContact = _uiState.value.contactNameInput
        if (newContact.isBlank()) {
            return
        }

        viewModelScope.launch {
            userPreferencesRepository.addContact(newContact)
            _uiState.update { it.copy(contactNameInput = "") }
        }
    }
}

data class MainScreenState(
    val contactNameInput: String = "",
    val contacts: List<String> = emptyList()
)