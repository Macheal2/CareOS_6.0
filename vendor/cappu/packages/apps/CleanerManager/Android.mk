LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_DEX_PREOPT := false
LOCAL_MULTILIB :=32

#下面这个选项是生成的apk 是放在system区域还是data区域/tests表示在data区域，optional在system区域
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
     cleanerapache \
     xutils
#    android-support-v4 \
#    glide3

    
LOCAL_MANIFEST_FILE := /app/src/main/AndroidManifest.xml

My_All_Files := $(shell find $(LOCAL_PATH)/app/src/main/java)
My_All_Files := $(My_All_Files:$(LOCAL_PATH)/./%=$(LOCAL_PATH)%)
MY_CPP_LIST  := $(filter %.java ,$(My_All_Files)) 
MY_CPP_LIST  := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)

LOCAL_SRC_FILES := $(MY_CPP_LIST) 

LOCAL_RESOURCE_DIR = $(LOCAL_PATH)/app/src/main/res

src_dirs := $(LOCAL_PATH)/app/src/main/java
res_dirs := res $(LOCAL_PATH)/app/src/main/res

# LOCAL_AAPT_FLAGS = -c hdpi

LOCAL_PACKAGE_NAME := CleanedrManager

#LOCAL_JAVA_LIBRARIES += telephony-common cappu-framework

LOCAL_PRIVILEGED_MODULE := true
#platform表示为为系统用户apk编译，shared表示为普通用户编译
LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := shared
#LOCAL_CERTIFICATE := PRESIGNED

#LOCAL_JNI_SHARED_LIBRARIES := libkookjni liblocSDK4d    
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags   # 混淆编译文件
LOCAL_MULTILIB :=32
include $(BUILD_PACKAGE)
  
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := cleanerapache:app/libs/org.apache.http.legacy.jar \
                                        xutils:app/libs/xutils-3.5.0.aar
# glide3:app/libs/glide-3.7.0.jar
include $(BUILD_MULTI_PREBUILT)
