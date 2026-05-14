package com.nullify.cleaner.util

import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

object HashUtils {

    private const val CHUNK_SIZE = 4096

    fun sha256Full(filePath: String): String? = runCatching {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) return@runCatching null
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    fun sha256Partial(filePath: String): String? = runCatching {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) return@runCatching null
        val digest = MessageDigest.getInstance("SHA-256")
        val raf = RandomAccessFile(file, "r")
        val firstSize = CHUNK_SIZE.coerceAtMost(file.length().coerceAtMost(CHUNK_SIZE.toLong()).toInt())
        raf.readFully(ByteArray(firstSize)).also { digest.update(ByteArray(firstSize)) }
        if (file.length() > CHUNK_SIZE * 2) {
            raf.seek(file.length() - CHUNK_SIZE)
            raf.readFully(ByteArray(CHUNK_SIZE)).also { digest.update(ByteArray(CHUNK_SIZE)) }
        }
        raf.close()
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    fun md5(filePath: String): String? = runCatching {
        val file = File(filePath)
        if (!file.exists()) return@runCatching null
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()
}
