LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(shell mkdir -p  $(PRODUCT_OUT)/system/lib64)
$(shell cp -a $(LOCAL_PATH)/lib64 $(PRODUCT_OUT)/system/)
# Module name should match apk name to be installed
LOCAL_MODULE := CappuWeather
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := PRESIGNED

include $(BUILD_PREBUILT)



