# Copyright (C) 2018 Gavin Li <gavinli@thegavinli.com>
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "OpenWRT swconfig"
SECTION = "bsp"
LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://swlib.c;beginline=6;endline=13;md5=1c4718c95e3c271867207e80c073fff9"

DEPENDS += "libnl"

SRC_URI = "git://github.com/openwrt/openwrt.git;name=openwrt;protocol=https;branch=master;destsuffix=openwrt"
SRCREV_openwrt = "f621b539512e5b6c83dd2de1bc2dba06e40cd7ea"

S = "${WORKDIR}/openwrt/package/network/config/swconfig/src"
B = "${S}"

CLEANBROKEN = "1"
CFLAGS += "-Wall"
CFLAGS += "-I. -I${STAGING_DIR_HOST}/usr/include/libnl3 -include stdbool.h -include ctype.h"
LDFLAGS += "-static"

do_compile() {
    cat > uci.h <<EOF
#pragma once
#define UCI_TYPE_STRING 1
#define uci_foreach_element(a, b) for(;0;)
#define uci_to_option(e) ((void)e, (void *)0)
#define uci_to_section(e) ((void)e, (void *)0)
#define uci_alloc_context(e) ((void *)0)
#define uci_free_context(e) ((void)0)
#define uci_load(a,b,c) ((void)a, (void)b, (void)c)
#define uci_perror(e,msg) puts(msg "UCI Failure")
struct uci_element { char *name; };
struct uci_section { char *type; };
struct uci_option { int type; union { char *string; } v; };
struct uci_ptr {};
EOF
    mkdir -p linux
    cp ${WORKDIR}/openwrt/target/linux/generic/files/include/uapi/linux/switch.h linux/
    oe_runmake LIBS="-lsw -lnl-3 -lnl-genl-3"
}

do_install() {
    install -D swconfig ${D}/sbin/swconfig
}
