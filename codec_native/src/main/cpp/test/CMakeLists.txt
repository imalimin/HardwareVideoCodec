cmake_minimum_required(VERSION 3.4.1)
project(hwvc_test)
include_directories(
        ${PROJECT_SOURCE_DIR}/../common/include
        ${PROJECT_SOURCE_DIR}/../render/include
)

file(
        GLOB
        SOURCES
        ${PROJECT_SOURCE_DIR}/*cpp
)
add_library( # Sets the name of the library.
        hwvc_test

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
        hwvc_test
        hwvc_render
        hwvcom
        ${log-lib})