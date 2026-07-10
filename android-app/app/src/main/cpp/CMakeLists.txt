cmake_minimum_required(VERSION 3.22.1)
project("ql-chat" VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# Force 16KB page alignment for all shared libraries (required for Android 15+)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# --------------------------------------------------------------------------
# llama.cpp integration
# --------------------------------------------------------------------------

# Path to llama.cpp source (will be added as git submodule)
set(LLAMA_SRC ${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp)

# Check if llama.cpp exists, otherwise use placeholder
if(EXISTS "${LLAMA_SRC}/CMakeLists.txt")
    message(STATUS "Found llama.cpp at: ${LLAMA_SRC}")

    # Android-specific settings
    if(DEFINED ANDROID_ABI)
        message(STATUS "Detected Android ABI: ${ANDROID_ABI}")
        if(ANDROID_ABI STREQUAL "arm64-v8a")
            set(GGML_SYSTEM_ARCH "ARM")
            set(GGML_CPU_KLEIDIAI ON)
            set(GGML_OPENMP ON)
        elseif(ANDROID_ABI STREQUAL "x86_64")
            set(GGML_SYSTEM_ARCH "x86")
            set(GGML_CPU_KLEIDIAI OFF)
            set(GGML_OPENMP OFF)
        else()
            message(FATAL_ERROR "Unsupported ABI: ${ANDROID_ABI}")
        endif()
    endif()

    add_subdirectory(${LLAMA_SRC} build-llama)

    add_library(ql-jni SHARED llama-jni.cpp)

    target_compile_definitions(ql-jni PRIVATE
        GGML_SYSTEM_ARCH=${GGML_SYSTEM_ARCH}
        GGML_CPU_KLEIDIAI=$<BOOL:${GGML_CPU_KLEIDIAI}>
        GGML_OPENMP=$<BOOL:${GGML_OPENMP}>
    )

    target_include_directories(ql-jni PRIVATE
        ${LLAMA_SRC}
        ${LLAMA_SRC}/common
        ${LLAMA_SRC}/include
        ${LLAMA_SRC}/ggml/include
        ${LLAMA_SRC}/ggml/src
    )

    target_link_libraries(ql-jni
        llama
        llama-common
        android
        log
    )
else()
    message(WARNING "llama.cpp not found at ${LLAMA_SRC}, building with placeholder")
    # Placeholder build for development
    add_library(ql-jni SHARED llama-jni.cpp)
    target_link_libraries(ql-jni android log)
endif()
