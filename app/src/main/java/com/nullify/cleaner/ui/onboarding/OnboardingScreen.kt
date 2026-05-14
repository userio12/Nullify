package com.nullify.cleaner.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nullify.cleaner.ui.common.ModeIndicator
import com.nullify.cleaner.domain.mode.ModeLevel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkCapabilities(context)
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    if (state.isComplete) return

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(targetState = state.currentStep, label = "onboarding") { step ->
            when (step) {
                0 -> WelcomeStep(state, viewModel, context)
                1 -> PermissionStep(state, viewModel, context)
                2 -> ModeSummaryStep(state, viewModel, context)
                3 -> CompletionStep(state, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { i ->
                val dotColor = if (i <= state.currentStep) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(_state: OnboardingUiState, viewModel: OnboardingViewModel, _context: android.content.Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome to Nullify", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your ultimate Android cleaning suite. Let's get you set up.",
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.nextStep() }, modifier = Modifier.fillMaxWidth()) { Text("Get Started") }
        TextButton(onClick = { viewModel.complete() }) { Text("Skip setup") }
    }
}

@Composable
private fun PermissionStep(state: OnboardingUiState, viewModel: OnboardingViewModel, context: android.content.Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Grant Permissions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            icon = Icons.Default.Storage,
            title = "Storage Access",
            description = "Required to scan and clean files on your device",
            isGranted = state.hasStoragePermission,
            onGrant = { viewModel.requestStoragePermission(context) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isShizukuAvailable) {
            PermissionCard(
                icon = Icons.Default.Handyman,
                title = "Shizuku",
                description = "Elevated operations without root",
                isGranted = false,
                onGrant = { viewModel.requestShizukuPermission() }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        PermissionCard(
            icon = Icons.Default.Visibility,
            title = "Accessibility Service",
            description = "Enables automated cache clearing (optional)",
            isGranted = state.isAccessibilityEnabled,
            onGrant = { viewModel.openAccessibilitySettings(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.nextStep() }, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
        TextButton(onClick = { viewModel.complete() }) { Text("Skip") }
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, title: String, description: String, isGranted: Boolean, onGrant: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isGranted) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary)
            } else {
                OutlinedButton(onClick = onGrant) { Text("Grant") }
            }
        }
    }
}

@Composable
private fun ModeSummaryStep(state: OnboardingUiState, viewModel: OnboardingViewModel, _context: android.content.Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Your Capabilities", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Based on your device, Nullify will use the most capable mode available.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        CapabilityCard("Root Access", state.isRootAvailable, ModeLevel.ROOT)
        CapabilityCard("Shizuku", state.isShizukuAvailable, ModeLevel.SHIZUKU)
        CapabilityCard("Accessibility Service", state.isAccessibilityEnabled, ModeLevel.ACCESSIBILITY)

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.nextStep() }, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@Composable
private fun CapabilityCard(label: String, available: Boolean, level: ModeLevel) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (available) Icons.Default.CheckCircle else Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Text(label, modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyLarge)
            ModeIndicator(modeName = level.name, level = level)
        }
    }
}

@Composable
private fun CompletionStep(_state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("All Set!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nullify is ready to clean your device. Tap finish to start.",
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.complete() }, modifier = Modifier.fillMaxWidth()) { Text("Finish") }
    }
}
