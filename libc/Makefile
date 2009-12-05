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

DIRS := \
    argz \
    ctype \
    errno \
    iconv \
    locale \
    math \
    misc \
    reent \
    search \
    signal \
    stdio \
    stdlib \
    string \
    time

LLJVM_LD := java -jar ../lljvm.jar ld
JAVA_LIBS := lljvm.runtime.System lljvm.runtime.Posix lljvm.runtime.Error

all: libc.class

libc.class: libc.o
	../lljvm-backend libc.o | $(LLJVM_LD) ${JAVA_LIBS} > libc.j
	java jasmin.Main libc.j

libc.o: ${OBJS}
	llvm-ld -link-as-library ${OBJS} -o libc.o

clean:
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done
	rm -f libc.o libc.j libc.class

${OBJS}: FORCE
	cd $* && $(MAKE) all

FORCE:
