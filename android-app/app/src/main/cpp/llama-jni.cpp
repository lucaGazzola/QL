#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>

#define LOG_TAG "LlamaEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// Check if we're building with real llama.cpp
#if __has_include("llama.h")
    #define LLAMA_AVAILABLE 1
    #include "llama.h"
    #include "common.h"
    #include "sampling.h"
#else
    #define LLAMA_AVAILABLE 0
#endif

// Constants
constexpr int N_THREADS_MIN = 2;
constexpr int N_THREADS_MAX = 8;
constexpr int N_THREADS_HEADROOM = 1;
constexpr int DEFAULT_CONTEXT_SIZE = 2048;
constexpr int BATCH_SIZE = 256;
constexpr float DEFAULT_SAMPLER_TEMP = 0.7f;

#if LLAMA_AVAILABLE
// Real llama.cpp state
static llama_model *g_model = nullptr;
static llama_context *g_context = nullptr;
static llama_batch g_batch;
static common_sampler *g_sampler = nullptr;
static bool g_initialized = false;

static llama_context *init_context(llama_model *model, int n_ctx = DEFAULT_CONTEXT_SIZE) {
    if (!model) {
        LOGE("Model is null");
        return nullptr;
    }

    const int n_cpus = (int)sysconf(_SC_NPROCESSORS_ONLN);
    const int n_threads = std::max(N_THREADS_MIN, std::min(N_THREADS_MAX,
        n_cpus - N_THREADS_HEADROOM));
    LOGI("Using %d threads (CPUs: %d)", n_threads, n_cpus);

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = n_ctx;
    ctx_params.n_batch = BATCH_SIZE;
    ctx_params.n_threads = n_threads;
    ctx_params.n_threads_batch = n_threads;

    auto *context = llama_init_from_model(model, ctx_params);
    if (!context) {
        LOGE("Failed to create context");
    }
    return context;
}
#endif

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

#if LLAMA_AVAILABLE
    if (g_initialized) {
        LOGW("Engine already initialized, freeing previous instance");
        // Free previous resources
        if (g_sampler) { common_sampler_free(g_sampler); g_sampler = nullptr; }
        if (g_batch.n_tokens > 0) { llama_batch_free(g_batch); g_batch = {}; }
        if (g_context) { llama_free(g_context); g_context = nullptr; }
        if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    }

    // Initialize backend
    llama_backend_init();

    // Load model
    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (!g_model) {
        LOGE("Failed to load model");
        return 0;
    }

    // Create context
    g_context = init_context(g_model, n_ctx);
    if (!g_context) {
        LOGE("Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return 0;
    }

    // Initialize batch and sampler
    g_batch = llama_batch_init(BATCH_SIZE, 0, 1);

    common_params_sampling sparams;
    sparams.temp = DEFAULT_SAMPLER_TEMP;
    g_sampler = common_sampler_init(g_model, sparams);

    g_initialized = true;
    LOGI("Engine initialized successfully");

    return reinterpret_cast<jlong>(g_context);
#else
    env->ReleaseStringUTFChars(model_path, path);
    LOGW("Running in placeholder mode - llama.cpp not available");
    return 1L; // Dummy handle
#endif
}

JNIEXPORT void JNICALL
Java_com_ql_chat_LlamaEngine_nativeFree(
    JNIEnv *env,
    jobject thiz,
    jlong handle
) {
    LOGI("Freeing engine handle: %ld", handle);

#if LLAMA_AVAILABLE
    if (g_sampler) { common_sampler_free(g_sampler); g_sampler = nullptr; }
    if (g_batch.n_tokens > 0) { llama_batch_free(g_batch); g_batch = {}; }
    if (g_context) { llama_free(g_context); g_context = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    llama_backend_free();
    g_initialized = false;
    LOGI("Engine freed successfully");
#endif
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

#if LLAMA_AVAILABLE
    if (!g_initialized || !g_context || !g_model) {
        env->ReleaseStringUTFChars(prompt, prompt_str);
        return env->NewStringUTF("Error: Engine not initialized");
    }

    // Tokenize input
    auto tokens = common_tokenize(g_context, prompt_str, true, true);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    LOGI("Input tokens: %zu", tokens.size());

    // Clear memory for new generation
    llama_memory_clear(llama_get_memory(g_context), false);

    // Process input tokens in batches
    for (int i = 0; i < (int)tokens.size(); i += BATCH_SIZE) {
        const int batch_size = std::min((int)tokens.size() - i, BATCH_SIZE);
        common_batch_clear(g_batch);

        for (int j = 0; j < batch_size; j++) {
            bool is_last_input = (i + j + 1 >= (int)tokens.size());
            common_batch_add(g_batch, tokens[i + j], i + j, {0}, is_last_input);
        }

        if (llama_decode(g_context, g_batch) != 0) {
            LOGE("llama_decode failed during input processing");
            return env->NewStringUTF("Error: Failed to process input");
        }
    }

    // Generate output tokens
    std::string response;
    llama_pos start_pos = tokens.size();

    for (int i = 0; i < max_tokens; i++) {
        LOGI("Generating token %d...", i);

        // Sample next token
        auto new_token_id = common_sampler_sample(g_sampler, g_context, -1);
        common_sampler_accept(g_sampler, new_token_id, true);

        // Check for end of generation
        if (llama_vocab_is_eog(llama_model_get_vocab(g_model), new_token_id)) {
            LOGI("End of generation reached");
            break;
        }

        // Convert token to text
        auto token_text = common_token_to_piece(g_context, new_token_id);
        response += token_text;

        // Decode the new token
        common_batch_clear(g_batch);
        common_batch_add(g_batch, new_token_id, start_pos + i, {0}, true);

        if (llama_decode(g_context, g_batch) != 0) {
            LOGE("llama_decode failed during generation");
            break;
        }

        if ((i + 1) % 5 == 0) {
            LOGI("Generated %d tokens so far", i + 1);
        }
    }

    LOGI("Generated response length: %zu", response.size());
    return env->NewStringUTF(response.c_str());
#else
    // Placeholder mode - return dummy response
    env->ReleaseStringUTFChars(prompt, prompt_str);
    std::string response = "This is a placeholder response. ";
    response += "To use the actual model, build with llama.cpp source. ";
    response += "Add llama.cpp as a git submodule to the android-app directory. ";
    response += "Your message was: ";
    response += prompt_str;
    return env->NewStringUTF(response.c_str());
#endif
}

JNIEXPORT jstring JNICALL
Java_com_ql_chat_LlamaEngine_nativeSystemInfo(
    JNIEnv *env,
    jobject thiz
) {
#if LLAMA_AVAILABLE
    return env->NewStringUTF(llama_print_system_info());
#else
    return env->NewStringUTF("Placeholder mode - llama.cpp not available");
#endif
}

} // extern "C"
