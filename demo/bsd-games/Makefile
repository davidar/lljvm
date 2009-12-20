CC := ../../lljvm-cc
CFLAGS := -Dlint -D'setregid(...)'
SOURCE_DIR := ../../thirdparty/bsd-games
CLASSES := banner.class number.class morse.class pig.class
SCRIPTS := ${CLASSES:.class=}
TEST_STR := The quick brown fox jumps over the lazy dog.
TEST_NUM := 12345678901234567890123456789012345678901234567890123456789012345

all: ${SOURCE_DIR} ${CLASSES}

${SOURCE_DIR}:
	cd ../../thirdparty && $(MAKE) bsd-games

banner.class: ${SOURCE_DIR}/banner/banner.c err.c
	$(CC) ${CFLAGS} -include sys/types.h $^ -o banner

number.class: ${SOURCE_DIR}/number/number.c err.c
	$(CC) ${CFLAGS} $^ -o number

morse.class: ${SOURCE_DIR}/morse/morse.c err.c
	$(CC) ${CFLAGS} $^ -o morse

pig.class: ${SOURCE_DIR}/pig/pig.c err.c
	$(CC) ${CFLAGS} $^ -o pig

check: all
	./banner -w80 ${TEST_STR} | diff -q - banner.txt && echo OK
	./number ${TEST_NUM}      | diff -q - number.txt && echo OK
	./morse ${TEST_STR}       | diff -q - morse.txt  && echo OK
	echo ${TEST_STR} | ./pig  | diff -q - pig.txt    && echo OK

clean:
	rm -f ${CLASSES} ${SCRIPTS}
