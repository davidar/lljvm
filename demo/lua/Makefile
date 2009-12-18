CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/lua/src
CLASSES := lua.class luac.class liblua.class
SCRIPTS := lua luac
CWD := $(shell pwd)

all: ${SOURCE_DIR}
	cd ${SOURCE_DIR} && $(MAKE) CC="${CWD}/$(CC)" CCLD="${CWD}/$(CC) -link" all
	cd ${SOURCE_DIR} && cp ${CLASSES} ${SCRIPTS} ${CWD}

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) lua

clean:
	-cd ${SOURCE_DIR} && $(MAKE) clean
	rm -f ${CLASSES} ${SCRIPTS}
