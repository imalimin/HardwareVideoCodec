cmake_minimum_required(VERSION 3.4.1)

set(BUILD_PATH ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(SDL_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/sdl2)
set(CODEC_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/codec)

if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0")
endif()

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

#SDL
file(
        GLOB
        SDL_SOURCES
        ${SDL_PATH}/src/*.c
        ${SDL_PATH}/src/audio/*.c
        ${SDL_PATH}/src/audio/android/*.c
        ${SDL_PATH}/src/audio/dummy/*.c
        ${SDL_PATH}/src/atomic/SDL_atomic.c
        ${SDL_PATH}/src/atomic/SDL_spinlock.c
        ${SDL_PATH}/src/core/android/*.c
        ${SDL_PATH}/src/cpuinfo/*.c
        ${SDL_PATH}/src/dynapi/*.c
        ${SDL_PATH}/src/events/*.c
        ${SDL_PATH}/src/file/*.c
        ${SDL_PATH}/src/haptic/*.c
        ${SDL_PATH}/src/haptic/dummy/*.c
        ${SDL_PATH}/src/joystick/*.c
        ${SDL_PATH}/src/joystick/android/*.c
        ${SDL_PATH}/src/loadso/dlopen/*.c
        ${SDL_PATH}/src/power/*.c
        ${SDL_PATH}/src/power/android/*.c
        ${SDL_PATH}/src/filesystem/dummy/*.c
        ${SDL_PATH}/src/render/*.c
        ${SDL_PATH}/src/render/*/*.c
        ${SDL_PATH}/src/stdlib/*.c
        ${SDL_PATH}/src/thread/*.c
        ${SDL_PATH}/src/thread/pthread/*.c
        ${SDL_PATH}/src/timer/*.c
        ${SDL_PATH}/src/timer/unix/*.c
        ${SDL_PATH}/src/video/*.c
        ${SDL_PATH}/src/video/android/*.c
)
add_library(
        SDL2
        SHARED
        ${SDL_SOURCES}
)
include_directories(
        ${SDL_PATH}/include
        ${SDL_PATH}/src/dynapi
)

target_link_libraries(SDL2 ${log-lib} GLESv1_CM GLESv2 EGL android dl)

#codec
set(source_files
        ${CODEC_PATH}/native-lib.cpp )
add_library( # Sets the name of the library.
        codec

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${source_files})
set_target_properties(codec PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY
        ${BUILD_PATH}/${ANDROID_ABI})
target_link_libraries(codec
        SDL2
        GLESv1_CM
        GLESv2
        ${log-lib} )

SET(CMAKE_BUILE_TYPE "RELEASE")
SET(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -O3 -Wall")
SET(CMAKE_BUILE_TYPE "DEBUG")
SET(CMAKE_CXX_FLAGS_DEBUG "-g -Wall")