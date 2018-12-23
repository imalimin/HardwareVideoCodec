cmake_minimum_required(VERSION 3.4.1)
project(hwvc_render)
include_directories(
        ${PROJECT_SOURCE_DIR}/../common/include
)

if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
else ()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0")
endif ()
file(
        GLOB
        SOURCES
        *cpp
        filter/*cpp
)
add_library( # Sets the name of the library.
        hwvc_render

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
        hwvc_render
        hwvcom
        # Links the target library to the log library
        # included in the NDK.
        GLESv2
        ${log-lib})