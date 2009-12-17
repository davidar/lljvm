CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/datalog
CWD := $(shell pwd)
CONFIGURE := ${CWD}/${SOURCE_DIR}/configure

all: build/Makefile
	cd build && $(MAKE) CCLD="${CWD}/$(CC) -link"
	cd build && cp datalog datalog.class ${CWD}

build/Makefile: ${SOURCE_DIR}
	mkdir -p build
	cd build && $(CONFIGURE) CC="${CWD}/$(CC)" LD="${CWD}/$(CC) -link"

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) datalog

clean:
	rm -rf build
	rm -f datalog datalog.class
