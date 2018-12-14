cmake_minimum_required(VERSION 3.4.1)
project(sdl2)

file(
        GLOB
        SDL_SOURCES
        src/*.c
        src/audio/*.c
        src/audio/android/*.c
        src/audio/dummy/*.c
        src/atomic/SDL_atomic.c
        src/atomic/SDL_spinlock.c
        src/core/android/*.c
        src/cpuinfo/*.c
        src/dynapi/*.c
        src/events/*.c
        src/file/*.c
        src/haptic/*.c
        src/haptic/dummy/*.c
        src/joystick/*.c
        src/joystick/android/*.c
        src/loadso/dlopen/*.c
        src/power/*.c
        src/power/android/*.c
        src/filesystem/dummy/*.c
        src/render/*.c
        src/render/*/*.c
        src/stdlib/*.c
        src/thread/*.c
        src/thread/pthread/*.c
        src/timer/*.c
        src/timer/unix/*.c
        src/video/*.c
        src/video/android/*.c
)
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )
add_library(
        sdl2
        SHARED
        ${SDL_SOURCES}
)
target_link_libraries(SDL2 ${log-lib} GLESv1_CM GLESv2 EGL android dl)