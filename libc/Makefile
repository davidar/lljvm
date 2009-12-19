OBJS := \
    argz.o \
    ctype.o \
    errno.o \
    iconv.o \
    locale.o \
    math.o \
    misc.o \
    reent.o \
    search.o \
    signal.o \
    stdio.o \
    stdlib.o \
    string.o \
    time.o
DIRS := ${OBJS:.o=}

CC := CLASSPATH=../java/build:$${CLASSPATH-.} ../lljvm-cc
CFLAGS := -nostdlib -classname=lljvm.lib.c -g0

all: ../thirdparty/newlib ../java/build/lljvm/lib/c.class

../thirdparty/newlib:
	cd ../thirdparty && $(MAKE) newlib

../java/build/lljvm/lib/c.class:
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done
	$(CC) -link-as-library ${CFLAGS} ${OBJS} -o ../java/build/libc

clean:
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done
	rm -f ../java/build/lljvm/lib/c.class

${OBJS}: ; @true
