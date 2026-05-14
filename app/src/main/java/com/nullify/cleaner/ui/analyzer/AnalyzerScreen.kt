package com.nullify.cleaner.ui.analyzer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.domain.cleaner.StorageAnalyzer
import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    onBack: () -> Unit,
    viewModel: AnalyzerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAnalysis() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Analyzer") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = state.isLoading, enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(visible = !state.isLoading, enter = fadeIn(), exit = fadeOut()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                item {
                    VolumeSelector(
                        volumes = state.volumes,
                        selectedPath = state.currentNode?.path ?: "",
                        onVolumeSelected = { viewModel.navigateTo(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.currentNode?.let { node ->
                        BreadcrumbBar(
                            path = node.path,
                            onSegmentClick = { path -> viewModel.navigateTo(path) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Text("Storage Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    state.currentNode?.let { node ->
                        if (node.children.isNotEmpty()) {
                            TreemapView(
                                node = node,
                                onFolderClick = { path -> viewModel.navigateTo(path) }
                            )
                        } else {
                            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Empty directory", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Largest Files", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(state.topFiles.take(50), key = { it.path }) { file ->
                    FileRow(file = file)
                }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbBar(path: String, onSegmentClick: (String) -> Unit) {
    val segments = path.split("/").filter { it.isNotBlank() }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.foldIndexed("") { index, acc, segment ->
            val fullPath = "/$acc$segment".replace("//", "/")
            Text(
                text = if (index == 0) "/" else segment,
                modifier = Modifier.clickable { onSegmentClick(fullPath) },
                style = MaterialTheme.typography.bodySmall,
                color = if (index == segments.lastIndex) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (index == segments.lastIndex) androidx.compose.ui.text.font.FontWeight.Bold else null
            )
            if (index < segments.lastIndex) {
                Icon(Icons.Default.ChevronRight, contentDescription = null,
                    modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            "$acc$segment/"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeSelector(
    volumes: List<StorageAnalyzer.VolumeInfo>,
    selectedPath: String,
    onVolumeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = volumes.firstOrNull { it.path == selectedPath }?.label ?: volumes.firstOrNull()?.label ?: "Select volume"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Volume") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            volumes.forEach { volume ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(volume.label, fontWeight = FontWeight.Medium)
                            Text("${formatBytes(volume.usedBytes)} / ${formatBytes(volume.totalBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = {
                        expanded = false
                        onVolumeSelected(volume.path)
                    }
                )
            }
        }
    }
}

private val categoryColors = mapOf(
    "IMAGE" to Color(0xFF4CAF50),
    "VIDEO" to Color(0xFF2196F3),
    "AUDIO" to Color(0xFF9C27B0),
    "DOCUMENT" to Color(0xFF009688),
    "ARCHIVE" to Color(0xFFFF9800),
    "APK" to Color(0xFFF44336),
    "CACHE" to Color(0xFF607D8B),
    "THUMBNAIL" to Color(0xFF795548),
    "OTHER" to Color(0xFF9E9E9E)
)

@Composable
private fun TreemapView(node: StorageAnalyzer.StorageNode, onFolderClick: (String) -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val topChildren = node.children.take(30)
        val totalSize = topChildren.sumOf { it.size }.coerceAtLeast(1)

        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val padding = 2f
            val areaWidth = size.width
            val areaHeight = size.height

            var x = padding
            var y = padding
            val rowHeight = areaHeight / kotlin.math.ceil(topChildren.size / 4.0).toFloat().coerceAtLeast(1f)
            var currentCol = 0
            val maxCols = 4

            for (child in topChildren) {
                val fraction = child.size.toFloat() / totalSize
                val childWidth = (areaWidth * fraction).coerceAtLeast(40f)

                if (currentCol >= maxCols || x + childWidth > areaWidth) {
                    x = padding
                    y += rowHeight
                    currentCol = 0
                }

                val color = categoryColors[child.fileCategory.name] ?: Color(0xFF9E9E9E)
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size((childWidth - padding).coerceAtLeast(0f), (rowHeight - padding).coerceAtLeast(0f))
                )

                x += childWidth
                currentCol++
            }
        }
    }
}

@Composable
private fun FileRow(file: FileItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(file.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Text(formatBytes(file.size), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
