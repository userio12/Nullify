package com.nullify.cleaner.util

object Constants {
    const val DATABASE_NAME = "nullify_database"
    const val PREFS_NAME = "nullify_prefs"
    const val RECYCLE_BIN_EXPIRY_MS = 86_400_000L
    const val DEFAULT_SCAN_THREADS = 4
    const val TREEMAP_MAX_DEPTH = 4
    const val TOP_FILES_LIMIT = 100
    const val SCAN_PROGRESS_INTERVAL = 10

    val INTERNAL_STORAGE_PATH = "/storage/emulated/0"
    val ROOT_DATA_PATHS = listOf("/data/data", "/data/user/0", "/data/user_de/0")

    val KNOWN_DATA_PATHS = listOf(
        "/data/data",
        "/data/user/0",
        "/data/user_de/0",
        "/sdcard/Android/data",
        "/sdcard/Android/obb"
    )

    val CACHE_DIR_PATTERNS = listOf(
        "cache", "code_cache", "app_webview", "app_cache",
        ".thumbnails", "thumbnails", "temp", "tmp"
    )

    val SYSTEM_CACHE_PATHS = listOf("/cache", "/data/local/tmp")
}
