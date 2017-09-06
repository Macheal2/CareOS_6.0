
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := cappu-res
LOCAL_CERTIFICATE := platform

LOCAL_AAPT_FLAGS := -x3
LOCAL_NO_CAPPU_RES := true
LOCAL_MODULE_TAGS := optional

# Install this alongside the libraries.
LOCAL_MODULE_PATH := $(TARGET_OUT_JAVA_LIBRARIES)

# Create package-export.apk, which other packages can use to get
# PRODUCT-agnostic resource data like IDs and type definitions.
LOCAL_EXPORT_PACKAGE_RESOURCES := true

include $(BUILD_PACKAGE)

# define a global intermediate target that other module may depend on.
.PHONY: cappu-res-package-target
cappu-res-package-target: $(LOCAL_BUILT_MODULE)
