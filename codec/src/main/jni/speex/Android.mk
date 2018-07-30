LOCAL_PATH := $(call my-dir)

######## BUILD speex ############
# 有三个测试文件不用编译testenc_uwb.c、testenc_wb.c 、testenc.c
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/libspeex/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/libspeex/*.cpp)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/libspeex
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_MODULE := libspeex
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
######## END speex ############