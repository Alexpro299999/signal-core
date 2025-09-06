package com.x_xsan.signalcore

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x_xsan.signalcore.ui.theme.SignalCoreTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignalCoreTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionsState()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.hasNotificationListenerPermission) {
                    PermissionRequestUI(
                        text = "Приложению нужен доступ к уведомлениям для работы.",
                        buttonText = "Выдать доступ к уведомлениям",
                        intentAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                } else if (!uiState.hasDndPermission) {
                    PermissionRequestUI(
                        text = "Для работы звука в тихом режиме нужно выдать разрешение.",
                        buttonText = "Выдать разрешение на звук",
                        intentAction = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    )
                } else if (!uiState.hasBatteryOptimizationPermission) {
                    HuaweiBatteryPermissionRequestUI(onDismiss = { viewModel.onBatteryOptimizationDismissed() })
                } else {
                    MainAppUI(viewModel = viewModel)
                }
                if (uiState.isEditDialogOpen) {
                    EditContactDialog(
                        value = uiState.editContactInput,
                        onValueChange = { viewModel.onEditContactNameChange(it) },
                        onConfirm = { viewModel.onEditDialogConfirm() },
                        onDismiss = { viewModel.onEditDialogDismiss() }
                    )
                }
            }
        }
    }
}

@Composable
fun EditContactDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить имя контакта") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Новое имя") }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun MainAppUI(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    OutlinedTextField(
        value = uiState.contactNameInput,
        onValueChange = { viewModel.onContactNameChange(it) },
        label = { Text("Имя контакта в Telegram") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = { viewModel.onAddContactClick() }) {
        Text("Добавить")
    }
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(uiState.contacts) { contactName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = contactName,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Row {
                    IconButton(onClick = { viewModel.onEditContactClick(contactName) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Изменить контакт"
                        )
                    }
                    IconButton(onClick = { viewModel.onRemoveContactClick(contactName) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить контакт"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HuaweiBatteryPermissionRequestUI(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val text = "ВАЖНО: Для стабильной работы на вашем телефоне, пожалуйста, отключите автоматическое управление для 'Signal Core' в 'Диспетчере запуска'."
    val buttonText = "Открыть Диспетчер запуска"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            try {
                Log.d("Permission", "Trying to open Huawei App Launch manager")
                val intent = Intent()
                intent.component = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$PowerUsageSummaryActivity"
                )
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("Permission", "Failed to open Huawei manager, opening generic settings", e)
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }) {
            Text(buttonText)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onDismiss) {
            Text("Продолжить")
        }
    }
}

@Composable
fun PermissionRequestUI(text: String, buttonText: String, intentAction: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            context.startActivity(Intent(intentAction))
        }) {
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPermissionRequest() {
    SignalCoreTheme {
        PermissionRequestUI(
            text = "Приложению нужен доступ к уведомлениям.",
            buttonText = "Выдать доступ",
            intentAction = ""
        )
    }
}