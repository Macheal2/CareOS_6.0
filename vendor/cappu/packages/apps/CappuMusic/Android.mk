LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/cappu/music/IMediaPlaybackService.aidl \
    src/com/cappu/music/bluetooth/avrcp/IBTAvrcpMusic.aidl \
    src/com/cappu/music/bluetooth/avrcp/IBTAvrcpMusicCallback.aidl
#LOCAL_STATIC_JAVA_LIBRARIES := \
#    android-support-v4
    
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PACKAGE_NAME := CappuMusic

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current

LOCAL_OVERRIDES_PACKAGES := Music MusicFX
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))


