
#LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := optional
#LOCAL_JAVA_LIBRARIES += magcomm-framework
#LOCAL_STATIC_JAVA_LIBRARIES := android-common \
#                    android-support-v4 
#LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_SDK_VERSION := current

#LOCAL_PACKAGE_NAME := Bootaudio

#LOCAL_CERTIFICATE := platform

#LOCAL_DEX_PREOPT := false

#LOCAL_MULTILIB :=32

#include $(BUILD_PACKAGE)
#include $(CLEAR_VARS)

#include $(BUILD_MULTI_PREBUILT)

#include $(call all-makefiles-under,$(LOCAL_PATH))






LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_DEX_PREOPT := false
LOCAL_MULTILIB :=32

#下面这个选项是生成的apk 是放在system区域还是data区域/tests表示在data区域，optional在system区域
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-common 

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Bootaudio
LOCAL_PRIVILEGED_MODULE := true
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
  
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
  
include $(BUILD_MULTI_PREBUILT)











