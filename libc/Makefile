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

BFLAGS := -classname=lljvm.lib.c
LLJVM_LD := java -classpath ../java/build lljvm.tools.ld.Main
JAVA_LIBS := lljvm.runtime.System lljvm.runtime.Posix lljvm.runtime.Error

all: ../thirdparty/newlib ../java/build/lljvm/lib/c.class

../thirdparty/newlib:
	cd ../thirdparty && $(MAKE) newlib

../java/build/lljvm/lib/c.class: libc.bc
	../lljvm-backend ${BFLAGS} $< | $(LLJVM_LD) ${JAVA_LIBS} > libc.j
	java jasmin.Main -d ../java/build libc.j

libc.bc: ${OBJS}
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done
	llvm-ld -link-as-library $^ -o $@

clean:
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done
	rm -f libc.bc libc.j

${OBJS}: ; @true
