
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := SpeechSettings
LOCAL_MODULE_TAGS := optional
#platform表示为为系统用户apk编译，shared表示为普通用户编译
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES := cappu-framework
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4 cappuspeech
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := cappuspeech:lib/SpeechApi.jar
include $(BUILD_MULTI_PREBUILT)
