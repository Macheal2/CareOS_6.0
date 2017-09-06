#
# Copyright (C) 2014 MediaTek Inc.
# Modification based on code covered by the mentioned copyright
# and/or permission notice(s).
#
# Copyright 2007-2008 The Android Open Source Project 

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
# Include res dir from chips
chips_dir := ../../../../../../frameworks/ex/chips/res
contacts_common_dir := ../../../../../../packages/apps/ContactsCommon
contacts_ext_dir := ../../../../../../packages/apps/ContactsCommon/ext
phone_common_dir := ../../../../../../packages/apps/PhoneCommon

src_dirs := src \
    $(phone_common_dir)/src \
    $(contacts_common_dir)/src \
    $(contacts_ext_dir)/src

res_dirs := res \
    $(phone_common_dir)/res \
    $(contacts_common_dir)/res \
    $(contacts_common_dir)/res_ext \
    $(chips_dir)

$(shell rm -f $(LOCAL_PATH)/chips)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_PACKAGE_NAME := MtkMms
LOCAL_OVERRIDES_PACKAGES := Mms
LOCAL_DEX_PREOPT := false

# Builds against the public SDK
#LOCAL_SDK_VERSION := current

LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_JAVA_LIBRARIES += org.apache.http.legacy.boot
LOCAL_JAVA_LIBRARIES += voip-common
LOCAL_JAVA_LIBRARIES += ims-common
LOCAL_JAVA_LIBRARIES += cappu-framework
LOCAL_STATIC_JAVA_LIBRARIES += android-common jsr305
LOCAL_STATIC_JAVA_LIBRARIES += libchips
LOCAL_STATIC_JAVA_LIBRARIES += android-common-chips
LOCAL_STATIC_JAVA_LIBRARIES += com.android.vcard
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.mms.ext
LOCAL_STATIC_JAVA_LIBRARIES += cappuspeech \
	android-support-v4_even
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.mms.callback \
    com.android.services.telephony.common \
    guava \
    android-support-v13 \
    android-support-v4 \
    libphonenumber


LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.phone.common
LOCAL_AAPT_FLAGS += --extra-packages com.android.mtkex.chips
LOCAL_AAPT_FLAGS += --extra-packages com.android.contacts.common

LOCAL_REQUIRED_MODULES := SoundRecorder
ifeq ($(strip $(MTK_RCS_SUPPORT)),yes)
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
else
#LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
endif

LOCAL_PRIVILEGED_MODULE := true
LOCAL_MULTILIB :=32
include $(BUILD_PACKAGE)
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v4_even:libs/android-support-v4.jar 

include $(BUILD_MULTI_PREBUILT)
# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
