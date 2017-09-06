LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) 
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13_file \
    kookutil

LOCAL_PACKAGE_NAME := FileExplorer

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v13_file:libs/android-support-v13.jar \
    kookutil:libs/kookutil.jar

# Use the folloing include to make our test apk.
include $(BUILD_MULTI_PREBUILT)
#include $(call all-makefiles-under,$(LOCAL_PATH))









