package com.nullify.cleaner.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullify.cleaner.data.local.entity.CleanupLogEntity
import com.nullify.cleaner.domain.model.StorageInfo
import com.nullify.cleaner.util.formatBytes
import org.koin.androidx.compose.koinViewModel

private val cardShape = RoundedCornerShape(16.dp)

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val cleanupStats by viewModel.recentCleanups.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    state.storageInfo?.let { info ->
                        Text("${formatBytes(info.usedBytes)} of ${formatBytes(info.totalBytes)} used", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                TextButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else {
                state.storageInfo?.let { StorageGaugeCard(it) }
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Tools")
                Spacer(modifier = Modifier.height(12.dp))
                ToolGrid(onNavigate = onNavigate)
                Spacer(modifier = Modifier.height(24.dp))
                state.storageInfo?.let { CategoryBreakdownCard(it) }
                Spacer(modifier = Modifier.height(24.dp))
                if (cleanupStats.isNotEmpty()) {
                    SectionHeader("Recent")
                    Spacer(modifier = Modifier.height(12.dp))
                    CleanupHistoryCard(logs = cleanupStats)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
}

@Composable
private fun StorageGaugeCard(info: StorageInfo) {
    val usedPercent by animateFloatAsState(targetValue = info.usedPercent, label = "pct")
    val accent = Color(0xFF3B82F6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Device Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(136.dp)) {
                CircularProgressIndicator(
                    progress = { usedPercent },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    color = when { usedPercent > 0.85f -> Color(0xFFEF4444); usedPercent > 0.7f -> Color(0xFFF59E0B); else -> accent },
                    trackColor = Color(0xFF1F2937)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.1f".format(usedPercent * 100), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("used", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 2.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Total", formatBytes(info.totalBytes))
                StatItem("Used", formatBytes(info.usedBytes))
                StatItem("Free", formatBytes(info.freeBytes))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp)
    }
}

@Composable
private fun ToolGrid(onNavigate: (String) -> Unit) {
    val tools = remember {
        listOf(
            ToolItem("Analyzer", "Visualize space", Icons.Default.Analytics, "analyzer"),
            ToolItem("Orphans", "Leftover data", Icons.Default.Delete, "corpse_finder"),
            ToolItem("Junk", "Caches & temps", Icons.Default.CleaningServices, "junk_cleaner"),
            ToolItem("Duplicates", "Identical files", Icons.Default.ContentCopy, "duplicate_finder"),
            ToolItem("Apps", "Manage installed", Icons.Default.Phonelink, "app_manager"),
            ToolItem("Schedule", "Auto-cleanup", Icons.Default.Schedule, "scheduler")
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tools.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { tool ->
                    Card(
                        modifier = Modifier.weight(1f).clickable { onNavigate(tool.route) },
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(tool.icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(tool.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private data class ToolItem(val name: String, val description: String, val icon: ImageVector, val route: String)

@Composable
private fun CategoryBreakdownCard(info: StorageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Storage Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            CategoryBar("Apps", info.appBytes, info.totalBytes, Color(0xFF3B82F6))
            Spacer(modifier = Modifier.height(8.dp))
            CategoryBar("Media", info.mediaBytes, info.totalBytes, Color(0xFF10B981))
            Spacer(modifier = Modifier.height(8.dp))
            CategoryBar("Cache", info.cacheBytes, info.totalBytes, Color(0xFFEF4444))
            Spacer(modifier = Modifier.height(8.dp))
            CategoryBar("Misc", info.miscBytes, info.totalBytes, Color(0xFF6B7280))
        }
    }
}

@Composable
private fun CategoryBar(label: String, value: Long, total: Long, color: Color) {
    val fraction = if (total > 0) value.toFloat() / total else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text(formatBytes(value), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
            drawRoundRect(color = Color(0xFF1F2937), cornerRadius = CornerRadius(3.dp.toPx()))
            if (fraction > 0f) drawRoundRect(color = color, size = size.copy(width = size.width * fraction), cornerRadius = CornerRadius(3.dp.toPx()))
        }
    }
}

@Composable
private fun CleanupHistoryCard(logs: List<CleanupLogEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            logs.take(5).forEachIndexed { i, log ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HorizontalSplit, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(log.toolType.replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                            Text(java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    Text(formatBytes(log.bytesFreed), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6))
                }
                if (i < minOf(logs.size, 5) - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFF1F2937))
            }
        }
    }
}
