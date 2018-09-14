LOCAL_PATH := $(call my-dir)

######## BUILD glhelper ############
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/*.cpp)

# 打印引入的C文件列表
$(warning $(LOCAL_SRC_FILES))

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_CFLAGS := -D__cpusplus -g -mfloat-abi=softfp -mfpu=neon -march=armv7-a -mtune=cortex-a8 -DHAVE_NEON=1
endif
ifeq ($(TARGET_ARCH_ABI),$(filter $(TARGET_ARCH_ABI), armeabi-v7a x86))
LOCAL_ARM_NEON := true
endif

LOCAL_CFLAGS += -DNO_CRYPTO
LOCAL_MODULE := libglhelper
LOCAL_LDLIBS := -llog -lGLESv2
LOCAL_STATIC_LIBRARIES := cpufeatures

include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/cpufeatures)
######## END glhelper ############