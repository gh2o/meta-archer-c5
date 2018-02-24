SUMMARY = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

inherit ccache
inherit kernel
inherit kernel-yocto

S = "${WORKDIR}/source"
STAGING_KERNEL_DIR = "${S}"

KMACHINE = "${MACHINE}"
KBRANCH = "linux-4.9.y"

SRC_URI = " \
    git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux-stable.git;name=kernel;branch=${KBRANCH} \
    git://github.com/openwrt/openwrt.git;name=openwrt;protocol=https;branch=master;destsuffix=openwrt \
    file://MIPS-Reset-C0-STATUS-IM.patch \
    file://MIPS-UASM-grow-labels-relocs.patch \
    "

SRCREV_kernel = "3e598a7089eef1fe04d5b87cc154295302960e62"
SRCREV_openwrt = "f621b539512e5b6c83dd2de1bc2dba06e40cd7ea"
SRCREV_FORMAT = "kernel+openwrt"

LINUX_VERSION = "4.9.82"
PV = "${LINUX_VERSION}+g0+git${SRCPV}"

# quilt is too slow
PATCHTOOL = "patch"

COMPATIBLE_MACHINE_archer-c5 = "archer-c5"
SRC_URI_append_archer-c5 = " file://archer-c5-${LINUX_KERNEL_TYPE}.scc "
OPENWRT_FILES_DIRS_archer-c5 = " \
    openwrt/target/linux/generic/files \
    openwrt/target/linux/ar71xx/files \
    "
OPENWRT_PATCH_DIRS_archer-c5 = " \
    openwrt/target/linux/generic/backport-4.9 \
    openwrt/target/linux/generic/pending-4.9 \
    openwrt/target/linux/generic/hack-4.9 \
    openwrt/target/linux/ar71xx/patches-4.9 \
    "

python do_patch() {
    from glob import glob
    workdir = d.getVar('WORKDIR')
    s = d.getVar('S')
    for dir in d.getVar('OPENWRT_FILES_DIRS').split():
        bb.process.run(['cp', '-Tr', os.path.join(workdir, dir), s])
    patches = []
    for dir in d.getVar('OPENWRT_PATCH_DIRS').split():
        patches.extend(glob(os.path.join(workdir, dir, '*.patch')))
    d.appendVar('SRC_URI', ' ' + ' '.join('file://' + p for p in patches))
    bb.build.exec_func('patch_do_patch', d)
}

# extra tasks
addtask kernel_version_sanity_check after do_kernel_metadata do_kernel_checkout before do_compile
addtask kernel_configcheck after do_configure before do_compile
addtask validate_branches before do_patch after do_kernel_checkout
