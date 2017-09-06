
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
#LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES:= \
  sim_android_mtkcit_cittools_CITJNI.c
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	mediatek/external/sensor-tools

 LOCAL_SHARED_LIBRARIES	:= \
 libdl  \
 libutils   \
 libcutils  \
  libfile_op \
 libnvram \
 libhwm	
 	
LOCAL_MODULE:= libcitjni

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
