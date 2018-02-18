SUMMARY = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

inherit kernel
inherit kernel-yocto

DEPENDS += "lzma-native"

LINUX_KERNEL_TYPE = "kexec"
KERNEL_FEATURES = ""
KMACHINE = "${MACHINE}"
KBRANCH = "standard/base"
KMETA = "meta"

SRC_URI = " \
    git://git.yoctoproject.org/linux-yocto-4.9.git;name=machine;branch=${KBRANCH} \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-4.9;destsuffix=${KMETA} \
    "

SRCREV_machine = "f7a6d45fff853173bfbf61706aeffcd1d1e99467"
SRCREV_meta = "ef2f5d9a0ac1c5ac60e76b18b0bb3393be450336"
SRCREV_FORMAT = "meta_machine"

LINUX_VERSION = "4.9.78"
PV = "${LINUX_VERSION}+git${SRCPV}"

MULTI_PROVIDER_WHITELIST = "virtual/kernel"
KERNEL_IMAGETYPE = "vmlinux.bin"

COMPATIBLE_MACHINE_archer-c5 = "archer-c5"
SRC_URI_append_archer-c5 = " file://archer-c5-kexec.scc "
LOAD_ADDRESS_archer-c5 = "0x80060000"

do_tplink_image[dirs] = "${B}"
python do_tplink_image() {

    # compress vmlinux.bin via lzma
    bb.process.run(['lzmp', '-fk', d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin')])
    with open(d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin.lzma'), 'rb') as fd:
        vmlinux_lzma = fd.read()

    # generate output image
    load_addr = 0x80060000
    out_image = bytearray(0x200)
    out_image.extend(vmlinux_lzma)
    def write_word(offset, val):
        out_image[offset:offset+4] = val.to_bytes(4, 'big')
    write_word(0x74, load_addr)
    write_word(0x78, load_addr)
    write_word(0x84, len(vmlinux_lzma))

    # write output image
    with open(d.expand('${KERNEL_OUTPUT_DIR}/tplImage'), 'wb') as fd:
        fd.write(out_image)
}

do_deploy_append() {
    install -m0644 ${KERNEL_OUTPUT_DIR}/tplImage ${DEPLOYDIR}/tplImage-${KERNEL_IMAGE_BASE_NAME}
    ln -sf tplImage-${KERNEL_IMAGE_BASE_NAME} ${DEPLOYDIR}/tplImage
}

addtask tplink_image after do_compile before do_deploy

# extra tasks
addtask kernel_version_sanity_check after do_kernel_metadata do_kernel_checkout before do_compile
addtask kernel_configcheck after do_configure before do_compile
addtask validate_branches before do_patch after do_kernel_checkout
