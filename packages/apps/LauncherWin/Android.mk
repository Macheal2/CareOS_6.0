LOCAL_PATH := $(call my-dir)

#include $(LOCAL_PATH)/KookLib/Android.mk
include $(CLEAR_VARS)

#执行某个shell文件脚本
    #$(info $(shell ($(LOCAL_PATH)/unpack.sh)))
#$(info $(shell python $(LOCAL_PATH)/configManiXML.py start))
#$(info $(shell echo "--------------start--------------"))

#Android.mk 判断文件是否存在，若存在则复制该文件到某个目录
#$(shell test -f [文件] && echo yes)的值如果是yes, 则文件存在，然后进行shell cp 动作
#如下脚本作用是拷贝客置化的文件到out目录下面去
    # HAVE_TEST_CUST_FILE := $(shell test -f vendor/huaqin/resource/$(HQ_PROJECT)_$(HQ_CLIENT)/$(LOCAL_PATH)/DroidSansFallback.ttf && echo yes)  
    #ifeq ($(HAVE_TEST_CUST_FILE),yes)  
    #    $(shell cp -f vendor/huaqin/resource/$(HQ_PROJECT)_$(HQ_CLIENT)/$(LOCAL_PATH)/DroidSansFallback.ttf $(PRODUCT_OUT)/system/fonts/DroidSansFallback.ttf)  
    #endif  



#PRODUCT_COPY_FILES += \
#       $(LOCAL_PATH)/libs/armeabi/libkookjni.so:/obj/lib/libkookjni.so \
#       $(LOCAL_PATH)/libs/armeabi/libkookjni.so:/system/lib/libkookjni.so \
#       $(LOCAL_PATH)/libs/armeabi/liblocSDK4d.so:/obj/lib/liblocSDK4d.so \
#       $(LOCAL_PATH)/libs/armeabi/liblocSDK4d.so:/system/lib/liblocSDK4d.so \

#$(shell mkdir -p  $(PRODUCT_OUT)/system/lib/)   
#$(shell cp -a $(LOCAL_PATH)/libs/armeabi/libkookjni.so $(PRODUCT_OUT)/obj/lib/libkookjni.so)
#$(shell cp -a $(LOCAL_PATH)/libs/armeabi/libkookjni.so $(PRODUCT_OUT)/system/lib/libkookjni.so)
#$(shell cp -a $(LOCAL_PATH)/libs/armeabi/liblocSDK4d.so $(PRODUCT_OUT)/obj/lib/liblocSDK4d.so)
#$(shell cp -a $(LOCAL_PATH)/libs/armeabi/liblocSDK4d.so $(PRODUCT_OUT)/system/lib/liblocSDK4d.so)
       
$(shell mkdir -p  $(PRODUCT_OUT)/system/priv-app/LauncherWin/lib/arm)
$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/libkookjni.so $(PRODUCT_OUT)/system/priv-app/LauncherWin/lib/arm)
$(shell cp  -af  $(LOCAL_PATH)/libs/armeabi/liblocSDK6a.so $(PRODUCT_OUT)/system/priv-app/LauncherWin/lib/arm)

#$(shell cp -a $(LOCAL_PATH)/libs/armeabi/libkookjni.so $(PRODUCT_OUT)/generic/system/lib/libkookjni.so)
#android 5.0 +  copy so file
#$(shell mkdir -p  $(PRODUCT_OUT)/system/app/LauncherWin)
#$(shell cp -a $(LOCAL_PATH)/libs $(PRODUCT_OUT)/system/app/LauncherWin/)

LOCAL_DEX_PREOPT := false
LOCAL_MULTILIB :=32

#下面这个选项是生成的apk 是放在system区域还是data区域/tests表示在data区域，optional在system区域
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-common \
    cappuspeech \
    jsoup \
    kookexception \
    libphonenumber-7.0 \
    Location \
    android-support-v4_launcher \
    libammsdk

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/cappu/music/IMediaPlaybackService.aidl 
    
# LOCAL_AAPT_FLAGS = -c hdpi

LOCAL_PACKAGE_NAME := LauncherWin

LOCAL_JAVA_LIBRARIES += telephony-common cappu-framework

LOCAL_PRIVILEGED_MODULE := true
#platform表示为为系统用户apk编译，shared表示为普通用户编译
LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := shared
#LOCAL_CERTIFICATE := PRESIGNED

LOCAL_OVERRIDES_PACKAGES := Home

#LOCAL_JNI_SHARED_LIBRARIES := libkookjni liblocSDK4d
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
  
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := jsoup:libs/jsoup-1.8.1.jar \
    libphonenumber-7.0:libs/libphonenumber-7.0.jar \
    Location:libs/locSDK_6.11.jar \
    kookexception:libs/kookexception.jar \
    android-support-v4_launcher:libs/android-support-v4.jar \
    libammsdk:libs/libammsdk.jar   

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))

#$(info $(shell python $(LOCAL_PATH)/configManiXML.py end))
#$(info $(shell echo "--------------end--------------"))
