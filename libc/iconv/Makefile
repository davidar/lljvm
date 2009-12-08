OBJS := \
    ccs.o \
    ces.o \
    lib.o
DIRS := ${OBJS:.o=}

all: ../iconv.o

../iconv.o: ${OBJS}
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done
	llvm-ld -link-as-library $^ -o $@

clean:
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done
	rm -f ../iconv.o

${OBJS}: ; @true
