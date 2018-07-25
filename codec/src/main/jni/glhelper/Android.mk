LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
# allow missing dependencies
APP_ALLOW_MISSING_DEPS :=true
LOCAL_LDFLAGS += -fPIC
LOCAL_LDLIBS    := -lm -llog
# -g 后面的一系列附加项目添加了才能使用 arm_neon.h 头文件

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -mfloat-abi=softfp -mfpu=neon -march=armv7-a -mtune=cortex-a8
endif

LOCAL_MODULE := glhelper
LOCAL_SRC_FILES := com_lmy_codec_helper_GLHelper.c \

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    # 采用NEON优化技术
    LOCAL_ARM_NEON := true
endif

LOCAL_C_INCLUDES += $(LOCAL_PATH)/
LOCAL_LDLIBS := -llog -lz -ljnigraphics -landroid -lm -pthread -lGLESv2
include $(BUILD_SHARED_LIBRARY)