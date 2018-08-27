LOCAL_PATH := $(call my-dir)

######## BUILD speexdsp ############
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/libspeexdsp/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/libspeexdsp/*.cpp)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/libspeexdsp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_MODULE := libspeexdsp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
######## END speexdsp ############