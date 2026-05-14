package com.nullify.cleaner.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.util.formatBytes

@Composable
fun ProgressOverlay(
    currentItem: String,
    progress: Int,
    maxProgress: Int,
    bytesFound: Long,
    modifier: Modifier = Modifier
) {
    val blue = Color(0xFF3B82F6)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = blue)
            Spacer(modifier = Modifier.height(16.dp))
            if (maxProgress > 0) {
                LinearProgressIndicator(
                    progress = { progress.toFloat() / maxProgress.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                    color = blue,
                    trackColor = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("$progress / ${if (maxProgress > 0) maxProgress else 0}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = currentItem,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (bytesFound > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Found: ${formatBytes(bytesFound)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
