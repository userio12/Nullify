package com.nullify.cleaner.ui.junk

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.ui.common.EmptyState
import com.nullify.cleaner.ui.common.ProgressOverlay
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JunkScreen(
    onBack: () -> Unit,
    viewModel: JunkViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isCleaning, state.cleaningBytes) {
        if (!state.isCleaning && state.cleaningBytes > 0) {
            snackbar.showSnackbar("Cleaned ${formatBytes(state.cleaningBytes)}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Junk Cleaner") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.startScan() }) {
                        Icon(Icons.Default.Search, contentDescription = "Scan", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = state.isScanning, enter = fadeIn(), exit = fadeOut()) {
                ProgressOverlay(
                    currentItem = "Scanning for junk...",
                    progress = 0,
                    maxProgress = 0,
                    bytesFound = state.scannedBytes
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.categories.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                EmptyState(
                    icon = Icons.Default.CleaningServices,
                    title = "No Junk Found",
                    message = "Tap the search icon to scan for cache and temp files."
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.categories.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(state.categories, key = { it.name }) { category ->
                            val index = state.categories.indexOf(category)
                            JunkCategoryCard(category = category, onToggle = { viewModel.toggleCategory(index) })
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            val totalSelected = state.categories.filter { it.isSelected }.sumOf { it.totalBytes }
                            Button(
                                onClick = { viewModel.cleanSelected() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clean Selected (${formatBytes(totalSelected)})")
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
private fun JunkCategoryCard(category: JunkCategory, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = category.isSelected, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("${category.items.size} files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatBytes(category.totalBytes), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEF4444))
        }
    }
}
