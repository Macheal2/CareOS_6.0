LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(shell mkdir -p  $(PRODUCT_OUT)/system/app/CaiYiDaDao)
$(shell cp -a $(LOCAL_PATH)/lib $(PRODUCT_OUT)/system/app/CaiYiDaDao/)
# Module name should match apk name to be installed
LOCAL_MODULE := CaiYiDaDao
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := CaiYiDaDao.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MULTILIB :=32
LOCAL_CERTIFICATE := PRESIGNED

include $(BUILD_PREBUILT)



