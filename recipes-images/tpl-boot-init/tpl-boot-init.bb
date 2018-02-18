DESCRIPTION = "/init for tpl-boot-image"
LICENSE = "BSD"
SECTION = "init"

LIC_FILES_CHKSUM = " "
SRC_URI = "file://init"

FILES_${PN} = "/init"

do_install() {
    install -m0755 ${WORKDIR}/init ${D}/init
}
