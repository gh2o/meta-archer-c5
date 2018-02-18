DESCRIPTION = "Boot initramfs"
LICENSE = "BSD"
SECTION = "images"

inherit image
DEPENDS_remove = "qemuwrapper-cross depmodwrapper-cross"

PACKAGE_INSTALL = "busybox"
