cmake_minimum_required(VERSION 3.4.1)
project(hwvc_native)
include_directories(
        ${PROJECT_SOURCE_DIR}/../common/include
        ${PROJECT_SOURCE_DIR}/../render/include
)

file(
        GLOB
        SOURCES
        ${PROJECT_SOURCE_DIR}/*cpp
        ${PROJECT_SOURCE_DIR}/filter/*cpp
        ${PROJECT_SOURCE_DIR}/processor/*cpp
)
add_library( # Sets the name of the library.
        hwvc_native

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SOURCES}
        )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        hwvc_native
        hwvc_render
        hwvcom
        android
        ${log-lib})