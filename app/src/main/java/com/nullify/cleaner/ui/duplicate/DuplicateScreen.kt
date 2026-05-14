package com.nullify.cleaner.ui.duplicate

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.nullify.cleaner.domain.model.DuplicateGroup
import com.nullify.cleaner.ui.common.EmptyState
import com.nullify.cleaner.ui.common.ProgressOverlay
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateScreen(
    onBack: () -> Unit,
    viewModel: DuplicateViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isScanning, state.groups.size, state.scannedBytes) {
        if (!state.isScanning && state.groups.isNotEmpty()) {
            snackbar.showSnackbar("Found ${state.groups.size} duplicate groups (${formatBytes(state.scannedBytes)})")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Duplicate Finder") },
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
                    currentItem = "Scanning for duplicates...",
                    progress = 0,
                    maxProgress = 0,
                    bytesFound = state.scannedBytes
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.groups.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                EmptyState(
                    icon = Icons.Default.ContentCopy,
                    title = "No Duplicates Found",
                    message = "Tap the search icon to scan common directories for duplicate files."
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.groups.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text("Found ${state.groups.size} duplicate groups",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        val totalWaste = state.groups.sumOf { group ->
                            group.files.drop(1).sumOf { it.size }
                        }
                        Text("Wasted space: ${formatBytes(totalWaste)}",
                            style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(state.groups, key = { it.fileHash }) { group ->
                        DuplicateGroupCard(group = group)
                    }
            }
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(group: DuplicateGroup) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Group: ${group.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(formatBytes(group.totalBytes), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEF4444))
            }
            Text("${group.files.size} files | Hash: ${group.fileHash.take(12)}...",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            group.files.forEach { file ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("• ${file.name}", style = MaterialTheme.typography.bodySmall, maxLines = 1,
                        modifier = Modifier.weight(1f))
                    Text(formatBytes(file.size), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
