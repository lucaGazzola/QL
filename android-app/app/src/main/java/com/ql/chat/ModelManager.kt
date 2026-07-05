package com.ql.chat

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class ModelManager(private val modelsDir: File) {

    companion object {
        const val MODEL_REPO = "unsloth/Qwen3.5-4B-GGUF"
        const val MODEL_FILENAME = "Qwen3.5-4B-UD-Q4_K_XL.gguf"
        private const val BASE_URL = "https://huggingface.co"
        private const val TEMP_SUFFIX = ".downloading"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    init {
        modelsDir.mkdirs()
    }

    fun modelExists(): Boolean {
        val modelFile = File(modelsDir, MODEL_FILENAME)
        val tempFile = File(modelsDir, MODEL_FILENAME + TEMP_SUFFIX)
        // Model exists only if file is present and no download in progress
        return modelFile.exists() && !tempFile.exists()
    }

    fun getModelPath(): String {
        return File(modelsDir, MODEL_FILENAME).absolutePath
    }

    fun getModelUrl(): String {
        return "$BASE_URL/$MODEL_REPO/resolve/main/$MODEL_FILENAME"
    }

    fun getModelSizeMB(): Long {
        val file = File(modelsDir, MODEL_FILENAME)
        return if (file.exists()) file.length() / (1024 * 1024) else 0
    }

    @Throws(IOException::class)
    fun downloadModel(
        onProgress: ((percent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit)? = null
    ): String {
        val outputFile = File(modelsDir, MODEL_FILENAME)
        val tempFile = File(modelsDir, MODEL_FILENAME + TEMP_SUFFIX)

        // If model already exists and is valid, return it
        if (outputFile.exists() && !tempFile.exists()) {
            return outputFile.absolutePath
        }

        // Check if we can resume a partial download
        var downloadedBytes = 0L
        if (tempFile.exists()) {
            downloadedBytes = tempFile.length()
            println("Resuming download from $downloadedBytes bytes")
        }

        val requestBuilder = Request.Builder()
            .url(getModelUrl())

        // Add Range header for resume support
        if (downloadedBytes > 0) {
            requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
        }

        val response = client.newCall(requestBuilder.build()).execute()

        // Handle range not satisfiable (file already complete)
        if (response.code == 416) {
            tempFile.renameTo(outputFile)
            return outputFile.absolutePath
        }

        if (!response.isSuccessful) {
            throw IOException("Download failed: ${response.code}")
        }

        val body = response.body ?: throw IOException("Empty response body")
        val contentLength = body.contentLength()
        val totalBytes = if (response.code == 206) {
            downloadedBytes + contentLength
        } else {
            contentLength
        }

        // If it's a fresh download (not 206 Partial), delete any temp file
        if (response.code != 206) {
            tempFile.delete()
            downloadedBytes = 0
        }

        body.byteStream().use { input ->
            FileOutputStream(tempFile, downloadedBytes > 0).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    if (totalBytes > 0) {
                        val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                        onProgress?.invoke(progress, downloadedBytes, totalBytes)
                    }
                }
            }
        }

        // Verify download completed
        if (totalBytes > 0 && downloadedBytes < totalBytes) {
            throw IOException("Download incomplete: $downloadedBytes of $totalBytes bytes")
        }

        // Rename temp file to final file
        if (!tempFile.renameTo(outputFile)) {
            throw IOException("Failed to rename temp file to final location")
        }

        return outputFile.absolutePath
    }

    fun deleteModel(): Boolean {
        val modelFile = File(modelsDir, MODEL_FILENAME)
        val tempFile = File(modelsDir, MODEL_FILENAME + TEMP_SUFFIX)
        tempFile.delete()
        return modelFile.delete()
    }

    fun getDownloadState(): DownloadState {
        val modelFile = File(modelsDir, MODEL_FILENAME)
        val tempFile = File(modelsDir, MODEL_FILENAME + TEMP_SUFFIX)

        return when {
            modelFile.exists() && !tempFile.exists() -> DownloadState.COMPLETE
            tempFile.exists() -> DownloadState.IN_PROGRESS
            else -> DownloadState.NOT_STARTED
        }
    }

    enum class DownloadState {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE
    }
}
