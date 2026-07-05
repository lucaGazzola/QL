#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global engine handle (will be replaced with actual llama.cpp context)
static jlong g_engine_handle = 0;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_ql_chat_LlamaEngine_nativeInit(
    JNIEnv *env,
    jobject thiz,
    jstring model_path,
    jint n_ctx
) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing engine with model: %s, context: %d", path, n_ctx);

    // TODO: Replace with actual llama_init_from_file()
    // llama_model *model = llama_init_from_file(path, params);
    // g_engine_handle = reinterpret_cast<jlong>(model);

    env->ReleaseStringUTFChars(model_path, path);

    // Return dummy handle for now
    return 1L;
}

JNIEXPORT void JNICALL
Java_com_ql_chat_LlamaEngine_nativeFree(
    JNIEnv *env,
    jobject thiz,
    jlong handle
) {
    LOGI("Freeing engine handle: %ld", handle);
    // TODO: Replace with actual llama_free()
    // llama_model *model = reinterpret_cast<llama_model*>(handle);
    // llama_free(model);
}

JNIEXPORT jstring JNICALL
Java_com_ql_chat_LlamaEngine_nativeGenerate(
    JNIEnv *env,
    jobject thiz,
    jlong handle,
    jstring prompt,
    jint max_tokens
) {
    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating with prompt length: %zu, max_tokens: %d",
         strlen(prompt_str), max_tokens);

    // TODO: Replace with actual llama completion
    // For now, return a placeholder response
    std::string response = "Hello! I'm a placeholder response. "
                           "The actual llama.cpp integration needs to be completed.";

    env->ReleaseStringUTFChars(prompt, prompt_str);

    return env->NewStringUTF(response.c_str());
}

} // extern "C"
