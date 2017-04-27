# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(TARGET_BUILD_APPS),)
support_library_root_dir := frameworks/support
else
support_library_root_dir := prebuilts/sdk/current/support
endif

LOCAL_MODULE_TAGS := eng
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v14-preference \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v4 \
    android-support-design \
    android-support-transition

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res \
    $(support_library_root_dir)/v7/appcompat/res \
    $(support_library_root_dir)/v7/preference/res \
    $(support_library_root_dir)/v7/recyclerview/res \
    $(support_library_root_dir)/v14/preference/res \
    $(support_library_root_dir)/v14/preference/res \
    $(support_library_root_dir)/design/res \
    $(support_library_root_dir)/transition/res

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.preference \
	--extra-packages android.support.v14.preference \
	--extra-packages android.support.v7.appcompat \
	--extra-packages android.support.v7.recyclerview \
	--extra-packages android.support.design \
	--extra-packages android.support.transition

LOCAL_PACKAGE_NAME := EmergencyInfo
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_JACK_COVERAGE_INCLUDE_FILTER := com.android.emergency.*

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
