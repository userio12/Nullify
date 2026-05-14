package com.nullify.cleaner.ui.navigation

import android.os.Build
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.layerBackdrop

object TabRoutes {
    const val HOME = "home"
    const val QUICK_TOOLS = "quick_tools"
    const val SETTINGS = "settings"

    val tabs = listOf(
        NavTab("Home", Icons.Outlined.Home, HOME),
        NavTab("QuickTools", Icons.Outlined.GridView, QUICK_TOOLS),
        NavTab("Settings", Icons.Outlined.Settings, SETTINGS)
    )
}

@Composable
fun AppScaffold(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    val backdrop = rememberLayerBackdrop {
        if (Build.VERSION.SDK_INT >= 31) {
            // For API 31+, draw pure black so the glass captures behind
            drawRect(Color(0xFF000000))
        } else {
            // Pre-31 fallback: no backdrop effect, draw surface directly
            drawRect(Color(0xFF000000))
        }
        drawContent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
                .layerBackdrop(backdrop)
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                        initialOffsetX = { dir * it }
                    ) togetherWith
                    slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                        targetOffsetX = { -dir * it }
                    ) using SizeTransform(clip = false)
                },
                label = "tabContent"
            ) { tab ->
                content(tab)
            }
        }

        BottomNavBar(
            selectedIndex = currentTab,
            onTabSelected = { onTabSelected(it) },
            tabs = TabRoutes.tabs,
            backdrop = backdrop,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
