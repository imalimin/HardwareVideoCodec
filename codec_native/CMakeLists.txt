cmake_minimum_required(VERSION 3.4.1)
set(CPP_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp)
add_subdirectory(${CMAKE_SOURCE_DIR}/../hwvc hwvc.out)
add_subdirectory(${CPP_PATH}/native)