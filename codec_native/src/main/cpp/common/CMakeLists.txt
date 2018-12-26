cmake_minimum_required(VERSION 3.4.1)
project(hwvcom)

set(PREBUILD_STATIC "${PROJECT_SOURCE_DIR}/prebuild/static/${ANDROID_ABI}")

include_directories(
        ${local_src}/include/libjpeg-turbo
)

if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
else ()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0")
endif ()

#------------------STATIC START----------------------
#libjpeg
add_library(
        libjpeg
        STATIC
        IMPORTED)
set_target_properties(
        libjpeg
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libjpeg.a")

#libturbojpeg
add_library(
        libturbojpeg
        STATIC
        IMPORTED)
set_target_properties(
        libturbojpeg
        PROPERTIES IMPORTED_LOCATION
        "${PREBUILD_STATIC}/libturbojpeg.a")

#------------------STATIC END----------------------

file(
        GLOB
        SOURCES
        ${PROJECT_SOURCE_DIR}/*.cpp
        ${PROJECT_SOURCE_DIR}/entity/*.cpp
        ${PROJECT_SOURCE_DIR}/thread/*.cpp
        ${PROJECT_SOURCE_DIR}/decoder/*.cpp
)
add_library(
        hwvcom
        SHARED
        ${SOURCES}
)

find_library(
        log-lib
        log)

target_link_libraries(
        hwvcom
        libjpeg
        libturbojpeg
        ${log-lib}
)