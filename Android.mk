LOCAL_PATH := $(call my-dir)

# LineageOS builds the kernel in-tree and exports its uapi headers under
# KERNEL_OBJ/usr. This device ships a prebuilt kernel, so stage the prebuilt
# headers at the same location for the CAF audio HALs that include from there.
# asm/ and asm-generic/ are dropped: they shadow the bionic arch headers and
# redefine types such as struct sigaction.
SAPPHIRE_KERNEL_HEADERS := $(LOCAL_PATH)-kernel/kernel-headers
SAPPHIRE_KERNEL_USR := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr

$(SAPPHIRE_KERNEL_USR): $(SAPPHIRE_KERNEL_HEADERS)/Makefile
	@echo "Staging prebuilt kernel headers: $@"
	rm -rf $@
	mkdir -p $@/include $@/techpack/audio
	cp -aR $(SAPPHIRE_KERNEL_HEADERS)/. $@/include/
	rm -rf $@/include/asm $@/include/asm-generic
	ln -sf ../../include $@/techpack/audio/include
	ln -sf . $@/include/audio
