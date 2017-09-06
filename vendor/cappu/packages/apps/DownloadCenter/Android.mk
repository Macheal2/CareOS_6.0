LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#下面这个选项是生成的apk 是放在system区域还是data区域/tests表示在data区域，optional在system区域
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-common locSDK_3.3 android-support-v4 apache_download

LOCAL_SRC_FILES := $(call all-java-files-under, src)
# LOCAL_AAPT_FLAGS = -c hdpi

LOCAL_PACKAGE_NAME := DownloadCenter

#platform表示为为系统用户apk编译，shared表示为普通用户编译
LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := shared
LOCAL_DEX_PREOPT := false

#LOCAL_OVERRIDES_PACKAGES := Home
LOCAL_JAVA_LIBRARIES := cappu-framework

##################################################

# 拷贝到系统区 begin
#PRODUCT_COPY_FILES += \
#	$(LOCAL_PATH)/libs/armeabi/liblocSDK3.so:system/lib/liblocSDK3.so	
# 拷贝到系统区 end

$(shell mkdir -p  $(PRODUCT_OUT)/system/app/DownloadCenter/lib/arm)
$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/liblocSDK3.so $(PRODUCT_OUT)/system/app/DownloadCenter/lib/arm)

# 打包到APK begin
#PRODUCT_COPY_FILES += \
#	$(LOCAL_PATH)/libs/armeabi/liblocSDK3.so:obj/lib/liblocSDK3.so
    
#LOCAL_JNI_SHARED_LIBRARIES := liblocSDK3     
# 打包到APK end

##################################################

include $(BUILD_PACKAGE)
  
include $(CLEAR_VARS) 
LOCAL_MODULE_TAGS := optional

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := launchernetwork:libs/launchernetwork.jar
  
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := locSDK_3.3:libs/locSDK_3.3.jar apache_download:libs/org.apache.http.legacy.jar
include $(BUILD_MULTI_PREBUILT)

