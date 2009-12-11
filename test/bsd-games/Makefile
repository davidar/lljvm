CC := ../../lljvm-cc
CFLAGS := -Dlint -D'setregid(...)'
SOURCE_DIR := ../../thirdparty/bsd-games
CLASSES := banner.class number.class morse.class pig.class

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

clean:
	rm -f ${CLASSES}
