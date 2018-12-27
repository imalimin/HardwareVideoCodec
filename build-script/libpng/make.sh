
#!/bin/bash
rm -rf product
rm -rf CMakeCache.txt
rm -rf CMakeFiles
rm -rf cmake_install.cmake
rm -rf Makefile
rm -rf CTestTestfile.cmake

ANDROID_NDK_HOME=/Users/lmy/Library/Android/android-ndk-r16b

ANDROID_ARMV5_CFLAGS="-march=armv5te"
ANDROID_ARMV7_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=neon"
ANDROID_ARMV8_CFLAGS="-march=armv8-a "
ANDROID_X86_CFLAGS="-march=i386 -mtune=intel -mssse3 -mfpmath=sse -m32"
ANDROID_X86_64_CFLAGS="-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel"

build(){
	ANDROID_ARCH_ABI=$1
    CFALGS="$2"

    PREFIX=$(pwd)/product/${ANDROID_ARCH_ABI}/

	echo "-------------- BUILD $ANDROID_ARCH_ABI ---------------"
	cmake -DDEBUG=NO -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK_HOME}/build/cmake/android.toolchain.cmake \
          -DANDROID_NDK=${ANDROID_NDK_HOME} \
          -DANDROID_ABI=${ANDROID_ARCH_ABI} \
          -DANDROID_TOOLCHAIN=clang \
          -DANDROID_PLATFORM=android-14 \
          -DCMAKE_BUILD_TYPE=Release \
          -DCMAKE_POSITION_INDEPENDENT_CODE=1 \
          -DCMAKE_INSTALL_PREFIX=${PREFIX} \
          -DANDROID_ARM_NEON=TRUE \
          -DCMAKE_C_FLAGS="${CFALGS} -Os -Wall -pipe -fPIC" \
          -DCMAKE_CXX_FLAGS="${CFALGS} -Os -Wall -pipe -fPIC" \
          -DANDROID_CPP_FEATURES=rtti exceptions \
          -DPNG_SHARED=OFF \
          -DPNG_TESTS=OFF \
        .  
	  

    make clean
    make
    make install
}

build armeabi-v7a "$ANDROID_ARMV7_CFLAGS"

build x86 "$ANDROID_X86_CFLAGS"

 
rm -rf CMakeCache.txt
rm -rf CMakeFiles
rm -rf cmake_install.cmake
rm -rf Makefile
rm -rf CTestTestfile.cmake