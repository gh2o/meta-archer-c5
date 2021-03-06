DESCRIPTION = "Boot initramfs"
LICENSE = "BSD"
SECTION = "images"

inherit image
DEPENDS_remove = "qemuwrapper-cross depmodwrapper-cross"
KERNELDEPMODDEPEND = ""

USE_DEVFS := "0"
IMAGE_DEVICE_TABLES := "${THISDIR}/tpl-boot-image-devtable.txt"
PACKAGE_INSTALL = "base-files kexec-tools tpl-boot-init busybox"
