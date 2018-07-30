#!/bin/bash
NDK=/home/limingyi/android-ndk-r14b
PLATFORM=$NDK/platforms/android-18/arch-x86/
TOOLCHAIN=$NDK/toolchains/x86-4.9/prebuilt/linux-x86_64
PREFIX=./android/x86

function build_one
{
  ./configure \
  --prefix=$PREFIX \
  --enable-pic \
  --enable-static \
  --enable-shared \
  --disable-asm \
  --host=i686-linux \
  --cross-prefix=$TOOLCHAIN/bin/i686-linux-android- \
  --sysroot=$PLATFORM \
  --extra-cflags="-fPIC -DX264_VERSION -DANDROID -DHAVE_PTHREAD -DNDEBUG -static -O3 -march=atom -mtune=atom -mssse3 -ffast-math -ftree-vectorize -mfpmath=sse" \
  --extra-asflags="-f elf -m x86 -DARCH_X86_64=0"

  make clean
  make -j4
  make install
}

build_one
