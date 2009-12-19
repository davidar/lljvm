CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/datalog
CWD := $(shell pwd)
CONFIGURE := ${CWD}/${SOURCE_DIR}/configure

TESTS := \
    ancestor \
    bidipath \
    laps \
    long \
    path \
    pq \
    revpath \
    says \
    true

all: ../lua/liblua.class build/Makefile
	cd build && $(MAKE) CCLD="${CWD}/$(CC) -link"
	cd build && cp datalog datalog.class ${CWD}

../lua/liblua.class:
	cd ../lua && $(MAKE)

build/Makefile: ${SOURCE_DIR}
	mkdir -p build
	cd build && $(CONFIGURE) CC="${CWD}/$(CC)" LD="${CWD}/$(CC) -link"

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) datalog

check: all
	for t in ${TESTS}; do \
	    echo -n "$$t... "; \
	    ./datalog ${SOURCE_DIR}/$$t.prolog | sort \
	        | diff -q - ${SOURCE_DIR}/$$t.txt >/dev/null \
	        && echo PASS || echo FAIL; \
	done

clean:
	rm -rf build
	rm -f datalog datalog.class
