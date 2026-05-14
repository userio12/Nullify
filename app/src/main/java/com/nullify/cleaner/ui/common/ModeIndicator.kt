package com.nullify.cleaner.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.domain.mode.ModeLevel

@Composable
fun ModeIndicator(
    modeName: String,
    level: ModeLevel,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        ModeLevel.ROOT -> Color(0xFF4CAF50)
        ModeLevel.SHIZUKU -> Color(0xFF2196F3)
        ModeLevel.ACCESSIBILITY -> Color(0xFFFF9800)
        ModeLevel.BASIC -> Color(0xFF9E9E9E)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = modeName,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
