CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/jpeg
CLASSES := cjpeg.class djpeg.class jpegtran.class rdjpgcom.class wrjpgcom.class
SCRIPTS := ${CLASSES:.class=}

all: ${SOURCE_DIR} build/Makefile
	cd build && $(MAKE) CCLD="../$(CC) -link"
	cd build && cp ${CLASSES} ${SCRIPTS} ..

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) jpeg

build/Makefile:
	mkdir -p build
	cd build && ../${SOURCE_DIR}/configure CC="../$(CC)" LD="../$(CC) -link"

check: all
	-./cjpeg -h
	-./djpeg -h
	-./jpegtran -h
	-./rdjpgcom -h
	-./wrjpgcom -h

clean:
	rm -rf build
	rm -f ${CLASSES} ${SCRIPTS}
