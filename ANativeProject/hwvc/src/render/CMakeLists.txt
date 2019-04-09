cmake_minimum_required(VERSION 3.4.1)
project(hwvc_render)
include_directories(
        ${PROJECT_SOURCE_DIR}/../common/include
        ${PROJECT_SOURCE_DIR}/../codec/include
        ${PROJECT_SOURCE_DIR}/../codec/include/ff
)
option(GL_DEBUG "Whether show opengl logs." ON)
if (GL_DEBUG)
    add_definitions(-DGL_DEBUG)
    message(GL_DEBUG "Show opengl logs。")
endif ()

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
        ${PROJECT_SOURCE_DIR}/*cpp
        ${PROJECT_SOURCE_DIR}/filter/*cpp
        ${PROJECT_SOURCE_DIR}/drawer/*cpp
        ${PROJECT_SOURCE_DIR}/texture/*cpp
        ${PROJECT_SOURCE_DIR}/egl/*cpp
        ${PROJECT_SOURCE_DIR}/units/*cpp
        ${PROJECT_SOURCE_DIR}/processor/*cpp
        ${PROJECT_SOURCE_DIR}/entity/*cpp
        ${PROJECT_SOURCE_DIR}/tinyxml/*cpp
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
        hwvc_codec
        # Links the target library to the log library
        # included in the NDK.
        GLESv2
        EGL
        android
        ${log-lib})