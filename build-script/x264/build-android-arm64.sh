#!/bin/bash
NDK=/home/limingyi/android-ndk-r14b
PLATFORM=$NDK/platforms/android-21/arch-arm64/
TOOLCHAIN=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64
PREFIX=./android/arm64

function build_one
{
  ./configure \
  --prefix=$PREFIX \
  --enable-static \
  --enable-shared \
  --enable-pic \
  --host=aarch64-linux \
  --cross-prefix=$TOOLCHAIN/bin/aarch64-linux-android- \
  --sysroot=$PLATFORM \
  --extra-cflags="-fPIC -DX264_VERSION -DANDROID -DHAVE_PTHREAD -DNDEBUG -static -D__ARM_ARCH_8__ -D__ARM_ARCH_8A__ -O3 -march=armv8-a -mtune=cortex-a57.cortex-a53 -ftree-vectorize -ffast-math" \

  make clean
  make -j4
  make install
}

build_one
