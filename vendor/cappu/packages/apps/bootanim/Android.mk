LOCAL_PATH := $(call my-dir)
$(shell mkdir -p  $(PRODUCT_OUT)/system)

$(shell cp -a $(LOCAL_PATH)/media $(PRODUCT_OUT)/system/)
