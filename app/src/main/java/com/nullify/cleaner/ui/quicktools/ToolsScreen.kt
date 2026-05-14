package com.nullify.cleaner.ui.quicktools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class ToolCard(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

private val allTools = listOf(
    ToolCard("Storage Analyzer", "Visualize usage", Icons.Default.Analytics, "analyzer", Color(0xFF3B82F6)),
    ToolCard("Corpse Finder", "Leftover data", Icons.Default.Delete, "corpse_finder", Color(0xFFEF4444)),
    ToolCard("Junk Cleaner", "Cache & temps", Icons.Default.CleaningServices, "junk_cleaner", Color(0xFF10B981)),
    ToolCard("Duplicate Finder", "Identical files", Icons.Default.ContentCopy, "duplicate_finder", Color(0xFF8B5CF6)),
    ToolCard("App Manager", "Manage apps", Icons.Default.Phonelink, "app_manager", Color(0xFFF59E0B)),
    ToolCard("Scheduler", "Auto cleanup", Icons.Default.Schedule, "scheduler", Color(0xFFEC4899))
)

@Composable
fun ToolsScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Text(
            "Quick Tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "All cleaning tools at a glance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allTools, key = { it.name }) { tool ->
                ToolCardItem(tool = tool, onClick = { onNavigate(tool.route) })
            }
        }
    }
}

@Composable
private fun ToolCardItem(tool: ToolCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tool.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(tool.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}
