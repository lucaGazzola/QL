cmake_minimum_required(VERSION 3.22.1)
project("ql-chat")

# llama.cpp will be built from source or linked as prebuilt
# For now, we define the JNI bridge that will call into llama.cpp

add_library(ql-jni SHARED
    llama-jni.cpp
)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Link against llama.cpp (to be provided as prebuilt or built from source)
# target_link_libraries(ql-jni llama)

target_link_libraries(ql-jni
    android
    log
    ${JNI_LIBRARIES}
)
