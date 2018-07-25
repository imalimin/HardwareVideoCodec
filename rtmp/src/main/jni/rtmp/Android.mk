LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
# fix undefined reference to bug
# LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

MY_CPP_LIST := $(wildcard $(LOCAL_PATH)/librtmp/*.c)
MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/*.c)
MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/*.cpp)

# LOCAL_SRC_FILES := $(MY_CPP_LIST)
LOCAL_SRC_FILES := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)

# 打印引入的C文件列表
$(warning $(LOCAL_SRC_FILES))

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/librtmp
LOCAL_CFLAGS += -DNO_CRYPTO
LOCAL_MODULE := librtmp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)