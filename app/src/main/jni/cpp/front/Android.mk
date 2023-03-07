LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ftext
LOCAL_SRC_FILES := front_interface.cc text_normalize.cc base/type_conv.cc

LOCAL_CFLAGS += -std=gnu++11
LOCAL_LDLIBS += -llog
$(warning $(LOCAL_CFLAGS))

include $(BUILD_STATIC_LIBRARY)

