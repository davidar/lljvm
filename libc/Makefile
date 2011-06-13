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

CC := ../lljvm-cc
CFLAGS := -cp ../java/build/:../thirdparty/jasmin/jasmin.jar -nostdlib -jlisting -classname=lljvm.lib.c -g0

all: ../thirdparty/newlib ../java/build/lljvm/lib/c.class

../thirdparty/newlib:
	cd ../thirdparty && $(MAKE) newlib

../thirdparty/jasmin/jasmin.jar:
	cd ../thirdparty && $(MAKE) jasmin

../java/build/lljvm/lib/c.class: ../thirdparty/jasmin/jasmin.jar
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done
	mkdir -p ../java/build/libc
	$(CC) -link-as-library ${CFLAGS} ${OBJS} -o ../java/build/libc

clean: cleanlib
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done

cleanlib:
	rm -f ../java/build/lljvm/lib/c.class

${OBJS}: ; @true
