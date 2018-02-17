FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

COMPATIBLE_MACHINE_archer-c5 = "archer-c5"

SRC_URI_append_archer-c5 = " file://archer-c5.scc "

do_tplink_image() {
}

do_tplink_image_tpl-boot() {
    false 2
}

addtask tplink_image after do_compile before do_deploy
