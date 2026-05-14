package com.nullify.cleaner.ui.corpse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.ui.common.EmptyState
import com.nullify.cleaner.ui.common.ProgressOverlay
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorpseScreen(
    onBack: () -> Unit,
    viewModel: CorpseViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.cleanedBytes) {
        if (state.cleanedBytes > 0) {
            snackbar.showSnackbar("Deleted files, freed ${formatBytes(state.cleanedBytes)}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Corpse Finder") },
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
                    currentItem = "Scanning for leftover files...",
                    progress = 0,
                    maxProgress = 0,
                    bytesFound = state.scannedBytes
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.items.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                EmptyState(
                    icon = Icons.Default.FolderOff,
                    title = "No Leftover Files Found",
                    message = "Tap the search icon to scan for orphaned app data."
                )
            }
            AnimatedVisibility(visible = !state.isScanning && state.items.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Found ${state.items.size} items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(formatBytes(state.items.sumOf { it.size }), style = MaterialTheme.typography.titleMedium, color = Color(0xFFEF4444))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(state.items, key = { it.path }) { item ->
                            CorpseCard(
                                item = item,
                                isSelected = item.path in state.selectedItems,
                                onToggle = { viewModel.toggleItem(item.path) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.selectAll() },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Select All") }
                                Button(
                                    onClick = { viewModel.deselectAll() },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Deselect All") }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.selectedItems.isNotEmpty()) {
                                Button(
                                    onClick = { viewModel.cleanSelected() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Selected (${state.selectedItems.size})")
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.isCleaning, enter = fadeIn(), exit = fadeOut()) {
                ProgressOverlay(
                    currentItem = "Cleaning...",
                    progress = state.cleaningProgress,
                    maxProgress = state.cleaningTotal,
                    bytesFound = state.cleaningBytes
                )
            }
        }
    }

private val cardShape = RoundedCornerShape(16.dp)

@Composable
private fun CorpseCard(item: CleanupItem, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(item.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFEF4444).copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(formatBytes(item.size), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
            }
        }
    }
}
