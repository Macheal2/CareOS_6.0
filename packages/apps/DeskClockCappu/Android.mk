LOCAL_PATH:= $(call my-dir)

#$(shell mkdir -p  $(PRODUCT_OUT)/system/app/DeskClock)
#ifeq ($(PRODUCT_AAPT_PREF_CONFIG), hdpi)
#$(shell cp -a $(LOCAL_PATH)/out/DeskClock/hdpi/DeskClock.apk  $(PRODUCT_OUT)/system/app/DeskClock/DeskClock.apk)
#else
#ifeq ($(PRODUCT_AAPT_PREF_CONFIG), xhdpi)
#$(shell cp -a $(LOCAL_PATH)/out/DeskClock/xhdpi/DeskClock.apk  $(PRODUCT_OUT)/system/app/DeskClock/DeskClock.apk)
#else
#$(shell cp -a $(LOCAL_PATH)/out/DeskClock/DeskClock.apk  $(PRODUCT_OUT)/system/app/DeskClock/DeskClock.apk)
#endif
#endif
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := framework \
        mediatek-framework \
        mediatek-common \
        cappu-framework
       
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.deskclock.ext

LOCAL_RENDERSCRIPT_TARGET_API := 18
LOCAL_RENDERSCRIPT_COMPATIBILITY := 18

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)

LOCAL_PACKAGE_NAME := DeskClockCappu

LOCAL_OVERRIDES_PACKAGES := AlarmClock

#LOCAL_SDK_VERSION := current
#LOCAL_SDK_VERSION := 14

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_MULTILIB := both

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
