package com.nullify.cleaner.service.shizuku

import rikka.shizuku.Shizuku

object CleanerShizukuService {

    var isRunning = false
        private set

    fun start() {
        if (!Shizuku.pingBinder()) return
        isRunning = true
    }

    fun stop() {
        isRunning = false
    }

    fun executeShell(command: String): Result<String> = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val output = process.inputStream.bufferedReader().readText().trim()
        val error = process.errorStream.bufferedReader().readText().trim()
        process.waitFor()
        if (error.isNotEmpty()) throw RuntimeException(error)
        output
    }

    fun deletePath(path: String, isDirectory: Boolean): Boolean = runCatching {
        val cmd = if (isDirectory) "rm -rf \"$path\"" else "rm -f \"$path\""
        executeShell(cmd).isSuccess
    }.getOrDefault(false)

    fun listDirectory(path: String): List<String> = runCatching {
        executeShell("ls -1 \"$path\"").getOrThrow().lines().filter { it.isNotBlank() }
    }.getOrDefault(emptyList())

    fun fileExists(path: String): Boolean = runCatching {
        executeShell("test -e \"$path\" && echo true || echo false").getOrThrow() == "true"
    }.getOrDefault(false)
}
