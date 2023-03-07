LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)
$(warning $(MY_LOCAL_PATH))

include $(CLEAR_VARS)

LOCAL_MODULE := fonttextclient

LOCAL_SRC_FILES := fonttextclient.cc

LOCAL_STATIC_LIBRARIES := ftext

LOCAL_CFLAGS += -std=gnu++11 -D USE_ANDROID_LOG
LOCAL_LDLIBS += -llog -landroid

include $(BUILD_SHARED_LIBRARY)

include $(MY_LOCAL_PATH)/cpp/front/Android.mk





