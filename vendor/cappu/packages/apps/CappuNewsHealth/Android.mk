LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES :=apache_download:libs/org.apache.http.legacy.jar

LOCAL_STATIC_JAVA_LIBRARIES := apache_download 
LOCAL_PACKAGE_NAME := CappuNewsHealth

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))


