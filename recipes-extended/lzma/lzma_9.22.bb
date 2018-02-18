SUMMARY = "Old LZMA utils"
LICENSE = "PD"

SRC_URI = "http://archive.ubuntu.com/ubuntu/pool/main/l/${BPN}/${BPN}_${PV}.orig.tar.gz"
SRC_URI[md5sum] = "bfdcf75308860aa983f7e4293e93b652"
SRC_URI[sha256sum] = "0fa2ec459701e403b72662cf920d252466910161fa2077dd0f6d7c078002da19"

LIC_FILES_CHKSUM = "file://lzma.txt;beginline=21;endline=31;md5=f41c17a428e7ec06266e43dfca089734"

SRC_URI += "file://02_lzmp.patch"

do_compile() {
    cd CPP/7zip/Bundles/LzmaCon
    oe_runmake -f makefile.gcc all
}

do_install() {
    cd CPP/7zip/Bundles/LzmaCon
    install -D -m0755 lzmp ${D}/usr/bin/lzmp
}

BBCLASSEXTEND = "native nativesdk"
