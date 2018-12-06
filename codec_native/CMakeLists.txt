cmake_minimum_required(VERSION 3.4.1)

set(BUILD_PATH ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(SDL_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/sdl2)
set(CODEC_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/codec)

#include(${SDL_PATH}/cmake)
include_directories(${SDL_PATH}/include)

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
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )
target_link_libraries(codec
        sdl2::SDL2
        GLESv1_CM
        GLESv2
        ${log-lib} )

SET(CMAKE_BUILE_TYPE "RELEASE")
SET(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -O3 -Wall")
SET(CMAKE_BUILE_TYPE "DEBUG")
SET(CMAKE_CXX_FLAGS_DEBUG "-g -Wall")