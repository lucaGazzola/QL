package com.ql.chat

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ModelManager(private val modelsDir: File) {

    companion object {
        const val MODEL_REPO = "unsloth/Qwen3.5-4B-GGUF"
        const val MODEL_FILENAME = "Qwen3.5-4B-UD-Q4_K_XL.gguf"
        private const val BASE_URL = "https://huggingface.co"
    }

    init {
        modelsDir.mkdirs()
    }

    fun modelExists(): Boolean {
        return File(modelsDir, MODEL_FILENAME).exists()
    }

    fun getModelPath(): String {
        return File(modelsDir, MODEL_FILENAME).absolutePath
    }

    fun getModelUrl(): String {
        return "$BASE_URL/$MODEL_REPO/resolve/main/$MODEL_FILENAME"
    }

    @Throws(IOException::class)
    fun downloadModel(
        onProgress: ((percent: Int) -> Unit)? = null
    ): String {
        val outputFile = File(modelsDir, MODEL_FILENAME)
        if (outputFile.exists()) {
            return outputFile.absolutePath
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(getModelUrl())
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Download failed: ${response.code}")
        }

        val body = response.body ?: throw IOException("Empty response body")
        val contentLength = body.contentLength()
        var downloaded = 0L

        body.byteStream().use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    if (contentLength > 0) {
                        val progress = ((downloaded * 100) / contentLength).toInt()
                        onProgress?.invoke(progress)
                    }
                }
            }
        }

        return outputFile.absolutePath
    }
}
