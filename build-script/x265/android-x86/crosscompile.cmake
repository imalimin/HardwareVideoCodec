# CMake toolchain file for cross compiling x265 for ARM arch
# This feature is only supported as experimental. Use with caution.
# Please report bugs on bitbucket
# Run cmake with: cmake -DCMAKE_TOOLCHAIN_FILE=crosscompile.cmake -G "Unix Makefiles" ../../source && ccmake ../../source

set(CROSS_COMPILE_ARM 0)
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR x86)

# specify the cross compiler
set(CMAKE_C_COMPILER /home/lmy/android/android-ndk-r14b/toolchains/my/x86-4.9/bin/arm-linux-androideabi-gcc)
set(CMAKE_CXX_COMPILER /home/lmy/android/android-ndk-r14b/toolchains/my/x86-4.9/bin/arm-linux-androideabi-g++)

# specify the target environment
SET(CMAKE_FIND_ROOT_PATH  /home/lmy/android/android-ndk-r14b/toolchains/my/x86-4.9/bin/)
