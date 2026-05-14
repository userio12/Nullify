package com.nullify.cleaner.ui.navigation

sealed class Route(val route: String) {
    data object Onboarding : Route("onboarding")
    data object Home : Route("home")
    data object QuickTools : Route("quick_tools")
    data object Settings : Route("settings")
    data object Analyzer : Route("analyzer/{path}") {
        fun createRoute(path: String = "/storage/emulated/0") = "analyzer/${java.net.URLEncoder.encode(path, "UTF-8")}"
    }
    data object Corpse : Route("corpse_finder")
    data object Junk : Route("junk_cleaner")
    data object Duplicate : Route("duplicate_finder")
    data object AppManager : Route("app_manager")
    data object Scheduler : Route("scheduler")
    data object ExclusionRules : Route("exclusion_rules")
    data object RecycleBin : Route("recycle_bin")
}
