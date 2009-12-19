CC := ../../lljvm-cc
CFLAGS := -Dlint -D'setregid(...)'
SOURCE_DIR := ../../thirdparty/bsd-games
CLASSES := banner.class number.class morse.class pig.class
SCRIPTS := ${CLASSES:.class=}

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
	./banner -w80 \#
	./number 123
	./morse SOS
	echo "hello world" | ./pig

clean:
	rm -f ${CLASSES} ${SCRIPTS}
