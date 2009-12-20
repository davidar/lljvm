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
	rm -f testout*
	./djpeg -dct int -ppm -outfile testout.ppm ${SOURCE_DIR}/testorig.jpg
	cmp ${SOURCE_DIR}/testimg.ppm testout.ppm && echo OK
	./cjpeg -dct int -outfile testout.jpg ${SOURCE_DIR}/testimg.ppm
	cmp ${SOURCE_DIR}/testimg.jpg testout.jpg && echo OK
	./djpeg -dct int -ppm -outfile testoutp.ppm ${SOURCE_DIR}/testprog.jpg
	cmp ${SOURCE_DIR}/testimg.ppm testoutp.ppm && echo OK
	./cjpeg -dct int -progressive -opt \
	    -outfile testoutp.jpg ${SOURCE_DIR}/testimg.ppm
	cmp ${SOURCE_DIR}/testimgp.jpg testoutp.jpg && echo OK
	./jpegtran -outfile testoutt.jpg ${SOURCE_DIR}/testprog.jpg
	cmp ${SOURCE_DIR}/testorig.jpg testoutt.jpg && echo OK
	rm -f testout*

clean:
	rm -rf build
	rm -f ${CLASSES} ${SCRIPTS}
