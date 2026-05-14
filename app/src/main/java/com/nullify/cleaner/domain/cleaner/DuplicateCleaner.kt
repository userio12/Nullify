package com.nullify.cleaner.domain.cleaner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.DuplicateGroup
import com.nullify.cleaner.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

class DuplicateCleaner {

    private val chunkSize = 4096

    suspend fun scan(
        files: List<FileItem>,
        detectNearDuplicates: Boolean = true
    ): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "Grouping by size", currentProgress = 0, totalProgress = 100, bytesFound = 0))

        val imageFiles = mutableListOf<FileItem>()
        val otherFiles = mutableListOf<FileItem>()

        files.filter { !it.isDirectory }.forEach {
            if (detectNearDuplicates && it.name.isImageFile()) imageFiles.add(it) else otherFiles.add(it)
        }

        val sizeGroups = otherFiles.groupBy { it.size }.filter { it.value.size > 1 }

        val allDuplicates = mutableListOf<DuplicateGroup>()

        emit(CleanProgress(currentItem = "Computing partial hashes", currentProgress = 20, totalProgress = 100))

        val exactCandidates = mutableListOf<List<FileItem>>()
        var processed = 0
        val totalCandidates = sizeGroups.values.sumOf { it.size }

        for ((_, groupFiles) in sizeGroups) {
            val hashGroups = groupFiles.groupBy { computePartialHash(it.path) }
            exactCandidates.addAll(hashGroups.values.filter { it.size > 1 })
            processed += groupFiles.size
            emit(CleanProgress(
                currentItem = "Partial hash: $processed/$totalCandidates",
                currentProgress = 20 + (processed * 20 / totalCandidates.coerceAtLeast(1)),
                totalProgress = 100
            ))
        }

        emit(CleanProgress(currentItem = "Computing full hashes", currentProgress = 40, totalProgress = 100))

        var hashProcessed = 0
        val totalHashCandidates = exactCandidates.sumOf { it.size }

        for (candidates in exactCandidates) {
            val fullHashGroups = candidates.groupBy { computeFullHash(it.path) }
                .filterKeys { it != null }
                .mapKeys { it.key!! }
            for ((hash, hashFiles) in fullHashGroups) {
                if (hashFiles.size > 1) {
                    allDuplicates.add(DuplicateGroup(
                        id = hash.take(12),
                        fileHash = hash,
                        totalBytes = hashFiles.sumOf { it.size },
                        files = hashFiles.sortedByDescending { it.lastModified },
                        isExactDuplicate = true
                    ))
                }
            }
            hashProcessed += candidates.size
            emit(CleanProgress(
                currentItem = "Full hash: $hashProcessed/$totalHashCandidates",
                currentProgress = 40 + (hashProcessed * 20 / totalHashCandidates.coerceAtLeast(1)),
                totalProgress = 100,
                bytesFound = allDuplicates.sumOf { it.totalBytes }
            ))
        }

        if (detectNearDuplicates && imageFiles.size > 1) {
            emit(CleanProgress(currentItem = "Detecting near-duplicate images", currentProgress = 65, totalProgress = 100))

            val imageHashes = withContext(Dispatchers.IO) {
                imageFiles.mapNotNull { file ->
                    val hash = computeAverageHash(file.path)
                    if (hash != null) file to hash else null
                }
            }

            emit(CleanProgress(currentItem = "Comparing image hashes", currentProgress = 80, totalProgress = 100))

            val compared = mutableSetOf<Int>()
            for (i in imageHashes.indices) {
                if (i in compared) continue
                val similarGroup = mutableListOf(imageHashes[i])
                for (j in i + 1 until imageHashes.size) {
                    if (j in compared) continue
                    val distance = hammingDistance(imageHashes[i].second, imageHashes[j].second)
                    if (distance <= 10) {
                        similarGroup.add(imageHashes[j])
                        compared.add(j)
                    }
                }
                if (similarGroup.size > 1) {
                    val firstHash = imageHashes[i].second
                    allDuplicates.add(DuplicateGroup(
                        id = "near_${firstHash.take(12)}",
                        fileHash = firstHash,
                        totalBytes = similarGroup.sumOf { it.first.size },
                        files = similarGroup.map { it.first }.sortedByDescending { it.lastModified },
                        isExactDuplicate = false,
                        similarity = 1.0f - (similarGroup.zipWithNext { a, b ->
                            hammingDistance(a.second, b.second)
                        }.average().toFloat() / 64f)
                    ))
                }
                compared.add(i)
                emit(CleanProgress(
                    currentItem = "Comparing: ${i + 1}/${imageHashes.size}",
                    currentProgress = 80 + (i * 15 / imageHashes.size.coerceAtLeast(1)),
                    totalProgress = 100,
                    bytesFound = allDuplicates.sumOf { it.totalBytes }
                ))
            }
        }

        val totalSavings = allDuplicates.sumOf { group -> group.files.drop(1).sumOf { it.size } }

        emit(CleanProgress(
            currentItem = "Found ${allDuplicates.size} duplicate groups (${allDuplicates.count { !it.isExactDuplicate }} similar images)",
            currentProgress = 100,
            totalProgress = 100,
            bytesFound = totalSavings,
            duplicateGroups = allDuplicates,
            isComplete = true
        ))
    }

    fun computePartialHash(filePath: String): String? = runCatching {
        val file = File(filePath)
        if (!file.exists() || !file.canRead()) return@runCatching null

        val digest = MessageDigest.getInstance("SHA-256")
        val raf = RandomAccessFile(file, "r")

        val firstSize = chunkSize.coerceAtMost(file.length().coerceAtMost(chunkSize.toLong()).toInt())
        val firstChunk = ByteArray(firstSize)
        raf.readFully(firstChunk)
        digest.update(firstChunk)

        if (file.length() > chunkSize * 2) {
            raf.seek(file.length() - chunkSize)
            val lastChunk = ByteArray(chunkSize)
            raf.readFully(lastChunk)
            digest.update(lastChunk)
        }

        raf.close()
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    fun computeFullHash(filePath: String): String? = runCatching {
        val file = File(filePath)
        if (!file.exists()) return@runCatching null

        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    fun computeAverageHash(filePath: String): String? = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = false; inSampleSize = 2 }
        val bitmap = BitmapFactory.decodeFile(filePath, options) ?: return@runCatching null
        val small = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        bitmap.recycle()

        val pixels = IntArray(64)
        small.getPixels(pixels, 0, 8, 0, 0, 8, 8)
        small.recycle()

        val gray = pixels.map { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        val average = gray.average().toInt()
        gray.joinToString("") { if (it >= average) "1" else "0" }
    }.getOrNull()

    private fun hammingDistance(hash1: String, hash2: String): Int {
        val len = minOf(hash1.length, hash2.length)
        var distance = 0
        for (i in 0 until len) {
            if (hash1[i] != hash2[i]) distance++
        }
        return distance + kotlin.math.abs(hash1.length - hash2.length)
    }

    private fun String.isImageFile(): Boolean = lowercase().let {
        it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
        it.endsWith(".gif") || it.endsWith(".webp") || it.endsWith(".bmp")
    }
}
