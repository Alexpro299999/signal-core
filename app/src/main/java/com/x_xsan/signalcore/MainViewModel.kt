import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.x_xsan.signalcore.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        checkDndPermission()

        viewModelScope.launch {
            userPreferencesRepository.priorityContacts.collect { contactsFromRepo ->
                _uiState.update { it.copy(contacts = contactsFromRepo) }
            }
        }
    }

    fun checkDndPermission() {
        val hasPermission = notificationManager.isNotificationPolicyAccessGranted
        Log.d("MainViewModel", "Checking DND permission. Has permission? -> $hasPermission")
        _uiState.update { it.copy(shouldShowDndPermissionRequest = !hasPermission) }
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

data class MainScreenState(
    val contactNameInput: String = "",
    val contacts: List<String> = emptyList(),
    val shouldShowDndPermissionRequest: Boolean = false
)