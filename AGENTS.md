# Nullify — AGENTS.md

## Build & Run

```sh
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # signed release APK (needs release.keystore)
./gradlew clean                  # delete build/ output
```

- AGP `8.11.0`, Kotlin `2.0.21`, Gradle `8.14.3`, JDK 17. Compose compiler managed by `org.jetbrains.kotlin.plugin.compose`.
- Version catalog: `gradle/libs.versions.toml` — use version refs, never hardcode.
- KSP is enabled for Room annotation processing.
- Release signing via `release.properties` + `release.keystore` (both committed — credentials plaintext).
- Lint checks on release builds disabled (`checkReleaseBuilds = false`).

## Architecture

- Single module `:app`, package `com.nullify.cleaner`.
- Entrypoint: `MainActivity.kt` — Jetpack Compose + Navigation Compose.
- **9 feature screens**: Dashboard, Storage Analyzer, CorpseFinder, JunkCleaner, DuplicateFinder, AppManager, Scheduler, ExclusionRules, RecycleBin.
- **3 operating modes**: Root (Runtime.exec + su), Shizuku (rikka.shizuku), AccessibilityService (fallback).
- **MVI per screen**: `State` data class, `ViewModel` with `StateFlow`, `@Composable Screen`.
- **Clean architecture** with internal packages: `data/`, `domain/`, `ui/`, `service/`, `di/`.

## Key Dependencies

| Library | Catalog Key | Purpose |
|---------|-------------|---------|
| Room + KSP | `room-*` | Local DB (4 entities: cleanup_logs, schedules, exclusion_rules, deleted_files) |
| DataStore | `datastore-preferences` | Preferences |
| Koin | `koin-*` | DI (4 modules: app, data, domain, ui) |

| Shizuku | `shizuku-api`, `shizuku-provider` | Non-root ADB-level elevation + Sui (Magisk) support |
| WorkManager | `workmanager` | Scheduled periodic cleanup |
| Navigation Compose | `androidx-navigation-compose` | Screen routing |

## Important Constraints

- **resolutionStrategy.force()** in `app/build.gradle.kts` pins specific Kotlin stdlib (2.0.21) and AndroidX versions. When adding new deps, ensure compatibility with these forced versions.
- **Packaging pickFirsts/merges** configured for KMP metadata conflicts. Extend these lists if duplicate file errors appear when adding KMP-transitive deps.
- **Kotlin stdlib** dependency explicitly excludes `kotlin-stdlib-jdk7` and `kotlin-stdlib-jdk8` to avoid version conflicts.
- **No `.gitignore` at root** — only `app/.gitignore` exists. Root-level generated files may get committed.
- **ProGuard**: Release minified with `proguard-android-optimize.txt` + `app/proguard-rules.pro`. Debug is unminified.

## Execution Mode Selection

Auto-detected in `DomainModule.kt` — order: Root → Shizuku → Basic.

## Permission Model

| Feature | Permission | Notes |
|---------|-----------|-------|
| Basic file ops | `MANAGE_EXTERNAL_STORAGE` | For SDK 30+ shared storage |
| Package queries | `QUERY_ALL_PACKAGES` | App list + orphan detection |
| Shizuku | Shizuku runtime grant | Requires Shizuku app installed |
| Root | `su` binary | Auto-detected at runtime |
| Accessibility | `BIND_ACCESSIBILITY_SERVICE` | Manual enable in system settings |
| Scheduling | `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` | Foreground service + alarms |
