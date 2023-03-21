LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)
$(warning $(MY_LOCAL_PATH))

include $(CLEAR_VARS)

LOCAL_MODULE := fonttextclient

LOCAL_SRC_FILES := fonttextclient.cc

LOCAL_STATIC_LIBRARIES := ftext MNN

LOCAL_CFLAGS += -std=gnu++11 -D USE_ANDROID_LOG
LOCAL_LDLIBS += -llog -landroid


include $(BUILD_SHARED_LIBRARY)

#LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#LOCAL_MODULE := paddle_lite_jni
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_SRC_FILES := $(MY_LOCAL_PATH)/libpaddle_lite_jni.so
#include $(BUILD_PREBUILT)




include $(MY_LOCAL_PATH)/cpp/front/Android.mk








