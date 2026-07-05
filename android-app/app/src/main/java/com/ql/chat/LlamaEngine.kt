package com.ql.chat

class LlamaEngine {
    init {
        System.loadLibrary("ql-jni")
    }

    private external fun nativeInit(modelPath: String, nCtx: Int): Long
    private external fun nativeFree(handle: Long)
    private external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int): String
    private external fun nativeSystemInfo(): String

    private var handle: Long = 0L

    fun init(modelPath: String, nCtx: Int = 4096) {
        handle = nativeInit(modelPath, nCtx)
        if (handle == 0L) {
            throw IllegalStateException("Failed to initialize LLM engine")
        }
    }

    fun generate(prompt: String, maxTokens: Int = 2048): String {
        if (handle == 0L) {
            throw IllegalStateException("Engine not initialized")
        }
        return nativeGenerate(handle, prompt, maxTokens)
    }

    fun systemInfo(): String {
        return nativeSystemInfo()
    }

    fun free() {
        if (handle != 0L) {
            nativeFree(handle)
            handle = 0L
        }
    }

    protected fun finalize() {
        free()
    }
}
