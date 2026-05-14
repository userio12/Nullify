package com.nullify.cleaner.ui.exclusion

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.nullify.cleaner.data.local.entity.ExclusionRuleEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExclusionRulesScreen(
    onBack: () -> Unit,
    viewModel: ExclusionRuleViewModel = koinViewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val toolFilters = listOf("all", "corpse_finder", "junk_cleaner", "duplicate_finder")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Exclusion Rules") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                toolFilters.forEach { filter ->
                    FilterChip(
                        selected = state.selectedToolFilter == filter,
                        onClick = { viewModel.setToolFilter(filter) },
                        label = { Text(filter.replace("_", " ").replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val filteredRules = if (state.selectedToolFilter == "all") rules
            else rules.filter { it.toolType == state.selectedToolFilter || it.toolType == "all" }

            AnimatedVisibility(visible = filteredRules.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No exclusion rules yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AnimatedVisibility(visible = filteredRules.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredRules, key = { it.id }) { rule ->
                        ExclusionRuleCard(
                            rule = rule,
                            onToggle = { viewModel.toggleRule(rule) },
                            onDelete = { viewModel.deleteRule(rule) }
                        )
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddRuleDialog(
            pattern = state.newPattern,
            ruleType = state.newRuleType,
            toolType = state.newToolType,
            error = state.error,
            onPatternChange = { viewModel.updateNewPattern(it) },
            onRuleTypeChange = { viewModel.updateNewRuleType(it) },
            onToolTypeChange = { viewModel.updateNewToolType(it) },
            onConfirm = { viewModel.addRule() },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }
}

@Composable
private fun ExclusionRuleCard(rule: ExclusionRuleEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.pattern, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Row {
                    Text(rule.ruleType.replace("_", " "), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(rule.toolType, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Switch(checked = rule.isEnabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRuleDialog(
    pattern: String,
    ruleType: String,
    toolType: String,
    error: String?,
    onPatternChange: (String) -> Unit,
    onRuleTypeChange: (String) -> Unit,
    onToolTypeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val ruleTypes = listOf("path_glob", "path_regex", "file_extension", "file_name")
    val toolTypes = listOf("all", "corpse_finder", "junk_cleaner", "duplicate_finder")
    var ruleTypeExpanded by remember { mutableStateOf(false) }
    var toolTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exclusion Rule") },
        text = {
            Column {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = onPatternChange,
                    label = { Text("Pattern") },
                    placeholder = { Text("e.g. *.tmp or /cache/*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = ruleTypeExpanded, onExpandedChange = { ruleTypeExpanded = it }) {
                    OutlinedTextField(
                        value = ruleType.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rule Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ruleTypeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = ruleTypeExpanded, onDismissRequest = { ruleTypeExpanded = false }) {
                        ruleTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.replace("_", " ")) },
                                onClick = { onRuleTypeChange(type); ruleTypeExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = toolTypeExpanded, onExpandedChange = { toolTypeExpanded = it }) {
                    OutlinedTextField(
                        value = toolType.replace("_", " ").replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Applies To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toolTypeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = toolTypeExpanded, onDismissRequest = { toolTypeExpanded = false }) {
                        toolTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                                onClick = { onToolTypeChange(type); toolTypeExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
