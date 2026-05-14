package com.nullify.cleaner.ui.scheduler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.domain.model.Schedule
import com.nullify.cleaner.ui.common.EmptyState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    onBack: () -> Unit,
    viewModel: SchedulerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.schedules) {
        if (state.schedules.isNotEmpty()) snackbar.showSnackbar("${state.schedules.size} schedule(s)")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Scheduler") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = state.isLoading, enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(visible = !state.isLoading && state.schedules.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                EmptyState(
                    icon = Icons.Default.Schedule,
                    title = "No Schedules",
                    message = "Tap + to create an automatic cleanup schedule."
                )
            }
            AnimatedVisibility(visible = !state.isLoading && state.schedules.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                items(state.schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggle = { viewModel.toggleSchedule(schedule) },
                        onDelete = { viewModel.deleteSchedule(schedule) }
                    )
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateScheduleDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, hours, tools -> viewModel.createSchedule(name, hours, tools) }
        )
    }
    }
}
@Composable
private fun ScheduleCard(schedule: Schedule, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(schedule.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("Every ${schedule.intervalHours}h | ${schedule.toolTypes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                schedule.lastRunAt?.let {
                    Text("Last run: ${java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))}",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = schedule.isEnabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("daily") }
    var selectedTools by remember { mutableStateOf(setOf("corpse_finder", "junk_cleaner")) }

    val presets = listOf(
        "daily" to "Daily (24h)" to 24,
        "weekly" to "Weekly (168h)" to 168,
        "monthly" to "Monthly (720h)" to 720,
        "custom" to "Custom hours" to 0
    )

    var customHours by remember { mutableStateOf("") }

    val toolOptions = listOf(
        "corpse_finder" to "Corpse Finder",
        "junk_cleaner" to "Junk Cleaner",
        "duplicate_finder" to "Duplicate Finder"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Schedule") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Interval", style = MaterialTheme.typography.bodyMedium)
                presets.forEach { (key, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPreset == key.first,
                            onClick = { selectedPreset = key.first }
                        )
                        Text(key.second, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (selectedPreset == "custom") {
                    OutlinedTextField(
                        value = customHours,
                        onValueChange = { customHours = it },
                        label = { Text("Hours") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tools to run:", style = MaterialTheme.typography.bodyMedium)
                toolOptions.forEach { (key, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = key in selectedTools,
                            onCheckedChange = {
                                selectedTools = if (key in selectedTools) selectedTools - key
                                else selectedTools + key
                            }
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hours = if (selectedPreset == "custom") customHours.toIntOrNull() ?: 24
                    else presets.first { it.first.first == selectedPreset }.second
                    onCreate(name.ifBlank { "Cleanup" }, hours.coerceAtLeast(1), selectedTools.toList())
                },
                enabled = selectedTools.isNotEmpty() && (selectedPreset != "custom" || customHours.toIntOrNull() != null)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
