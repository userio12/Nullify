package com.nullify.cleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nullify.cleaner.data.preferences.AppPreferences
import com.nullify.cleaner.ui.analyzer.AnalyzerScreen
import com.nullify.cleaner.ui.appmanager.AppManagerScreen
import com.nullify.cleaner.ui.corpse.CorpseScreen
import com.nullify.cleaner.ui.dashboard.DashboardScreen
import com.nullify.cleaner.ui.duplicate.DuplicateScreen
import com.nullify.cleaner.ui.exclusion.ExclusionRulesScreen
import com.nullify.cleaner.ui.junk.JunkScreen
import com.nullify.cleaner.ui.navigation.AppScaffold
import com.nullify.cleaner.ui.navigation.Route
import com.nullify.cleaner.ui.onboarding.OnboardingScreen
import com.nullify.cleaner.ui.quicktools.ToolsScreen
import com.nullify.cleaner.ui.recyclebin.RecycleBinScreen
import com.nullify.cleaner.ui.scheduler.SchedulerScreen
import com.nullify.cleaner.ui.settings.SettingsScreen
import com.nullify.cleaner.ui.theme.MyComposeApplicationTheme
import org.koin.java.KoinJavaComponent.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    NullifyNavHost()
                }
            }
        }
    }
}

@Composable
fun NullifyNavHost() {
    val navController = rememberNavController()
    val prefs: AppPreferences = get(AppPreferences::class.java)
    val isFirstLaunch by prefs.firstLaunch.collectAsState(initial = true)
    var currentTab by remember { mutableStateOf(0) }

    val startDest = if (isFirstLaunch) Route.Onboarding.route else Route.Home.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Home.route) {
            AppScaffold(
                currentTab = currentTab,
                onTabSelected = { index ->
                    currentTab = index
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Home.route) { inclusive = true }
                    }
                }
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(onNavigate = { route -> navController.navigate(route) })
                    1 -> ToolsScreen(onNavigate = { route -> navController.navigate(route) })
                    2 -> SettingsScreen(onNavigate = { route -> navController.navigate(route) })
                }
            }
        }

        composable(Route.Analyzer.route, arguments = listOf(navArgument("path") { type = NavType.StringType; defaultValue = "/storage/emulated/0" })) {
            AnalyzerScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.Corpse.route) { CorpseScreen(onBack = { navController.popBackStack() }) }
        composable(Route.Junk.route) { JunkScreen(onBack = { navController.popBackStack() }) }
        composable(Route.Duplicate.route) { DuplicateScreen(onBack = { navController.popBackStack() }) }
        composable(Route.AppManager.route) { AppManagerScreen(onBack = { navController.popBackStack() }) }
        composable(Route.Scheduler.route) { SchedulerScreen(onBack = { navController.popBackStack() }) }
        composable(Route.ExclusionRules.route) { ExclusionRulesScreen(onBack = { navController.popBackStack() }) }
        composable(Route.RecycleBin.route) { RecycleBinScreen(onBack = { navController.popBackStack() }) }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyComposeApplicationTheme {
        NullifyNavHost()
    }
}
