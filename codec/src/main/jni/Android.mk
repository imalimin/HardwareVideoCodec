LOCAL_PATH := $(call my-dir)

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

include $(CLEAR_VARS)
LOCAL_LDFLAGS += -fPIC
LOCAL_LDLIBS    := -lm -llog
# -g 后面的一系列附加项目添加了才能使用 arm_neon.h 头文件

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_CFLAGS += -std=c99 -g -mfloat-abi=softfp -mfpu=neon -march=armv7-a -mtune=cortex-a8
endif

LOCAL_MODULE := codec
LOCAL_SRC_FILES =: com_lmy_codec_x264_X264Encoder.c
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
# 采用NEON优化技术
LOCAL_ARM_NEON := true
endif

LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_LDLIBS := -llog -lz -ljnigraphics -landroid -lm -pthread
LOCAL_SHARED_LIBRARIES := libyuv libx264
include $(BUILD_SHARED_LIBRARY)