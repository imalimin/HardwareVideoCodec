LOCAL_PATH := $(call my-dir)
# include $(call all-subdir-makefiles)

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    include $(CLEAR_VARS)
    LOCAL_MODULE := libyuv
    LOCAL_SRC_FILES := lib/armeabi-v7a/libyuv.so
    include $(PREBUILT_SHARED_LIBRARY)

    include $(CLEAR_VARS)
    LOCAL_MODULE := libx264
    LOCAL_SRC_FILES := lib/armeabi-v7a/libx264.so
    include $(PREBUILT_SHARED_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),$(filter $(TARGET_ARCH_ABI), x86 x86_64))
    include $(CLEAR_VARS)
    LOCAL_MODULE := libyuv
    LOCAL_SRC_FILES := lib/x86/libyuv.so
    include $(PREBUILT_SHARED_LIBRARY)

    include $(CLEAR_VARS)
    LOCAL_MODULE := libx264
    LOCAL_SRC_FILES := lib/x86/libx264.so
    include $(PREBUILT_SHARED_LIBRARY)
endif

######## BUILD glhelper ############
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/glhelper/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/glhelper/*.cpp)

# 打印引入的C文件列表
$(warning $(LOCAL_SRC_FILES))

LOCAL_C_INCLUDES := $(LOCAL_PATH)/glhelper
LOCAL_CFLAGS += -DNO_CRYPTO
LOCAL_MODULE := libglhelper
LOCAL_LDLIBS := -llog -lGLESv2
include $(BUILD_SHARED_LIBRARY)
######## END glhelper ############

######## BUILD speex ############
# 有三个测试文件不用编译testenc_uwb.c、testenc_wb.c 、testenc.c
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/speex/libspeex/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/speex/libspeex/*.cpp)

# 打印引入的C文件列表
$(warning $(LOCAL_SRC_FILES))

LOCAL_C_INCLUDES := $(LOCAL_PATH)/speex/libspeex
LOCAL_C_INCLUDES += $(LOCAL_PATH)/speex/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/speex/speex
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_MODULE := libspeex
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
######## END speex ############

######## BUILD codec ############
include $(CLEAR_VARS)
# allow missing dependencies
APP_ALLOW_MISSING_DEPS :=true
LOCAL_LDFLAGS += -fPIC
LOCAL_LDLIBS    := -lm -llog
# -g 后面的一系列附加项目添加了才能使用 arm_neon.h 头文件

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -mfloat-abi=softfp -mfpu=neon -march=armv7-a -mtune=cortex-a8
endif

# ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
#     LOCAL_CFLAGS += -std=c99 -g -mfloat-abi=softfp -mfpu=neon -march=armv8-a -mtune=cortex-a53
# endif

LOCAL_MODULE := codec
LOCAL_SRC_FILES := Java_com_lmy_codec_x264_X264Encoder.cpp \
    X264Encoder.cpp \

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    # 采用NEON优化技术
    LOCAL_ARM_NEON := true
endif
# ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
#     LOCAL_CFLAGS += -DHAVE_NEON -DX265_ARCH_ARM
#     LOCAL_ARM_NEON := true
# endif

LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_LDLIBS := -llog -lz -ljnigraphics -landroid -lm -pthread
LOCAL_SHARED_LIBRARIES := libyuv libx264 libglhelper libspeex
include $(BUILD_SHARED_LIBRARY)
######## END codec ############