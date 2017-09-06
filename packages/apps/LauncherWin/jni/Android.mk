LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
#表示将要生成一个名称为libkook-jni.so的库文件
LOCAL_MODULE := kookjni

#表示生成库文件的源文件是kook-jni.c
LOCAL_SRC_FILES := kookjni.c 

                   
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog 

#user: 指该模块只在user版本下才编译
#eng: 指该模块只在eng版本下才编译
#tests: 指该模块只在tests版本下才编译
#optional:指该模块在所有版本下都编译
LOCAL_MODULE_TAGS := optional

LOCAL_PRELINK_MODULE := false

#表示会生成一个动态(BUILD_SHARED_LIBRARY),静态（BUILD_STATIC_LIBRARY）链接库，即so文件，生成的库文件名称为lib*.so
include $(BUILD_SHARED_LIBRARY)
