LOCAL_PATH := $(call my-dir)

######## BUILD glhelper ############
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.c)
LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/*.cpp)

# 打印引入的C文件列表
$(warning $(LOCAL_SRC_FILES))

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_CFLAGS += -DNO_CRYPTO
LOCAL_MODULE := libglhelper
LOCAL_LDLIBS := -llog -lGLESv2
include $(BUILD_SHARED_LIBRARY)
######## END glhelper ############