LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE:= MNN
LOCAL_SRC_FILES:= ../../../obj/local/arm64-v8a/libMNN.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := ftext
LOCAL_SRC_FILES := front_interface.cc text_normalize.cc base/type_conv.cc G2pEModel.cc

LOCAL_CFLAGS += -std=gnu++11
LOCAL_LDLIBS += -llog
$(warning $(LOCAL_CFLAGS))

include $(BUILD_STATIC_LIBRARY)



