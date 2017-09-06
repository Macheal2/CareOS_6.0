LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(shell mkdir -p  $(PRODUCT_OUT)/system/app/IShare)

# Module name should match apk name to be installed
LOCAL_MODULE := IShare
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := IShare.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MULTILIB :=32
LOCAL_CERTIFICATE := PRESIGNED

include $(BUILD_PREBUILT)



