CC := ../../lljvm-cc
SOURCE_DIR := ../../thirdparty/libpng
CLASSES := libpng.class pngtest.class
SCRIPTS := pngtest

all: libpng.class

libpng.class: ../zlib/libz.class build/Makefile
	cd build && $(MAKE) CCLD="../$(CC) -link"
	$(CC) -link-as-library -o libpng build/libpng_la-*.o -L../zlib -lm -lz

../zlib/libz.class:
	cd ../zlib && $(MAKE)

build/Makefile: ${SOURCE_DIR}
	mkdir -p build
	cd build && \
	    ../${SOURCE_DIR}/configure CC="../$(CC)" LD="../$(CC) -link" \
	        CFLAGS='-I../../../thirdparty/zlib' LDFLAGS='-L../../zlib'

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) libpng

check: pngtest
	./pngtest ${SOURCE_DIR}/pngtest.png
	rm -f pngout.png

pngtest: libpng.class
	cd build && $(MAKE) pngtest.o
	$(CC) -link -o pngtest build/pngtest.o -lpng -L../zlib

clean:
	rm -rf build
	rm -f ${CLASSES} ${SCRIPTS}
