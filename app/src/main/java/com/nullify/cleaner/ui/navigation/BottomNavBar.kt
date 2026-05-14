package com.nullify.cleaner.ui.navigation

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

data class NavTab(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val pillShape = RoundedCornerShape(32.dp)
private val activeBlue = Color(0xFF3B82F6)
private val surfaceDark = Color(0xFF0A0A0A)

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<NavTab>,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(72.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { pillShape },
                    effects = {
                        if (Build.VERSION.SDK_INT >= 31) {
                            vibrancy()
                            blur(12f.dp.toPx())
                            if (Build.VERSION.SDK_INT >= 33) {
                                lens(16f.dp.toPx(), 32f.dp.toPx())
                            }
                        }
                    },
                    onDrawSurface = {
                        drawRect(surfaceDark.copy(alpha = 0.5f))
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .drawBackdrop(
                                            backdrop = backdrop,
                                            shape = { RoundedCornerShape(16.dp) },
                                            effects = {
                                                if (Build.VERSION.SDK_INT >= 31) {
                                                    vibrancy()
                                                    blur(8f.dp.toPx())
                                                    if (Build.VERSION.SDK_INT >= 33) {
                                                        lens(12f.dp.toPx(), 24f.dp.toPx())
                                                    }
                                                }
                                            },
                                            onDrawSurface = {
                                                drawRect(activeBlue, blendMode = BlendMode.Hue)
                                                drawRect(activeBlue.copy(alpha = 0.65f))
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 4.dp)
                .graphicsLayer {
                    val index = selectedIndex
                    val totalWidth = 320f
                    val tabW = totalWidth / tabs.size
                    translationX = index * tabW + (tabW - 48f) / 2f
                    translationY = 72f - 6f
                    shadowElevation = 12f
                    spotShadowColor = activeBlue.copy(alpha = 0.6f)
                }
                .clip(RoundedCornerShape(2.dp))
                .background(activeBlue)
        )
    }
}
