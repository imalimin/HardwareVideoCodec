cmake_minimum_required(VERSION 3.4.1)
project(hwvc_codec)
set(PREBUILD_STATIC "${CMAKE_CURRENT_SOURCE_DIR}/prebuild/static/${ANDROID_ABI}")
set(PREBUILD_SHARED "${CMAKE_CURRENT_SOURCE_DIR}/prebuild/shared/${ANDROID_ABI}")
include_directories(
        ${PROJECT_SOURCE_DIR}/../common/include
        ${CMAKE_CURRENT_SOURCE_DIR}/include/x264
        ${CMAKE_CURRENT_SOURCE_DIR}/include/ff
)

if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
else ()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0")
endif ()
#------------------STATIC START----------------------
add_library(
        libx264
        STATIC
        IMPORTED)
set_target_properties(
        libx264
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libx264.a")

add_library(
        libavcodec
        STATIC
        IMPORTED)
set_target_properties(
        libavcodec
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libavcodec.a")

add_library(
        libavformat
        STATIC
        IMPORTED)
set_target_properties(
        libavformat
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libavformat.a")

add_library(
        libavresample
        STATIC
        IMPORTED)
set_target_properties(
        libavresample
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libavresample.a")

add_library(
        libavutil
        STATIC
        IMPORTED)
set_target_properties(
        libavutil
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libavutil.a")

#------------------STATIC END----------------------
add_library(
        hwvc_ffmpeg
        SHARED
        decoder/Decoder.cpp
        )
target_link_libraries(
        hwvc_ffmpeg
        libx264
        libavcodec
        libavformat
        libavresample
        libavutil)

file(
        GLOB
        SOURCES
        ${PROJECT_SOURCE_DIR}/*cpp
        ${PROJECT_SOURCE_DIR}/decoder/*cpp
)
add_library( # Sets the name of the library.
        hwvc_codec

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
        hwvc_codec
        hwvcom
        hwvc_ffmpeg
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})