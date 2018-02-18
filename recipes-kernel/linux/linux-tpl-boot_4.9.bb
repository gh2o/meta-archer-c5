SUMMARY = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

inherit ccache
inherit kernel
inherit kernel-yocto

DEPENDS += "lzma-native"

LINUX_KERNEL_TYPE = "kexec"
KERNEL_FEATURES = ""
KMACHINE = "${MACHINE}"
KBRANCH = "linux-4.9.y"

SRC_URI = " \
    git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux-stable.git;name=kernel;branch=${KBRANCH} \
    git://github.com/openwrt/openwrt.git;name=openwrt;protocol=https;branch=master;destsuffix=openwrt \
    "

SRCREV_kernel = "3e598a7089eef1fe04d5b87cc154295302960e62"
SRCREV_openwrt = "f621b539512e5b6c83dd2de1bc2dba06e40cd7ea"
SRCREV_FORMAT = "kernel_openwrt"

LINUX_VERSION = "4.9.82"
PV = "${LINUX_VERSION}+git${SRCPV}"

MULTI_PROVIDER_WHITELIST = "virtual/kernel"
KERNEL_IMAGETYPE = "vmlinux.bin"
INITRAMFS_IMAGE = "tpl-boot-image"
INITRAMFS_IMAGE_BUNDLE = "1"

COMPATIBLE_MACHINE_archer-c5 = "archer-c5"
SRC_URI_append_archer-c5 = " file://archer-c5-kexec.scc "
LOAD_ADDRESS_archer-c5 = "0x80060000"

do_patch_append_archer-c5() {
    mkdir -p ${S}/patches
    rm -f ${S}/patches/series
    for x in \
        ${WORKDIR}/openwrt/target/linux/generic/backport-4.9/*.patch \
        ${WORKDIR}/openwrt/target/linux/generic/pending-4.9/*.patch \
        ${WORKDIR}/openwrt/target/linux/generic/hack-4.9/*.patch \
        ${WORKDIR}/openwrt/target/linux/ar71xx/patches-4.9/*.patch
    do
        cp $x ${S}/patches
        basename $x >> ${S}/patches/series
    done
    (cd ${S} && quilt push -a)
    cp -Tr ${WORKDIR}/openwrt/target/linux/generic/files ${S}
    cp -Tr ${WORKDIR}/openwrt/target/linux/ar71xx/files ${S}
}

do_tplink_image[dirs] = "${B}"
python do_tplink_image() {

    # compress vmlinux.bin via lzma
    bb.process.run(['lzmp', '-1fk', d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin.initramfs')])
    with open(d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin.initramfs.lzma'), 'rb') as fd:
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

addtask tplink_image after do_bundle_initramfs before do_deploy

# extra tasks
addtask kernel_version_sanity_check after do_kernel_metadata do_kernel_checkout before do_compile
addtask kernel_configcheck after do_configure before do_compile
addtask validate_branches before do_patch after do_kernel_checkout
