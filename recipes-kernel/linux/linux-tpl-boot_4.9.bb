require linux-tpl_4.9.inc

LINUX_KERNEL_TYPE = "kexec"
KERNEL_IMAGETYPE = "vmlinux.bin"

DEPENDS += "lzma-native"

KERNEL_FEATURES = ""
KERNEL_EXTRA_ARGS += "load-y=0xffffffff84060000"

INITRAMFS_IMAGE = "tpl-boot-image"
INITRAMFS_IMAGE_BUNDLE = "1"

do_tplink_image[dirs] = "${B}"
python do_tplink_image() {

    # compress vmlinux.bin via lzma
    bb.process.run(['lzmp', '-1fk', d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin.initramfs')])
    with open(d.expand('${KERNEL_OUTPUT_DIR}/vmlinux.bin.initramfs.lzma'), 'rb') as fd:
        vmlinux_lzma = fd.read()

    # generate output image
    load_addr = 0x84060000
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

# we don't generate any packages
PACKAGES = ""
PACKAGES_remove = "kernel-devicetree kernel-image-vmlinux.bin"
do_install[noexec] = "1"
