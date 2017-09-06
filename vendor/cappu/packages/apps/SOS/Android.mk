ifeq ($(strip $(CAPPU_SOS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)

##################################################
#PRODUCT_COPY_FILES += \
#	$(LOCAL_PATH)/libs/armeabi/libBaiduMapSDK_v2_4_2.so:system/lib/libBaiduMapSDK_v2_4_2.so \
#	$(LOCAL_PATH)/libs/armeabi/liblocSDK3.so:system/lib/liblocSDK3.so

$(shell mkdir -p  $(PRODUCT_OUT)/system/lib64)
$(shell cp -a $(LOCAL_PATH)/libs/arm64-v8a/* $(PRODUCT_OUT)/system/lib64)
$(shell mkdir -p  $(PRODUCT_OUT)/system/lib)
$(shell cp -a $(LOCAL_PATH)/libs/armeabi-v7a/* $(PRODUCT_OUT)/system/lib)

#$(shell mkdir -p  $(PRODUCT_OUT)/system/app/SOS/lib/arm)
#$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/libBaiduMapSDK_base_v4_4_0.so $(PRODUCT_OUT)/system/app/SOS/lib/arm)
#$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/libBaiduMapSDK_map_v4_4_0.so $(PRODUCT_OUT)/system/app/SOS/lib/arm)
#$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/liblocSDK7a.so $(PRODUCT_OUT)/system/app/SOS/lib/arm)

##################################################
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += cappu-framework
LOCAL_STATIC_JAVA_LIBRARIES := BaiduLBS_Android

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4

#LOCAL_JNI_SHARED_LIBRARIES := liblocSDK3 libBaiduMapSDK_v2_4_2

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := SOS

LOCAL_CERTIFICATE := platform

LOCAL_DEX_PREOPT := false


LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := BaiduLBS_Android:libs/BaiduLBS_Android.jar 

include $(BUILD_MULTI_PREBUILT)
##################################################

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
