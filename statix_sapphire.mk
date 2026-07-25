#
# Copyright (C) 2024 The LineageOS Project
# Copyright (C) 2026 StatiXOS
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from sapphire device
$(call inherit-product, device/xiaomi/sapphire/device.mk)

# Inherit some common StatiX stuff.
$(call inherit-product, vendor/statix/config/common.mk)
$(call inherit-product, vendor/statix/config/gsm.mk)

# Register the qcom-caf project paths.
#
# The CAF Android.mk files resolve their cross-project include paths through
# $(call project-path-for,qcom-audio) and friends.  StatiX ships the pathmap
# helpers in vendor/statix/build/core/pathmap.mk but, unlike vendor/lineage,
# never populates the map -- so those lookups expand to an empty string and the
# resulting "/agm/ipc/..." paths are rejected as outside the source tree.
#
# This has to run after common.mk (which pulls in pathmap.mk and defines
# project-set-path) and cannot live in BoardConfig.mk, since board config is
# evaluated after product config.  The platform is spelled out rather than
# taken from QCOM_HARDWARE_VARIANT for the same reason: BoardConfigQcom.mk has
# not run yet at this point.
QCOM_HARDWARE_PATH := hardware/qcom-caf/sm6225
$(call project-set-path,qcom-audio,$(QCOM_HARDWARE_PATH)/audio)
$(call project-set-path,qcom-display,$(QCOM_HARDWARE_PATH)/display)
$(call project-set-path,qcom-media,$(QCOM_HARDWARE_PATH)/media)

# StatiX build type
STATIX_BUILD_TYPE := UNOFFICIAL

# Device configs
TARGET_HAS_UDFPS := true

PRODUCT_NAME := statix_sapphire
PRODUCT_DEVICE := sapphire
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BRAND := Redmi
PRODUCT_MODEL := Redmi Note 13

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="sapphire_global-user 15 AQ3A.240829.003 OS2.0.210.0.VNGMIXM release-keys" \
    BuildFingerprint=Redmi/sapphire_global/sapphire:15/AQ3A.240829.003/OS2.0.210.0.VNGMIXM:user/release-keys
