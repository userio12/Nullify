package com.nullify.cleaner.ui.appmanager

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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
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
import com.nullify.cleaner.domain.model.AppInfo
import com.nullify.cleaner.ui.common.EmptyState
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    onBack: () -> Unit,
    viewModel: AppManagerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("App Manager") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            AnimatedVisibility(visible = state.isLoading, enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(visible = !state.isLoading && state.apps.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                EmptyState(icon = Icons.Default.Smartphone, title = "No Apps Found", message = "")
            }
            AnimatedVisibility(visible = !state.isLoading && state.apps.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${state.apps.size} apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row {
                        SortOrder.entries.forEach { order ->
                            val label = when (order) { SortOrder.NAME_ASC -> "A-Z"; SortOrder.NAME_DESC -> "Z-A"; SortOrder.SIZE_DESC -> "Big"; SortOrder.SIZE_ASC -> "Small" }
                            TextButton(onClick = { viewModel.setSortOrder(order) }) {
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                    color = if (state.sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(state.apps, key = { it.packageName }) { app ->
                        AppCard(
                            app = app,
                            onForceStop = { viewModel.forceStop(app.packageName) },
                            onClearCache = { viewModel.clearCache(app.packageName) },
                            onUninstall = { viewModel.uninstall(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: AppInfo,
    onForceStop: () -> Unit,
    onClearCache: () -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phonelink,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (app.isSystemApp) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.appName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row {
                        if (app.isSystemApp) {
                            Text("System", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("v${app.versionName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatBytes(app.totalSize), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    if (!app.isEnabled) Text("Disabled", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SizeLabel("App", app.appSize)
                SizeLabel("Data", app.dataSize)
                SizeLabel("Cache", app.cacheSize)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onForceStop) {
                    Icon(Icons.Default.Stop, contentDescription = "Force Stop", tint = Color(0xFFEF4444))
                }
                IconButton(onClick = onClearCache) {
                    Icon(Icons.Default.Storage, contentDescription = "Clear Cache", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onUninstall) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Uninstall", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun SizeLabel(label: String, bytes: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatBytes(bytes), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
