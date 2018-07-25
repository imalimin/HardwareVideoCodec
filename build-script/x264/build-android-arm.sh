#!/bin/bash
NDK=/home/limingyi/android-ndk-r14b
PLATFORM=$NDK/platforms/android-19/arch-arm/
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
ARM_INC=$PLATFORM/usr/include/
ARM_LIB=$PLATFORM/usr/lib/
PREFIX=./android/arm

function build_one
{
  ./configure \
  --prefix=$PREFIX \
  --enable-pic \
  --enable-static \
  --enable-shared \
  --disable-lavf \
  --host=arm-linux \
  --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
  --sysroot=$PLATFORM \
  --extra-cflags="-fPIC -marm -DX264_VERSION -DANDROID -DHAVE_PTHREAD -DNDEBUG -static -D__ARM_ARCH_7__ -D__ARM_ARCH_7A__ -O3 -march=armv7-a -mfpu=neon -mtune=generic-armv7-a -mfloat-abi=softfp -ftree-vectorize -mvectorize-with-neon-quad -ffast-math" \
#  --extra-cflags=" -I$ARM_INC -fPIC -DANDROID -fpic -mthumb-interwork -ffunction-sections -funwind-tables -fstack-protector -fno-short-enums -march=armv7-a -mtune=cortex-a9 -mfloat-abi=softfp -mfpu=neon -D__ARM_ARCH_7__ -D__ARM_ARCH_7A__  -Wno-psabi -msoft-float -mthumb -Os -fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 -DANDROID  -Wa,--noexecstack -MMD -MP " \
#  --extra-ldflags="-nostdlib -Bdynamic -Wl,--no-undefined -Wl,-z,noexecstack  -Wl,-z,nocopyreloc -Wl,-soname,/system/lib/libz.so -Wl,-rpath-link=$ARM_LIB,-dynamic-linker=/system/bin/linker -L$ARM_LIB -nostdlib $ARM_LIB/crtbegin_dynamic.o $ARM_LIB/crtend_android.o -lc -lm -ldl -lgcc"

  make clean
  make -j4
  make install
}

build_one
