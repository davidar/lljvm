CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/zlib
CLASSES := libz.class minigzip.class
SCRIPTS := minigzip
CWD := $(shell pwd)

all: ${SOURCE_DIR}
	cd ${SOURCE_DIR} && $(MAKE) CC="${CWD}/$(CC)" CCLD="${CWD}/$(CC) -link"
	cd ${SOURCE_DIR} && cp ${CLASSES} ${SCRIPTS} ${CWD}

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) zlib

check: all
	cd ${SOURCE_DIR} && $(MAKE) check

clean:
	-cd ${SOURCE_DIR} && $(MAKE) clean
	rm -f ${CLASSES} ${SCRIPTS}
